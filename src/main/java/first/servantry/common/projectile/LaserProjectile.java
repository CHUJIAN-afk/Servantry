package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ProjectileRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

/**
 * 激光射弹 - 高速直线飞行的红色激光束。
 * <p>
 * 特性：
 * <ul>
 *   <li>高速直线飞行，无追踪</li>
 *   <li>长条形伤害碰撞箱</li>
 *   <li>方块碰撞检测，碰到方块后消失</li>
 *   <li>红色球形头部拖尾渲染</li>
 * </ul>
 * </p>
 */
public class LaserProjectile extends Projectile implements IBlockCollision<LaserProjectile>, ICollideAttack<LaserProjectile> {

    public LaserProjectile() {
        super();
    }

    public LaserProjectile(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
    }

    @Override
    public void tick() {
        if (getOwner().level().isClientSide()) {
            setTrailTimer(getTrailDuration());
        }
        super.tick();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }

    @Override
    public void onCollisionAttack(Set<LivingEntity> hitTargets) {
        for (LivingEntity target : hitTargets) {
            DamageSource source = getDamageSource();
            if (source != null) {
                UUID uuid = null;
                if (source instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof Servant servant) {
                    uuid = servant.getUuid();
                }
                InvincibleData.criteriaAttack(target, uuid, 0, source, getDamage(), InvincibleData.Type.PARTIAL);
            }
        }
    }

    @Override
    public boolean isValidCollisionTarget(LaserProjectile entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            if (servant != null) {
                return servant.isTarget(target);
            }
        }
        return false;
    }

    // ===================== IBlockCollision =====================

    @Override
    public AABB getBlockCollisionBox() {
        return new AABB(-0.1, -0.1, -0.1, 0.1, 0.1, 0.1);
    }

    // ===================== ICollideAttack =====================

    @Override
    public AABB getHitbox() {
        return new AABB(-0.1, -0.1, -0.5, 0.1, 0.1, 0.5);
    }

    @Override
    public int getCollisionSampleNodes() {
        return 4;
    }

    // ===================== 属性 =====================

    @Override
    public float getDamage() {
        return 8f;
    }

    @Override
    public ProjectileType<? extends Projectile> getProjectileType() {
        return ProjectileRegister.LaserProjectile.get();
    }

    @Override
    public int getTrailDuration() {
        return 10;
    }
}