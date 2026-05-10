package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.servant.Twins;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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
        setDrag(1);
        setMaxSpeed(4);
    }

    public LaserProjectile(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
    }

    @Override
    public void tick() {
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
                InvincibleData.criteriaAttack(target, uuid, 4, source, getDamage(), InvincibleData.Type.PARTIAL);
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
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.1, -0.1, -0.1, 0.1, 0.1, 0.1);
    }

    // ===================== ICollideAttack =====================

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.05, -0.05, -1, 0.05, 0.05, 0);
    }

    @Override
    public int getCollisionSampleNodes() {
        return 4;
    }

    // ===================== 属性 =====================

    @Override
    public float getDamage() {
        if (damageSource instanceof ServantDamageSource source) {
            Servant servant = source.getServant();
            if (servant != null) {
                float damage = servant.getDamage();
                if (servant instanceof Twins) {
                    damage *= 1.15f;
                }
                return damage;
            }
        }
        return 6;
    }

    @Override
    public AttachmentEntityType<? extends Projectile> getType() {
        return AttachmentEntityRegister.LaserProjectile.get();
    }

    @Override
    public int getTrailDuration() {
        return 10;
    }
}