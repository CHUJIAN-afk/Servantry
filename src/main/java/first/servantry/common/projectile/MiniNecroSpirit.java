package first.servantry.common.projectile;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.attachment.TargetCache;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.common.sound.Playable;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 死魂灵射弹 - 跟踪目标飞行，可穿透方块，只命中追踪目标。
 * 命中时造成三次伤害：100%/50%/50%，对追踪目标伤害×1.25。
 */
public class MiniNecroSpirit extends Projectile implements ICollideAttack<MiniNecroSpirit> {

    /** 追踪目标的UUID */
    private LivingEntity trackingTarget;
    private Vec3 targetPos = null;

    public MiniNecroSpirit() {
        super();
    }

    public MiniNecroSpirit(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1.0f);
        setMaxLife(200);
        setGravity(0);
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            LivingEntity target = getTrackingTarget();
            if (target != null && target.isAlive()) {
                AABB box = trackingTarget.getBoundingBox();
                targetPos = box.getCenter();
            }
            if (targetPos != null) {
                applyForce(targetPos.subtract(getPos()).normalize());
            }
            // 粒子拖尾
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xff1200)
                                     .edgeColor(0xd40f00)
                                     .lifetime(4)
                                     .lifetimeRandom(3)
                                     .spin(0.2f)
                                     .spinRandom(0.2f)
                                     .scale(0.02f)
                                     .scaleRandom(0.008f)
                    )
                    .pos(getPos())
                    .offset(0.05)
                    .emit();
        }
        super.tick();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            ServantryHelper servantryHelper = ServantryHelper.get(owner);
            TargetCache targetCache = servantryHelper.getTargetCache();
            List<LivingEntity> entities = targetCache.getEntities();
            List<LivingEntity> mainTargets = new ArrayList<>();
            for (LivingEntity living : entities) {
                if (servant.isTarget(living)){
                    if (getPos().distanceToSqr(living.getBoundingBox().getCenter()) < 9) {
                        mainTargets.add(living);
                    }
                }
            }
            for (LivingEntity living : mainTargets) {
                float amount = getDamage();
                if (living == trackingTarget) {
                    amount *= 1.25f;
                }
                InvincibleData.attack(living)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(amount)
                        .apply();
                InvincibleData.attack(living)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(amount * 0.5f)
                        .apply();
                InvincibleData.attack(living)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(amount * 0.5f)
                        .apply();
                for (LivingEntity entity : entities) {
                    if (entity == living || (living.distanceToSqr(entity) < 4 && servant.isTarget(living))) {
                        InvincibleData.attack(living)
                                .attacker(getUuid())
                                .damageSource(source)
                                .damageAmount(amount * 1.75f)
                                .apply();
                    }
                }
            }
            // 命中粒子效果
            Vec3 hitPoint = hitContexts.getFirst().hitPoint();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xff1200)
                                     .edgeColor(0xd40f00)
                                     .lifetime(10)
                                     .lifetimeRandom(20)
                                     .spin(0.3f)
                                     .spinRandom(0.3f)
                                     .friction(0.8f)
                                     .scale(0.04f)
                                     .scaleRandom(0.02f))
                    .pos(hitPoint)
                    .velocity(getVelocity())
                    .offset(0.3)
                    .count(200)
                    .speed(1)
                    .spread(3)
                    .emit();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xff7c00)
                                     .edgeColor(0xd66b00)
                                     .lifetime(10)
                                     .lifetimeRandom(20)
                                     .spin(0.3f)
                                     .spinRandom(0.3f)
                                     .friction(0.7f)
                                     .scale(0.04f)
                                     .scaleRandom(0.02f))
                    .pos(hitPoint)
                    .velocity(getVelocity())
                    .offset(0.3)
                    .count(200)
                    .speed(1)
                    .spread(3)
                    .emit();
            Playable.play(SoundEvents.GENERIC_EXPLODE, owner.level(), hitPoint, owner.getSoundSource());
        }
        setRemove();
    }

    public LivingEntity getTrackingTarget() {
        return trackingTarget;
    }

    public void setTrackingTarget(LivingEntity trackingTarget) {
        this.trackingTarget = trackingTarget;
    }

    @Override
    public boolean isValidCollisionTarget(MiniNecroSpirit entity, LivingEntity target) {
        return trackingTarget == target;
    }

    @Override
    public boolean canCollideAttack() {
        return trackingTarget != null;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-1.5, -1.5, -1.5, 1.5, 1.5, 1.5);
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.MINI_NECRO_SPIRIT.get();
    }
}
