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

import java.util.ArrayList;
import java.util.List;

/**
 * 方块碰撞接口，使用原版碰撞检测API实现精确的碰撞检测与位置修正。
 * <p>
 * 设计原则：
 * <ul>
 *   <li>完全解耦：接口默认实现，实现类无需额外变量</li>
 *   <li>高性能：直接使用原版Shapes.collide()算法</li>
 *   <li>稳定可靠：基于运动向量的方向性体素剔除，防止吸附和穿透</li>
 * </ul>
 * </p>
 *
 * @param <T> 附件实体类型
 */
public interface IBlockCollision<T extends AttachmentEntity> {

    /**
     * 清零碰撞轴的速度分量。
     */
    static Vec3 clearVelocity(Vec3 velocity, CollisionContext context) {
        return new Vec3(
                context.collisionX() ? 0 : velocity.x,
                context.collisionY() ? 0 : velocity.y,
                context.collisionZ() ? 0 : velocity.z
        );
    }

    /**
     * 获取碰撞箱大小（相对于实体中心）。
     */
    AABB getBlockCollisionBox();

    /**
     * 是否启用碰撞检测。
     */
    default boolean canCollideWithBlocks() {
        return true;
    }

    /**
     * 碰撞回调，实现类可在此处理碰撞后的速度变化。
     *
     * @param context 碰撞上下文
     */
    default void onBlockCollision(CollisionContext context) {
    }

