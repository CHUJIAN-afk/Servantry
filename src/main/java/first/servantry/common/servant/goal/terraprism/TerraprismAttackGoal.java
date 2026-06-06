package first.servantry.common.servant.goal.terraprism;

import first.servantry.api.entity.Ellipse;
import first.servantry.api.entity.PathNode;
import first.servantry.api.entity.PlannedPath;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Terraprism;
import first.servantry.utils.EasingCurve;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 泰拉棱镜仆从的攻击状态机。
 */
public class TerraprismAttackGoal extends ServantGoal<Terraprism> {

    private boolean firstStrike = true;
    private boolean lastEllipse = false;
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
            lastEllipse = false;
        }
        if (!servant.isExecutingPath()) {
            if (firstStrike) {
                planFirstStrike();
                firstStrike = false;
                lastEllipse = false;
            } else {
                if (servant.getOwner().getRandom().nextDouble() < 0.8) {
                    planEllipseSlash();
                    lastEllipse = true;
                } else {
                    planHourglassSlash();
                    lastEllipse = false;
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
        int duration = 7;
        for (int i = 1; i <= duration; i++) {
            float progress = (float) i / duration;
            nodes.add(servant.getEulerNode(start.lerp(end, EasingCurve.EASE_IN_OUT_QUAD.apply(progress)), direction, planeNormal));
        }
        servant.setPath(nodes);
    }

    /**
     * 规划椭圆斩击攻击路径。
     */
    private void planEllipseSlash() {
        // 目标头顶4格、半径4格的圆上，选择离startPos最近的位置
        Vec3 center = lastTargetPos.add(0, 3, 0);
        Vec3 startPos = servant.getPos();
        Vec3 toStart = startPos.subtract(center);
        double angle = Math.atan2(toStart.z, toStart.x);
        Vec3 attackPrepPos = center.add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);

        // 获取当前运动状态
        Vec3 currentVel = servant.getCurrentVelocity();

        Vec3 planeNormal = lastEllipse ? servant.getCurrentNormal() : Ellipse.randomPlaneNormal(servant.getOwner()
                                                                                                        .getRandom(), lastTargetPos, attackPrepPos);
        Ellipse ellipse = new Ellipse(lastTargetPos, attackPrepPos, planeNormal, 0.45f);

        PathNode attackPrepPathNode = servant.getEulerNode(ellipse.getPoint(0), ellipse.getPoint(0)
                .subtract(ellipse.getCenter()).normalize(), planeNormal);

        List<PathNode> nodes = new ArrayList<>();

        int duration = 14;
        int prepTicks = Math.min(4, (int) (startPos.distanceTo(attackPrepPos)));
        if (prepTicks > 0) {
            for (int i = 0; i <= prepTicks; i++) {
                float progress = (float) i / prepTicks;
                Vec3 point = servant.calculateBezierPoint(progress, startPos, startPos.add(currentVel), attackPrepPos);
                PathNode lerp = servant.getCurrentPathNode().lerp(attackPrepPathNode, progress);
                nodes.add(new PathNode(point, lerp.yaw(), lerp.pitch(), lerp.roll()));
            }
        }
        int attackTicks = duration - prepTicks;
        for (int i = 0; i < attackTicks; i++) {
            float progress = (float) i / attackTicks;
            Vec3 point = ellipse.getPoint(EasingCurve.LINEAR.apply(progress));
            Vec3 tipDir = point.subtract(ellipse.getCenter()).normalize();
            nodes.add(servant.getEulerNode(point, tipDir, planeNormal));
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
        Vec3 attackPrepPos = center.add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);
        Vec3 endPos = target.getBoundingBox().getCenter();

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

        List<PathNode> nodes = new ArrayList<>();

        PathNode attackStartNode = null;
        int prepTicks = 8;
        for (int i = 0; i <= prepTicks; i++) {
            float progress = EasingCurve.EASE_OUT_QUAD.apply((float) i / prepTicks);
            Vec3 point = servant.calculateBezierPoint(progress, startPos, startPos.add(currentVel), attackPrepPos);
            Vec3 tipDir = currentTip.lerp(attackDir, progress);
            PathNode pathNode = servant.getEulerNode(point, tipDir, currentNormal);
            nodes.add(pathNode);
            attackStartNode = pathNode;
        }

        PathNode attackEndNode = new PathNode(endPos, attackStartNode.yaw(), attackStartNode.pitch(), attackStartNode.roll());
        int attackTicks = 6;
        for (int i = 0; i <= attackTicks; i++) {
            float progress = EasingCurve.EASE_IN_OUT_QUAD.apply((float) i / attackTicks);
            nodes.add(attackStartNode.lerp(attackEndNode, progress));
        }
        servant.setPath(nodes);
    }

    /**
     * 规划连锁攻击路径。
     */
    private void planChainStrike() {
        Vec3 endPos = servant.getTarget().getBoundingBox().getCenter();
        int duration = 7;
        Vec3 currentNormal = servant.getCurrentNormal();
        List<PathNode> nodes = new ArrayList<>();
        Ellipse ellipse = new Ellipse(endPos, servant.getPos(), currentNormal, 0.25f);
        for (int i = 1; i <= duration; i++) {
            float progress = (float) i / duration;
            Vec3 ellipseP = ellipse.getPoint(progress * 0.5f);
            Vec3 tipDir = ellipseP.subtract(ellipse.getCenter()).normalize();
            nodes.add(servant.getEulerNode(ellipseP, tipDir, currentNormal));
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