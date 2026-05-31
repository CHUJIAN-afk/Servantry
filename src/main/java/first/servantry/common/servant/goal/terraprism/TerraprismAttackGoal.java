package first.servantry.common.servant.goal.terraprism;

import first.servantry.api.entity.PathNode;
import first.servantry.api.entity.PlannedPath;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Terraprism;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 泰拉棱镜仆从的攻击状态机。
 */
public class TerraprismAttackGoal extends ServantGoal<Terraprism> {

    private boolean firstStrike = true;
    private Vec3 lastTargetPos = Vec3.ZERO;

    public TerraprismAttackGoal(Terraprism servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget()) && servant.attacking;
    }

    @Override
    public void start() {
        firstStrike = true;
        lastTargetPos = servant.getTarget().getBoundingBox().getCenter();
    }

    @Override
    public void tick() {
        PlannedPath currentPath = servant.getCurrentPath();
        if (currentPath != null && currentPath.getCurrentIndex() == (currentPath.getNodes().size() / 3)) {
            servant.hitTargets.clear();
        }
        applyPositionCorrection();
        if (servant.isTargetChange()) {
            planChainStrike();
        }
        if (!servant.isExecutingPath()) {
            if (firstStrike) {
                firstStrike = false;
                planFirstStrike();
            } else {
                if (servant.getOwner().getRandom().nextDouble() < 0.5) {
                    planEllipseSlash();
                } else {
                    planHourglassSlash();
                }
            }
        }
    }

    /**
     * 规划直线攻击路径。
     */
    private void planFirstStrike() {
        LivingEntity target = servant.getTarget();
        Vec3 start = servant.getPos();
        Vec3 end = target.getBoundingBox().getCenter();
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 1e-4) {
            direction = new Vec3(0, 0, 1);
        }
        end = end.add(direction.normalize().scale(2));
        Vec3 planeNormal = direction.cross(new Vec3(0, 1, 0)).normalize();
        if (planeNormal.lengthSqr() < 1e-4) {
            planeNormal = new Vec3(1, 0, 0);
        }
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            float delta = (float) i / 6;
            delta = delta * delta;
            nodes.add(servant.getEulerNode(start.lerp(end, delta), direction, planeNormal));
        }
        servant.setPath(nodes);
    }

    /**
     * 规划椭圆斩击攻击路径。
     */
    private void planEllipseSlash() {
        Player owner = servant.getOwner();
        int duration = 14;
        int blendTicks = 6;
        Vec3 currentPos = servant.getPos();
        Vec3 T = lastTargetPos;

        // 椭圆几何参数
        float randAngle = owner.getRandom().nextFloat() * Mth.TWO_PI;
        float randY = 0.5f + owner.getRandom().nextFloat() * 2.5f;
        Vec3 farPoint = T.add(Math.cos(randAngle) * 4, randY, Math.sin(randAngle) * 4);
        Vec3 major = farPoint.subtract(T).scale(0.5);
        Vec3 center = T.add(major);
        Vec3 majorDir = major.normalize();
        Vec3 minorDir = majorDir.cross(new Vec3(owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5).normalize()).normalize();
        if (minorDir.lengthSqr() < 1e-5) minorDir = new Vec3(0, 1, 0);
        Vec3 minor = minorDir.scale(major.length() * 0.75);

        // 当前运动状态
        Vec3 currentTip = Vec3.directionFromRotation(servant.getPitch(), servant.getYaw()).normalize();
        Vec3 currentNormal = servant.getCurrentNormal();

        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= duration; i++) {
            float progress = (float) i / duration;
            float biasedT = progress - 0.08f * Mth.sin(progress * Mth.TWO_PI);
            float theta = biasedT * Mth.TWO_PI;

            Vec3 ellipseP = servant.calculateEllipsePoint(center, major, minor, theta);
            Vec3 tangent = servant.calculateEllipseTangent(major, minor, theta);
            Vec3 tipDir = ellipseP.subtract(center).normalize();
            Vec3 normal = tipDir.cross(tangent).normalize();
            if (normal.lengthSqr() < 1e-5) normal = majorDir.cross(tipDir).normalize();

            if (i <= blendTicks) {
                // 前半段：从自身位置平滑过渡到椭圆轨迹
                float delta = (float) i / blendTicks;
                float smooth = delta * delta * (3.0f - 2.0f * delta);
                Vec3 p = currentPos.lerp(ellipseP, smooth);
                Vec3 bTip = currentTip.lerp(tipDir, smooth);
                Vec3 bNormal = currentNormal.lerp(normal, smooth);
                nodes.add(servant.getEulerNode(p, bTip, bNormal));
            } else {
                nodes.add(servant.getEulerNode(ellipseP, tipDir, normal));
            }
        }
        servant.setPath(nodes);
    }

    /**
     * 规划刺击路径。
     */
    private void planHourglassSlash() {
        LivingEntity target = servant.getTarget();
        Vec3 startPos = servant.getPos();
        // 目标头顶4格、半径4格的圆上，选择离startPos最近的位置
        Vec3 center = new Vec3(target.getX(), target.getY() + 3, target.getZ());
        Vec3 toStart = startPos.subtract(center);
        double angle = Math.atan2(toStart.z, toStart.x);
        Vec3 attackPrepPos = center.add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
        Vec3 endPos = target.getBoundingBox().getCenter().offsetRandom(target.getRandom(), 0.5f);

        // 计算攻击方向
        Vec3 attackDir = endPos.subtract(attackPrepPos);
        if (attackDir.lengthSqr() < 1e-5) {
            attackDir = new Vec3(0, -1, 0);
        }
        endPos = endPos.add(attackDir.normalize().scale(3));

        // 获取当前运动状态
        Vec3 currentVel = servant.getCurrentVelocity();
        Vec3 currentTip = Vec3.directionFromRotation(servant.getPitch(), servant.getYaw()).normalize();
        Vec3 currentNormal = servant.getCurrentNormal();

        Vec3 planeNormal = attackDir.cross(new Vec3(0, 1, 0)).normalize();
        if (planeNormal.lengthSqr() < 1e-4) {
            planeNormal = new Vec3(1, 0, 0);
        }

        int prepTicks = 7;
        int attackTicks = 7;

        List<PathNode> nodes = new ArrayList<>();

        for (int i = 1; i <= prepTicks; i++) {
            float delta = (float) i / prepTicks;
            Vec3 point = servant.calculateBezierPoint(delta, startPos, startPos.add(currentVel), attackPrepPos);
            Vec3 tipDir = currentTip.lerp(attackDir, delta);
            Vec3 bNormal = currentNormal.lerp(planeNormal, delta);
            nodes.add(servant.getEulerNode(point, tipDir, bNormal));
        }

        for (int i = 1; i <= attackTicks; i++) {
            float delta = (float) i / attackTicks;
            delta = delta * delta * delta;
            Vec3 point = attackPrepPos.lerp(endPos, delta);
            nodes.add(servant.getEulerNode(point, attackDir, planeNormal));
        }
        servant.setPath(nodes);
    }

    /**
     * 规划连锁攻击路径。
     */
    private void planChainStrike() {
        lastTargetPos = servant.getTarget().getBoundingBox().getCenter();
        double thrustAngleThreshold = 0.9;
        int thrustAttackTicks = 10;
        int sweepAttackTicks = 10;
        double curvePullOutward = 0.35;

        Vec3 startPos = servant.getPos();

        Vec3 T = lastTargetPos;

        Vec3 fwd = T.subtract(startPos);
        double dist = fwd.length();
        if (dist < 1e-5) fwd = new Vec3(0, 0, 1);
        fwd = fwd.normalize();

        if (dist < 6.0) {
            T = T.add(fwd.scale(4.5));
            dist = T.subtract(startPos).length();
        }

        // 获取当前运动状态
        double speed;
        Vec3 currentVel = servant.getCurrentVelocity();
        ArrayList<PathNode> history = servant.getHistoryNodes();
        if (history.size() > 1) {
            speed = startPos.subtract(history.get(1).pos()).length();
        } else {
            speed = 1.0;
        }

        Vec3 currentTip = Vec3.directionFromRotation(servant.getPitch(), servant.getYaw()).normalize();
        Vec3 currentNormal = servant.getCurrentNormal();

        boolean isThrustMode = currentTip.dot(fwd) > thrustAngleThreshold;
        List<PathNode> nodes = new ArrayList<>();

        if (isThrustMode) {
            // 直刺模式
            Vec3 thrustDir = T.subtract(startPos).normalize();
            Vec3 thrustNormal = thrustDir.cross(new Vec3(0, 1, 0)).normalize();
            if (thrustNormal.lengthSqr() < 1e-4) thrustNormal = new Vec3(1, 0, 0);

            Vec3 P0 = startPos;
            Vec3 P3 = T.add(thrustDir.scale(3));
            double Rp = Math.max(dist * 0.3, speed * 2.0);
            Vec3 P1 = startPos.add(currentVel.scale(Rp));
            Vec3 P2 = P3.subtract(thrustDir.scale(Rp));

            for (int i = 1; i <= thrustAttackTicks; i++) {
                float t = (float) i / thrustAttackTicks;
                Vec3 p = servant.calculateBezierPoint(t, P0, P1, P2, P3);
                Vec3 tipDir = servant.slerpVector(currentTip, thrustDir, t);
                Vec3 bNormal = servant.slerpVector(currentNormal, thrustNormal, t);
                nodes.add(servant.getEulerNode(p, tipDir, bNormal));
            }
        } else {
            // 横扫模式
            Vec3 sweepUp = currentNormal;
            Vec3 chord = T.subtract(startPos);
            Vec3 chordDir = chord.normalize();

            Vec3 outward = chordDir.cross(sweepUp).normalize();
            if (outward.lengthSqr() < 1e-4) outward = chordDir.cross(new Vec3(0, 1, 0)).normalize();

            Vec3 P0 = startPos;
            Vec3 P3 = T.add(chordDir.scale(0.5));
            double Rp = Math.max(dist * 0.4, speed * 3.0);
            Vec3 P1 = startPos.add(currentVel.scale(Rp));
            Vec3 P2 = P3.subtract(chordDir.scale(Rp)).add(outward.scale(-dist * curvePullOutward));

            for (int i = 1; i <= sweepAttackTicks; i++) {
                float t = (float) i / sweepAttackTicks;
                Vec3 p = servant.calculateBezierPoint(t, P0, P1, P2, P3);
                Vec3 pivot = startPos.add(chord.scale(0.5)).add(outward.scale(-dist * curvePullOutward * 0.5));
                Vec3 targetTip = p.subtract(pivot).normalize();
                Vec3 tipDir = servant.slerpVector(currentTip, targetTip, t);
                Vec3 bNormal = servant.slerpVector(currentNormal, sweepUp, t);
                nodes.add(servant.getEulerNode(p, tipDir, bNormal));
            }
        }
        servant.setPath(nodes);
    }

    /**
     * 位置修正机制使,仆从轨迹能够实时追踪移动中的目标。
     */
    private void applyPositionCorrection() {
        LivingEntity target = servant.getTarget();
        Vec3 currentTargetCenter = target.getBoundingBox().getCenter();
        Vec3 offset = currentTargetCenter.subtract(lastTargetPos);
        if (offset.lengthSqr() > 1e-5) {
            PlannedPath path = servant.getCurrentPath();
            if (path != null) {
                List<PathNode> nodes = path.getNodes();
                int startIdx = path.getCurrentIndex();
                int remaining = nodes.size() - startIdx;
                for (int i = 0; i < remaining; i++) {
                    PathNode node = nodes.get(startIdx + i);
                    float weight = (float) (i + 1) / remaining;
                    Vec3 blendedOffset = offset.scale(weight);
                    nodes.set(startIdx + i, new PathNode(node.pos().add(blendedOffset), node.yaw(), node.pitch(), node.roll()));
                }
            }
        }
        lastTargetPos = currentTargetCenter;
    }
}