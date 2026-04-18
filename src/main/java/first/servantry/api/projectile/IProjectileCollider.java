package first.servantry.api.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;

public interface IProjectileCollider {

    boolean onHitBlock(AdvancedProjectile projectile, BlockHitResult hitResult);
    boolean onHitEntity(AdvancedProjectile projectile, EntityHitResult hitResult);

    default float getProjectileHitboxSize() {
        return 0.2f;
    }

    default boolean canHitEntity(AdvancedProjectile projectile, Entity entity) {
        LivingEntity owner = projectile.getOwner();
        if (!entity.isSpectator() && entity.isAlive() && entity.isPickable()) {
            return owner == null || (!entity.is(owner) && !entity.isAlliedTo(owner));
        }
        return false;
    }

    /**
     * 核心逻辑：利用上一帧和当前帧的坐标发射射线
     */
    default void processCollision(AdvancedProjectile projectile) {
        if (projectile.isRemoved()) return;

        LinkedList<first.servantry.api.servant.PathNode> history = projectile.getHistoryNodes();
        if (history.size() < 2) return;

        Vec3 start = history.get(1).pos();
        Vec3 end = history.get(0).pos();

        // 1. 方块检测
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, projectile.getOwner());
        HitResult blockHit = projectile.getLevel().clip(context);
        
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation(); // 截断射线防止穿墙伤人
            if (onHitBlock(projectile, (BlockHitResult) blockHit)) {
                projectile.setPos(end);
                projectile.discard();
                return;
            }
        }

        // 2. 实体检测
        float size = getProjectileHitboxSize();
        AABB searchBox = new AABB(start, end).inflate(size + 1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                projectile.getLevel(), projectile.getOwner(), start, end, searchBox, e -> canHitEntity(projectile, e)
        );

        if (entityHit != null) {
            if (onHitEntity(projectile, entityHit)) {
                projectile.setPos(entityHit.getLocation());
                projectile.discard();
            }
        }
    }
}