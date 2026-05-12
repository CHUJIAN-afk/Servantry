package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.AttachingProjectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.MobEffectRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
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
public class StardustProjectile extends AttachingProjectile implements ICollideAttack<StardustProjectile> {

    private static final int PARASITISM_DURATION = 100;
    private static final int MAX_PARASITISM_LEVEL = 10;

    private LivingEntity chaseTarget;

    public StardustProjectile() {
        super();
    }

    public StardustProjectile(DamageSource damageSource, Vec3 startPos) {
        super(startPos, Vec3.ZERO);
        setDamageSource(damageSource);
        setDrag(0.9f);
        setMaxSpeed(1.2f);
        setMaxLife(100);
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
    public boolean isValidCollisionTarget(StardustProjectile entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            if (servant != null) {
                return servant.isTarget(target);
            }
        }
        return false;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        setAttachedTarget(target);
        attachTo(hit.hitPoint());
        DamageSource source = getDamageSource();
        if (source != null) {
            InvincibleData.criteriaAttack(target, getUuid(), 0, source, getDamage(), InvincibleData.Type.PARTIAL);
        }
        MobEffectInstance existing = target.getEffect(MobEffectRegister.CellParasitism);
        int amplifier = existing == null ? 0 : Math.min(existing.getAmplifier() + 1, MAX_PARASITISM_LEVEL - 1);
        target.addEffect(new MobEffectInstance(MobEffectRegister.CellParasitism, PARASITISM_DURATION, amplifier));
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (isAttached()) {
                LivingEntity target = getAttachedTarget();
                if (target == null || !target.isAlive()) {
                    setRemove();
                }
            } else {
                tickFlying();
            }
        }
        super.tick();
    }

    private void tickFlying() {
        if (life >= 10 && chaseTarget != null && chaseTarget.isAlive()) {
            Vec3 targetCenter = chaseTarget.getBoundingBox().getCenter();
            applyForce(targetCenter.subtract(getPos()).normalize().scale(0.3));
            setTrailTimer(getTrailDuration());
        } else {
            setRemove();
        }
    }

    @Override
    public void onRemove() {
        Level level = owner.level();
        if (!level.isClientSide()) {
            Vec3 pos = getPos();
            RandomSource random = owner.getRandom();
            int count = random.nextInt(2, 3);
            double baseSpeed = 0.2;
            if (isAttached()) {
                DamageSource source = getDamageSource();
                LivingEntity target = getAttachedTarget();
                if (source != null && target != null && target.isAlive()) {
                    InvincibleData.criteriaAttack(target, getUuid(), 0, source, getDamage(), InvincibleData.Type.PARTIAL);
                }
                count *= 4;
                baseSpeed *= 4;
            }
            Vec3 velocity;
            if (attachedTarget != null) {
                velocity = attachedTarget.getBoundingBox().getCenter().subtract(pos.offsetRandom(random, (float) (attachedTarget.getBoundingBox().getSize() * 2)));
            } else {
                velocity = pos.subtract(pos.offsetRandom(random, 1f)).scale(0.5);
            }

            ParticleHelper.create((ServerLevel) owner.level())
                    .generic(builder -> builder
                            .color(51, 204, 255)
                            .colorRandom(0.2F, 0.2F, 0.0F)
                            .lifetime(5)
                            .lifetimeRandom(10)
                            .friction(0.75F)
                            .spinRandom(0.5F)
                    )
                    .pos(pos)
                    .velocity(velocity)
                    .spread(90)
                    .count(count)
                    .speed(baseSpeed)
                    .spread(1)
                    .emit();
        }
    }

    @Override
    public float getDamage() {
        if (damageSource instanceof ServantDamageSource source) {
            Servant servant = source.getServant();
            if (servant != null) {
                return servant.getDamage();
            }
        }
        return 6;
    }

    @Override
    public AttachmentEntityType<? extends AttachingProjectile> getType() {
        return AttachmentEntityRegister.StardustProjectile.get();
    }

    public void setChaseTarget(LivingEntity chaseTarget) {
        this.chaseTarget = chaseTarget;
    }

}