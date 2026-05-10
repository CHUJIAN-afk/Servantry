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
import java.util.List;
import java.util.Set;

/**
 * 碰撞攻击接口，为附件实体提供基于历史轨迹的精确碰撞检测与攻击触发能力。
 *
 * @param <T> 附件实体类型
 */
public interface ICollideAttack<T extends AttachmentEntity> {

    /**
     * 获取用于碰撞检测的局部碰撞盒
     */
    AABB getHitbox();

    /** 当碰撞检测命中目标时调用，执行具体的攻击逻辑 */
    void onCollisionAttack(Set<LivingEntity> hitTargets);

    /**
     * 获取用于碰撞检测采样的历史节点数量
     */
    default int getCollisionSampleNodes() {
        return 2;
    }

    /**
     * 是否启用碰撞攻击检测
     */
    default boolean canCollideAttack() {
        return true; }

    /** 判断目标是否为有效的碰撞对象 */
    default boolean isValidCollisionTarget(T entity, LivingEntity target) {
        return target.isAlive() && target != entity.getOwner();
    }

    /**
     * 执行基于历史轨迹的精确碰撞检测，并触发攻击
     */
    default void processCollision(T entity) {
        if (!canCollideAttack()) return;

        AABB localBox = getHitbox();
        if (localBox == null) return;

        double minDim = Math.min(Math.min(localBox.getXsize(), localBox.getYsize()), localBox.getZsize());
        ArrayList<PathNode> historyNodes = entity.getHistoryNodes();
        int sampleNodes = getCollisionSampleNodes();

        if (minDim <= 0 || historyNodes.size() < 2 || sampleNodes < 2) return;

        int segments = Math.min(sampleNodes - 1, historyNodes.size() - 1);

        // 阶段一：轨迹采样并构建 OBB
        List<OBB> sweepOBBs = new ArrayList<>();
        AABB broadAABB = null;

        Vec3 boxCenterOffset = localBox.getCenter();
        Vec3 boxSize = new Vec3(localBox.getXsize(), localBox.getYsize(), localBox.getZsize());
        boolean hasCenterOffset = boxCenterOffset.lengthSqr() > 1e-5;

        for (int s = 0; s < segments; s++) {
            PathNode current = historyNodes.get(s);
            PathNode prev = historyNodes.get(s + 1);
            PathNode older = historyNodes.size() > s + 2 ? historyNodes.get(s + 2) : prev;

            // 二次贝塞尔曲线控制点
            Vec3 P0 = prev.pos();
            Vec3 P1 = current.pos();
            Vec3 controlPoint = P0.add(P0.subtract(older.pos()).scale(0.5));

            double stepDist = minDim * 0.5;
            int steps = Math.max(1, (int) Math.ceil(P0.distanceTo(P1) / stepDist));

            for (int i = 0; i <= steps; i++) {
                float t = (float) i / steps;
                OBB obb = createSweepOBB(P0, controlPoint, P1, prev, current, t, boxCenterOffset, boxSize, hasCenterOffset);
                sweepOBBs.add(obb);
                broadAABB = (broadAABB == null) ? obb.getBoundingBox() : broadAABB.minmax(obb.getBoundingBox());
            }
        }

        if (broadAABB == null) return;

        // 阶段二：粗筛潜在目标
        Player owner = entity.getOwner();
        if (owner == null) return;

        List<LivingEntity> potentialTargets = owner.level().getEntitiesOfClass(LivingEntity.class, broadAABB);

        // 阶段三：精确 OBB-AABB 相交检测
        Set<LivingEntity> hitTargets = new HashSet<>();
        for (LivingEntity target : potentialTargets) {
            if (!isValidCollisionTarget(entity, target)) continue;
            if (checkOBBCollision(sweepOBBs, target.getBoundingBox())) {
                hitTargets.add(target);
            }
        }

        if (!hitTargets.isEmpty()) {
            onCollisionAttack(hitTargets);
        }
    }

    /**
     * 创建扫掠 OBB（贝塞尔曲线插值）
     */
    private OBB createSweepOBB(Vec3 P0, Vec3 controlPoint, Vec3 P1, PathNode prev, PathNode current,
                               float t, Vec3 boxCenterOffset, Vec3 boxSize, boolean hasCenterOffset) {
        double mt = 1.0 - t;

        // 二次贝塞尔曲线插值位置: B(t) = (1-t)²P0 + 2(1-t)tC + t²P1
        Vec3 pos = P0.scale(mt * mt)
                .add(controlPoint.scale(2 * mt * t))
                .add(P1.scale(t * t));

        // 线性插值旋转角度
        float yaw = Mth.rotLerp(t, prev.yaw(), current.yaw());
        float pitch = Mth.rotLerp(t, prev.pitch(), current.pitch());
        float roll = Mth.rotLerp(t, prev.roll(), current.roll());

        // 应用碰撞盒中心偏移（考虑旋转）
        Vec3 hitCenter = pos;
        if (hasCenterOffset) {
            hitCenter = hitCenter.add(boxCenterOffset.xRot((float) Math.toRadians(-pitch))
                    .yRot((float) Math.toRadians(-yaw)));
        }

        return new OBB(hitCenter, boxSize, yaw, pitch, roll);
    }

    /**
     * 检查目标 AABB 是否与任意 OBB 相交
     */
    private boolean checkOBBCollision(List<OBB> sweepOBBs, AABB targetBox) {
        for (OBB obb : sweepOBBs) {
            if (obb.intersects(targetBox)) return true;
        }
        return false;
    }
}
