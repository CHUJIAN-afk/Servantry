package first.servantry.common.servent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.ai.ActionController;
import first.servantry.api.ai.ServantAction;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.IDamagingOnCollide;
import first.servantry.api.servant.ITrailRenderer;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ItemRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

    private int trailTimer = 0;
    private float idleBlend = 0f;
    private float idleBlendO = 0f;
    private final Set<Integer> swingHitTargets = new HashSet<>();

    public LivingEntity currentTarget = null;

    public EnchantedThrowingKnives(PathNode node) {
        super(node);
        // 初始化动作控制器
        this.ai = new ActionController<>(this, new ActionKnifeIdle(this));
    }

    @SuppressWarnings("unchecked")
    public ActionController<EnchantedThrowingKnives> getAi() { return (ActionController<EnchantedThrowingKnives>) ai; }

    @Override
    public float getBaseDamage() { return 0.6f; }

    @Override
    public float getBaseKnockback() { return 0; }

    @Override
    public AABB getHitbox() { return new AABB(-0.2, -0.05, -0.8, 0.2, 0.05, -0.2); }

    @Override
    public void onPathNodeConsumed(PathNode node) {
        if ("hit_clear".equals(node.feature())) {
            swingHitTargets.clear();
        }
    }

    @Override
    public void collisionAttack(Set<LivingEntity> hitTargets) {
        for (LivingEntity target : hitTargets) {
            if (!swingHitTargets.contains(target.getId())) {
                int invulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0; // 无视无敌帧
                target.hurt(getDamageSource(), getBaseDamage());
                target.invulnerableTime = invulnerableTime;
                swingHitTargets.add(target.getId());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        Player owner = getOwner();
        if (owner != null) {
            if (owner.level().isClientSide()) clientTick();
            else serverTickAI(owner);
        }
    }

    private void clientTick() {
        String currentActionId = getAi().getCurrentAction().getId();

        if ("dash".equals(currentActionId)) {
            trailTimer = 10;
        } else if (trailTimer > 0) {
            trailTimer--;
        }

        idleBlendO = idleBlend;
        if ("idle".equals(currentActionId) || "return".equals(currentActionId)) {
            idleBlend = Math.min(1.0f, idleBlend + 0.1f);
        } else {
            idleBlend = Math.max(0.0f, idleBlend - 0.25f);
        }
    }

    private void serverTickAI(Player owner) {
        LivingEntity newTarget = verifyTarget(owner);

        // 如果目标切换，并且当前处于攻击状态，强行打断进行新的连刺
        if (currentTarget != null && newTarget != null && currentTarget.getId() != newTarget.getId()) {
            if (!(getAi().getCurrentAction() instanceof ActionKnifeIdle) && !(getAi().getCurrentAction() instanceof ActionKnifeReturn)) {
                this.setPath(Collections.emptyList()); // 清空路径，半空折返
                getAi().forceAction(new ActionKnifeDash(this), newTarget);
            }
        }

        this.currentTarget = newTarget;
        getAi().tick(newTarget);
    }

    private LivingEntity verifyTarget(Player owner) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        List<LivingEntity> potentialTargets = data.getNearbyTargets(owner, this, 32.0, currentTarget == null);

        if (potentialTargets.isEmpty()) return null;

        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity e : potentialTargets) {
            double score = e.distanceToSqr(this.getPos());
            if (score < bestScore) {
                bestScore = score;
                bestTarget = e;
            }
        }
        return bestTarget;
    }

    // 开放给 Action 调用的动态位移工具
    public void shiftQueue(Vec3 offset) {
        if (offset.lengthSqr() > 1e-5) {
            LinkedList<PathNode> queue = this.getPathQueue();
            for (int i = 0; i < queue.size(); i++) {
                PathNode node = queue.get(i);
                queue.set(i, new PathNode(node.feature(), node.pos().add(offset), node.yaw(), node.pitch(), node.roll()));
            }
        }
    }

    public PathNode getInterpolatedIdleState(Player owner, int order, int total, float partialTick) {
        float angle = (owner.tickCount + partialTick) * 0.05f + (order * Mth.TWO_PI / total);
        float radius = 1.2f + (total > 4 ? (total - 4) * 0.025f : 0f);

        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());

        Vec3 targetPos = new Vec3(px, py, pz).add(Math.cos(angle) * radius, owner.getBbHeight() + 1.2, Math.sin(angle) * radius);
        Vec3 centerAxis = new Vec3(px, py + owner.getBbHeight() + 1.2, pz);

        Vec3 toAxis = centerAxis.subtract(targetPos).normalize();
        if (toAxis.lengthSqr() < 1e-4) toAxis = new Vec3(1, 0, 0);
        Vec3 tipDir = new Vec3(0, -1, 0);

        return getEulerNode(targetPos, tipDir, toAxis, "");
    }

    @Override
    public ServantAction<?> createAction(String id) {
        return switch (id) {
            case "dash" -> new ActionKnifeDash(this);
            case "return" -> new ActionKnifeReturn(this);
            default -> new ActionKnifeIdle(this);
        };
    }

    // ================== Action 状态机剧本 ==================

    public static class ActionKnifeIdle extends ServantAction<EnchantedThrowingKnives> {
        public ActionKnifeIdle(EnchantedThrowingKnives servant) { super(servant); }
        @Override public String getId() { return "idle"; }

        @Override
        public void tick(LivingEntity target) {
            if (target != null) {
                servant.getAi().trySetAction(new ActionKnifeDash(servant), target);
                return;
            }
            Player owner = servant.getOwner();
            ServantData data = owner.getData(AttachmentRegister.ServantData);
            PathNode idleNode = servant.getInterpolatedIdleState(owner, data.getOrder(servant), Math.max(1, data.getSameSize(servant)), 1.0f);

            Vec3 nextPos = servant.getPos().lerp(idleNode.pos(), 0.25f);
            float nextYaw = Mth.rotLerp(0.25f, servant.getYaw(), idleNode.yaw());
            float nextPitch = Mth.rotLerp(0.25f, servant.getPitch(), idleNode.pitch());
            float nextRoll = Mth.rotLerp(0.25f, servant.getRoll(), idleNode.roll());

            servant.setPath(Collections.singletonList(new PathNode("", nextPos, nextYaw, nextPitch, nextRoll)));
        }
    }

    public static class ActionKnifeDash extends ServantAction<EnchantedThrowingKnives> {
        private Vec3 lastTargetPos;

        public ActionKnifeDash(EnchantedThrowingKnives servant) { super(servant); }
        @Override public String getId() { return "dash"; }
        @Override public boolean isAttack() { return true; }

        @Override
        public void onStart(LivingEntity target) {
            if (target == null) return;
            Player owner = servant.getOwner();

            // setupDashFromCurrent
            float hw = target.getBbWidth() * 0.4f;
            double offsetX = (owner.getRandom().nextDouble() - 0.5) * 2.0 * hw;
            double offsetY = target.getBbHeight() * (0.2 + owner.getRandom().nextDouble() * 0.6);
            double offsetZ = (owner.getRandom().nextDouble() - 0.5) * 2.0 * hw;

            Vec3 targetPoint = target.position().add(offsetX, offsetY, offsetZ);
            Vec3 start = servant.getPos();
            Vec3 dir = targetPoint.subtract(start);

            if (dir.lengthSqr() < 1e-4) dir = new Vec3(0, -1, 0);
            dir = dir.normalize();

            Vec3 dashEndPos = targetPoint.add(dir.scale(3.0));
            Vec3 dashDir = dir;

            Vec3 randomUp = new Vec3(owner.getRandom().nextFloat()-0.5, owner.getRandom().nextFloat()-0.5, owner.getRandom().nextFloat()-0.5).normalize();
            Vec3 dashNormal = dir.cross(randomUp).normalize();
            if (dashNormal.lengthSqr() < 1e-4) dashNormal = new Vec3(0, 1, 0);

            // generateDash
            int ticks = owner.getRandom().nextInt(8, 10);
            List<PathNode> nodes = new ArrayList<>();
            for (int i = 1; i <= ticks; i++) {
                float t = (float) i / ticks;
                Vec3 p = start.lerp(dashEndPos, t);
                float dashSpinRotations = 1.0f;
                float currentAngle = t * dashSpinRotations * (float) Math.PI * 2f;

                Vector3f rotatedTipF = new Vector3f((float) dashDir.x, (float) dashDir.y, (float) dashDir.z);
                rotatedTipF.rotateAxis(currentAngle, (float) dashNormal.x, (float) dashNormal.y, (float) dashNormal.z);
                Vec3 currentTip = new Vec3(rotatedTipF.x(), rotatedTipF.y(), rotatedTipF.z());

                nodes.add(servant.getEulerNode(p, currentTip, dashNormal, i == 1 ? "hit_clear" : null));
            }
            servant.setPath(nodes);
            this.lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
        }

        @Override
        public void tick(LivingEntity target) {
            if (servant.isExecutingPath()) {
                if (target != null) {
                    Vec3 currentTargetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
                    if (this.lastTargetPos != null) {
                        servant.shiftQueue(currentTargetCenter.subtract(this.lastTargetPos));
                    }
                    this.lastTargetPos = currentTargetCenter;
                }
            } else {
                if (target != null) {
                    servant.getAi().forceAction(new ActionKnifeDash(servant), target);
                } else {
                    servant.getAi().forceAction(new ActionKnifeReturn(servant), null);
                }
            }
        }
        @Override public boolean isFinished() { return false; }
    }

    public static class ActionKnifeReturn extends ServantAction<EnchantedThrowingKnives> {
        private Vec3 lastPlayerPos;

        public ActionKnifeReturn(EnchantedThrowingKnives servant) { super(servant); }
        @Override public String getId() { return "return"; }

        @Override
        public void onStart(LivingEntity target) {
            Player owner = servant.getOwner();
            this.lastPlayerPos = owner.position();

            Vec3 startPos = servant.getPos();
            ServantData data = owner.getData(AttachmentRegister.ServantData);
            PathNode idleNode = servant.getInterpolatedIdleState(owner, data.getOrder(servant), Math.max(1, data.getSameSize(servant)), 1.0f);
            Vec3 endPos = idleNode.pos();

            int ticks = 20;
            List<PathNode> nodes = new ArrayList<>();
            for (int i = 1; i <= ticks; i++) {
                float t = (float) i / ticks;
                float ease = t * (2f - t);
                Vec3 pos = startPos.lerp(endPos, ease);
                float yaw = Mth.rotLerp(ease, servant.getYaw(), idleNode.yaw());
                float pitch = Mth.rotLerp(ease, servant.getPitch(), idleNode.pitch());
                float roll = Mth.rotLerp(ease, servant.getRoll(), idleNode.roll());
                nodes.add(new PathNode("", pos, yaw, pitch, roll));
            }
            servant.setPath(nodes);
        }

        @Override
        public void tick(LivingEntity target) {
            Player owner = servant.getOwner();
            if (servant.isExecutingPath()) {
                if (this.lastPlayerPos != null) {
                    servant.shiftQueue(owner.position().subtract(this.lastPlayerPos));
                }
                this.lastPlayerPos = owner.position();
            } else {
                servant.getAi().forceAction(new ActionKnifeIdle(servant), target);
            }
        }
        @Override public boolean isFinished() { return false; }
    }

    // ============== ITrailRenderer 接口实现 ==============

    @Override public int getTrailTimer() { return trailTimer; }
    @Override public int getTrailHistoryLength() { return 3; }
    @Override public int getTrailStartIndex() { return Math.max(0, 10 - trailTimer); }

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
    public ServantType<? extends Servant> getType() {
        return ServantRegister.EnchantedThrowingKnives.get();
    }
}