package first.servantry.api.entity;

import first.servantry.api.PathNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * 方块碰撞接口，使用原版碰撞检测API实现精确的碰撞检测与位置修正。
 *
 * @param <T> 附件实体类型
 */
public interface IBlockCollision<T extends AttachmentEntity> {

    AABB getBlockCollisionBox();

    default boolean canCollideWithBlocks(T entity) {
        return true;
    }

    /**
     * 碰撞回调，实现类可在此清零碰撞方向的速度。
     *
     * @param entity  附件实体
     * @param context 碰撞上下文，包含碰撞位置和碰撞轴信息
     */
    default void onBlockCollision(T entity, CollisionContext context) {
    }

    /**
     * 执行方块碰撞检测并修正位置。
     * <p>
     * 使用扫描检测：沿移动路径逐步检测碰撞，确保高速移动时不会穿透。
     * 修正位置时会额外推出小间隙，防止吸附在方块表面。
     * </p>
     */
    default void processBlockCollision(T entity) {
        if (!canCollideWithBlocks(entity)) return;

        var history = entity.getHistoryNodes();
        if (history.isEmpty()) return;

        // 获取上一tick的位置（历史队列第二个元素，因为第一个是当前tick刚添加的）
        Vec3 from = history.size() >= 2 ? history.get(1).pos() : history.getFirst().pos();
        Vec3 to = entity.currentPathNode.pos();
        Vec3 motion = to.subtract(from);

        // 如果没有移动，只检测当前位置是否重叠
        if (motion.lengthSqr() < 1e-8) {
            checkAndResolveOverlap(entity, to);
            return;
        }

        // 扫描移动路径，检测碰撞
        AABB box = getBlockCollisionBox();
        var level = entity.getOwner().level();

        // 收集移动路径上的所有碰撞箱
        AABB searchBox = box.move(to).expandTowards(motion.scale(-1)).inflate(1);
        List<VoxelShape> colliders = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(searchBox.minX, searchBox.minY, searchBox.minZ),
                BlockPos.containing(searchBox.maxX, searchBox.maxY, searchBox.maxZ))) {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty()) {
                colliders.add(shape.move(pos.getX(), pos.getY(), pos.getZ()));
            }
        }

        if (colliders.isEmpty()) return;

        // 使用原版碰撞检测计算实际可移动距离
        double dx = motion.x, dy = motion.y, dz = motion.z;
        AABB currentBox = box.move(from);

        // Y轴碰撞
        dy = Shapes.collide(net.minecraft.core.Direction.Axis.Y, currentBox, colliders, dy);
        boolean collisionY = dy != motion.y;
        if (collisionY) currentBox = currentBox.move(0, dy, 0);

        // X/Z轴碰撞（根据移动大小决定顺序）
        boolean xFirst = Math.abs(dx) > Math.abs(dz);
        boolean collisionX, collisionZ;
        if (xFirst) {
            dx = Shapes.collide(net.minecraft.core.Direction.Axis.X, currentBox, colliders, dx);
            collisionX = dx != motion.x;
            if (collisionX) currentBox = currentBox.move(dx, 0, 0);
            dz = Shapes.collide(net.minecraft.core.Direction.Axis.Z, currentBox, colliders, dz);
            collisionZ = dz != motion.z;
        } else {
            dz = Shapes.collide(net.minecraft.core.Direction.Axis.Z, currentBox, colliders, dz);
            collisionZ = dz != motion.z;
            if (collisionZ) currentBox = currentBox.move(0, 0, dz);
            dx = Shapes.collide(net.minecraft.core.Direction.Axis.X, currentBox, colliders, dx);
            collisionX = dx != motion.x;
        }

        // 发生碰撞时修正位置并回调
        if (collisionX || collisionY || collisionZ) {
            // 添加小间隙推出，防止吸附
            double gap = 0.001;
            if (collisionX) dx += Math.signum(dx) * gap;
            if (collisionY) dy += Math.signum(dy) * gap;
            if (collisionZ) dz += Math.signum(dz) * gap;

            Vec3 correctedPos = from.add(dx, dy, dz);
            entity.currentPathNode = new PathNode(correctedPos, entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
            onBlockCollision(entity, new CollisionContext(correctedPos, collisionX, collisionY, collisionZ));
        }
    }

    /**
     * 检测当前位置是否与方块重叠，如果重叠则推出。
     */
    private void checkAndResolveOverlap(T entity, Vec3 pos) {
        AABB box = getBlockCollisionBox().move(pos);
        var level = entity.getOwner().level();

        // 收集碰撞箱
        List<VoxelShape> colliders = new ArrayList<>();
        AABB searchBox = box.inflate(0.5);
        for (BlockPos blockPos : BlockPos.betweenClosed(
                BlockPos.containing(searchBox.minX, searchBox.minY, searchBox.minZ),
                BlockPos.containing(searchBox.maxX, searchBox.maxY, searchBox.maxZ))) {
            BlockState state = level.getBlockState(blockPos);
            VoxelShape shape = state.getCollisionShape(level, blockPos);
            if (!shape.isEmpty()) {
                colliders.add(shape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            }
        }

        if (colliders.isEmpty()) return;

        // 检测重叠并推出
        double gap = 0.001;
        for (VoxelShape shape : colliders) {
            AABB shapeBounds = shape.bounds();
            if (box.intersects(shapeBounds)) {
                // 计算最小推出距离
                double pushX = 0, pushY = 0, pushZ = 0;

                if (box.maxX > shapeBounds.minX && box.minX < shapeBounds.minX) {
                    pushX = shapeBounds.minX - box.maxX - gap;
                } else if (box.minX < shapeBounds.maxX && box.maxX > shapeBounds.maxX) {
                    pushX = shapeBounds.maxX - box.minX + gap;
                }

                if (box.maxY > shapeBounds.minY && box.minY < shapeBounds.minY) {
                    pushY = shapeBounds.minY - box.maxY - gap;
                } else if (box.minY < shapeBounds.maxY && box.maxY > shapeBounds.maxY) {
                    pushY = shapeBounds.maxY - box.minY + gap;
                }

                if (box.maxZ > shapeBounds.minZ && box.minZ < shapeBounds.minZ) {
                    pushZ = shapeBounds.minZ - box.maxZ - gap;
                } else if (box.minZ < shapeBounds.maxZ && box.maxZ > shapeBounds.maxZ) {
                    pushZ = shapeBounds.maxZ - box.minZ + gap;
                }

                // 选择最小推出距离
                double absX = Math.abs(pushX), absY = Math.abs(pushY), absZ = Math.abs(pushZ);

                if (absY > 0 && (absY <= absX || absX == 0) && (absY <= absZ || absZ == 0)) {
                    Vec3 correctedPos = pos.add(0, pushY, 0);
                    entity.currentPathNode = new PathNode(correctedPos, entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
                    onBlockCollision(entity, new CollisionContext(correctedPos, false, true, false));
                } else if (absX > 0 && (absX <= absZ || absZ == 0)) {
                    Vec3 correctedPos = pos.add(pushX, 0, 0);
                    entity.currentPathNode = new PathNode(correctedPos, entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
                    onBlockCollision(entity, new CollisionContext(correctedPos, true, false, false));
                } else if (absZ > 0) {
                    Vec3 correctedPos = pos.add(0, 0, pushZ);
                    entity.currentPathNode = new PathNode(correctedPos, entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
                    onBlockCollision(entity, new CollisionContext(correctedPos, false, false, true));
                }
                return;
            }
        }
    }

    /**
     * 反转指定轴的速度分量，并应用衰减。
     * <p>
     * 衰减系数为0.8，若反转后分量绝对值小于0.01则清零。
     * </p>
     */
    default Vec3 bounceVelocityAxis(Vec3 velocity, boolean x, boolean y, boolean z) {
        double damping = 0.35;
        double vx = x ? -velocity.x * damping : velocity.x;
        double vy = y ? -velocity.y * damping : velocity.y;
        double vz = z ? -velocity.z * damping : velocity.z;
        return new Vec3(vx, vy, vz);
    }

    /**
     * 反转碰撞上下文中指定轴的速度分量。
     */
    default Vec3 bounceVelocityAxis(Vec3 velocity, CollisionContext context) {
        return bounceVelocityAxis(velocity, context.collisionX(), context.collisionY(), context.collisionZ());
    }

    /**
     * 碰撞上下文记录类，封装碰撞位置和碰撞轴信息。
     */
    record CollisionContext(Vec3 position, boolean collisionX, boolean collisionY, boolean collisionZ) {
    }
}
