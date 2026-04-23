package first.servantry.api.servant;

import first.servantry.api.OBB;
import first.servantry.api.PathNode;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 碰撞攻击接口，为仆从提供基于历史轨迹的精确碰撞检测与攻击触发能力。
 * <p>
 * 实现该接口的仆从类将获得沿运动轨迹扫掠的碰撞检测功能。
 * 当仆从的 OBB（有向包围盒）在历史轨迹的平滑插值路径上与有效目标相交时，
 * 会触发 {@link #collisionAttack(Set)} 方法对命中的所有目标执行攻击逻辑。
 * </p>
 * <p>
 * 核心机制：
 * <ol>
 *   <li>从历史节点中采样多个线段，对每段使用二次贝塞尔曲线平滑插值，生成精细的位置与旋转序列；</li>
 *   <li>在每个采样点构建仆从的 OBB 包围盒（考虑自定义碰撞盒偏移）；</li>
 *   <li>合并所有 OBB 的 AABB 作为粗筛范围，筛选潜在目标；</li>
 *   <li>对每个潜在目标进行精确的 OBB-AABB 相交检测；</li>
 *   <li>收集所有相交目标，调用攻击方法。</li>
 * </ol>
 * </p>
 */
public interface ICollideAttack {

    /**
     * 获取仆从用于碰撞检测的局部碰撞盒（AABB）。
     * <p>
     * 该碰撞盒定义在仆从的局部坐标系中（原点为仆从位置，无旋转时的轴对齐包围盒）。
     * 实际检测时会根据每个采样点的旋转、位置和偏移量变换为世界坐标系中的 OBB。
     * </p>
     *
     * @return 局部轴对齐包围盒
     */
    AABB getHitbox();

    /**
     * 当碰撞检测命中目标时调用，执行具体的攻击逻辑。
     * <p>
     * 参数 {@code hitTargets} 包含了当前帧内所有被碰撞检测命中的有效目标。
     * 实现者可以在此方法内造成伤害、施加效果或播放音效。
     * </p>
     *
     * @param hitTargets 被命中的目标集合（非空）
     */
    void collisionAttack(Set<LivingEntity> hitTargets);

    /**
     * 执行基于历史轨迹的精确碰撞检测，并触发攻击。
     * <p>
     * 该方法由仆从的 {@code tick()} 逻辑自动调用，无需手动触发。
     * 检测流程分为三个阶段：
     * <ol>
     *   <li><b>轨迹采样</b>：从历史节点队列中取出指定数量的节点，对每对相邻节点使用二次贝塞尔曲线插值，
     *       生成细粒度的位置和旋转采样点。</li>
     *   <li><b>OBB 构建与粗筛</b>：为每个采样点构建 OBB，并合并所有 OBB 的 AABB 作为粗筛范围，
     *       快速获取潜在目标列表。</li>
     *   <li><b>精确相交检测</b>：遍历潜在目标，用其 AABB 与每个采样点的 OBB 进行精确相交测试，
     *       收集所有命中的目标。</li>
     * </ol>
     * </p>
     *
     * @param servant 执行碰撞检测的仆从实例
     */
    default void processCollision(Servant servant) {
        AABB localBox = getHitbox();
        double dx = localBox.getXsize();
        double dy = localBox.getYsize();
        double dz = localBox.getZsize();
        double minDim = Math.min(Math.min(dx, dy), dz);
        LinkedList<PathNode> historyNodes = servant.getHistoryNodes();
        int sampleNodes = getCollisionSampleNodes();

        // 仅当碰撞盒尺寸有效、历史节点足够且采样节点数合法时才进行检测
        if (minDim > 0 && historyNodes.size() >= 2 && sampleNodes >= 2) {
            // 计算需要插值的轨迹段数（采样点数 - 1）
            int segments = Math.min(sampleNodes - 1, historyNodes.size() - 1);

            List<OBB> sweepOBBs = new ArrayList<>();
            AABB broadAABB = null;
            Vec3 boxCenterOffset = localBox.getCenter();
            Vec3 boxSize = new Vec3(dx, dy, dz);

            // ========== 阶段一：轨迹采样并构建 OBB ==========
            for (int s = 0; s < segments; s++) {
                // 当前节点（线段终点）、前一节点（线段起点）、更前一节点（用于贝塞尔控制点计算）
                PathNode current = historyNodes.get(s);
                PathNode prev = historyNodes.get(s + 1);
                PathNode older = historyNodes.size() > s + 2 ? historyNodes.get(s + 2) : prev;

                Vec3 P1 = current.pos();
                Vec3 P0 = prev.pos();
                Vec3 P_minus1 = older.pos();

                // 计算二次贝塞尔曲线的控制点 C，使曲线在 P0 处的切线与 V0 方向一致
                Vec3 V0 = P0.subtract(P_minus1);
                Vec3 C = P0.add(V0.scale(0.5));

                // 根据碰撞盒最小尺寸决定采样步长，保证碰撞检测的连续性
                double stepDist = minDim * 0.5;
                double pathLength = P0.distanceTo(P1);
                int steps = Math.max(1, (int) Math.ceil(pathLength / stepDist));

                for (int i = 0; i <= steps; i++) {
                    float t = (float) i / steps;
                    double mt = 1.0 - t;

                    // 二次贝塞尔曲线插值位置
                    Vec3 pos = P0.scale(mt * mt)
                            .add(C.scale(2 * mt * t))
                            .add(P1.scale(t * t));

                    // 线性插值旋转角度（欧拉角）
                    float yaw = Mth.rotLerp(t, prev.yaw(), current.yaw());
                    float pitch = Mth.rotLerp(t, prev.pitch(), current.pitch());
                    float roll = Mth.rotLerp(t, prev.roll(), current.roll());

                    // 应用碰撞盒中心偏移（考虑旋转）
                    Vec3 hitCenter = pos;
                    if (boxCenterOffset.lengthSqr() > 1e-5) {
                        hitCenter = hitCenter.add(boxCenterOffset.xRot((float) Math.toRadians(-pitch)).yRot((float) Math.toRadians(-yaw)));
                    }

                    // 构建该采样点的 OBB，并加入列表
                    OBB obb = new OBB(hitCenter, boxSize, yaw, pitch, roll);
                    sweepOBBs.add(obb);

                    // 更新粗筛 AABB
                    AABB obbBox = obb.getBoundingBox();
                    broadAABB = (broadAABB == null) ? obbBox : broadAABB.minmax(obbBox);
                }
            }

            // 没有生成任何 OBB 则直接返回
            if (broadAABB == null) return;

            // ========== 阶段二：粗筛潜在目标 ==========
            Player owner = servant.getOwner();
            List<LivingEntity> potentialTargets = owner.level().getEntitiesOfClass(LivingEntity.class, broadAABB);
            Set<LivingEntity> hitTargets = new HashSet<>();

            // ========== 阶段三：精确 OBB-AABB 相交检测 ==========
            for (LivingEntity target : potentialTargets) {
                if (servant.isTarget(target)) {
                    AABB targetBox = target.getBoundingBox();
                    for (OBB obb : sweepOBBs) {
                        if (obb.intersects(targetBox)) {
                            hitTargets.add(target);
                            break; // 一旦命中即可跳出内层循环
                        }
                    }
                }
            }

            // 若有命中目标，执行攻击逻辑
            if (!hitTargets.isEmpty()) {
                collisionAttack(hitTargets);
            }
        }
    }

    /**
     * 获取用于碰撞检测采样的历史节点数量。
     * <p>
     * 该值决定了参与贝塞尔插值的节点个数，影响检测的精度和性能。
     * 值越大，检测范围覆盖的轨迹越长，但计算量也相应增加。
     * 默认返回 2，表示仅采样最近两个节点（一段轨迹）。
     * </p>
     *
     * @return 采样节点数量，至少为 2
     */
    default int getCollisionSampleNodes() {
        return 2;
    }

}