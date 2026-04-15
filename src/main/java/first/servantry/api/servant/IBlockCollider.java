package first.servantry.api.servant;

import first.servantry.api.PathNode;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;

/**
 * 为仆从实现此接口让仆从拥有方块碰撞及滑动偏移能力
 */
public interface IBlockCollider {

    /**
     * @return 碰撞排斥距离（也就是仆从的“物理厚度”，防止模型贴图穿模，默认 0.1 格）
     */
    default double getBlockCollisionRepulsion() {
        return 0.1;
    }

    /**
     * @return 是否开启方块碰撞，可以利用此方法在特定攻击状态下（例如回归时）无视方块穿透
     */
    default boolean shouldCollideWithBlocks(Servant servant) {
        return true;
    }

    /**
     * 核心逻辑：计算射线并使整个路径队列滑动偏移
     */
    default void processBlockCollision(Servant servant) {
        if (!shouldCollideWithBlocks(servant)) return;

        LinkedList<PathNode> futureNodes = servant.getFutureNodes();
        if (futureNodes.isEmpty()) return;

        Player owner = servant.getOwner();
        if (owner == null) return;

        Vec3 startPos = servant.getPos();
        PathNode nextNode = futureNodes.peek();
        Vec3 endPos = nextNode.pos();

        // 没动就不需要检测
        if (startPos.distanceToSqr(endPos) < 1e-5) return;

        // 发射探路射线
        ClipContext context = new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                owner // 将玩家作为射线的实体上下文
        );

        BlockHitResult hitResult = owner.level().clip(context);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            Vec3 hitPos = hitResult.getLocation();
            Direction dir = hitResult.getDirection();
            Vec3 normal = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());

            // 计算仆从原本意图的移动向量
            Vec3 intendedMove = endPos.subtract(startPos);

            // 【核心数学原理】：通过点积计算出朝向墙壁内部的分量，并将其剔除，只保留平行于墙壁的滑动分量
            // V_slide = V_intended - (V_intended · Normal) * Normal
            double dot = intendedMove.dot(normal);
            Vec3 slideMove = intendedMove;
            if (dot < 0) { // 如果正在往墙里钻
                slideMove = intendedMove.subtract(normal.scale(dot));
            }

            // 计算安全位置：撞击点 + 滑动分量 + 根据法线向外推一点点（防止浮点误差导致卡在墙皮里）
            Vec3 safePos = hitPos.add(normal.scale(getBlockCollisionRepulsion())).add(slideMove);

            // 算出修正后的位置与原本位置的差值
            Vec3 offset = safePos.subtract(endPos);

            // 【神来之笔】：将这股因为撞墙产生的偏移力，传递给整个未来轨迹队列
            // 这样武器的动作(例如椭圆斩)不会被截断，而是整个轨迹发生扭曲，顺着墙壁继续华丽地施展
            for (int i = 0; i < futureNodes.size(); i++) {
                PathNode node = futureNodes.get(i);
                futureNodes.set(i, new PathNode(
                        node.feature(),
                        node.pos().add(offset),
                        node.yaw(),
                        node.pitch(),
                        node.roll()
                ));
            }
        }
    }
}