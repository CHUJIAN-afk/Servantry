package first.servantry.common.servent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.servant.PathNode;
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
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class EnchantedThrowingKnives extends Servant implements IDamagingOnCollide, ITrailRenderer {

    public enum KnifeState {
        IDLE, DASHING, RETURN
    }

    private KnifeState state = KnifeState.IDLE;
    private int trailTimer = 0;

    // 动态位移补偿
    private Vec3 lastTargetPos = null;
    private Vec3 lastPlayerPos = null;

    // 穿刺核心参数
    private Vec3 dashStartPos = null;
    private Vec3 dashEndPos = null;
    private Vec3 dashDir = null;
    private Vec3 dashNormal = null;

    private float idleBlend = 0f;
    private float idleBlendO = 0f;

    private final Set<Integer> swingHitTargets = new HashSet<>();

    public EnchantedThrowingKnives(PathNode node) {
        super(node);
    }

    @Override
    public float getBaseDamage() {
        return 0.6f;
    }

    @Override
    public float getBaseKnockback() {
        return 0;
    }

    @Override
    public AABB getHitbox() {
        return new AABB(-0.2, -0.05, -0.8, 0.2, 0.05, -0.2);
    }

    @Override
    public void collisionAttack(Set<LivingEntity> hitTargets) {
        for (LivingEntity target : hitTargets) {
            if (!swingHitTargets.contains(target.getId())) {
                int invulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0; // 无视无敌帧
                target.hurt(getDamageSource(),getBaseDamage());
                target.invulnerableTime = invulnerableTime;
                swingHitTargets.add(target.getId());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!getHistoryNodes().isEmpty() && "hit_clear".equals(getHistoryNodes().getFirst().feature())) {
            swingHitTargets.clear();
        }

        Player owner = getOwner();
        if (owner != null) {
            if (owner.level().isClientSide()) clientTick();
            else serverTickAI(owner);
        }
    }

    private void clientTick() {
        if (state == KnifeState.DASHING) {
            trailTimer = 10;
        } else if (trailTimer > 0) {
            trailTimer--;
        }

        idleBlendO = idleBlend;
        if (state == KnifeState.IDLE || state == KnifeState.RETURN) {
            idleBlend = Math.min(1.0f, idleBlend + 0.1f);
        } else {
            idleBlend = Math.max(0.0f, idleBlend - 0.25f);
        }
    }

    private void serverTickAI(Player owner) {
        // 1. 每 3tick 刷新目标或无目标时强制刷新
        verifyTarget(owner);
        LivingEntity target = getTarget();

        // 2. 动态位移补偿 (核心防丢失机制)
        if (this.isExecutingPath()) {
            if (target != null && state == KnifeState.DASHING) {
                // 补偿始终跟随目标的中心点偏移
                Vec3 currentTargetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
                if (this.lastTargetPos != null) {
                    shiftQueue(currentTargetCenter.subtract(this.lastTargetPos));
                }
                this.lastTargetPos = currentTargetCenter;
            }
            if (state == KnifeState.RETURN && this.lastPlayerPos != null) {
                shiftQueue(owner.position().subtract(this.lastPlayerPos));
            }
            this.lastPlayerPos = owner.position();
            return; // 正在执行固定的 ticks 动画时，阻塞状态机流动
        }

        this.lastPlayerPos = owner.position();

        // 3. 状态机流转
        if (target != null) {
            // 如果处于 IDLE/RETURN，或者上一次 DASHING 刚刚结束（isExecutingPath 为 false）
            if (state == KnifeState.IDLE || state == KnifeState.RETURN || state == KnifeState.DASHING) {
                // 每次都以当前位置（即上次冲刺的终点）起手，直接穿刺
                setupDashFromCurrent(owner, target);
                state = KnifeState.DASHING;
                generateDash();
                // 记录目标中心点，用于位移补偿
                this.lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
            }
        } else {
            if (state != KnifeState.RETURN && state != KnifeState.IDLE) {
                state = KnifeState.RETURN;
                stateTick = 0;
            }
            if (state == KnifeState.RETURN) {
                if (stateTick == 0) {
                    generateReturn(owner);
                    stateTick++;
                } else {
                    state = KnifeState.IDLE; // 20 tick 回归完成，进入待机
                }
            } else {
                handleIdle(owner);
            }
        }
    }

    private void shiftQueue(Vec3 offset) {
        if (offset.lengthSqr() > 1e-5) {
            LinkedList<PathNode> queue = this.getPathQueue();
            for (int i = 0; i < queue.size(); i++) {
                PathNode node = queue.get(i);
                queue.set(i, new PathNode(node.feature(), node.pos().add(offset), node.yaw(), node.pitch(), node.roll()));
            }
        }
    }

    private void verifyTarget(Player owner) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        LivingEntity currentTarget = getTarget();

        List<LivingEntity> potentialTargets = data.getNearbyTargets(owner, this, 32.0, currentTarget == null);
        if (potentialTargets.isEmpty()) {
            setTarget(null);
            return;
        }

        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity e : potentialTargets) {
            double score = e.distanceToSqr(this.getPos());
            if (e.distanceToSqr(owner) <= 36.0) score -= 10000.0;

            if (score < bestScore) {
                bestScore = score;
                bestTarget = e;
            }
        }

        // 切换目标处理
        if (bestTarget != currentTarget) {
            setTarget(bestTarget);
            if (bestTarget != null && state != KnifeState.IDLE && state != KnifeState.RETURN) {
                // 打断当前路径，直接从半空中折返起手
                this.setPath(Collections.emptyList());
                setupDashFromCurrent(owner, bestTarget);
                state = KnifeState.DASHING;
                generateDash();
                this.lastTargetPos = bestTarget.position().add(0, bestTarget.getBbHeight() / 2.0, 0);
            }
        }
    }

    private void setupDashFromCurrent(Player owner, LivingEntity target) {
        // 根据目标碰撞箱计算安全的随机偏移点 (取宽度的 80%，高度的 20%~80%)
        float hw = target.getBbWidth() * 0.4f;
        double offsetX = (owner.getRandom().nextDouble() - 0.5) * 2.0 * hw;
        double offsetY = target.getBbHeight() * (0.2 + owner.getRandom().nextDouble() * 0.6);
        double offsetZ = (owner.getRandom().nextDouble() - 0.5) * 2.0 * hw;

        Vec3 targetPoint = target.position().add(offsetX, offsetY, offsetZ);
        Vec3 start = this.getPos();
        Vec3 dir = targetPoint.subtract(start);

        if (dir.lengthSqr() < 1e-4) dir = new Vec3(0, -1, 0);
        dir = dir.normalize();

        this.dashStartPos = start;
        // 穿透该偏移点 3 格作为终点
        this.dashEndPos = targetPoint.add(dir.scale(3.0));
        this.dashDir = dir;

        // 随机选择法线平面初始值（这个就是飞刀首尾翻滚所在的平面的法线，即旋转轴）
        Vec3 randomUp = new Vec3(owner.getRandom().nextFloat()-0.5, owner.getRandom().nextFloat()-0.5, owner.getRandom().nextFloat()-0.5).normalize();
        this.dashNormal = dir.cross(randomUp).normalize();
        if (this.dashNormal.lengthSqr() < 1e-4) this.dashNormal = new Vec3(0, 1, 0);
    }

    private void generateDash() {
        int ticks = getOwner().getRandom().nextInt(8, 10);
        List<PathNode> nodes = new ArrayList<>();

        for (int i = 1; i <= ticks; i++) {
            float t = (float) i / ticks;
            Vec3 p = this.dashStartPos.lerp(this.dashEndPos, t);

            //让刀尖的朝向 (Dir) 围绕旋转轴 (Normal) 进行旋转
            float dashSpinRotations = 1.0f;
            float currentAngle = t * dashSpinRotations * (float) Math.PI * 2f;

            Vector3f rotatedTipF = new Vector3f((float)this.dashDir.x, (float)this.dashDir.y, (float)this.dashDir.z);
            rotatedTipF.rotateAxis(currentAngle, (float)this.dashNormal.x, (float)this.dashNormal.y, (float)this.dashNormal.z);

            Vec3 currentTip = new Vec3(rotatedTipF.x(), rotatedTipF.y(), rotatedTipF.z());

            // 传入时：刀尖朝向为旋转后的 currentTip，所在平面的法线固定为 dashNormal
            nodes.add(getEulerNode(p, currentTip, this.dashNormal, i == 1 ? "hit_clear" : null));
        }
        this.setPath(nodes);
    }

    private void generateReturn(Player owner) {
        Vec3 startPos = this.getPos();
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        PathNode idleNode = getInterpolatedIdleState(owner, data.getOrder(this), Math.max(1, data.getSameSize(this)), 1.0f);
        Vec3 endPos = idleNode.pos();

        int ticks = 20; // 【核心机制】：回归绝对耗时 20 tick
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= ticks; i++) {
            float t = (float) i / ticks;
            float ease = t * (2f - t);
            Vec3 pos = startPos.lerp(endPos, ease);

            float yaw = Mth.rotLerp(ease, getYaw(), idleNode.yaw());
            float pitch = Mth.rotLerp(ease, getPitch(), idleNode.pitch());
            float roll = Mth.rotLerp(ease, getRoll(), idleNode.roll());

            nodes.add(new PathNode("", pos, yaw, pitch, roll));
        }
        this.setPath(nodes);
    }

    public PathNode getInterpolatedIdleState(Player owner, int order, int total, float partialTick) {
        float angle = (owner.tickCount + partialTick) * 0.05f + (order * Mth.TWO_PI / total);

        // 【核心机制】：根据飞刀数量动态调整半径，基础 1.2，超过 4 把后开始扩圈防重叠
        float radius = 1.2f + (total > 4 ? (total - 4) * 0.025f : 0f);

        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());

        Vec3 targetPos = new Vec3(px, py, pz).add(Math.cos(angle) * radius, owner.getBbHeight() + 1.2, Math.sin(angle) * radius);
        Vec3 centerAxis = new Vec3(px, py + owner.getBbHeight() + 1.2, pz);

        // 刀面朝向中心轴，剑尖绝对朝下
        Vec3 toAxis = centerAxis.subtract(targetPos).normalize();
        if (toAxis.lengthSqr() < 1e-4) toAxis = new Vec3(1, 0, 0);
        Vec3 tipDir = new Vec3(0, -1, 0);

        return getEulerNode(targetPos, tipDir, toAxis, "");
    }

    private void handleIdle(Player owner) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        PathNode idleNode = getInterpolatedIdleState(owner, data.getOrder(this), Math.max(1, data.getSameSize(this)), 1.0f);

        Vec3 nextPos = this.getPos().lerp(idleNode.pos(), 0.25f);
        float nextYaw = Mth.rotLerp(0.25f, this.getYaw(), idleNode.yaw());
        float nextPitch = Mth.rotLerp(0.25f, this.getPitch(), idleNode.pitch());
        float nextRoll = Mth.rotLerp(0.25f, this.getRoll(), idleNode.roll());

        this.setPath(Collections.singletonList(new PathNode("", nextPos, nextYaw, nextPitch, nextRoll)));
    }

