package first.servantry.common.servent.gaol;

import first.servantry.api.ai.ServantGoal;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.PlannedPath;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.servent.Terraprism;
import first.servantry.register.AttachmentRegister;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 泰拉棱镜仆从的攻击目标类。
 * <p>
 * 攻击流程采用状态机驱动，包含四个阶段：
 * <ol>
 *   <li>PREP - 准备阶段：仆从上升到准备位置并调整朝向</li>
 *   <li>FIRST_STRIKE - 首次攻击：直线冲刺攻击目标</li>
 *   <li>CONTINUOUS - 连续攻击：交替执行椭圆斩击和沙漏斩击</li>
 *   <li>CHAIN - 连锁攻击：目标切换时的快速衔接攻击</li>
 * </ol>
 * </p>
 * <p>
 * 核心特性：
 * <ul>
 *   <li>攻击周期固定为 14 tick，确保节奏一致性</li>
 *   <li>持续位置修正：只要存在合法目标就实时调整轨迹</li>
 *   <li>伤害控制：动作执行到一半时清空已攻击列表，确保每个目标每轮攻击仅受一次伤害</li>
 * </ul>
 * </p>
 */
public class TerraprismAttackGoal extends ServantGoal<Terraprism> {

    public TerraprismAttackGoal(Terraprism servant) {
        super(servant);
    }

    // ===================== 状态定义 =====================

    /** 攻击阶段枚举 */
    enum Phase { PREP, FIRST_STRIKE, CONTINUOUS, CHAIN }

    /** 当前攻击阶段 */
    private Phase phase;

    /** 上一次记录的目标位置，用于位置修正计算 */
    private Vec3 lastTargetPos;

    /** 连续攻击循环计数器 */
    private int loopCount;

    /** 准备阶段的目标位置 */
    private Vec3 prepPos;

    /** 准备阶段的等待计数器 */
    private int prepTick;

    /** 上一次是否为椭圆斩击模式，用于交替切换 */
    private boolean lastWasEllipse = true;

    // ===================== 目标条件判断 =====================

    @Override
    public boolean canUse() {
        return servant.getTarget() != null && canTransitionToAttack(servant.getOwner());
    }