    /**
     * 执行方块碰撞检测并修正位置。
     * <p>
     * 核心算法：
     * <ol>
     *   <li>获取运动向量（从历史位置到当前位置）</li>
     *   <li>基于运动方向严格过滤体素（只收集运动前方的方块）</li>
     *   <li>使用Shapes.collide()计算每轴实际可移动距离</li>
     *   <li>修正位置并触发回调</li>
     * </ol>
     * </p>
     */
    default void processBlockCollision(T entity) {
        if (!canCollideWithBlocks()) return;

        var history = entity.getHistoryNodes();
        if (history.size() < 2) return;

        Vec3 from = history.getFirst().pos();
        Vec3 to = entity.currentPathNode.pos();
        Vec3 motion = to.subtract(from);

        // 无移动时检测重叠
        if (motion.lengthSqr() < 1e-10) {
            checkAndResolveOverlap(entity, to);
            return;
        }

        AABB box = getBlockCollisionBox().move(from);
        var level = entity.getOwner().level();

        // 基于运动方向严格过滤体素
        List<VoxelShape> colliders = collectDirectionalColliders(level, box, motion);
        if (colliders.isEmpty()) return;

        // 使用原版碰撞检测计算实际可移动距离
        // 非扫描轴微收缩（1e-5），解决贴墙滑动时的"拼缝卡顿"问题
        double dx = motion.x, dy = motion.y, dz = motion.z;
        AABB currentBox = box;

        // Y轴碰撞（优先处理）：收缩X和Z，防止擦墙卡顿
        dy = Shapes.collide(Direction.Axis.Y, currentBox.inflate(-1e-5, 0, -1e-5), colliders, dy);
        boolean collisionY = dy != motion.y;
        if (collisionY) {
            currentBox = currentBox.move(0, dy, 0);
        }

        // X/Z轴碰撞（根据移动大小决定顺序）
        boolean xMajor = Math.abs(dx) >= Math.abs(dz);
        boolean collisionX, collisionZ;

        if (xMajor) {
            // X轴扫掠：收缩Y和Z，防止擦地/擦墙顶卡顿
            dx = Shapes.collide(Direction.Axis.X, currentBox.inflate(0, -1e-5, -1e-5), colliders, dx);
            collisionX = dx != motion.x;
            if (collisionX) currentBox = currentBox.move(dx, 0, 0);
            // Z轴扫掠：收缩X和Y
            dz = Shapes.collide(Direction.Axis.Z, currentBox.inflate(-1e-5, -1e-5, 0), colliders, dz);
            collisionZ = dz != motion.z;
        } else {
            // Z轴扫掠：收缩X和Y
            dz = Shapes.collide(Direction.Axis.Z, currentBox.inflate(-1e-5, -1e-5, 0), colliders, dz);
            collisionZ = dz != motion.z;
            if (collisionZ) currentBox = currentBox.move(0, 0, dz);
            // X轴扫掠：收缩Y和Z
            dx = Shapes.collide(Direction.Axis.X, currentBox.inflate(0, -1e-5, -1e-5), colliders, dx);
            collisionX = dx != motion.x;
        }

        // 发生碰撞时修正位置并回调
        if (collisionX || collisionY || collisionZ) {
            Vec3 correctedPos = from.add(dx, dy, dz);
            entity.currentPathNode = new PathNode(correctedPos, entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
            onBlockCollision(new CollisionContext(correctedPos, collisionX, collisionY, collisionZ));
        }
    }

    /**
     * 基于运动方向严格过滤体素，只收集运动前方的方块。
     * <p>
     * 方向性剔除规则（基于方块碰撞边界）：
     * <ul>
     *   <li>motion.x > 0：只收集方块 minX >= box.minX 的方块</li>
     *   <li>motion.x < 0：只收集方块 maxX <= box.maxX 的方块</li>
     *   <li>motion.x == 0：不进行X轴方向过滤</li>
     *   <li>Y/Z轴同理</li>
     * </ul>
     * </p>
     */
    private List<VoxelShape> collectDirectionalColliders(Level level, AABB box, Vec3 motion) {
        List<VoxelShape> colliders = new ArrayList<>();

        // 构建精确的移动路径箱体
        AABB pathBox = box.expandTowards(motion);

        BlockPos minPos = BlockPos.containing(pathBox.minX, pathBox.minY, pathBox.minZ);
        BlockPos maxPos = BlockPos.containing(pathBox.maxX, pathBox.maxY, pathBox.maxZ);

        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    VoxelShape shape = state.getCollisionShape(level, pos);
                    if (shape.isEmpty()) continue;

                    // 获取方块在世界中的实际碰撞箱
                    VoxelShape worldShape = shape.move(x, y, z);
                    AABB shapeBounds = worldShape.bounds();

                    // 方向性过滤：只保留运动前方的方块
                    // 使用 >= 和 <= 覆盖 motion == 0 的情况，彻底无视静止轴方向上紧贴的方块
                    // X轴剔除
                    if (motion.x >= 0 && shapeBounds.maxX <= box.minX) continue;
                    if (motion.x <= 0 && shapeBounds.minX >= box.maxX) continue;

                    // Y轴剔除
                    if (motion.y >= 0 && shapeBounds.maxY <= box.minY) continue;
                    if (motion.y <= 0 && shapeBounds.minY >= box.maxY) continue;

                    // Z轴剔除
                    if (motion.z >= 0 && shapeBounds.maxZ <= box.minZ) continue;
                    if (motion.z <= 0 && shapeBounds.minZ >= box.maxZ) continue;

                    colliders.add(worldShape);
                }
            }
        }
        return colliders;
    }

    /**
     * 检测当前位置是否与方块重叠，如果重叠则推出。
     */
    private void checkAndResolveOverlap(T entity, Vec3 pos) {
        AABB box = getBlockCollisionBox().move(pos);
        var level = entity.getOwner().level();

        List<VoxelShape> colliders = new ArrayList<>();
        AABB searchBox = box.inflate(0.5);

        BlockPos minPos = BlockPos.containing(searchBox.minX, searchBox.minY, searchBox.minZ);
        BlockPos maxPos = BlockPos.containing(searchBox.maxX, searchBox.maxY, searchBox.maxZ);

        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(blockPos);
                    VoxelShape shape = state.getCollisionShape(level, blockPos);
                    if (!shape.isEmpty()) {
                        colliders.add(shape.move(x, y, z));
                    }
                }
            }
        }

        if (colliders.isEmpty()) return;

        // 检测重叠并推出
        double gap = 1.0E-5;
        for (VoxelShape shape : colliders) {
            AABB shapeBounds = shape.bounds();
            if (box.intersects(shapeBounds)) {
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

                double absX = Math.abs(pushX), absY = Math.abs(pushY), absZ = Math.abs(pushZ);

                Vec3 correctedPos = pos;
                boolean cx = false, cy = false, cz = false;

                if (absY > 0 && (absY <= absX || absX == 0) && (absY <= absZ || absZ == 0)) {
                    correctedPos = pos.add(0, pushY, 0);
                    cy = true;
                } else if (absX > 0 && (absX <= absZ || absZ == 0)) {
                    correctedPos = pos.add(pushX, 0, 0);
                    cx = true;
                } else if (absZ > 0) {
                    correctedPos = pos.add(0, 0, pushZ);
                    cz = true;
                }

                if (cx || cy || cz) {
                    entity.currentPathNode = new PathNode(correctedPos, entity.currentPathNode.yaw(), entity.currentPathNode.pitch(), entity.currentPathNode.roll());
                    onBlockCollision(new CollisionContext(correctedPos, cx, cy, cz));
                }
                return;
            }
        }
    }

    /**
     * 碰撞上下文记录类。
     */
    record CollisionContext(Vec3 position, boolean collisionX, boolean collisionY, boolean collisionZ) {
    }
}