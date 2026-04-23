package first.servantry.api.entity;

import first.servantry.api.OBB;
import first.servantry.api.PathNode;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 碰撞攻击接口，为附件实体提供基于历史轨迹的精确碰撞检测与攻击触发能力。
 * <p>
 * 实现该接口的附件实体（仆从或射弹）将获得沿运动轨迹扫掠的碰撞检测功能。
 * 当实体的 OBB（有向包围盒）在历史轨迹的平滑插值路径上与有效目标相交时，
 * 会触发 {@link #onCollisionAttack(Set)} 方法对命中的所有目标执行攻击逻辑。
 * </p>
 *
 * <h3>核心机制</h3>
 * <ol>
 *   <li><b>轨迹采样</b>：从历史节点中采样多个线段，对每段使用二次贝塞尔曲线平滑插值</li>
 *   <li><b>OBB 构建</b>：在每个采样点构建实体的 OBB 包围盒（考虑自定义碰撞盒偏移）</li>
 *   <li><b>粗筛阶段</b>：合并所有 OBB 的 AABB 作为粗筛范围，快速筛选潜在目标</li>
 *   <li><b>精确检测</b>：对每个潜在目标进行 OBB-AABB 相交检测</li>
 *   <li><b>攻击触发</b>：收集所有相交目标，调用攻击回调</li>
 * </ol>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class MyServant extends Servant implements ICollideAttack {
 *     @Override
 *     public AABB getHitbox() {
 *         return new AABB(-0.3, -0.3, -0.3, 0.3, 0.3, 0.3);
 *     }
 *
 *     @Override
 *     public void onCollisionAttack(Set<LivingEntity> hitTargets) {
 *         Player owner = getOwner();
 *         for (LivingEntity target : hitTargets) {
 *             target.hurt(owner.damageSources().playerAttack(owner), getDamage());
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see AttachmentEntity
 */
public interface ICollideAttack {

    // ===================== 核心抽象方法 =====================

    /**
     * 获取用于碰撞检测的局部碰撞盒（AABB）。
     * <p>
     * 该碰撞盒定义在实体的局部坐标系中（原点为实体位置，无旋转时的轴对齐包围盒）。
     * 实际检测时会根据每个采样点的旋转、位置和偏移量变换为世界坐标系中的 OBB。
     * </p>
     *
     * @return 局部轴对齐包围盒，不应返回 null
     */
    AABB getHitbox();

    /**
     * 当碰撞检测命中目标时调用，执行具体的攻击逻辑。
     * <p>
     * 参数 {@code hitTargets} 包含当前帧内所有被碰撞检测命中的有效目标。
     * 实现者可在此方法内造成伤害、施加效果或播放音效。
     * </p>
     *
     * @param hitTargets 被命中的目标集合（非空）
     */
    void onCollisionAttack(Set<LivingEntity> hitTargets);

    // ===================== 可重写的配置方法 =====================

    /**
     * 获取用于碰撞检测采样的历史节点数量。
     * <p>
     * 该值决定了参与贝塞尔插值的节点个数，影响检测的精度和性能。
     * 值越大，检测范围覆盖的轨迹越长，但计算量也相应增加。
     * </p>
     *
     * @return 采样节点数量，至少为 2
     */
    default int getCollisionSampleNodes() {
        return 2;
    }

    /**
     * 判断目标是否为有效的碰撞对象。
     * <p>
     * 默认实现检查目标是否存活且不是所有者。
     * 子类可重写以添加额外的过滤条件（如阵营检测、无敌状态等）。
     * </p>
     *
     * @param entity 附件实体实例
     * @param target 待检测的目标
     * @return true 表示目标有效，应参与碰撞检测
     */
    default boolean isValidCollisionTarget(AttachmentEntity entity, LivingEntity target) {
        return target.isAlive() && target != entity.getOwner();
    }

    // ===================== 碰撞检测主方法 =====================

    /**
     * 执行基于历史轨迹的精确碰撞检测，并触发攻击。
     * <p>
     * 该方法在实体的 {@code tick()} 方法中自动调用。
     * 检测流程分为三个阶段：
     * <ol>
     *   <li><b>轨迹采样</b>：从历史节点队列中取出指定数量的节点，
     *       对每对相邻节点使用二次贝塞尔曲线插值，生成细粒度的位置和旋转采样点</li>
     *   <li><b>OBB 构建与粗筛</b>：为每个采样点构建 OBB，
     *       并合并所有 OBB 的 AABB 作为粗筛范围，快速获取潜在目标列表</li>
     *   <li><b>精确相交检测</b>：遍历潜在目标，
     *       用其 AABB 与每个采样点的 OBB 进行精确相交测试，收集所有命中的目标</li>
     * </ol>
     * </p>
     *
     * @param entity 执行碰撞检测的附件实体实例
     */
    default void processCollision(AttachmentEntity entity) {
        AABB localBox = getHitbox();
        if (localBox == null) return;

        // 计算碰撞盒尺寸
        double dx = localBox.getXsize();
        double dy = localBox.getYsize();
        double dz = localBox.getZsize();
        double minDim = Math.min(Math.min(dx, dy), dz);

        // 获取历史节点
        LinkedList<PathNode> historyNodes = entity.getHistoryNodes();
        int sampleNodes = getCollisionSampleNodes();

        // 前置条件检查：碰撞盒有效、历史节点足够、采样节点数合法
        if (minDim <= 0 || historyNodes.size() < 2 || sampleNodes < 2) {
            return;
        }

        // 计算需要插值的轨迹段数
        int segments = Math.min(sampleNodes - 1, historyNodes.size() - 1);

        // 预分配列表和变量
        List<OBB> sweepOBBs = new ArrayList<>();
        AABB broadAABB = null;
        Vec3 boxCenterOffset = localBox.getCenter();
        Vec3 boxSize = new Vec3(dx, dy, dz);
        boolean hasCenterOffset = boxCenterOffset.lengthSqr() > 1e-5;

        // ========== 阶段一：轨迹采样并构建 OBB ==========
        for (int s = 0; s < segments; s++) {
            // 获取三个节点用于贝塞尔曲线计算
            PathNode current = historyNodes.get(s);       // 线段终点
            PathNode prev = historyNodes.get(s + 1);      // 线段起点
            PathNode older = historyNodes.size() > s + 2 ? historyNodes.get(s + 2) : prev; // 更早节点

            // 计算二次贝塞尔曲线控制点
            Vec3 P1 = current.pos();
            Vec3 P0 = prev.pos();
            Vec3 P_minus1 = older.pos();
            Vec3 V0 = P0.subtract(P_minus1);
            Vec3 controlPoint = P0.add(V0.scale(0.5));

            // 根据碰撞盒最小尺寸决定采样步长，保证碰撞检测的连续性
            double stepDist = minDim * 0.5;
            double pathLength = P0.distanceTo(P1);
            int steps = Math.max(1, (int) Math.ceil(pathLength / stepDist));

            // 沿贝塞尔曲线采样
            for (int i = 0; i <= steps; i++) {
                float t = (float) i / steps;
                double mt = 1.0 - t;

                // 二次贝塞尔曲线插值位置: B(t) = (1-t)²P0 + 2(1-t)tC + t²P1
                Vec3 pos = P0.scale(mt * mt)
                        .add(controlPoint.scale(2 * mt * t))
                        .add(P1.scale(t * t));

                // 线性插值旋转角度（欧拉角）
                float yaw = Mth.rotLerp(t, prev.yaw(), current.yaw());
                float pitch = Mth.rotLerp(t, prev.pitch(), current.pitch());
                float roll = Mth.rotLerp(t, prev.roll(), current.roll());

                // 应用碰撞盒中心偏移（考虑旋转）
                Vec3 hitCenter = pos;
                if (hasCenterOffset) {
                    hitCenter = hitCenter.add(boxCenterOffset.xRot((float) Math.toRadians(-pitch))
                            .yRot((float) Math.toRadians(-yaw)));
                }

                // 构建该采样点的 OBB
                OBB obb = new OBB(hitCenter, boxSize, yaw, pitch, roll);
                sweepOBBs.add(obb);

                // 更新粗筛 AABB（合并所有 OBB 的包围盒）
                AABB obbBox = obb.getBoundingBox();
                broadAABB = (broadAABB == null) ? obbBox : broadAABB.minmax(obbBox);
            }
        }

        // 没有生成任何 OBB 则直接返回
        if (broadAABB == null) return;

        // ========== 阶段二：粗筛潜在目标 ==========
        Player owner = entity.getOwner();
        if (owner == null) return;

        List<LivingEntity> potentialTargets = owner.level().getEntitiesOfClass(LivingEntity.class, broadAABB);
        Set<LivingEntity> hitTargets = new HashSet<>();

        // ========== 阶段三：精确 OBB-AABB 相交检测 ==========
        for (LivingEntity target : potentialTargets) {
            // 检查目标有效性
            if (!isValidCollisionTarget(entity, target)) {
                continue;
            }

            // 对每个 OBB 进行相交检测
            AABB targetBox = target.getBoundingBox();
            for (OBB obb : sweepOBBs) {
                if (obb.intersects(targetBox)) {
                    hitTargets.add(target);
                    break; // 命中即可跳出内层循环
                }
            }
        }

        // 若有命中目标，执行攻击逻辑
        if (!hitTargets.isEmpty()) {
            onCollisionAttack(hitTargets);
        }
    }
}
