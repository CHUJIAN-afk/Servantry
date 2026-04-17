package first.servantry.common.servent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.Servantry;
import first.servantry.api.PathNode;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.IDamagingOnCollide;
import first.servantry.api.servant.ITrailRenderer;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ItemRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class Terraprism extends Servant implements IDamagingOnCollide, ITrailRenderer {

    public enum PrismState {
        IDLE,               // 0: 待机
        PREP,               // 1: 准备
        FIRST_STRIKE,       // 2: 初次斩击
        ELLIPSE_SLASH,      // 3: 椭圆循环
        HOURGLASS,          // 4: 沙漏刺击
        CHAIN_STRIKE,       // 5: 连锁斩击
        RETURN;             // 6: 归来

        public boolean isAttackState() {
            return this == FIRST_STRIKE || this == ELLIPSE_SLASH || this == HOURGLASS || this == CHAIN_STRIKE;
        }
    }

    private static final List<PrismState> CONTINUOUS_ATTACKS = Arrays.asList(
            PrismState.ELLIPSE_SLASH,
            PrismState.HOURGLASS
    );

    private static final String HIT_CLEAR = "hit_clear";

    private PrismState state = PrismState.IDLE;
    private int stateTick = 0;
    private int loopCount = 0;

    private int targetId = -1;
    private Vec3 lastTargetPos = null;
    private Vec3 prepPos = null;
    private Vec3 lastPlayerPos = null;

    private boolean returnLocked = false;
    private int trailTimer = 0;

    private float idleBlend = 0f;
    private float idleBlendO = 0f;

    private final Set<Integer> swingHitTargets = new HashSet<>();
    private static final ResourceLocation TRAIL_TEXTURE = Servantry.rl("textures/trail.png");

    public Terraprism(PathNode node) {
        super(node);
    }

    @Override
    public float getBaseDamage() {
        return 9;
    }

    @Override
    public float getBaseKnockback() {
        return 0.1f;
    }

    @Override
    public AABB getHitbox() {
        return new AABB(-0.1, -0.04, -0.75, 0.1, 0.04, 0.25);
    }

    private float accelerate(float t) {
        return t * t;
    }

    private PathNode getEulerNode(Vec3 pos, Vec3 tipDir, Vec3 bladeNormal) {
        if (tipDir.lengthSqr() < 1e-4) tipDir = new Vec3(0, 0, 1);
        tipDir = tipDir.normalize();

        float yaw = (float) (Math.atan2(-tipDir.x, tipDir.z) * (180D / Math.PI));
        double horiz = Math.sqrt(tipDir.x * tipDir.x + tipDir.z * tipDir.z);
        float pitch = (float) (Math.atan2(-tipDir.y, horiz) * (180D / Math.PI));

        Vec3 defaultUp = new Vec3(0, 1, 0).xRot((float) Math.toRadians(pitch)).yRot((float) Math.toRadians(yaw));
        Vec3 projNormal = bladeNormal.subtract(tipDir.scale(bladeNormal.dot(tipDir))).normalize();
        if (projNormal.lengthSqr() < 1e-4) projNormal = defaultUp;

        double dot = defaultUp.dot(projNormal);
        Vec3 cross = defaultUp.cross(projNormal);
        float roll = (float) (Math.atan2(cross.dot(tipDir), dot) * (180D / Math.PI));

        return new PathNode(pos, yaw, pitch, roll);
    }

    public PathNode getInterpolatedIdleState(Player owner, int order, int total, float partialTick) {
        float playerYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        float rad = (float) Math.toRadians(-playerYaw + 180);
        float backX = (float) Math.sin(rad);
        float backZ = (float) Math.cos(rad);
        float rightX = (float) Math.cos(rad);
        float rightZ = (float) -Math.sin(rad);

        double localZ = 0.5 + order * 0.12;
        double floatSpeed = 0.08 + order * 0.01;
        double floatAngle = (owner.tickCount + partialTick) * floatSpeed + order * 1.33;

        double px = owner.xo + (owner.getX() - owner.xo) * partialTick;
        double py = owner.yo + (owner.getY() - owner.yo) * partialTick;
        double pz = owner.zo + (owner.getZ() - owner.zo) * partialTick;
        Vec3 playerPos = new Vec3(px, py, pz);

        Vec3 targetPos = playerPos.add(
                localZ * backX + Math.cos(floatAngle) * 0.075 * rightX,
                owner.getBbHeight() * 0.6 + Math.sin(floatAngle) * 0.075,
                localZ * backZ + Math.cos(floatAngle) * 0.075 * rightZ
        );

        return new PathNode(targetPos, playerYaw - 90, 75 - order * 5f, 100);
    }

    public PathNode getIdleState(Player owner, int order, int total) {
        return getInterpolatedIdleState(owner, order, total, 1f);
    }

    @Override
    public void collisionAttack(Set<LivingEntity> hitTargets) {
        if (isExecutingPath()) {
            for (LivingEntity target : hitTargets) {
                if (!swingHitTargets.contains(target.getId())) {
                    int invulnerableTime = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    target.hurt(getDamageSource(), getBaseDamage());
                    target.invulnerableTime = invulnerableTime;
                    swingHitTargets.add(target.getId());
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!getHistoryNodes().isEmpty() && HIT_CLEAR.equals(getHistoryNodes().getFirst().feature())) {
            swingHitTargets.clear();
        }

        Player owner = getOwner();
        if (owner != null) {
            if (owner.level().isClientSide()) {
                clientTick();
            } else {
                serverTick();
            }
        }
    }

    private void clientTick() {
        if (state.isAttackState()) {
            trailTimer = 12;
        } else if (trailTimer > 0) {
            trailTimer--;
        }
        idleBlendO = idleBlend;
        if (state == PrismState.IDLE) {
            idleBlend = Math.min(1.0f, idleBlend + 0.1f);
        } else {
            idleBlend = Math.max(0.0f, idleBlend - 0.25f);
        }
    }

    private void serverTick() {
        Player owner = getOwner();

        if (this.isExecutingPath() && this.targetId != -1 && owner.level() instanceof ServerLevel serverLevel) {
            Entity targetEntity = serverLevel.getEntity(this.targetId);
            if (targetEntity instanceof LivingEntity livingTarget) {
                Vec3 currentTargetCenter = livingTarget.position().add(0, livingTarget.getBbHeight() / 2.0, 0);
                if (this.lastTargetPos != null) {
                    Vec3 offset = currentTargetCenter.subtract(this.lastTargetPos);
                    if (offset.lengthSqr() > 1e-5) {
                        LinkedList<PathNode> queue = this.getPathQueue();
                        int qSize = queue.size();
                        for (int i = 0; i < qSize; i++) {
                            PathNode node = queue.get(i);
                            float weight = (float) (i + 1) / qSize;
                            Vec3 blendedOffset = offset.scale(weight);

                            queue.set(i, new PathNode(
                                    node.feature(),
                                    node.pos().add(blendedOffset),
                                    node.yaw(),
                                    node.pitch(),
                                    node.roll()
                            ));
                        }
                    }
                }
                this.lastTargetPos = currentTargetCenter;
            }
        }

        if (this.getPos().distanceToSqr(owner.position()) > 4096.0) {
            if (!returnLocked) {
                state = PrismState.RETURN;
                stateTick = 0;
                targetId = -1;
                returnLocked = true;
                this.setPath(new ArrayList<>());
            }
        }

        LivingEntity target = null;
        if (!returnLocked) {
            target = verifyTarget(owner);
            if (target == null && state != PrismState.IDLE && state != PrismState.RETURN) {
                state = PrismState.RETURN;
                stateTick = 0;
                this.setPath(new ArrayList<>());
            }
        }

        if (this.isExecutingPath()) {
            this.lastPlayerPos = owner.position();
            return;
        }

        if (target != null && state.isAttackState()) {
            switch (state) {
                case FIRST_STRIKE -> generateFirstStrike(owner, target);
                case ELLIPSE_SLASH -> generateEllipseSlash(owner, target);
                case HOURGLASS -> generateHourglass(owner, target);
                case CHAIN_STRIKE -> generateChainStrike(owner, target);
                default -> {}
            }
        } else {
            switch (state) {
                case IDLE -> handleIdle(owner, target);
                case PREP -> handlePrep(target);
                case RETURN -> handleReturn(owner, target);
                default -> {}
            }
        }
    }

    private LivingEntity verifyTarget(Player owner) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        // 【核心优化接入】：使用缓存方法代替原版的每 Tick O(N) 扫描，完美解决性能卡顿
        List<LivingEntity> potentialTargets = data.getNearbyTargets(owner, this, 32.0, state == PrismState.IDLE);

        if (potentialTargets.isEmpty()) {
            targetId = -1;
            return null;
        }

        int order = data.getOrder(this);

        potentialTargets.sort(Comparator.comparingDouble(e -> {
            double distSqr = e.distanceToSqr(getPos());
            double score = distSqr;
            double DANGER_DIST_SQR = 6.0 * 6.0;

            if (e.distanceToSqr(owner) < DANGER_DIST_SQR) {
                score -= 10000.0;
            }
            if (e.getId() == targetId) {
                score -= 1000.0;
            }
            int hashBias = (e.getId() * 31 + order * 17) % 5;
            score += hashBias * 40.0;
            return score;
        }));

        LivingEntity newTarget = potentialTargets.getFirst();

        if (targetId != -1 && targetId != newTarget.getId() && lastTargetPos != null) {
            state = PrismState.CHAIN_STRIKE;
            loopCount = 0;
            this.setPath(new ArrayList<>());
        }

        targetId = newTarget.getId();
        lastTargetPos = newTarget.position().add(0, newTarget.getBbHeight() / 2, 0);
        return newTarget;
    }

    private boolean canTransitionToPrep(Player owner) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        int maxOrder = -1;
        Servant chosen = null;
        for (Servant s : data.getServants()) {
            if (s instanceof Terraprism ts && ts.state == PrismState.IDLE) {
                int order = data.getOrder(s);
                if (order > maxOrder) {
                    maxOrder = order;
                    chosen = s;
                }
            }
        }
        return chosen == this;
    }

    private void pickRandomAttack(Player owner) {
        this.loopCount = 0;
        this.state = CONTINUOUS_ATTACKS.get(owner.getRandom().nextInt(CONTINUOUS_ATTACKS.size()));
    }

    private void advanceAttack(Player owner) {
        this.loopCount++;
        if (this.loopCount >= 2 && owner.getRandom().nextBoolean()) {
            List<PrismState> pool = new ArrayList<>(CONTINUOUS_ATTACKS);
            pool.remove(this.state);
            this.state = pool.get(owner.getRandom().nextInt(pool.size()));
            this.loopCount = 0;
        }
    }

    private void handleIdle(Player owner, LivingEntity target) {
        if (target != null && canTransitionToPrep(owner)) {
            state = PrismState.PREP; stateTick = 0; prepPos = this.getPos().add(0, 3.5, 0); return;
        }
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        PathNode idleState = getIdleState(owner, data.getOrder(this), data.getServants().size());

        Vec3 nextPos = this.getPos().lerp(idleState.pos(), 0.25f);
        float nextYaw = Mth.rotLerp(0.25f, this.getYaw(), idleState.yaw());
        float nextPitch = Mth.rotLerp(0.25f, this.getPitch(), idleState.pitch());
        float nextRoll = Mth.rotLerp(0.25f, this.getRoll(), idleState.roll());

        this.setPath(Collections.singletonList(new PathNode(nextPos, nextYaw, nextPitch, nextRoll)));
    }

    private void handleReturn(Player owner, LivingEntity target) {
        if (!returnLocked && target != null && canTransitionToPrep(owner)) {
            state = PrismState.PREP;
            stateTick = 0;
            prepPos = this.getPos().add(0, 1.5, 0);
            return;
        }
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        PathNode idleState = getIdleState(owner, data.getOrder(this), data.getServants().size());

        Vec3 playerVel = Vec3.ZERO;
        if (this.lastPlayerPos != null) {
            playerVel = owner.position().subtract(this.lastPlayerPos);
        }

        Vec3 nextPos = this.getPos().add(playerVel).lerp(idleState.pos(), 0.45f);

        float nextYaw = Mth.rotLerp(0.3f, this.getYaw(), idleState.yaw());
        float nextPitch = Mth.rotLerp(0.3f, this.getPitch(), idleState.pitch());
        float nextRoll = Mth.rotLerp(0.3f, this.getRoll(), idleState.roll());

        this.setPath(Collections.singletonList(new PathNode(nextPos, nextYaw, nextPitch, nextRoll)));

        double playerSpeedSqr = playerVel.lengthSqr();
        double threshold = 1.5 + playerSpeedSqr * 10.0;

        if (this.getPos().distanceToSqr(idleState.pos()) < threshold) {
            state = PrismState.IDLE;
            stateTick = 0;
            returnLocked = false;
        }

    }

    private void handlePrep(LivingEntity target) {
        // 【核心漏洞修复】：当处于 PREP 但怪物意外死亡或超距导致 target 为 null 时
        // 直接打断进入 RETURN 状态，彻底消灭 `target.getEyePosition()` 引发的 NPE！
        if (target == null) {
            state = PrismState.RETURN;
            stateTick = 0;
            this.setPath(new ArrayList<>());
            return;
        }

        if (prepPos == null) prepPos = this.getPos().add(0, 2, 0);

        Vec3 nextPos = this.getPos().lerp(prepPos, 0.2f);
        float nextYaw = this.getYaw();
        float nextPitch = this.getPitch();
        float nextRoll = this.getRoll();

        Vec3 toTarget = target.getEyePosition().subtract(this.getPos());
        if (toTarget.lengthSqr() > 1e-4) {
            Vec3 bladeNormal = toTarget.cross(new Vec3(0, 1, 0)).normalize();
            PathNode prepNode = getEulerNode(this.getPos(), toTarget, bladeNormal);

            nextYaw = Mth.rotLerp(0.3f, this.getYaw(), prepNode.yaw());
            nextPitch = Mth.rotLerp(0.3f, this.getPitch(), prepNode.pitch());
            nextRoll = Mth.rotLerp(0.3f, this.getRoll(), prepNode.roll());
        }

        this.setPath(Collections.singletonList(new PathNode(nextPos, nextYaw, nextPitch, nextRoll)));

        if (this.getPos().distanceToSqr(prepPos) < 0.5) {
            stateTick++;
            if (stateTick > 4) {
                state = PrismState.FIRST_STRIKE;
                stateTick = 0;
            }
        }
    }

    private void generateFirstStrike(Player owner, LivingEntity target) {
        int duration = 6;
        Vec3 start = this.getPos();
        Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);

        List<PathNode> nodes = new ArrayList<>();
        Vec3 moveDir = end.subtract(start);
        if (moveDir.lengthSqr() < 1e-4) moveDir = new Vec3(0, 0, 1);

        Vec3 planeNormal = moveDir.cross(new Vec3(0, 1, 0)).normalize();
        if (planeNormal.lengthSqr() < 1e-4) planeNormal = new Vec3(1, 0, 0);

        for (int i = 1; i <= duration; i++) {
            float t = accelerate((float) i / duration);
            Vec3 p = start.lerp(end, t);
            PathNode node = getEulerNode(p, moveDir, planeNormal);
            if (i == 1) {
                node = node.withFeature(HIT_CLEAR);
            }
            nodes.add(node);
        }

        this.setPath(nodes);
        pickRandomAttack(owner);
    }

    private void generateEllipseSlash(Player owner, LivingEntity target) {
        int duration = 16;
        int blendTicks = 6;

        Vec3 currentPos = this.getPos();
        Vec3 T = target.position().add(0, target.getBbHeight() / 2.0, 0);

        float randAngle = owner.getRandom().nextFloat() * (float)Math.PI * 2f;
        float randRadius = 3.0f + owner.getRandom().nextFloat() * 2.0f;
        float randY = 0.5f + owner.getRandom().nextFloat() * 2.5f;
        Vec3 farPoint = T.add(Math.cos(randAngle) * randRadius, randY, Math.sin(randAngle) * randRadius);

        Vec3 diff = farPoint.subtract(T);
        Vec3 major = diff.scale(0.5);
        Vec3 center = T.add(major);
        Vec3 majorDir = major.normalize();

        Vec3 randomUp = new Vec3(owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5).normalize();
        Vec3 minorDir = majorDir.cross(randomUp).normalize();
        if (minorDir.lengthSqr() < 1e-5) minorDir = new Vec3(0, 1, 0);

        double minorRadius = major.length() * 0.75;
        Vec3 minor = minorDir.scale(minorRadius);

        Vec3 currentVel;
        LinkedList<PathNode> history = this.getHistoryNodes();
        if (history.size() > 1) {
            currentVel = currentPos.subtract(history.get(1).pos());
            if (currentVel.lengthSqr() > 1e-5) currentVel = currentVel.normalize();
            else currentVel = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        } else {
            currentVel = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        }

        Vec3 currentTip = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        Quaternionf q = new Quaternionf().rotateY((float) Math.toRadians(-this.getYaw()))
                .rotateX((float) Math.toRadians(this.getPitch()))
                .rotateZ((float) Math.toRadians(this.getRoll()));
        Vector3f upV = new Vector3f(0, 1, 0).rotate(q);
        Vec3 currentNormal = new Vec3(upV.x(), upV.y(), upV.z()).normalize();

        float tBlend = (float) blendTicks / duration;
        float biasedTBlend = tBlend - 0.08f * Mth.sin(tBlend * Mth.TWO_PI);
        float thetaBlend = biasedTBlend * 2.0f * (float)Math.PI;

        Vec3 P3 = center.add(major.scale(Math.cos(thetaBlend))).add(minor.scale(Math.sin(thetaBlend)));
        Vec3 E_prime = major.scale(-Math.sin(thetaBlend)).add(minor.scale(Math.cos(thetaBlend))).normalize();

        double R = currentPos.distanceTo(P3) * 0.4;
        Vec3 P0 = currentPos;
        Vec3 P1 = currentPos.add(currentVel.scale(R));
        Vec3 P2 = P3.subtract(E_prime.scale(R));

        List<PathNode> nodes = new ArrayList<>();

        for (int i = 1; i <= duration; i++) {
            float progress = (float) i / duration;
            Vec3 p, tipDir, planeNormal;

            float biasedT = progress - 0.08f * Mth.sin(progress * Mth.TWO_PI);
            float theta = biasedT * 2.0f * (float)Math.PI;
            Vec3 targetP = center.add(major.scale(Math.cos(theta))).add(minor.scale(Math.sin(theta)));
            Vec3 targetTrueTangent = major.scale(-Math.sin(theta)).add(minor.scale(Math.cos(theta))).normalize();

            Vec3 targetTip = targetP.subtract(center).normalize();
            Vec3 targetNormal = targetTip.cross(targetTrueTangent).normalize();
            if (targetNormal.lengthSqr() < 1e-5) targetNormal = majorDir.cross(targetTip).normalize();

            if (i <= blendTicks) {
                float localT = (float) i / blendTicks;
                float smoothT = localT * localT * (3.0f - 2.0f * localT);
                float mt = 1.0f - localT;

                p = P0.scale(mt*mt*mt)
                        .add(P1.scale(3*mt*mt*localT))
                        .add(P2.scale(3*mt*localT*localT))
                        .add(P3.scale(localT*localT*localT));

                tipDir = slerpVector(currentTip, targetTip, smoothT);
                planeNormal = slerpVector(currentNormal, targetNormal, smoothT);
            } else {
                p = targetP;
                tipDir = targetTip;
                planeNormal = targetNormal;
            }
            PathNode node = getEulerNode(p, tipDir, planeNormal);
            if (i == 1) {
                node = node.withFeature(HIT_CLEAR);
            }
            nodes.add(node);
        }

        this.setPath(nodes);
        advanceAttack(owner);
    }

    private void generateHourglass(Player owner, LivingEntity target) {
        int prepTicks = 4;
        int attackTicks = 4;
        int retreatTicks = 8;

        Vec3 startPos = this.getPos();
        Vec3 T = target.getBoundingBox().getCenter();

        Vec3 toTarget = T.subtract(startPos);
        if (toTarget.lengthSqr() < 1e-5) toTarget = new Vec3(0, -1, 0);
        Vec3 attackDir = toTarget.normalize();

        if (attackDir.y > -0.2) {
            attackDir = new Vec3(attackDir.x, Math.min(-0.5, attackDir.y - 0.5), attackDir.z).normalize();
        }

        double dist = Math.max(7.0, startPos.distanceTo(T));
        Vec3 prepPos = T.subtract(attackDir.scale(dist));
        Vec3 hitPos = T.add(attackDir);

        Vector3f v = new Vector3f((float) attackDir.x, (float) attackDir.y, (float) attackDir.z);
        new Quaternionf().rotateY((float) (Math.PI * 0.8)).transform(v);
        Vec3 nextAttackDir = new Vec3(v.x(), v.y(), v.z()).normalize();
        Vec3 nextPrepPos = T.subtract(nextAttackDir.scale(dist));

        Vec3 currentVel = new Vec3(0, 1, 0);
        LinkedList<PathNode> history = this.getHistoryNodes();
        if (history.size() > 1) {
            currentVel = startPos.subtract(history.get(1).pos());
            if (currentVel.lengthSqr() > 1e-5) currentVel = currentVel.normalize();
            else currentVel = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        } else {
            currentVel = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        }

        Vec3 currentTip = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        Quaternionf q = new Quaternionf().rotateY((float) Math.toRadians(-this.getYaw()))
                .rotateX((float) Math.toRadians(this.getPitch()))
                .rotateZ((float) Math.toRadians(this.getRoll()));
        Vector3f upV = new Vector3f(0, 1, 0).rotate(q);
        Vec3 currentNormal = new Vec3(upV.x(), upV.y(), upV.z()).normalize();

        Vec3 planeNormal = attackDir.cross(new Vec3(0, 1, 0)).normalize();
        if (planeNormal.lengthSqr() < 1e-4) planeNormal = new Vec3(1, 0, 0);

        Vec3 P3 = prepPos;
        Vec3 E_prime = attackDir;
        double R = startPos.distanceTo(P3) * 0.4;
        Vec3 P0 = startPos;
        Vec3 P1 = startPos.add(currentVel.scale(R));
        Vec3 P2 = P3.subtract(E_prime.scale(R));

        List<PathNode> nodes = new ArrayList<>();

        for (int i = 1; i <= prepTicks; i++) {
            float localT = (float) i / prepTicks;
            float smoothT = localT * localT * (3.0f - 2.0f * localT);
            float mt = 1.0f - localT;

            Vec3 p = P0.scale(mt*mt*mt)
                    .add(P1.scale(3*mt*mt*localT))
                    .add(P2.scale(3*mt*localT*localT))
                    .add(P3.scale(localT*localT*localT));

            Vec3 tipDir = slerpVector(currentTip, attackDir, smoothT);
            Vec3 bNormal = slerpVector(currentNormal, planeNormal, smoothT);
            nodes.add(getEulerNode(p, tipDir, bNormal));
        }

        for (int i = 1; i <= attackTicks; i++) {
            float t = accelerate((float) i / attackTicks);
            Vec3 p = prepPos.lerp(hitPos, t);
            PathNode node = getEulerNode(p, attackDir, planeNormal);
            if (i == 1) {
                node = node.withFeature(HIT_CLEAR);
            }
            nodes.add(node);
        }

        Vec3 nextPlaneNormal = nextAttackDir.cross(new Vec3(0, 1, 0)).normalize();
        if (nextPlaneNormal.lengthSqr() < 1e-4) nextPlaneNormal = new Vec3(1, 0, 0);

        for (int i = 1; i <= retreatTicks; i++) {
            float t = (float) i / retreatTicks;
            float easeOut = t * (2.0f - t);
            Vec3 p = hitPos.lerp(nextPrepPos, easeOut);

            Vec3 tipDir = slerpVector(attackDir, nextAttackDir, easeOut);
            Vec3 bNormal = slerpVector(planeNormal, nextPlaneNormal, easeOut);
            nodes.add(getEulerNode(p, tipDir, bNormal));
        }

        this.setPath(nodes);
        advanceAttack(owner);
    }

    private void generateChainStrike(Player owner, LivingEntity target) {
        double thrustAngleThreshold = 0.9;
        int thrustAttackTicks = 2;
        int sweepAttackTicks = 10 + owner.getRandom().nextInt(1, 4);
        double curvePullOutward = 0.8;

        Vec3 startPos = this.getPos();
        Vec3 T = target.position().add(0, target.getBbHeight() / 2.0, 0);

        Vec3 fwd = T.subtract(startPos);
        double dist = fwd.length();
        if (dist < 1e-5) fwd = new Vec3(0, 0, 1);
        fwd = fwd.normalize();

        double speed = 1.0;
        Vec3 currentVel = new Vec3(0, 1, 0);
        LinkedList<PathNode> history = this.getHistoryNodes();
        if (history.size() > 1) {
            Vec3 rawVel = startPos.subtract(history.get(1).pos());
            speed = rawVel.length();
            if (speed > 1e-5) currentVel = rawVel.normalize();
            else currentVel = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        } else {
            currentVel = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        }

        Vec3 currentTip = Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
        Quaternionf q = new Quaternionf().rotateY((float) Math.toRadians(-this.getYaw()))
                .rotateX((float) Math.toRadians(this.getPitch()))
                .rotateZ((float) Math.toRadians(this.getRoll()));
        Vector3f upV = new Vector3f(0, 1, 0).rotate(q);
        Vec3 currentNormal = new Vec3(upV.x(), upV.y(), upV.z()).normalize();

        boolean isThrustMode = currentTip.dot(fwd) > thrustAngleThreshold;

        List<PathNode> nodes = new ArrayList<>();

        if (isThrustMode) {
            Vec3 thrustDir = T.subtract(startPos).normalize();
            Vec3 thrustNormal = thrustDir.cross(new Vec3(0, 1, 0)).normalize();
            if (thrustNormal.lengthSqr() < 1e-4) thrustNormal = new Vec3(1, 0, 0);

            Vec3 P0 = startPos;
            Vec3 P3 = T.add(thrustDir.scale(1.5));

            double R = Math.max(dist * 0.3, speed * 2.0);
            Vec3 P1 = startPos.add(currentVel.scale(R));
            Vec3 P2 = P3.subtract(thrustDir.scale(R));

            for (int i = 1; i <= thrustAttackTicks; i++) {
                float t = (float) i / thrustAttackTicks;
                float mt = 1.0f - t;

                Vec3 p = P0.scale(mt * mt * mt)
                        .add(P1.scale(3 * mt * mt * t))
                        .add(P2.scale(3 * mt * t * t))
                        .add(P3.scale(t * t * t));

                Vec3 tipDir = slerpVector(currentTip, thrustDir, t);
                Vec3 bNormal = slerpVector(currentNormal, thrustNormal, t);

                PathNode node = getEulerNode(p, tipDir, bNormal);
                if (i == 1) node = node.withFeature(HIT_CLEAR);
                nodes.add(node);
            }
        } else {
            Vec3 sweepUp = currentNormal;
            Vec3 chord = T.subtract(startPos);
            Vec3 chordDir = chord.normalize();
            Vec3 outward = chordDir.cross(sweepUp).normalize();
            if (outward.lengthSqr() < 1e-4) outward = chordDir.cross(new Vec3(0, 1, 0)).normalize();

            Vec3 pivot = startPos.add(chord.scale(0.5)).add(outward.scale(-dist * curvePullOutward * 0.5));

            Vec3 P0 = startPos;
            Vec3 P3 = T.add(chordDir.scale(0.5));

            double R = Math.max(dist * 0.4, speed * 3.0);
            Vec3 P1 = startPos.add(currentVel.scale(R));
            Vec3 P2 = P3.subtract(chordDir.scale(R)).add(outward.scale(-dist * curvePullOutward));

            for (int i = 1; i <= sweepAttackTicks; i++) {
                float t = (float) i / sweepAttackTicks;
                float mt = 1.0f - t;

                Vec3 p = P0.scale(mt * mt * mt)
                        .add(P1.scale(3 * mt * mt * t))
                        .add(P2.scale(3 * mt * t * t))
                        .add(P3.scale(t * t * t));

                Vec3 targetTip = p.subtract(pivot).normalize();

                Vec3 tipDir = slerpVector(currentTip, targetTip, t);
                Vec3 bNormal = slerpVector(currentNormal, sweepUp, t);

                PathNode node = getEulerNode(p, tipDir, bNormal);

                if (i == 1) node = node.withFeature(HIT_CLEAR);
                nodes.add(node);
            }
        }

        this.setPath(nodes);
        pickRandomAttack(owner);
    }
    private void buildRibbon(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose last, Vec3 cRel, Vec3 pRel, Vector3f cTip, Vector3f pTip, Vector3f cBase, Vector3f pBase, int cTipC, int pTipC, int cBaseC, int pBaseC) {
        consumer.addVertex(pose, (float) cRel.x + cBase.x(), (float) cRel.y + cBase.y(), (float) cRel.z + cBase.z()).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) cRel.x + cTip.x(), (float) cRel.y + cTip.y(), (float) cRel.z + cTip.z()).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x(), (float) pRel.y + pTip.y(), (float) pRel.z + pTip.z()).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) pRel.x + pBase.x(), (float) pRel.y + pBase.y(), (float) pRel.z + pBase.z()).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        consumer.addVertex(pose, (float) cRel.x + cBase.x(), (float) cRel.y + cBase.y(), (float) cRel.z + cBase.z()).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pBase.x(), (float) pRel.y + pBase.y(), (float) pRel.z + pBase.z()).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x(), (float) pRel.y + pTip.y(), (float) pRel.z + pTip.z()).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) cRel.x + cTip.x(), (float) cRel.y + cTip.y(), (float) cRel.z + cTip.z()).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
    }

    // ============== ITrailRenderer 接口实现 ==============

    @Override
    public int getTrailTimer() { return trailTimer; }

    @Override
    public int getTrailHistoryLength() { return 4; }

    @Override
    public int getTrailSegmentsPerNode() { return 8; }

    @Override
    public int getTrailStartIndex() {
        return Math.max(0, 10 - trailTimer); // 保证拖尾具有收缩消散特性
    }

    @Override
    public PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        float blend = Mth.lerp(partialTick, idleBlendO, idleBlend);
        Player owner = getOwner();
        if (blend > 0f && owner != null) {
            ServantData data = owner.getData(AttachmentRegister.ServantData);
            PathNode idealNode = getInterpolatedIdleState(owner, data.getOrder(this), Math.max(1, data.getServants().size()), partialTick);
            Vec3 pos = rawRenderNode.pos().lerp(idealNode.pos(), blend);
            float yaw = Mth.rotLerp(blend, rawRenderNode.yaw(), idealNode.yaw());
            float pitch = Mth.rotLerp(blend, rawRenderNode.pitch(), idealNode.pitch());
            float roll = Mth.rotLerp(blend, rawRenderNode.roll(), idealNode.roll());
            return new PathNode(rawRenderNode.feature(), pos, yaw, pitch, roll);
        }
        return rawRenderNode;
    }

    @Override
    public void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode visualRenderNode, List<TrailNode> smoothNodes) {
        VertexConsumer consumer = bufferSource.getBuffer(TrailRenderType.getTrail());
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        Vec3 currentRenderPos = visualRenderNode.pos();

        Player owner = getOwner();
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        int order = data.getOrder(this);
        int total = Math.max(1, data.getServants().size());

        float timeShift = (owner.tickCount + partialTick) * 0.015f;
        float hueShift = ((float) order / total + timeShift) % 1.0f;

        for (int i = 0; i < smoothNodes.size() - 1; i++) {
            TrailNode curr = smoothNodes.get(i);
            TrailNode prev = smoothNodes.get(i + 1);
            TrailNode nextOfPrev = (i + 2 < smoothNodes.size()) ? smoothNodes.get(i + 2) : prev;

            // 动态截面与挥动判定
            Vec3 currFwd = curr.pos.subtract(prev.pos);
            if (currFwd.lengthSqr() > 1e-5) currFwd = currFwd.normalize(); else currFwd = new Vec3(0, 1, 0);
            Vector3f cTipDir = new Vector3f(0, 0, 1).rotate(curr.rot);
            float cScale = Math.max(0.0f, 1.0f - (float) i / (smoothNodes.size() - 1));
            float cStab = Math.abs((float) currFwd.dot(new Vec3(cTipDir.x(), cTipDir.y(), cTipDir.z())));
            float cXBase = (0.05f + 0.25f * (1.0f - cStab) + 0.25f * cStab) * cScale;
            float cYBase = (0.05f + 0.15f * cStab) * cScale;

            Vec3 prevFwd = prev.pos.subtract(nextOfPrev.pos);
            if (prevFwd.lengthSqr() > 1e-5) prevFwd = prevFwd.normalize(); else prevFwd = new Vec3(0, 1, 0);
            Vector3f pTipDir = new Vector3f(0, 0, 1).rotate(prev.rot);
            float pScale = Math.max(0.0f, 1.0f - (float) (i + 1) / (smoothNodes.size() - 1));
            float pStab = Math.abs((float) prevFwd.dot(new Vec3(pTipDir.x(), pTipDir.y(), pTipDir.z())));
            float pXBase = (0.05f + 0.25f * (1.0f - pStab) + 0.25f * pStab) * pScale;
            float pYBase = (0.05f + 0.15f * pStab) * pScale;

            Vec3 currRel = curr.pos.subtract(currentRenderPos);
            Vec3 prevRel = prev.pos.subtract(currentRenderPos);

            Vector3f cTip = new Vector3f(0, 0, 0.6f).rotate(curr.rot), cR = new Vector3f(cXBase, 0, -0.2f).rotate(curr.rot), cL = new Vector3f(-cXBase, 0, -0.2f).rotate(curr.rot), cT = new Vector3f(0, cYBase, -0.2f).rotate(curr.rot), cB = new Vector3f(0, -cYBase, -0.2f).rotate(curr.rot);
            Vector3f pTip = new Vector3f(0, 0, 0.6f).rotate(prev.rot), pR = new Vector3f(pXBase, 0, -0.2f).rotate(prev.rot), pL = new Vector3f(-pXBase, 0, -0.2f).rotate(prev.rot), pT = new Vector3f(0, pYBase, -0.2f).rotate(prev.rot), pB = new Vector3f(0, -pYBase, -0.2f).rotate(prev.rot);

            float currHue = (((float) i / (smoothNodes.size() - 1)) * 0.85f + hueShift) % 1.0f;
            float prevHue = (((float) (i + 1) / (smoothNodes.size() - 1)) * 0.85f + hueShift) % 1.0f;

            int cColorRGB = Mth.hsvToRgb(currHue, 0.65f, 0.75f), pColorRGB = Mth.hsvToRgb(prevHue, 0.65f, 0.75f);
            int cr = (cColorRGB >> 16) & 0xFF, cg = (cColorRGB >> 8) & 0xFF, cb = cColorRGB & 0xFF;
            int pr = (pColorRGB >> 16) & 0xFF, pg = (pColorRGB >> 8) & 0xFF, pb = pColorRGB & 0xFF;

            int cTipA = Math.round(cScale * 0.1f * 255);
            int cBaseA = Math.round(Math.max(0f, 1.0f - ((float) i / (smoothNodes.size() - 1)) * 2.5f) * 0.04f * 255);
            int pTipA = Math.round(pScale * 0.1f * 255);
            int pBaseA = Math.round(Math.max(0f, 1.0f - ((float) (i + 1) / (smoothNodes.size() - 1)) * 2.5f) * 0.04f * 255);

            int cTipC = FastColor.ARGB32.color(cTipA, cr, cg, cb), cBaseC = FastColor.ARGB32.color(cBaseA, cr, cg, cb);
            int pTipC = FastColor.ARGB32.color(pTipA, pr, pg, pb), pBaseC = FastColor.ARGB32.color(pBaseA, pr, pg, pb);

            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cR, pR, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cL, pL, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cT, pT, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cB, pB, cTipC, pTipC, cBaseC, pBaseC);
        }
    }


    // ============== 主机体渲染 ==============

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        PathNode visualNode = getVisualRenderNode(this, partialTick, renderNode);

        Player owner = this.getOwner();
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        int order = data.getOrder(this);
        int total = Math.max(1, data.getServants().size());

        float hueShift = ((float) order / total + (owner.tickCount + partialTick) * 0.015f) % 1.0f;
        float breathFactor = 0.5f + 0.5f * Mth.sin(hueShift * Mth.TWO_PI);
        float currentScale = 1.0f + 0.10f * breathFactor;

        poseStack.pushPose();
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45));
        poseStack.scale(currentScale, currentScale, currentScale);

        int mColorRGB = Mth.hsvToRgb(hueShift, 0.75f - 0.35f * breathFactor, 1.0f);
        int mr = (mColorRGB >> 16) & 0xFF, mg = (mColorRGB >> 8) & 0xFF, mb = mColorRGB & 0xFF;

        MultiBufferSource baseSolidColorBuffer = type -> {
            VertexConsumer base = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucentCull(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS));
            return new VertexConsumer() {
                @Override public VertexConsumer addVertex(float x, float y, float z) { base.addVertex(x, y, z); return this; }
                @Override public VertexConsumer setColor(int r, int g, int b, int a) { base.setColor(mr, mg, mb, 255); return this; }
                @Override public VertexConsumer setUv(float u, float v) { base.setUv(u, v); return this; }
                @Override public VertexConsumer setUv1(int u, int v) { base.setUv1(u, v); return this; }
                @Override public VertexConsumer setUv2(int u, int v) { base.setUv2(u, v); return this; }
                @Override public VertexConsumer setNormal(float x, float y, float z) { base.setNormal(x, y, z); return this; }
            };
        };

        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.TerraPrism.get().getDefaultInstance(),
                ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, baseSolidColorBuffer, owner.level(), 0
        );

        if (trailTimer > 0) {
            poseStack.pushPose();
            poseStack.scale(1.05f, 1.05f, 1.05f);
            int mAlpha = 120 + (int)(60 * breathFactor);

            MultiBufferSource auraBufferSource = type -> {
                VertexConsumer base = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucentCull(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS));
                return new VertexConsumer() {
                    @Override public VertexConsumer addVertex(float x, float y, float z) { base.addVertex(x, y, z); return this; }
                    @Override public VertexConsumer setColor(int r, int g, int b, int a) { base.setColor(mr, mg, mb, mAlpha); return this; }
                    @Override public VertexConsumer setUv(float u, float v) { base.setUv(u, v); return this; }
                    @Override public VertexConsumer setUv1(int u, int v) { base.setUv1(u, v); return this; }
                    @Override public VertexConsumer setUv2(int u, int v) { base.setUv2(u, v); return this; }
                    @Override public VertexConsumer setNormal(float x, float y, float z) { base.setNormal(x, y, z); return this; }
                };
            };
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    ItemRegister.TerraPrism.get().getDefaultInstance(),
                    ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    poseStack, auraBufferSource, owner.level(), 0
            );
            poseStack.popPose();
        }
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(state);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.state = buf.readEnum(PrismState.class);
    }

    @Override
    public ServantType<? extends Servant> getType() {
        return ServantRegister.TerraPrism.get();
    }

}