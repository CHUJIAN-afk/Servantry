package first.servantry.api.entity;

import first.servantry.api.PathNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 方块碰撞接口，使用原版碰撞检测API实现精确的碰撞检测与位置修正。
 *
 * @param <T> 附件实体类型
 */
public interface IBlockCollision<T extends AttachmentEntity> {

    /**
     * 非扫描轴微收缩量，解决贴墙滑动时的"拼缝卡顿"问题
     */
    double SHRINK_EPSILON = 1e-5;

    /**
     * 碰撞箱重叠时的推出间隙
     */
    double PUSH_GAP = 1e-5;

    /**
     * 清零碰撞轴的速度分量
     */
    static Vec3 clearVelocity(Vec3 velocity, CollisionContext context) {
        return new Vec3(
                context.collisionX() ? 0 : velocity.x,
                context.collisionY() ? 0 : velocity.y,
                context.collisionZ() ? 0 : velocity.z
        );
    }

    /**
     * 反转碰撞轴的速度分量
     */
    static Vec3 bounceVelocity(Vec3 velocity, CollisionContext context, double damping, double threshold) {
        return new Vec3(
                context.collisionX() ? (Math.abs(velocity.x) * damping < threshold ? 0 : -velocity.x * damping) : velocity.x,
                context.collisionY() ? (Math.abs(velocity.y) * damping < threshold ? 0 : -velocity.y * damping) : velocity.y,
                context.collisionZ() ? (Math.abs(velocity.z) * damping < threshold ? 0 : -velocity.z * damping) : velocity.z
        );
    }

    static Vec3 bounceVelocity(Vec3 velocity, CollisionContext context) {
        return bounceVelocity(velocity, context, 0.4, 0.01);
    }

    /**
     * 获取碰撞箱（相对于实体中心）
     */
    @NotNull
    AABB getBlockCollisionBox();

    /**
     * 是否启用碰撞检测
     */
    default boolean canCollideWithBlocks() {
        return true;
    }

    /**
     * 碰撞回调，实现类可在此处理碰撞后的速度变化
     */
    default void onBlockCollision(CollisionContext context) {}

    /**
     * 执行方块碰撞检测并修正位置
     */
    default void processBlockCollision(T entity) {
        if (!canCollideWithBlocks()) return;

        ArrayList<PathNode> history = entity.getHistoryNodes();
        if (history.size() < 2) return;

        Vec3 from = history.getFirst().pos();
        Vec3 to = entity.currentPathNode.pos();
        Vec3 motion = to.subtract(from);

        if (motion.lengthSqr() < 1e-10) {
            checkAndResolveOverlap(entity, to);
            return;
        }

        AABB box = getBlockCollisionBox().move(from);
        Level level = entity.getOwner().level();

        List<VoxelShape> colliders = collectColliders(level, box, motion);
        if (colliders.isEmpty()) return;

        CollisionResult result = computeCollision(box, motion, colliders);
        if (result.collided()) {
            Vec3 correctedPos = from.add(result.dx(), result.dy(), result.dz());
            entity.currentPathNode = new PathNode(correctedPos, entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
            onBlockCollision(new CollisionContext(correctedPos, result.collisionX(), result.collisionY(), result.collisionZ()));
        }
    }

    /**
     * 收集运动路径上的碰撞体素（带方向性剔除）
     */
    private List<VoxelShape> collectColliders(Level level, AABB box, Vec3 motion) {
        List<VoxelShape> colliders = new ArrayList<>();
        AABB pathBox = box.expandTowards(motion);

        BlockPos.betweenClosedStream(
                BlockPos.containing(pathBox.minX, pathBox.minY, pathBox.minZ),
                BlockPos.containing(pathBox.maxX, pathBox.maxY, pathBox.maxZ)
        ).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (shape.isEmpty()) return;

            VoxelShape worldShape = shape.move(pos.getX(), pos.getY(), pos.getZ());
            AABB shapeBounds = worldShape.bounds();

            // 方向性剔除：只保留运动前方的方块
            if (shouldCull(motion, box, shapeBounds)) return;

            colliders.add(worldShape);
        });

