package first.servantry.common.servent;

import first.servantry.api.ai.ServantGoal;
import first.servantry.api.ai.ServantGoalSelector;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.ICollide;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.PlannedPath;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class Terraprism extends Servant implements ICollide {

    private boolean idle = true;
    private boolean attacking = false;
    private int trailTimer = 0;
    private float idleBlend = 0f;
    private float idleBlendO = 0f;

    public Terraprism(PathNode node) {
        super(node);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new AttackGoal());
        goalSelector.addGoal(1, new IdleGoal());
    }

    @Override
    public float getDamage() { return 9; }

    @Override
    public float getKnockback() { return 0.1f; }

    @Override
    public AABB getHitbox() {
        return new AABB(-0.1, -0.04, -0.75, 0.1, 0.04, 0.25);
    }

    /**
     * 加速函数：使用二次函数实现缓动效果
     * @param t 进度值 [0,1]
     * @return 加速后的进度值
     */
    public float accelerate(float t) { 
        return t * t; 
    }

    @Override
    public void collisionAttack(Set<LivingEntity> hitTargets) {
        if (isExecutingPath()) {
            for (LivingEntity target : hitTargets) {
                attack(target);
            }
        }
    }

    @Override
    public LivingEntity searchTarget() {
        Player owner = getOwner();
        return owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(16)).stream()
                .filter(this::isTarget)
                .sorted(Comparator.comparingDouble(target -> {
                    double score = target.distanceToSqr(getPos());
                    if (target.distanceToSqr(owner) < 36.0) {
                        score -= 10000.0;
                    }
                    if (target == getTarget()) {
                        score -= 1000.0;
                    }
                    score += (target.hashCode() * 31 + getOrder() * 17) % 5 * 40;
                    return score;
                }))
                .findFirst()
                .orElse(null);
    }

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
        return chosen == this;
    }

    @Override
    public void tick() {
        super.tick();
        Player owner = getOwner();
        if (owner.level().isClientSide()) {
            idleBlendO = idleBlend;
            if (attacking) {
                trailTimer = 12;
                idleBlend = Math.max(0.0f, idleBlend - 0.25f);
            } else {
                trailTimer--;
                idleBlend = Math.min(1.0f, idleBlend + 0.1f);
            }
        }
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(idle);
        buf.writeBoolean(attacking);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.idle = buf.readBoolean();
        this.attacking = buf.readBoolean();
    }

    public Vec3 applyTargetTracking(LivingEntity target, Vec3 lastTargetPos) {
        if (target == null || lastTargetPos == null) return lastTargetPos;
        Vec3 currentTargetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        Vec3 offset = currentTargetCenter.subtract(lastTargetPos);

        if (offset.lengthSqr() > 1e-5) {
            PlannedPath path = getCurrentPath();
            if (path != null) {
                List<PathNode> nodes = path.getNodes();
                int startIdx = path.getCurrentIndex();
                int qSize = nodes.size() - startIdx;
                for (int i = 0; i < qSize; i++) {
                    PathNode node = nodes.get(startIdx + i);
                    float weight = (float) (i + 1) / qSize;
                    Vec3 blendedOffset = offset.scale(weight);
                    nodes.set(startIdx + i, new PathNode(node.pos().add(blendedOffset), node.yaw(), node.pitch(), node.roll()));
                }
            }
        }
        return currentTargetCenter;
    }

    public PathNode getInterpolatedIdleState(Player owner, float partialTick) {
        float playerYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        float rad = (float) Math.toRadians(-playerYaw + 180);
        float backX = (float) Math.sin(rad);
        float backZ = (float) Math.cos(rad);
        float rightX = (float) Math.cos(rad);
        float rightZ = (float) -Math.sin(rad);
        int order = getOrder();
        double localZ = 0.5 + order * 0.12;
        double floatSpeed = 0.08 + order * 0.01;
        double floatAngle = (owner.tickCount + partialTick) * floatSpeed + order * 1.33;
        double px = owner.xo + (owner.getX() - owner.xo) * partialTick;
        double py = owner.yo + (owner.getY() - owner.yo) * partialTick;
        double pz = owner.zo + (owner.getZ() - owner.zo) * partialTick;
        Vec3 playerPos = new Vec3(px, py, pz);
        Vec3 targetPos = playerPos.add(localZ * backX + Math.cos(floatAngle) * 0.075 * rightX, owner.getBbHeight() * 0.6 + Math.sin(floatAngle) * 0.075, localZ * backZ + Math.cos(floatAngle) * 0.075 * rightZ);
        return new PathNode(targetPos, playerYaw - 90, 75 - order * 5f, 100);
    }

    public float getIdleBlendO() {
        return idleBlendO;
    }

    public float getIdleBlend() {
        return idleBlend;
    }

    class IdleGoal extends ServantGoal {

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }

        @Override
        public void start() {
            idle = true;
            attacking = false;
        }

        @Override
        public void stop() {
            idle = false;
        }

        @Override
        public void tick() {
            setPath(Collections.singletonList(getCurrentPathNode().lerp(getInterpolatedIdleState(getOwner(), 1f), 0.35f)));
        }

    }

    class AttackGoal extends ServantGoal {
        enum AttackPhase { PREP, FIRST_STRIKE, CONTINUOUS, CHAIN }

        private AttackPhase phase;
        private Vec3 lastTargetPos;
        private int loopCount;
        private Vec3 prepPos;
        private int prepTick;

        @Override
        public boolean canUse() {
            return getTarget() != null && canTransitionToAttack(getOwner());
        }

        @Override
        public boolean canContinueToUse() {
            if (isExecutingPath()) {
                return true;
            }
            LivingEntity target = getTarget();
            if (target == null) {
                return false;
            }
            Player owner = getOwner();
            return owner == null || getPos().distanceToSqr(owner.position()) <= 4096.0;
        }

        @Override public boolean isInterruptable() { return !isExecutingPath(); }

        @Override
        public void start() {
            attacking = true;
            idle = false;
            phase = AttackPhase.PREP;
            prepPos = getPos().add(0, 2, 0);
            prepTick = 0;
            loopCount = 0;
            lastTargetPos = null;
        }

        @Override
        public void stop() {
            attacking = false;
            phase = null;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();

            if (isTargetChange() && phase != AttackPhase.PREP && phase != AttackPhase.CHAIN) {
                if (target != null) {
                    loopCount = 0;
                    startChain(target);
                    return;
                }
            }

            switch (phase) {
                case PREP -> tickPrep(target);
                case FIRST_STRIKE -> tickFirstStrike(target);
                case CONTINUOUS -> tickContinuous(target);
                case CHAIN -> tickChain(target);
            }
        }

        // ================== PREP ==================

        private void tickPrep(LivingEntity target) {
            if (target == null) return;

            Vec3 nextPos = getPos().lerp(prepPos, 0.2f);
            float nextYaw = getYaw();
            float nextPitch = getPitch();
            float nextRoll = getRoll();

            Vec3 toTarget = target.getEyePosition().subtract(getPos());
            if (toTarget.lengthSqr() > 1e-4) {
                Vec3 bladeNormal = toTarget.cross(new Vec3(0, 1, 0)).normalize();
                PathNode prepNode = getEulerNode(getPos(), toTarget, bladeNormal);
                nextYaw = Mth.rotLerp(0.3f, getYaw(), prepNode.yaw());
                nextPitch = Mth.rotLerp(0.3f, getPitch(), prepNode.pitch());
                nextRoll = Mth.rotLerp(0.3f, getRoll(), prepNode.roll());
            }

            setPath(Collections.singletonList(new PathNode(nextPos, nextYaw, nextPitch, nextRoll)));

            if (getPos().distanceToSqr(prepPos) < 0.5) {
                prepTick++;
                if (prepTick > 4) {
                    startFirstStrike(target);
                }
            }
        }

        // ================== FIRST_STRIKE ==================

        private void startFirstStrike(LivingEntity target) {
            phase = AttackPhase.FIRST_STRIKE;

            int duration = 6;
            Vec3 start = getPos();
            Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);

            List<PathNode> nodes = new ArrayList<>();
            Vec3 moveDir = end.subtract(start);
            if (moveDir.lengthSqr() < 1e-4) moveDir = new Vec3(0, 0, 1);

            Vec3 planeNormal = moveDir.cross(new Vec3(0, 1, 0)).normalize();
            if (planeNormal.lengthSqr() < 1e-4) planeNormal = new Vec3(1, 0, 0);

            for (int i = 1; i <= duration; i++) {
                float t = accelerate((float) i / duration);
                Vec3 p = start.lerp(end, t);
                nodes.add(getEulerNode(p, moveDir, planeNormal));
            }
            setPath(nodes);
        }

        private void tickFirstStrike(LivingEntity target) {
            if (!isExecutingPath() && target != null) {
                startContinuous(target);
            }
        }

        // ================== CONTINUOUS ==================

        private void startContinuous(LivingEntity target) {
            phase = AttackPhase.CONTINUOUS;
            loopCount = 0;
            planEllipseOrHourglass(target);
        }

        private void planEllipseOrHourglass(LivingEntity target) {
            boolean isEllipse = getOwner().getRandom().nextBoolean();
            if (isEllipse) {
                planEllipseSlash(target);
            } else {
                planHourglassSlash(target);
            }
        }

        private void tickContinuous(LivingEntity target) {
            if (isExecutingPath()) {
                this.lastTargetPos = applyTargetTracking(target, this.lastTargetPos);
            } else if (target != null) {
                loopCount++;
                Player owner = getOwner();
                if (loopCount >= 2 && owner.getRandom().nextBoolean()) {
                    loopCount = 0;
                    boolean wasEllipse = lastWasEllipse;
                    if (wasEllipse) {
                        planHourglassSlash(target);
                    } else {
                        planEllipseSlash(target);
                    }
                } else {
                    planEllipseOrHourglass(target);
                }
            }
        }

        private boolean lastWasEllipse = true;

        // ================== ELLIPSE ==================

        private void planEllipseSlash(LivingEntity target) {
            if (target == null) return;
            lastWasEllipse = true;
            this.lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);

            Player owner = getOwner();
            int duration = 16;
            int blendTicks = 6;
            Vec3 currentPos = getPos();
            Vec3 T = lastTargetPos;

            // 生成随机的椭圆参数
            float randAngle = owner.getRandom().nextFloat() * (float)Math.PI * 2f;
            float randRadius = 3.0f + owner.getRandom().nextFloat() * 2.0f;
            float randY = 0.5f + owner.getRandom().nextFloat() * 2.5f;
            Vec3 farPoint = T.add(Math.cos(randAngle) * randRadius, randY, Math.sin(randAngle) * randRadius);

            // 计算椭圆几何参数
            Vec3 diff = farPoint.subtract(T);
            Vec3 major = diff.scale(0.5);  // 长轴向量
            Vec3 center = T.add(major);    // 椭圆中心
            Vec3 majorDir = major.normalize();

            // 计算短轴方向
            Vec3 randomUp = new Vec3(owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5).normalize();
            Vec3 minorDir = majorDir.cross(randomUp).normalize();
            if (minorDir.lengthSqr() < 1e-5) minorDir = new Vec3(0, 1, 0);

            double minorRadius = major.length() * 0.75;
            Vec3 minor = minorDir.scale(minorRadius);

            // 获取当前速度和方向
            Vec3 currentVel = getCurrentVelocity(currentPos);
            Vec3 currentTip = Vec3.directionFromRotation(getPitch(), getYaw()).normalize();
            Vec3 currentNormal = getCurrentNormal();

            // 计算贝塞尔曲线的控制点
            float tBlend = (float) blendTicks / duration;
            float biasedTBlend = tBlend - 0.08f * Mth.sin(tBlend * Mth.TWO_PI);
            float thetaBlend = biasedTBlend * 2.0f * (float)Math.PI;

            // 计算混合阶段的椭圆位置和切线方向
            Vec3 P3 = center.add(major.scale(Math.cos(thetaBlend))).add(minor.scale(Math.sin(thetaBlend)));
            Vec3 E_prime = major.scale(-Math.sin(thetaBlend)).add(minor.scale(Math.cos(thetaBlend))).normalize();

            // 设置贝塞尔曲线控制点
            double R = currentPos.distanceTo(P3) * 0.4;
            Vec3 P0 = currentPos;
            Vec3 P1 = currentPos.add(currentVel.scale(R));
            Vec3 P2 = P3.subtract(E_prime.scale(R));

            List<PathNode> nodes = new ArrayList<>();
            for (int i = 1; i <= duration; i++) {
                float progress = (float) i / duration;
                
                // 计算当前进度对应的椭圆参数
                float biasedT = progress - 0.08f * Mth.sin(progress * Mth.TWO_PI);
                float theta = biasedT * 2.0f * (float)Math.PI;
                
                // 计算目标位置和方向
                Vec3 targetP = calculateEllipsePoint(center, major, minor, theta);
                Vec3 targetTrueTangent = calculateEllipseTangent(major, minor, theta);
                Vec3 targetTip = targetP.subtract(center).normalize();
                Vec3 targetNormal = targetTip.cross(targetTrueTangent).normalize();
                if (targetNormal.lengthSqr() < 1e-5) targetNormal = majorDir.cross(targetTip).normalize();

                // 根据阶段选择插值方式
                if (i <= blendTicks) {
                    // 混合阶段：使用贝塞尔曲线平滑过渡
                    float localT = (float) i / blendTicks;
                    float smoothT = localT * localT * (3.0f - 2.0f * localT); // 平滑插值
                    
                    Vec3 p = calculateBezierPoint(P0, P1, P2, P3, localT);
                    Vec3 tipDir = slerpVector(currentTip, targetTip, smoothT);
                    Vec3 planeNormal = slerpVector(currentNormal, targetNormal, smoothT);
                    nodes.add(getEulerNode(p, tipDir, planeNormal));
                } else {
                    // 纯椭圆阶段
                    nodes.add(getEulerNode(targetP, targetTip, targetNormal));
                }
            }
            setPath(nodes);
        }

        // ================== HOURGLASS ==================

        private void planHourglassSlash(LivingEntity target) {
            if (target == null) return;
            lastWasEllipse = false;
            this.lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);

            // 攻击阶段时间配置
            int prepTicks = 4;    // 准备阶段
            int attackTicks = 4;  // 攻击阶段
            int retreatTicks = 8; // 撤退阶段

            Vec3 startPos = getPos();
            Vec3 T = target.getBoundingBox().getCenter();
            
            // 计算攻击方向
            Vec3 toTarget = T.subtract(startPos);
            if (toTarget.lengthSqr() < 1e-5) toTarget = new Vec3(0, -1, 0);
            Vec3 attackDir = toTarget.normalize();

            // 确保攻击方向有足够的向下角度
            if (attackDir.y > -0.2) {
                attackDir = new Vec3(attackDir.x, Math.min(-0.5, attackDir.y - 0.5), attackDir.z).normalize();
            }

            // 计算攻击路径的关键位置
            double dist = Math.max(7.0, startPos.distanceTo(T));
            Vec3 prepPos = T.subtract(attackDir.scale(dist));  // 准备位置
            Vec3 hitPos = T.add(attackDir);                    // 命中位置

            // 计算下一个攻击方向（旋转80度）
            Vector3f v = new Vector3f((float) attackDir.x, (float) attackDir.y, (float) attackDir.z);
            new Quaternionf().rotateY((float) (Math.PI * 0.8)).transform(v);
            Vec3 nextAttackDir = new Vec3(v.x(), v.y(), v.z()).normalize();
            Vec3 nextPrepPos = T.subtract(nextAttackDir.scale(dist));

            // 获取当前状态
            Vec3 currentVel = getCurrentVelocity(startPos);
            Vec3 currentTip = Vec3.directionFromRotation(getPitch(), getYaw()).normalize();
            Vec3 currentNormal = getCurrentNormal();

            // 计算攻击平面的法线
            Vec3 planeNormal = attackDir.cross(new Vec3(0, 1, 0)).normalize();
            if (planeNormal.lengthSqr() < 1e-4) planeNormal = new Vec3(1, 0, 0);

            // 设置准备阶段的贝塞尔曲线控制点
            Vec3 P3 = prepPos;
            Vec3 E_prime = attackDir;
            double R = startPos.distanceTo(P3) * 0.4;
            Vec3 P0 = startPos;
            Vec3 P1 = startPos.add(currentVel.scale(R));
            Vec3 P2 = P3.subtract(E_prime.scale(R));

            List<PathNode> nodes = new ArrayList<>();
            
            // 准备阶段：使用贝塞尔曲线平滑移动到准备位置
            for (int i = 1; i <= prepTicks; i++) {
                float localT = (float) i / prepTicks;
                float smoothT = localT * localT * (3.0f - 2.0f * localT); // 平滑插值

                Vec3 p = calculateBezierPoint(P0, P1, P2, P3, localT);
                Vec3 tipDir = slerpVector(currentTip, attackDir, smoothT);
                Vec3 bNormal = slerpVector(currentNormal, planeNormal, smoothT);
                nodes.add(getEulerNode(p, tipDir, bNormal));
            }

            // 攻击阶段：直线加速冲向目标
            for (int i = 1; i <= attackTicks; i++) {
                float t = accelerate((float) i / attackTicks);
                Vec3 p = prepPos.lerp(hitPos, t);
                nodes.add(getEulerNode(p, attackDir, planeNormal));
            }

            // 撤退阶段：平滑过渡到下一个攻击方向
            Vec3 nextPlaneNormal = nextAttackDir.cross(new Vec3(0, 1, 0)).normalize();
            if (nextPlaneNormal.lengthSqr() < 1e-4) nextPlaneNormal = new Vec3(1, 0, 0);

            for (int i = 1; i <= retreatTicks; i++) {
                float t = (float) i / retreatTicks;
                float easeOut = t * (2.0f - t); // 缓出效果
                Vec3 p = hitPos.lerp(nextPrepPos, easeOut);
                Vec3 tipDir = slerpVector(attackDir, nextAttackDir, easeOut);
                Vec3 bNormal = slerpVector(planeNormal, nextPlaneNormal, easeOut);
                nodes.add(getEulerNode(p, tipDir, bNormal));
            }
            setPath(nodes);
        }

        // ================== CHAIN ==================

        private void startChain(LivingEntity target) {
            phase = AttackPhase.CHAIN;
            planChainStrike(target);
        }

        private void planChainStrike(LivingEntity target) {
            if (target == null) return;
            this.lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
            Player owner = getOwner();

            // 攻击模式参数
            double thrustAngleThreshold = 0.9; // 直刺模式角度阈值
            int thrustAttackTicks = 2;         // 直刺攻击时长
            int sweepAttackTicks = 10 + owner.getRandom().nextInt(1, 4); // 横扫攻击时长
            double curvePullOutward = 0.8;     // 曲线外拉系数

            Vec3 startPos = getPos();
            Vec3 T = lastTargetPos;

            // 计算攻击方向
            Vec3 fwd = T.subtract(startPos);
            double dist = fwd.length();
            if (dist < 1e-5) fwd = new Vec3(0, 0, 1);
            fwd = fwd.normalize();

            // 如果距离太近，调整目标位置
            if (dist < 6.0) {
                T = T.add(fwd.scale(4.5));
                dist = T.subtract(startPos).length();
            }

            // 获取当前状态
            double speed;
            Vec3 currentVel = getCurrentVelocity(startPos);
            LinkedList<PathNode> history = getHistoryNodes();
            if (history.size() > 1) {
                Vec3 rawVel = startPos.subtract(history.get(1).pos());
                speed = rawVel.length();
            } else {
                speed = 1.0;
            }

            Vec3 currentTip = Vec3.directionFromRotation(getPitch(), getYaw()).normalize();
            Vec3 currentNormal = getCurrentNormal();

            // 根据角度选择攻击模式
            boolean isThrustMode = currentTip.dot(fwd) > thrustAngleThreshold;
            List<PathNode> nodes = new ArrayList<>();

            if (isThrustMode) {
                // 直刺模式：快速直线攻击
                Vec3 thrustDir = T.subtract(startPos).normalize();
                Vec3 thrustNormal = thrustDir.cross(new Vec3(0, 1, 0)).normalize();
                if (thrustNormal.lengthSqr() < 1e-4) thrustNormal = new Vec3(1, 0, 0);

                // 设置直刺贝塞尔曲线控制点
                Vec3 P0 = startPos;
                Vec3 P3 = T.add(thrustDir.scale(1.5)); // 稍微超过目标
                double Rp = Math.max(dist * 0.3, speed * 2.0);
                Vec3 P1 = startPos.add(currentVel.scale(Rp));
                Vec3 P2 = P3.subtract(thrustDir.scale(Rp));

                for (int i = 1; i <= thrustAttackTicks; i++) {
                    float t = (float) i / thrustAttackTicks;
                    Vec3 p = calculateBezierPoint(P0, P1, P2, P3, t);
                    Vec3 tipDir = slerpVector(currentTip, thrustDir, t);
                    Vec3 bNormal = slerpVector(currentNormal, thrustNormal, t);
                    nodes.add(getEulerNode(p, tipDir, bNormal));
                }
            } else {
                // 横扫模式：弧形攻击
                Vec3 sweepUp = currentNormal;
                Vec3 chord = T.subtract(startPos);
                Vec3 chordDir = chord.normalize();
                
                // 计算向外弯曲的方向
                Vec3 outward = chordDir.cross(sweepUp).normalize();
                if (outward.lengthSqr() < 1e-4) outward = chordDir.cross(new Vec3(0, 1, 0)).normalize();

                // 设置横扫贝塞尔曲线控制点
                Vec3 P0 = startPos;
                Vec3 P3 = T.add(chordDir.scale(0.5)); // 稍微超过目标
                double Rp = Math.max(dist * 0.4, speed * 3.0);
                Vec3 P1 = startPos.add(currentVel.scale(Rp));
                Vec3 P2 = P3.subtract(chordDir.scale(Rp)).add(outward.scale(-dist * curvePullOutward));

                for (int i = 1; i <= sweepAttackTicks; i++) {
                    float t = (float) i / sweepAttackTicks;
                    
                    // 计算弧形路径
                    Vec3 p = calculateBezierPoint(P0, P1, P2, P3, t);
                    
                    // 计算弧形切线方向（指向旋转中心）
                    Vec3 pivot = startPos.add(chord.scale(0.5)).add(outward.scale(-dist * curvePullOutward * 0.5));
                    Vec3 targetTip = p.subtract(pivot).normalize();
                    
                    Vec3 tipDir = slerpVector(currentTip, targetTip, t);
                    Vec3 bNormal = slerpVector(currentNormal, sweepUp, t);
                    nodes.add(getEulerNode(p, tipDir, bNormal));
                }
            }
            setPath(nodes);
        }

        private void tickChain(LivingEntity target) {
            if (isExecutingPath()) {
                this.lastTargetPos = applyTargetTracking(target, this.lastTargetPos);
            } else if (target != null) {
                loopCount = 0;
                startContinuous(target);
            }
        }
    }

    /**
     * 获取当前速度向量
     */
    private Vec3 getCurrentVelocity(Vec3 currentPos) {
        LinkedList<PathNode> history = getHistoryNodes();
        if (history.size() > 1) {
            Vec3 rawVel = currentPos.subtract(history.get(1).pos());
            if (rawVel.lengthSqr() > 1e-5) return rawVel.normalize();
        }
        return Vec3.directionFromRotation(getPitch(), getYaw()).normalize();
    }

    /**
     * 获取当前法线向量（基于旋转）
     */
    private Vec3 getCurrentNormal() {
        Quaternionf q = new Quaternionf().rotateY((float) Math.toRadians(-getYaw()))
                .rotateX((float) Math.toRadians(getPitch()))
                .rotateZ((float) Math.toRadians(getRoll()));
        Vector3f upV = new Vector3f(0, 1, 0).rotate(q);
        return new Vec3(upV.x(), upV.y(), upV.z()).normalize();
    }

    /**
     * 计算贝塞尔曲线上的点
     */
    private Vec3 calculateBezierPoint(Vec3 P0, Vec3 P1, Vec3 P2, Vec3 P3, float t) {
        float mt = 1.0f - t;
        return P0.scale(mt * mt * mt)
                .add(P1.scale(3 * mt * mt * t))
                .add(P2.scale(3 * mt * t * t))
                .add(P3.scale(t * t * t));
    }

    /**
     * 计算椭圆上的点
     */
    private Vec3 calculateEllipsePoint(Vec3 center, Vec3 major, Vec3 minor, float theta) {
        return center.add(major.scale(Math.cos(theta))).add(minor.scale(Math.sin(theta)));
    }

    /**
     * 计算椭圆切线方向
     */
    private Vec3 calculateEllipseTangent(Vec3 major, Vec3 minor, float theta) {
        return major.scale(-Math.sin(theta)).add(minor.scale(Math.cos(theta))).normalize();
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    @Override
    public ServantType<? extends Servant> getType() {
        return ServantRegister.TerraPrism.get();
    }

}