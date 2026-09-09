package first.summoner.common.projectile;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.lyra.common.sound.Playable;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 死魂灵射弹 - 跟踪目标飞行，可穿透方块，只命中追踪目标。
 * 命中时造成三次伤害：100%/50%/50%，对追踪目标伤害×1.25。
 */
public class MiniNecroSpirit extends Projectile implements IEntityCollision<MiniNecroSpirit> {

    /** 追踪目标的UUID */
    private LivingEntity trackingTarget;
    private Vec3 targetPos = null;

    public MiniNecroSpirit() {
        super(SummonerAttachmentEntityRegister.MINI_NECRO_SPIRIT);
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
        if (source != null) {
            LyraHelper summonerHelper = LyraHelper.get(owner);
            TargetCache targetCache = summonerHelper.getTargetCache();
            List<LivingEntity> mainTargets = targetCache.getEntitiesInRadius(getPos(), 3, this::isTarget);
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
                List<LivingEntity> splashTargets = targetCache.getEntitiesInRadius(living.position(), 2,
                        entity -> entity == living || isTarget(entity));
                for (LivingEntity entity : splashTargets) {
                    InvincibleData.attack(living)
                            .attacker(getUuid())
                            .damageSource(source)
                            .damageAmount(amount * 1.75f)
                            .apply();
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
        return new AABB(-0.25, -0.25, -1, 0.25, 0.25, 1);
    }
}
