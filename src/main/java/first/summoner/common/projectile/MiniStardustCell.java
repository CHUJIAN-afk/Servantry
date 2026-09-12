package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.AttachingProjectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 星细胞射弹 - 追踪目标并黏着施加寄生效果。
 */
public class MiniStardustCell extends AttachingProjectile implements IEntityCollision<MiniStardustCell> {

    private LivingEntity chaseTarget;
    private int trailTimer = 0;

    public MiniStardustCell() {
        super(SummonerAttachmentEntityRegister.MINI_STARDUST_CELL);
        setDrag(0.9f);
        setMaxLife(100);
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

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        setAttachedTarget(target);
        attachTo(hit.hitPoint());
        DamageSource source = getDamageSource();
        if (source != null) {
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
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            if (isAttached()) {
                LivingEntity target = getAttachedTarget();
                if (target == null || !target.isAlive()) {
                    setRemove();
                }
            } else {
                tickFlying();
            }
            if (trailTimer > 0) {
                trailTimer--;
            }
        } else if (trailTimer > 0) {
            trailTimer--;
        }
        super.tick();
    }

    private void tickFlying() {
        if (chaseTarget != null && chaseTarget.isAlive()) {
            Vec3 targetCenter = chaseTarget.getBoundingBox().getCenter();
            applyForce(targetCenter.subtract(getPos()).normalize().scale(0.3));
            setTrailTimer(getTrailDuration());
        } else {
            setRemove();
        }
    }

    @Override
    public void onRemove() {
        Level level = getLevel();
        if (!level.isClientSide()) {
            Vec3 pos = getPos();
            RandomSource random = getRandom();
            int count = random.nextInt(2, 3);
            double baseSpeed = 0.2;
            if (isAttached()) {
                DamageSource source = getDamageSource();
                LivingEntity target = getAttachedTarget();
                if (source != null && target != null && target.isAlive()) {
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
    }

    public void setChaseTarget(LivingEntity chaseTarget) {
        this.chaseTarget = chaseTarget;
    }
}