// ============== ITrailRenderer 接口实现 ==============

    @Override
    public int getTrailTimer() { return trailTimer; }

    @Override
    public int getTrailHistoryLength() { return 3; }

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
            PathNode idealNode = getInterpolatedIdleState(owner, data.getOrder(this), Math.max(1, data.getSameSize(this)), partialTick);
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
        Matrix4f pose = poseStack.last().pose();
        int cr = 136, cg = 204, cb = 255;
        Vec3 visualRenderPos = visualRenderNode.pos();

        for (int i = 0; i < smoothNodes.size() - 1; i++) {
            TrailNode curr = smoothNodes.get(i), prev = smoothNodes.get(i + 1);
            Vec3 currRel = curr.pos.subtract(visualRenderPos), prevRel = prev.pos.subtract(visualRenderPos);

            Vector3f cTip = new Vector3f(0, 0, 0.4f).rotate(curr.rot), cB = new Vector3f(0, 0, -0.4f).rotate(curr.rot);
            Vector3f pTip = new Vector3f(0, 0, 0.4f).rotate(prev.rot), pB = new Vector3f(0, 0, -0.4f).rotate(prev.rot);

            float currProgress = (float) i / smoothNodes.size(), prevProgress = (float) (i + 1) / smoothNodes.size();
            int cA = Math.round(Math.max(0f, 1.0f - currProgress) * 150), pA = Math.round(Math.max(0f, 1.0f - prevProgress) * 150);

            int cColor = FastColor.ARGB32.color(cA, cr, cg, cb), pColor = FastColor.ARGB32.color(pA, cr, cg, cb);
            int cColorBase = FastColor.ARGB32.color((int)(cA*0.3f), cr, cg, cb), pColorBase = FastColor.ARGB32.color((int)(pA*0.3f), cr, cg, cb);

            consumer.addVertex(pose, (float) currRel.x + cB.x(), (float) currRel.y + cB.y(), (float) currRel.z + cB.z()).setColor(cColorBase).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            consumer.addVertex(pose, (float) currRel.x + cTip.x(), (float) currRel.y + cTip.y(), (float) currRel.z + cTip.z()).setColor(cColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            consumer.addVertex(pose, (float) prevRel.x + pTip.x(), (float) prevRel.y + pTip.y(), (float) prevRel.z + pTip.z()).setColor(pColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            consumer.addVertex(pose, (float) prevRel.x + pB.x(), (float) prevRel.y + pB.y(), (float) prevRel.z + pB.z()).setColor(pColorBase).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        }
    }

    // ============== 主机体渲染 ==============

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        // 由于所有的贝塞尔与拖尾都被接口托管，本体渲染只需要做位移混合即可！
        PathNode visualNode = getVisualRenderNode(this, partialTick, renderNode);

        poseStack.pushPose();
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(0.8f, 0.8f, 0.8f);

        Player owner = getOwner();
        if(owner != null) {
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    ItemRegister.EnchantedThrowingKnives.get().getDefaultInstance(),
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    owner.level(),
                    0
            );
        }

        poseStack.popPose();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(state);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.state = buf.readEnum(KnifeState.class);
    }

    @Override
    public ServantType<? extends Servant> getType() {
        return ServantRegister.EnchantedThrowingKnives.get();
    }

}