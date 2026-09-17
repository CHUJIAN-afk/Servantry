package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 星细胞射弹 - 追踪目标并黏着施加寄生效果。
 */
public class MiniStardustCell extends Projectile implements IEntityCollision<MiniStardustCell> {

    private LivingEntity chaseTarget;
    @Nullable
    private LivingEntity attachedTarget;
    private float attachedYaw;
    private double attachedRadius;
    private double attachedVerticalOffset;
    private int trailTimer = 0;

    public MiniStardustCell() {
        super(SummonerAttachmentEntityRegister.MINI_STARDUST_CELL);
        setGravity(0);
        setMaxTickCount(100);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getTrailTimer, this::setTrailTimer);
    }

    public int getTrailDuration() {
        return 15;
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int trailTimer) {
        this.trailTimer = trailTimer;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.075, -0.075, -0.075, 0.075, 0.075, 0.075);
    }

    @Override
    public boolean canCollideAttack() {
        return !isAttached();
    }

    public boolean isAttached() {
        return attachedTarget != null;
    }

    @Nullable
    public LivingEntity getAttachedTarget() {
        return attachedTarget;
    }

    public void setAttachedTarget(@Nullable LivingEntity attachedTarget) {
        this.attachedTarget = attachedTarget;
    }

    public void attachTo(Vec3 position) {
        if (attachedTarget == null) {
            return;
        }
        Vec3 offset = position.subtract(attachedTarget.getBoundingBox().getCenter());
        this.attachedYaw = (float) Math.atan2(offset.z, offset.x);
        this.attachedRadius = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        this.attachedVerticalOffset = offset.y;
        setVelocity(Vec3.ZERO);
    }

    public void detach() {
        this.attachedTarget = null;
        this.attachedRadius = 0;
        this.attachedVerticalOffset = 0;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && (isAttached()
                ? attachedTarget != null && attachedTarget.isAlive()
                : chaseTarget != null && chaseTarget.isAlive());
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        setAttachedTarget(target);
        attachTo(hit.hitPoint());
        setCurrentPathNode(getCurrentPathNode().modifyPos(hit.hitPoint()));
        DamageSource source = getDamageSource();
        MobEffectInstance existing = target.getEffect(SummonerMobEffectRegister.CellParasitism);
        int amplifier = existing == null ? 0 : Math.min(existing.getAmplifier() + 1, 9);
        MobEffectInstance effectInstance = new MobEffectInstance(SummonerMobEffectRegister.CellParasitism, 100, amplifier);
        InvincibleData.attack(target)
                .attacker(getUuid())
                .damageSource(source)
                .damageAmount(getDamage())
                .effect(effectInstance)
                .apply();
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAttached()) {
            tickFlying();
        }
        if (trailTimer > 0) {
            trailTimer--;
        }
    }

    @Override
    public void tickPhysics() {
        super.tickPhysics();
        if (!isAttached()) {
            return;
        }
        LivingEntity target = attachedTarget;
        if (target == null || !target.isAlive()) {
            return;
        }
        Vec3 center = target.getBoundingBox().getCenter();
        Vec3 attachedPosition = center.add(
                Math.cos(attachedYaw) * attachedRadius,
                attachedVerticalOffset,
                Math.sin(attachedYaw) * attachedRadius
        );
        setPath(Collections.singletonList(new PathNode(attachedPosition, getYaw(), getPitch(), getRoll())));
    }

    private void tickFlying() {
        if (chaseTarget != null && chaseTarget.isAlive()) {
            Vec3 targetCenter = chaseTarget.getBoundingBox().getCenter();
            addVelocity(targetCenter.subtract(getPos()).normalize().scale(0.3));
            setTrailTimer(getTrailDuration());
        } else {
            setRemove();
        }
    }

    @Override
    public void onRemove() {
        Vec3 pos = getPos();
        RandomSource random = getRandom();
        int count = random.nextInt(2, 3);
        double baseSpeed = 0.2;
        if (isAttached()) {
            DamageSource source = getDamageSource();
            LivingEntity target = getAttachedTarget();
            if (target != null && target.isAlive()) {
                InvincibleData.attack(target)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(getDamage() * 0.5f)
                        .apply();
            }
            count *= 4;
            baseSpeed *= 4;
        }
        Vec3 velocity;
        if (attachedTarget != null) {
            velocity = pos.subtract(attachedTarget.getBoundingBox().getCenter());
        } else {
            velocity = pos.subtract(pos.offsetRandom(random, 1f)).scale(0.5);
        }
        GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                .centerColor(0x2fb2e1)
                .edgeColor(0x33ccff);
        ParticleHelper.create(getLevel())
                .generic(genericParticleBuilder
                        .lifetime(5)
                        .lifetimeRandom(25)
                        .spin(0.1f)
                        .spinRandom(0.5F)
                        .friction(0.75F)
                        .scale(0.035f)
                        .scaleRandom(0.005f)
                )
                .pos(pos)
                .velocity(velocity.normalize())
                .spread(0.02)
                .count(count)
                .speed(baseSpeed)
                .spread(1)
                .emit();
    }

    public void setChaseTarget(LivingEntity chaseTarget) {
        this.chaseTarget = chaseTarget;
    }
}
