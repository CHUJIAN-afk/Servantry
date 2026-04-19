package first.servantry.common.servent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.ai.ActionController;
import first.servantry.api.ai.ServantAction;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.IConeTrailRenderer;
import first.servantry.api.servant.IDamagingOnCollide;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SanguineBat extends Servant implements IDamagingOnCollide, IConeTrailRenderer {

    private int trailTimer = 0;
    private int age = 0; // 用于动画驱动的刻数
    private final Set<Integer> swingHitTargets = new HashSet<>();

    private float idleBlend = 0f;
    private float idleBlendO = 0f;

    public SanguineBat(PathNode node) {
        super(node);
        this.ai = new ActionController<>(this, new ActionBatIdle(this));
    }

    @SuppressWarnings("unchecked")
    public ActionController<SanguineBat> getAi() { return (ActionController<SanguineBat>) ai; }

    @Override
    public float getBaseDamage() { return 3.5f; }

    @Override
    public float getBaseKnockback() { return 0.3f; }

    @Override
    public AABB getHitbox() { return new AABB(-0.1, -0.1, -0.1, 0.1, 0.1, 0.1); }

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
                target.invulnerableTime = 0;
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
        this.age++;
        String currentActionId = getAi().getCurrentAction().getId();

        if ("attack".equals(currentActionId) || "return".equals(currentActionId)) {
            trailTimer = 15;
        } else {
            trailTimer = 0;
        }

        idleBlendO = idleBlend;
        if ("idle".equals(currentActionId) || "return_stay".equals(currentActionId)) {
            idleBlend = Math.min(1.0f, idleBlend + 0.15f);
        } else {
            idleBlend = Math.max(0.0f, idleBlend - 0.4f);
        }
    }

    private void serverTickAI(Player owner) {
        LivingEntity currentTarget = verifyTarget(owner);
        getAi().tick(currentTarget);
    }

    private LivingEntity verifyTarget(Player owner) {
        String actionId = getAi().getCurrentAction().getId();
        if (!"idle".equals(actionId) && !"prep".equals(actionId)) {
            LivingEntity currentTarget = getTarget();
            if (currentTarget != null && currentTarget.isAlive() && currentTarget.distanceToSqr(owner) <= 24.0 * 24.0) {
                return currentTarget;
            }
            setTarget(null);
            return null;
        }

        LivingEntity currentTarget = getTarget();
        if (currentTarget != null && currentTarget.isAlive() && currentTarget.distanceToSqr(owner) <= 24.0 * 24.0) {
            return currentTarget;
        }

        ServantData data = owner.getData(AttachmentRegister.ServantData);
        List<LivingEntity> potentialTargets = data.getNearbyTargets(owner, this, 24.0, currentTarget == null);



        if (potentialTargets.isEmpty()) {
            setTarget(null);
            return null;
        }

        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity e : potentialTargets) {
            double score = e.distanceToSqr(getOwner());
            if (e == currentTarget) {
                score /= 2;
            }
            if (score < bestScore) {
                bestScore = score;
                bestTarget = e;
            }
        }
        setTarget(bestTarget);
        return bestTarget;
    }

    public PathNode getInterpolatedIdlePos(Player owner, float partialTick) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        int order = data.getOrder(this);
        int total = Math.max(1, data.getSameSize(this));

        float angle = 0f;
        if (total > 1) {
            float offsetIndex = order - (total - 1) / 2.0f;
            float maxSpread = (float) Math.PI;
            float step = (float) (Math.PI / 7.0);
            if (step * (total - 1) > maxSpread) {
                step = maxSpread / (total - 1);
            }
            angle = offsetIndex * step;
        }

        float radius = 1.3f;
        float playerYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);

        Vec3 right = Vec3.directionFromRotation(0, playerYaw + 90);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 offset = right.scale(Math.sin(angle) * radius).add(up.scale(Math.cos(angle) * radius));

        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());
        Vec3 playerPos = new Vec3(px, py + owner.getBbHeight() + 0.1, pz);

        Vec3 finalPos = playerPos.add(offset);
        float headYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
        float headPitch = Mth.rotLerp(partialTick, owner.xRotO, owner.getXRot());

        return new PathNode("", finalPos, headYaw, headPitch, 0f);
    }

    @Override
    public ServantAction<?> createAction(String id) {
        return switch (id) {
            case "prep" -> new ActionBatPrep(this);
            case "attack" -> new ActionBatAttack(this);
            case "return" -> new ActionBatReturn(this);
            case "return_stay" -> new ActionBatReturnStay(this);
            default -> new ActionBatIdle(this);
        };
    }

    // ================== Action 状态机剧本 ==================

    public static class ActionBatIdle extends ServantAction<SanguineBat> {
        public ActionBatIdle(SanguineBat servant) { super(servant); }
        @Override public String getId() { return "idle"; }

        @Override
        public void tick(LivingEntity target) {
            if (target != null) {
                servant.getAi().trySetAction(new ActionBatPrep(servant), target);
                return;
            }
            PathNode idleTarget = servant.getInterpolatedIdlePos(servant.getOwner(), 1.0f);
            servant.getPathQueue().clear();
            servant.getPathQueue().add(idleTarget);
        }
    }

    public static class ActionBatPrep extends ServantAction<SanguineBat> {
        public ActionBatPrep(SanguineBat servant) { super(servant); }
        @Override public String getId() { return "prep"; }

        @Override
        public void tick(LivingEntity target) {
            if (target != null) servant.getAi().forceAction(new ActionBatAttack(servant), target);
            else servant.getAi().forceAction(new ActionBatIdle(servant), null);
        }
        @Override public boolean isFinished() { return false; }
    }

    public static class ActionBatAttack extends ServantAction<SanguineBat> {
        private int attackTotalTicks;
        private Vec3 lastTargetPos;
        private Vec3 lastIdlePos;

        public ActionBatAttack(SanguineBat servant) { super(servant); }
        @Override public String getId() { return "attack"; }
        @Override public boolean isAttack() { return true; }

        @Override
        public void onStart(LivingEntity target) {
            if (target == null) return;
            Player owner = servant.getOwner();
            Vec3 start = servant.getPos();
            Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);

            double dist = start.distanceTo(targetPos);
            Vec3 fwd = targetPos.subtract(start).normalize();
            if (fwd.lengthSqr() < 1e-4) fwd = new Vec3(0, 0, 1);

            Vec3 randomUp = new Vec3(owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5, owner.getRandom().nextDouble() - 0.5).normalize();
            Vec3 right = fwd.cross(randomUp).normalize();
            if (right.lengthSqr() < 1e-4) right = fwd.cross(new Vec3(0, 1, 0)).normalize();

            double minFlatness = 0.2;
            double maxFlatness = 0.7;
            double flatness = Mth.clamp(maxFlatness - (dist / 24.0) * (maxFlatness - minFlatness), minFlatness, maxFlatness);

            float ellipseSideFactor = owner.getRandom().nextBoolean() ? 1f : -1f;
            double minorRadius = (dist * 0.5) * flatness;

            Vec3 center = start.lerp(targetPos, 0.5);
            Vec3 majorAxis = targetPos.subtract(start).scale(0.5);
            Vec3 minorAxis = right.scale(minorRadius * ellipseSideFactor);

            int ticks = owner.getRandom().nextInt(20, 24);
            List<PathNode> nodes = new ArrayList<>();

            for (int i = 1; i <= ticks; i++) {
                float t = (float) i / ticks;
                double angle = Math.PI + 2.0 * Math.PI * t;
                Vec3 pos = center.add(majorAxis.scale(Math.cos(angle))).add(minorAxis.scale(Math.sin(angle)));

                Vec3 lookAt = (t <= 0.5f) ? targetPos : start;
                Vec3 dir = lookAt.subtract(pos).normalize();
                if (dir.lengthSqr() < 1e-4) dir = fwd;

                float yaw = (float) (Math.atan2(-dir.x, dir.z) * 180 / Math.PI);
                float pitch = (float) (Math.atan2(-dir.y, Math.sqrt(dir.x*dir.x + dir.z*dir.z)) * 180 / Math.PI);
                float roll = ellipseSideFactor * 45f * (float) Math.sin(t * Math.PI);

                nodes.add(new PathNode(i == 1 ? "hit_clear" : "", pos, yaw, pitch, roll));
            }
            servant.setPath(nodes);
            this.attackTotalTicks = servant.getPathQueue().size();
            this.lastTargetPos = targetPos;
            this.lastIdlePos = servant.getInterpolatedIdlePos(owner, 1.0f).pos();
        }

        @Override
        public void tick(LivingEntity target) {
            Player owner = servant.getOwner();
            if (servant.isExecutingPath()) {
                int remaining = servant.getPathQueue().size();
                boolean isFirstHalf = remaining >= this.attackTotalTicks / 2;
                if (target == null && isFirstHalf) {
                    servant.setPath(Collections.emptyList());
                    servant.getAi().forceAction(new ActionBatReturn(servant), null);
                    return;
                }

                // correctAttackPath
                Vec3 currentIdlePos = servant.getInterpolatedIdlePos(owner, 1.0f).pos();
                Vec3 dIdle = currentIdlePos.subtract(this.lastIdlePos);
                Vec3 currentTargetPos = this.lastTargetPos;
                Vec3 dTarget = Vec3.ZERO;

                if (target != null) {
                    currentTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
                    dTarget = currentTargetPos.subtract(this.lastTargetPos);
                }

                LinkedList<PathNode> q = servant.getPathQueue();
                for (int i = 0; i < remaining; i++) {
                    PathNode node = q.get(i);
                    float t = 1.0f - (float)(remaining - 1 - i) / Math.max(1, this.attackTotalTicks);
                    t = Mth.clamp(t, 0f, 1f);

                    double a = Math.PI + 2.0 * Math.PI * t;
                    float wIdle = (float) (0.5 * (1.0 - Math.cos(a)));
                    float wTarget = (float) (0.5 * (1.0 + Math.cos(a)));

                    Vec3 newPos = node.pos().add(dIdle.scale(wIdle)).add(dTarget.scale(wTarget));
                    Vec3 lookAt = (t <= 0.5f) ? currentTargetPos : currentIdlePos;
                    Vec3 dir = lookAt.subtract(newPos).normalize();
                    if (dir.lengthSqr() < 1e-4) dir = new Vec3(0, 0, 1);
                    float yaw = (float) (Math.atan2(-dir.x, dir.z) * 180 / Math.PI);
                    float pitch = (float) (Math.atan2(-dir.y, Math.sqrt(dir.x*dir.x + dir.z*dir.z)) * 180 / Math.PI);

                    q.set(i, new PathNode(node.feature(), newPos, yaw, pitch, node.roll()));
                }
                this.lastTargetPos = currentTargetPos;
                this.lastIdlePos = currentIdlePos;

            } else {
                servant.getAi().forceAction(new ActionBatReturnStay(servant), target);
            }
        }
        @Override public boolean isFinished() { return false; }
    }

    public static class ActionBatReturn extends ServantAction<SanguineBat> {
        private Vec3 lastIdlePos;
        public ActionBatReturn(SanguineBat servant) { super(servant); }
        @Override public String getId() { return "return"; }

        @Override
        public void onStart(LivingEntity target) {
            Player owner = servant.getOwner();
            Vec3 start = servant.getPos();
            PathNode idleNode = servant.getInterpolatedIdlePos(owner, 1.0f);
            Vec3 end = idleNode.pos();

            int ticks = 5;
            List<PathNode> nodes = new ArrayList<>();
            for (int i = 1; i <= ticks; i++) {
                float t = (float) i / ticks;
                Vec3 pos = start.lerp(end, t);

                Vec3 lookDir = end.subtract(pos);
                if (lookDir.lengthSqr() < 1e-4) lookDir = new Vec3(0, 0, 1);
                lookDir = lookDir.normalize();

                float yaw = (float) (Math.atan2(-lookDir.x, lookDir.z) * 180 / Math.PI);
                float pitch = (float) (Math.atan2(-lookDir.y, Math.sqrt(lookDir.x*lookDir.x + lookDir.z*lookDir.z)) * 180 / Math.PI);
                float roll = Mth.lerp(t, servant.getRoll(), 0f);

                nodes.add(new PathNode("", pos, yaw, pitch, roll));
            }
            servant.setPath(nodes);
            this.lastIdlePos = end;
        }

        @Override
        public void tick(LivingEntity target) {
            if (servant.isExecutingPath()) {
                if (this.lastIdlePos == null) return;
                Player owner = servant.getOwner();
                Vec3 currentIdlePos = servant.getInterpolatedIdlePos(owner, 1.0f).pos();
                Vec3 dIdle = currentIdlePos.subtract(this.lastIdlePos);

                LinkedList<PathNode> q = servant.getPathQueue();
                int remaining = q.size();
                for (int i = 0; i < remaining; i++) {
                    PathNode node = q.get(i);
                    float t = (float)(i + 1) / remaining;
                    Vec3 newPos = node.pos().add(dIdle.scale(t));

                    Vec3 dir = currentIdlePos.subtract(newPos).normalize();
                    float yaw = node.yaw();
                    float pitch = node.pitch();

                    if (dir.lengthSqr() > 1e-4) {
                        yaw = (float) (Math.atan2(-dir.x, dir.z) * 180 / Math.PI);
                        pitch = (float) (Math.atan2(-dir.y, Math.sqrt(dir.x*dir.x + dir.z*dir.z)) * 180 / Math.PI);
                    }
                    q.set(i, new PathNode(node.feature(), newPos, yaw, pitch, node.roll()));
                }
                this.lastIdlePos = currentIdlePos;
            } else {
                servant.getAi().forceAction(new ActionBatReturnStay(servant), null);
            }
        }
        @Override public boolean isFinished() { return false; }
    }

    public static class ActionBatReturnStay extends ServantAction<SanguineBat> {
        private int tick = 0;
        public ActionBatReturnStay(SanguineBat servant) { super(servant); }
        @Override public String getId() { return "return_stay"; }

        @Override
        public void tick(LivingEntity target) {
            tick++;
            if (tick >= 1) {
                servant.getAi().forceAction(new ActionBatIdle(servant), target);
            } else {
                PathNode idleTarget = servant.getInterpolatedIdlePos(servant.getOwner(), 1.0f);
                servant.getPathQueue().clear();
                servant.getPathQueue().add(idleTarget);
            }
        }
        @Override public boolean isFinished() { return false; }
    }


    // ============== 客户端渲染与模型 ==============

    @Override
    public PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        float blend = Mth.lerp(partialTick, idleBlendO, idleBlend);
        Player owner = getOwner();

        if (blend > 0f && owner != null) {
            PathNode idealNode = getInterpolatedIdlePos(owner, partialTick);
            Vec3 pos = rawRenderNode.pos().lerp(idealNode.pos(), blend);
            float yaw = Mth.rotLerp(blend, rawRenderNode.yaw(), idealNode.yaw());
            float pitch = Mth.rotLerp(blend, rawRenderNode.pitch(), idealNode.pitch());
            float roll = Mth.rotLerp(blend, rawRenderNode.roll(), idealNode.roll());
            return new PathNode(rawRenderNode.feature(), pos, yaw, pitch, roll);
        }
        return rawRenderNode;
    }

    private static class ClientBatModel {
        private static ModelPart ROOT_MODEL = null;
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/bat.png");

        public static ModelPart getModel() {
            if (ROOT_MODEL == null) {
                ROOT_MODEL = BatModel.createBodyLayer().bakeRoot();
            }
            return ROOT_MODEL;
        }
        public static ResourceLocation getTexture() { return TEXTURE; }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        PathNode visualNode = getVisualRenderNode(this, partialTick, renderNode);
        poseStack.pushPose();

        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));
        poseStack.translate(0, 0, 1.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));

        ModelPart model = ClientBatModel.getModel();
        model.xRot = (float) Math.PI / 2f;

        float time = this.age + partialTick;
        ModelPart body = model.getChild("body");
        ModelPart head = model.getChild("head");
        ModelPart rightWing = body.getChild("right_wing");
        ModelPart leftWing = body.getChild("left_wing");
        ModelPart rightWingTip = rightWing.getChild("right_wing_tip");
        ModelPart leftWingTip = leftWing.getChild("left_wing_tip");

        body.xRot = -(float) Math.PI / 5f;
        head.xRot = -(float) Math.PI / 2f;

        float flapSpeed = 0.5f;
        float flapMag = 0.85f;
        rightWing.yRot = Mth.cos(time * flapSpeed) * flapMag + 0.1f;
        leftWing.yRot = -rightWing.yRot;
        rightWingTip.yRot = Mth.cos(time * flapSpeed) * flapMag * 0.5f;
        leftWingTip.yRot = -rightWingTip.yRot;

        int bloodRedTint = FastColor.ARGB32.color(255, 255, 90, 90);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(ClientBatModel.getTexture()));
        model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, bloodRedTint);

        poseStack.popPose();
    }

    @Override public int getTrailTimer() { return trailTimer; }
    @Override public float getTrailMaxRadius() { return 0.1f; }
    @Override public int getTrailColorRGB(float progress) { return 0xE22A2A; }
    @Override public int getTrailResolution() { return 16; }
    @Override public float getTrailFadeOut(float progress) { return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0); }

    @Override
    public ServantType<? extends Servant> getType() {
        return ServantRegister.SanguineBat.get();
    }
}