    /**
     * 判断当前仆从是否可以进入攻击状态。
     * <p>
     * 选择优先级最高的空闲泰拉棱镜仆从执行攻击。
     * </p>
     */
    public boolean canTransitionToAttack(Player owner) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        int maxOrder = -1;
        Servant chosen = null;
        for (Servant s : data.getServants()) {
            if (s instanceof Terraprism ts && ts.idle) {
                int order = data.getOrder(s);
                if (order > maxOrder) {
                    maxOrder = order;
                    chosen = s;
                }
            }
        }
        return chosen == servant;
    }

    @Override
    public boolean canContinueToUse() {
        if (!servant.isTarget(servant.getTarget())) return false;
        if (servant.isExecutingPath()) return true;
        Player owner = servant.getOwner();
        return owner == null || servant.getPos().distanceToSqr(owner.position()) <= 4096.0;
    }

    @Override
    public boolean isInterruptable() {
        return !servant.isExecutingPath();
    }

    // ===================== 状态生命周期 =====================

    @Override
    public void start() {
        servant.attacking = true;
        servant.idle = false;
        phase = Phase.PREP;
        prepPos = servant.getPos().add(0, 2, 0);
        prepTick = 0;
        loopCount = 0;
        lastTargetPos = null;
    }

    @Override
    public void stop() {
        servant.attacking = false;
        servant.hitTargets.clear();
        phase = null;
    }

    // ===================== 主 Tick 循环 =====================

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();

        // 目标切换检测：在非准备和非连锁阶段时触发连锁攻击
        if (servant.isTargetChange() && phase != Phase.PREP && phase != Phase.CHAIN && target != null) {
            loopCount = 0;
            transitionToChain(target);
            return;
        }

        // 状态机调度
        switch (phase) {
            case PREP -> tickPrep(target);
            case FIRST_STRIKE -> tickFirstStrike(target);
            case CONTINUOUS -> tickContinuous(target);
            case CHAIN -> tickChain(target);
        }
    }

    // ===================== 准备阶段 (PREP) =====================

    /**
     * 准备阶段：仆从上升到准备位置并调整朝向目标。
     * <p>
     * 当仆从到达准备位置并稳定 5 tick 后，进入首次攻击阶段。
     * </p>
     */
    private void tickPrep(LivingEntity target) {
        if (target == null) return;

        // 计算平滑过渡位置和朝向
        Vec3 nextPos = servant.getPos().lerp(prepPos, 0.2f);
        float nextYaw = servant.getYaw();
        float nextPitch = servant.getPitch();
        float nextRoll = servant.getRoll();

        Vec3 toTarget = target.getEyePosition().subtract(servant.getPos());
        if (toTarget.lengthSqr() > 1e-4) {
            Vec3 bladeNormal = toTarget.cross(new Vec3(0, 1, 0)).normalize();
            PathNode prepNode = servant.getEulerNode(servant.getPos(), toTarget, bladeNormal);
            nextYaw = Mth.rotLerp(0.3f, servant.getYaw(), prepNode.yaw());
            nextPitch = Mth.rotLerp(0.3f, servant.getPitch(), prepNode.pitch());
            nextRoll = Mth.rotLerp(0.3f, servant.getRoll(), prepNode.roll());
        }

        servant.setPath(Collections.singletonList(new PathNode(nextPos, nextYaw, nextPitch, nextRoll)));

        // 检测是否到达准备位置
        if (servant.getPos().distanceToSqr(prepPos) < 0.5) {
            prepTick++;
            if (prepTick > 4) {
                transitionToFirstStrike(target);
            }
        }
    }

    // ===================== 首次攻击阶段 (FIRST_STRIKE) =====================

    /**
     * 进入首次攻击阶段：规划直线冲刺攻击路径。
     */
    private void transitionToFirstStrike(LivingEntity target) {
        phase = Phase.FIRST_STRIKE;
        servant.hitTargets.clear();

        Vec3 start = servant.getPos();
        Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 moveDir = end.subtract(start);
        if (moveDir.lengthSqr() < 1e-4) moveDir = new Vec3(0, 0, 1);

        Vec3 planeNormal = moveDir.cross(new Vec3(0, 1, 0)).normalize();
        if (planeNormal.lengthSqr() < 1e-4) planeNormal = new Vec3(1, 0, 0);

        // 生成 6 tick 的加速冲刺路径
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            float t = ((float) i / 6) * ((float) i / 6);
            Vec3 p = start.lerp(end, t);
            nodes.add(servant.getEulerNode(p, moveDir, planeNormal));
        }
        servant.setPath(nodes);
    }

    /**
     * 首次攻击阶段 Tick：路径完成后进入连续攻击阶段。
     */
    private void tickFirstStrike(LivingEntity target) {
        applyPositionCorrection(target);
        applyDamageControl(6);

        if (!servant.isExecutingPath() && target != null) {
            transitionToContinuous(target);
        }
    }

    // ===================== 连续攻击阶段 (CONTINUOUS) =====================

    /**
     * 进入连续攻击阶段：初始化循环计数并规划首次斩击。
     */
    private void transitionToContinuous(LivingEntity target) {
        phase = Phase.CONTINUOUS;
        loopCount = 0;
        planSlashAttack(target);
    }

    /**
     * 连续攻击阶段 Tick：执行位置修正、伤害控制，并在路径完成后规划下一轮攻击。
     */
    private void tickContinuous(LivingEntity target) {
        applyPositionCorrection(target);
        applyDamageControl(14);

        if (!servant.isExecutingPath() && target != null) {
            loopCount++;
            // 每两次攻击后切换攻击模式
            if (loopCount >= 2) {
                loopCount = 0;
                lastWasEllipse = !lastWasEllipse;
            }
            planSlashAttack(target);
        }
    }

    /**
     * 规划斩击攻击：根据当前模式选择椭圆斩击或沙漏斩击。
     */
    private void planSlashAttack(LivingEntity target) {
        if (target == null) return;
        servant.hitTargets.clear();
        lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);

        if (lastWasEllipse) {
            planEllipseSlash(target);
        } else {
            planHourglassSlash(target);
        }
    }

    // ===================== 椭圆斩击规划 =====================

    /**
     * 规划椭圆斩击攻击路径。
     * <p>
     * 路径由两部分组成：
     * <ul>
     *   <li>前 6 tick：贝塞尔曲线平滑过渡进入椭圆轨迹</li>
     *   <li>后 8 tick：沿椭圆轨迹环绕目标</li>
     * </ul>
     * 总计 14 tick 的攻击周期。
     * </p>
     */
    private void planEllipseSlash(LivingEntity target) {
        Player owner = servant.getOwner();
        int duration = 14;      // 固定攻击周期
        int blendTicks = 6;     // 混合过渡时长
        Vec3 currentPos = servant.getPos();
        Vec3 T = lastTargetPos;

        // 生成随机椭圆参数
        float randAngle = owner.getRandom().nextFloat() * Mth.TWO_PI;
        float randRadius = 3.0f + owner.getRandom().nextFloat() * 2.0f;
        float randY = 0.5f + owner.getRandom().nextFloat() * 2.5f;
        Vec3 farPoint = T.add(Math.cos(randAngle) * randRadius, randY, Math.sin(randAngle) * randRadius);

        // 计算椭圆几何参数
        Vec3 diff = farPoint.subtract(T);
        Vec3 major = diff.scale(0.5);
        Vec3 center = T.add(major);
        Vec3 majorDir = major.normalize();

        Vec3 randomUp = new Vec3(owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5).normalize();
        Vec3 minorDir = majorDir.cross(randomUp).normalize();
        if (minorDir.lengthSqr() < 1e-5) minorDir = new Vec3(0, 1, 0);
        double minorRadius = major.length() * 0.75;
        Vec3 minor = minorDir.scale(minorRadius);

        // 获取当前运动状态
        Vec3 currentVel = servant.getCurrentVelocity(currentPos);
        Vec3 currentTip = Vec3.directionFromRotation(servant.getPitch(), servant.getYaw()).normalize();
        Vec3 currentNormal = servant.getCurrentNormal();

        // 计算贝塞尔过渡控制点
        float biasedTBlend = ((float) blendTicks / duration) - 0.08f * Mth.sin(((float) blendTicks / duration) * Mth.TWO_PI);
        float thetaBlend = biasedTBlend * Mth.TWO_PI;
        Vec3 P3 = center.add(major.scale(Math.cos(thetaBlend))).add(minor.scale(Math.sin(thetaBlend)));
        Vec3 E_prime = major.scale(-Math.sin(thetaBlend)).add(minor.scale(Math.cos(thetaBlend))).normalize();
        double R = currentPos.distanceTo(P3) * 0.4;
        Vec3 P0 = currentPos;
        Vec3 P1 = currentPos.add(currentVel.scale(R));
        Vec3 P2 = P3.subtract(E_prime.scale(R));

        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= duration; i++) {
            float progress = (float) i / duration;
            float biasedT = progress - 0.08f * Mth.sin(progress * Mth.TWO_PI);
            float theta = biasedT * Mth.TWO_PI;

            Vec3 targetP = servant.calculateEllipsePoint(center, major, minor, theta);
            Vec3 targetTrueTangent = servant.calculateEllipseTangent(major, minor, theta);
            Vec3 targetTip = targetP.subtract(center).normalize();
            Vec3 targetNormal = targetTip.cross(targetTrueTangent).normalize();
            if (targetNormal.lengthSqr() < 1e-5) targetNormal = majorDir.cross(targetTip).normalize();

            if (i <= blendTicks) {
                // 混合阶段：贝塞尔曲线平滑过渡
                float localT = (float) i / blendTicks;
                float smoothT = localT * localT * (3.0f - 2.0f * localT);
                Vec3 p = servant.calculateBezierPoint(P0, P1, P2, P3, localT);
                Vec3 tipDir = servant.slerpVector(currentTip, targetTip, smoothT);
                Vec3 planeNormal = servant.slerpVector(currentNormal, targetNormal, smoothT);
                nodes.add(servant.getEulerNode(p, tipDir, planeNormal));
            } else {
                // 纯椭圆阶段
                nodes.add(servant.getEulerNode(targetP, targetTip, targetNormal));
            }
        }
        servant.setPath(nodes);
    }

    // ===================== 沙漏斩击规划 =====================

    /**
     * 规划沙漏斩击攻击路径。
     * <p>
     * 路径分为三部分：
     * <ul>
     *   <li>准备阶段 (5 tick)：贝塞尔曲线移动到攻击准备位置</li>
     *   <li>攻击阶段 (5 tick)：直线加速冲向目标</li>
     *   <li>撤退阶段 (4 tick)：平滑过渡到下一个攻击方向</li>
     * </ul>
     * 总计 14 tick 的攻击周期。
     * </p>
     */
    private void planHourglassSlash(LivingEntity target) {
        Vec3 startPos = servant.getPos();
        Vec3 T = target.getBoundingBox().getCenter();

        // 计算攻击方向
        Vec3 toTarget = T.subtract(startPos);
        if (toTarget.lengthSqr() < 1e-5) toTarget = new Vec3(0, -1, 0);
        Vec3 attackDir = toTarget.normalize();
        if (attackDir.y > -0.2) {
            attackDir = new Vec3(attackDir.x, Math.min(-0.5, attackDir.y - 0.5), attackDir.z).normalize();
        }

        // 计算关键位置
        double dist = Math.max(7.0, startPos.distanceTo(T));
        Vec3 prepPos = T.subtract(attackDir.scale(dist));
        Vec3 hitPos = T.add(attackDir.scale(6));

        // 计算下一个攻击方向（旋转 80 度）
        Vector3f v = new Vector3f((float) attackDir.x, (float) attackDir.y, (float) attackDir.z);
        new Quaternionf().rotateY((float) (Math.PI * 0.8)).transform(v);
        Vec3 nextAttackDir = new Vec3(v.x(), v.y(), v.z()).normalize();
        Vec3 nextPrepPos = T.subtract(nextAttackDir.scale(dist));

        // 获取当前运动状态
        Vec3 currentVel = servant.getCurrentVelocity(startPos);
        Vec3 currentTip = Vec3.directionFromRotation(servant.getPitch(), servant.getYaw()).normalize();
        Vec3 currentNormal = servant.getCurrentNormal();

        Vec3 planeNormal = attackDir.cross(new Vec3(0, 1, 0)).normalize();
        if (planeNormal.lengthSqr() < 1e-4) planeNormal = new Vec3(1, 0, 0);

        // 贝塞尔准备阶段控制点
        double R = startPos.distanceTo(prepPos) * 0.4;
        Vec3 P0 = startPos;
        Vec3 P1 = startPos.add(currentVel.scale(R));
        Vec3 P2 = prepPos.subtract(attackDir.scale(R));

        List<PathNode> nodes = new ArrayList<>();

        // 准备阶段 (5 tick)
        for (int i = 1; i <= 5; i++) {
            float localT = (float) i / 5;
            float smoothT = localT * localT * (3.0f - 2.0f * localT);
            Vec3 p = servant.calculateBezierPoint(P0, P1, P2, prepPos, localT);
            Vec3 tipDir = servant.slerpVector(currentTip, attackDir, smoothT);
            Vec3 bNormal = servant.slerpVector(currentNormal, planeNormal, smoothT);
            nodes.add(servant.getEulerNode(p, tipDir, bNormal));
        }

        // 攻击阶段 (5 tick)
        for (int i = 1; i <= 4; i++) {
            float t = ((float) i / 5) * ((float) i / 5);
            Vec3 p = prepPos.lerp(hitPos, t);
            nodes.add(servant.getEulerNode(p, attackDir, planeNormal));
        }

        // 撤退阶段 (4 tick)
        Vec3 nextPlaneNormal = nextAttackDir.cross(new Vec3(0, 1, 0)).normalize();
        if (nextPlaneNormal.lengthSqr() < 1e-4) nextPlaneNormal = new Vec3(1, 0, 0);
        for (int i = 1; i <= 5; i++) {
            float t = (float) i / 4;
            float easeOut = t * (2.0f - t);
            Vec3 p = hitPos.lerp(nextPrepPos, easeOut);
            Vec3 tipDir = servant.slerpVector(attackDir, nextAttackDir, easeOut);
            Vec3 bNormal = servant.slerpVector(planeNormal, nextPlaneNormal, easeOut);
            nodes.add(servant.getEulerNode(p, tipDir, bNormal));
        }
        servant.setPath(nodes);
    }

    // ===================== 连锁攻击阶段 (CHAIN) =====================

    /**
     * 进入连锁攻击阶段：目标切换时的快速衔接攻击。
     */
    private void transitionToChain(LivingEntity target) {
        phase = Phase.CHAIN;
        servant.hitTargets.clear();
        planChainStrike(target);
    }

    /**
     * 规划连锁攻击路径。
     * <p>
     * 根据当前朝向与目标方向的夹角选择攻击模式：
     * <ul>
     *   <li>夹角小于阈值：直刺模式（快速直线攻击）</li>
     *   <li>夹角大于阈值：横扫模式（弧形攻击）</li>
     * </ul>
     * </p>
     */
    private void planChainStrike(LivingEntity target) {
        if (target == null) return;
        lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
        double thrustAngleThreshold = 0.9;
        int thrustAttackTicks = 7;
        int sweepAttackTicks = 14;
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
        Vec3 currentVel = servant.getCurrentVelocity(startPos);
        var history = servant.getHistoryNodes();
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
                Vec3 p = servant.calculateBezierPoint(P0, P1, P2, P3, t);
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
                Vec3 p = servant.calculateBezierPoint(P0, P1, P2, P3, t);
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
     * 连锁攻击阶段 Tick：路径完成后进入连续攻击阶段。
     */
    private void tickChain(LivingEntity target) {
        applyPositionCorrection(target);
        applyDamageControl(servant.getCurrentPath().getNodes().size());

        if (!servant.isExecutingPath() && target != null) {
            loopCount = 0;
            transitionToContinuous(target);
        }
    }

    // ===================== 通用辅助方法 =====================

    /**
     * 位置修正机制：只要存在合法攻击目标，就持续调整路径节点位置。
     * <p>
     * 根据目标当前位置与上次记录位置的偏移，按距离权重修正剩余路径节点，
     * 使仆从轨迹能够实时追踪移动中的目标。
     * </p>
     */
    private void applyPositionCorrection(LivingEntity target) {
        if (target == null || lastTargetPos == null || !servant.isExecutingPath()) return;

        Vec3 currentTargetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
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

    /**
     * 伤害控制机制：在动作计划执行到一半时清空已攻击列表。
     * <p>
     * 确保每次攻击周期对每个目标仅造成一次伤害，同时避免伤害丢失。
     * 当路径执行到一半时清空列表，后半段可以再次攻击已攻击过的目标，
     * 但同一半段内不会重复攻击。
     * </p>
     *
     * @param totalDuration 该攻击周期的总时长（tick 数）
     */
    private void applyDamageControl(int totalDuration) {
        PlannedPath path = servant.getCurrentPath();
        if (path == null) return;

        int currentIndex = path.getCurrentIndex();
        // 当执行到一半时清空已攻击列表
        if (currentIndex == totalDuration / 2) {
            servant.hitTargets.clear();
        }
    }
}