        return colliders;
    }

    /**
     * 判断方块是否应该被剔除（完全在运动反方向）
     */
    private boolean shouldCull(Vec3 motion, AABB box, AABB shapeBounds) {
        // 使用 >= 和 <= 覆盖 motion == 0 的情况
        return (motion.x >= 0 && shapeBounds.maxX <= box.minX) ||
                (motion.x <= 0 && shapeBounds.minX >= box.maxX) ||
                (motion.y >= 0 && shapeBounds.maxY <= box.minY) ||
                (motion.y <= 0 && shapeBounds.minY >= box.maxY) ||
                (motion.z >= 0 && shapeBounds.maxZ <= box.minZ) ||
                (motion.z <= 0 && shapeBounds.minZ >= box.maxZ);
    }

    /**
     * 计算碰撞结果（使用原版 Shapes.collide 算法）
     */
    private CollisionResult computeCollision(AABB box, Vec3 motion, List<VoxelShape> colliders) {
        double dx = motion.x, dy = motion.y, dz = motion.z;
        AABB currentBox = box;

        // Y轴碰撞（优先处理）
        dy = Shapes.collide(Direction.Axis.Y, currentBox.inflate(-SHRINK_EPSILON, 0, -SHRINK_EPSILON), colliders, dy);
        boolean collisionY = dy != motion.y;
        if (collisionY) currentBox = currentBox.move(0, dy, 0);

        // X/Z轴碰撞（根据移动大小决定顺序）
        boolean xMajor = Math.abs(dx) >= Math.abs(dz);
        boolean collisionX, collisionZ;

        if (xMajor) {
            dx = Shapes.collide(Direction.Axis.X, currentBox.inflate(0, -SHRINK_EPSILON, -SHRINK_EPSILON), colliders, dx);
            collisionX = dx != motion.x;
            if (collisionX) currentBox = currentBox.move(dx, 0, 0);
            dz = Shapes.collide(Direction.Axis.Z, currentBox.inflate(-SHRINK_EPSILON, -SHRINK_EPSILON, 0), colliders, dz);
            collisionZ = dz != motion.z;
        } else {
            dz = Shapes.collide(Direction.Axis.Z, currentBox.inflate(-SHRINK_EPSILON, -SHRINK_EPSILON, 0), colliders, dz);
            collisionZ = dz != motion.z;
            if (collisionZ) currentBox = currentBox.move(0, 0, dz);
            dx = Shapes.collide(Direction.Axis.X, currentBox.inflate(0, -SHRINK_EPSILON, -SHRINK_EPSILON), colliders, dx);
            collisionX = dx != motion.x;
        }

        return new CollisionResult(dx, dy, dz, collisionX, collisionY, collisionZ);
    }

    /**
     * 检测并解决位置重叠（无移动时的穿透修正）
     */
    private void checkAndResolveOverlap(T entity, Vec3 pos) {
        AABB box = getBlockCollisionBox().move(pos);
        Level level = entity.getOwner().level();

        for (VoxelShape shape : collectNearbyColliders(level, box)) {
            AABB shapeBounds = shape.bounds();
            if (!box.intersects(shapeBounds)) continue;

            Vec3 push = computePushVector(box, shapeBounds);
            if (push.lengthSqr() > 0) {
                Vec3 correctedPos = pos.add(push);
                entity.currentPathNode = new PathNode(correctedPos,
                        entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
                onBlockCollision(new CollisionContext(correctedPos,
                        push.x != 0, push.y != 0, push.z != 0));
                return;
            }
        }
    }

    /**
     * 收集附近的碰撞体素
     */
    private List<VoxelShape> collectNearbyColliders(Level level, AABB box) {
        List<VoxelShape> colliders = new ArrayList<>();
        AABB searchBox = box.inflate(0.5);

        BlockPos.betweenClosedStream(
                BlockPos.containing(searchBox.minX, searchBox.minY, searchBox.minZ),
                BlockPos.containing(searchBox.maxX, searchBox.maxY, searchBox.maxZ)
        ).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty()) {
                colliders.add(shape.move(pos.getX(), pos.getY(), pos.getZ()));
            }
        });

        return colliders;
    }

    /**
     * 计算推出向量（选择最小推出距离的轴）
     */
    private Vec3 computePushVector(AABB box, AABB shapeBounds) {
        double pushX = 0, pushY = 0, pushZ = 0;

        if (box.maxX > shapeBounds.minX && box.minX < shapeBounds.minX) {
            pushX = shapeBounds.minX - box.maxX - PUSH_GAP;
        } else if (box.minX < shapeBounds.maxX && box.maxX > shapeBounds.maxX) {
            pushX = shapeBounds.maxX - box.minX + PUSH_GAP;
        }

        if (box.maxY > shapeBounds.minY && box.minY < shapeBounds.minY) {
            pushY = shapeBounds.minY - box.maxY - PUSH_GAP;
        } else if (box.minY < shapeBounds.maxY && box.maxY > shapeBounds.maxY) {
            pushY = shapeBounds.maxY - box.minY + PUSH_GAP;
        }

        if (box.maxZ > shapeBounds.minZ && box.minZ < shapeBounds.minZ) {
            pushZ = shapeBounds.minZ - box.maxZ - PUSH_GAP;
        } else if (box.minZ < shapeBounds.maxZ && box.maxZ > shapeBounds.maxZ) {
            pushZ = shapeBounds.maxZ - box.minZ + PUSH_GAP;
        }

        // 选择最小推出距离的轴
        double absX = Math.abs(pushX), absY = Math.abs(pushY), absZ = Math.abs(pushZ);

        if (absY > 0 && (absY <= absX || absX == 0) && (absY <= absZ || absZ == 0)) {
            return new Vec3(0, pushY, 0);
        } else if (absX > 0 && (absX <= absZ || absZ == 0)) {
            return new Vec3(pushX, 0, 0);
        } else if (absZ > 0) {
            return new Vec3(0, 0, pushZ);
        }

        return Vec3.ZERO;
    }

    /**
     * 碰撞上下文
     */
    record CollisionContext(Vec3 position, boolean collisionX, boolean collisionY, boolean collisionZ) {
    }

    /**
     * 碰撞计算结果
     */
    record CollisionResult(double dx, double dy, double dz, boolean collisionX, boolean collisionY, boolean collisionZ) {
        boolean collided() {
            return collisionX || collisionY || collisionZ;
        }
    }
}
