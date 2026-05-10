package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
public class LaserProjectile extends Projectile implements ICollideAttack<LaserProjectile> {

    public LaserProjectile() {
        super();
        setDrag(1);
        setMaxSpeed(4);
        setMaxLife(34);
    }

    public LaserProjectile(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
    }

    @Override
    public void onCollisionAttack(List<LivingEntity> hitTargets) {
        LivingEntity target = hitTargets.getFirst();
        DamageSource source = getDamageSource();
        if (source != null) {
            UUID uuid = null;
            if (source instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof Servant servant) {
                uuid = servant.getUuid();
            }
            InvincibleData.criteriaAttack(target, uuid, 4, source, getDamage(), InvincibleData.Type.PARTIAL);
        }
        setRemove();
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

    // ===================== ICollideAttack =====================

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.03, -0.03, -1, 0.03, 0.03, 0);
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
                return servant.getDamage() * 1.15f;
            }
        }
        return 2.4f;
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