package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ProjectileRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        setDrag(1.0f);
        setMaxSpeed(2.0f);
    }

    public LaserProjectile(UUID ownerUuid, UUID sourceServantUuid, Vec3 startPos, Vec3 direction) {
        super(startPos, null);
        setOwnerUuid(ownerUuid);
        setSourceServantUuid(sourceServantUuid);
        setDrag(1.0f);
        setMaxSpeed(2.0f);

        // 设置初始速度方向
        Vec3 normalizedDir = direction.normalize().scale(1.5);
        setVelocity(normalizedDir);

        // 设置朝向
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        setDesiredRotation(yaw, pitch, 0);
    }

    @Override
    public void tickBehavior(Player owner) {
        // 保持拖尾效果
        setTrailTimer(getTrailDuration());

        // 远距离检测
        if (getPos().distanceToSqr(owner.position()) > getMaxDistance() * getMaxDistance()) {
            markForRemoval();
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        // 碰到方块后消失
        markForRemoval();
    }

    @Override
    public void onCollisionAttack(Set<LivingEntity> hitTargets) {
        Player owner = getOwner();
        if (owner == null) return;

        Servant sourceServant = findServantByUuid(owner, getSourceServantUuid());
        if (sourceServant == null) return;

        for (LivingEntity target : hitTargets) {
            InvincibleData.servantAttack(target, sourceServant, 0,
                    sourceServant.getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
        }

        // 命中后消失
        markForRemoval();
    }

    private Servant findServantByUuid(Player owner, UUID servantUuid) {
        for (Servant servant : owner.getData(AttachmentRegister.EntityData).getServants()) {
            if (servant.getUuid().equals(servantUuid)) return servant;
        }
        return null;
    }

    // ===================== IBlockCollision =====================

    @Override
    public AABB getBlockCollisionBox() {
        // 方块碰撞箱为正方形小块
        return new AABB(-0.1, -0.1, -0.1, 0.1, 0.1, 0.1);
    }

    // ===================== ICollideAttack =====================

    @Override
    public AABB getHitbox() {
        // 伤害碰撞箱为长条形（沿运动方向延伸）
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
