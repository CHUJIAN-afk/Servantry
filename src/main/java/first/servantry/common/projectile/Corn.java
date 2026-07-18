package first.servantry.common.projectile;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.attachment.TargetCache;
import first.servantry.api.common.sound.Playable;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 玉米炮射弹 - 抛物线飞行，命中实体或方块时对3格内所有敌对目标造成伤害。
 */
public class Corn extends Projectile implements ICollideAttack<Corn>, IBlockCollision<Corn> {

    /** AOE伤害半径 */
    private static final double AOE_RADIUS = 3.0;

    public Corn() {
        super();
    }

    public Corn(DamageSource damageSource, Vec3 startPos, Vec3 velocity) {
        super(startPos, velocity);
        setDamageSource(damageSource);
        setDrag(0.99f);
        setMaxSpeed(4.0f);
        setMaxLife(200);
        setGravity(-0.05f);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        // 命中实体：在命中点处AOE
        if (!hitContexts.isEmpty()) {
            HitContext firstHit = hitContexts.getFirst();
            explodeAt(firstHit.hitPoint());
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        // 命中方块：在碰撞点处AOE
        explodeAt(context.position());
    }

    /**
     * 在指定位置爆炸，对3格内所有敌对目标造成伤害。
     */
    private void explodeAt(Vec3 explosionPos) {
        DamageSource source = getDamageSource();
        if (source instanceof ServantDamageSource sds) {
            Servant servant = sds.getServant();
            // 通过TargetCache获取附近实体
            ServantryHelper helper = ServantryHelper.get(owner);
            TargetCache targetCache = helper.getTargetCache();
            List<LivingEntity> nearbyEntities = targetCache.getEntities();

            for (LivingEntity entity : nearbyEntities) {
                if (servant.isTarget(entity)) {
                    double distance = entity.getBoundingBox().getCenter().distanceTo(explosionPos);
                    if (distance <= AOE_RADIUS) {
                        InvincibleData.attack(entity)
                                .attacker(getUuid())
                                .damageSource(source)
                                .damageAmount(getDamage())
                                .invincibleTime(10)
                                .apply();
                    }
                }
            }
        }
        Vec3 pos = getPos();
        ParticleHelper.create(owner.level())
                .type(ParticleTypes.EXPLOSION)
                .pos(pos)
                .count(10)
                .emit();
        ParticleHelper.create(owner.level())
                .type(ParticleTypes.EXPLOSION_EMITTER)
                .pos(pos)
                .count(10)
                .emit();
        Playable.play(SoundEvents.GENERIC_EXPLODE, owner.level(), pos, owner.getSoundSource());
        setRemove();
    }

    @Override
    public boolean isValidCollisionTarget(Corn entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource sds) {
            return sds.getServant().isTarget(target);
        }
        return ICollideAttack.super.isValidCollisionTarget(entity, target);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.CORN.get();
    }
}
