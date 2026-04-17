package first.servantry.common.servent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.PathNode;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.IConeTrailRenderer;
import first.servantry.api.servant.IDamagingOnCollide;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class SanguineBat extends Servant implements IDamagingOnCollide, IConeTrailRenderer {

    public enum SanguineState {
        IDLE, PREP, ATTACKING, RETURNING, RETURN_STAY
    }

    private SanguineState state = SanguineState.IDLE;
    private int stateTick = 0;
    private int trailTimer = 0;

    // 用于动画驱动的刻数
    private int age = 0;
    private final Set<Integer> swingHitTargets = new HashSet<>();

    private Vec3 lastTargetPos = null;
    private Vec3 lastIdlePos = null;

    // 渲染拟合参数，用于掩盖高速移动时的闪烁
    private float idleBlend = 0f;
    private float idleBlendO = 0f;

    // 椭圆攻击参数缓存
    private int attackTotalTicks = 24;

    public SanguineBat(PathNode node) {
        super(node);
    }

    @Override
    public float getBaseDamage() {
        return 3.5f;
    }

    @Override
    public float getBaseKnockback() {
        return 0.1f;
    }

    @Override
    public AABB getHitbox() {
        return new AABB(-0.1, -0.1, -0.1, 0.1, 0.1, 0.1);
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

        if (!getHistoryNodes().isEmpty() && "hit_clear".equals(getHistoryNodes().getFirst().feature())) {
            swingHitTargets.clear();
        }

        Player owner = getOwner();
        if (owner != null) {
            if (owner.level().isClientSide()) {
                clientTick();
            } else {
                serverTickAI(owner);
            }
        }
    }

    private void clientTick() {
        this.age++; // 累加动画刻

        if (state == SanguineState.ATTACKING || state == SanguineState.RETURNING) {
            trailTimer = 15;
        } else {
            trailTimer = 0;
        }

        idleBlendO = idleBlend;
        if (state == SanguineState.IDLE || state == SanguineState.RETURN_STAY) {
            idleBlend = Math.min(1.0f, idleBlend + 0.15f);
        } else {
            idleBlend = Math.max(0.0f, idleBlend - 0.4f);
        }
    }

    private LivingEntity verifyTarget(Player owner) {
        if (state != SanguineState.IDLE && state != SanguineState.PREP) {
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
        List<LivingEntity> potentialTargets = data.getNearbyTargets(owner, this, 24.0, true);

        if (potentialTargets.isEmpty()) {
            setTarget(null);
            return null;
        }

        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity e : potentialTargets) {
            double score = e.distanceToSqr(this.getPos());
            if (score < bestScore) {
                bestScore = score;
                bestTarget = e;
            }
        }

        setTarget(bestTarget);
        return bestTarget;
    }

    private void serverTickAI(Player owner) {
        LivingEntity currentTarget = verifyTarget(owner);

        if (this.isExecutingPath()) {
            if (state == SanguineState.ATTACKING) {
                int remaining = this.getPathQueue().size();
                boolean isFirstHalf = remaining >= this.attackTotalTicks / 2;

                if (currentTarget == null) {
                    if (isFirstHalf) {
                        this.setPath(Collections.emptyList());
                        state = SanguineState.RETURNING;
                        generateReturn(owner);
                        return;
                    }
                }
                correctAttackPath(owner, currentTarget);
            } else if (state == SanguineState.RETURNING) {
                correctReturnPath(owner);
            }
            return;
        }

        if (state == SanguineState.ATTACKING || state == SanguineState.RETURNING) {
            state = SanguineState.RETURN_STAY;
            stateTick = 0;
        } else if (state == SanguineState.RETURN_STAY) {
            stateTick++;
            if (stateTick >= 1) {
                state = SanguineState.IDLE;
            } else {
                handleIdle(owner);
            }
        } else if (state == SanguineState.PREP) {
            if (currentTarget != null) {
                state = SanguineState.ATTACKING;
                generateAttack(owner, currentTarget);
            } else {
                state = SanguineState.IDLE;
            }
        } else if (state == SanguineState.IDLE) {
            if (currentTarget != null) {
                state = SanguineState.PREP;
            } else {
                handleIdle(owner);
            }
        }
    }

    private void correctAttackPath(Player owner, LivingEntity target) {
        Vec3 currentIdlePos = getInterpolatedIdlePos(owner, 1.0f).pos();
        Vec3 dIdle = currentIdlePos.subtract(lastIdlePos);

        Vec3 currentTargetPos = lastTargetPos;
        Vec3 dTarget = Vec3.ZERO;

        if (target != null) {
            currentTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
            dTarget = currentTargetPos.subtract(lastTargetPos);
        }

        LinkedList<PathNode> q = this.getPathQueue();
        int remaining = q.size();
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

        lastTargetPos = currentTargetPos;
        lastIdlePos = currentIdlePos;
    }

    private void correctReturnPath(Player owner) {
        if (lastIdlePos == null) return;
        Vec3 currentIdlePos = getInterpolatedIdlePos(owner, 1.0f).pos();
        Vec3 dIdle = currentIdlePos.subtract(lastIdlePos);

        LinkedList<PathNode> q = this.getPathQueue();
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
        lastIdlePos = currentIdlePos;
    }

    private void generateAttack(Player owner, LivingEntity target) {
        Vec3 start = this.getPos();
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

        this.setPath(nodes);
        this.attackTotalTicks = this.getPathQueue().size();
        this.lastTargetPos = targetPos;
        this.lastIdlePos = getInterpolatedIdlePos(owner, 1.0f).pos();
    }

    private void generateReturn(Player owner) {
        Vec3 start = this.getPos();
        PathNode idleNode = getInterpolatedIdlePos(owner, 1.0f);
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
            float roll = Mth.lerp(t, this.getRoll(), 0f);

            nodes.add(new PathNode("", pos, yaw, pitch, roll));
        }
        this.setPath(nodes);
        this.lastIdlePos = end;
    }

    private void handleIdle(Player owner) {
        PathNode idleTarget = getInterpolatedIdlePos(owner, 1.0f);
        this.getPathQueue().clear();
        this.getPathQueue().add(idleTarget);
        this.lastIdlePos = idleTarget.pos();
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

        public static ResourceLocation getTexture() {
            return TEXTURE;
        }
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

        float flapSpeed = 1.8f;
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

    @Override
    public int getTrailTimer() { return trailTimer; }

    @Override
    public float getTrailMaxRadius() { return 0.1f; }

    @Override
    public int getTrailColorRGB(float progress) {
        return 0xE22A2A;
    }

    @Override
    public int getTrailResolution() {
        return 16;
    }

    @Override
    public float getTrailFadeOut(float progress) {
        return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0);
    }

    @Override
    public void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode visualRenderNode, List<TrailNode> smoothNodes) {
        IConeTrailRenderer.super.drawTrailVertices(poseStack, bufferSource, partialTick, servant, visualRenderNode, smoothNodes);

        if (!smoothNodes.isEmpty()) {
            VertexConsumer consumer = bufferSource.getBuffer(TrailRenderType.getTrail());
            Matrix4f pose = poseStack.last().pose();
            Vec3 renderPos = visualRenderNode.pos();
            Vec3 headPos = smoothNodes.getFirst().pos.subtract(renderPos);

            float radius = getTrailMaxRadius();
            int colorRGB = getTrailColorRGB(0f);
            int r = (colorRGB >> 16) & 0xFF;
            int g = (colorRGB >> 8) & 0xFF;
            int b = colorRGB & 0xFF;
            int colorVal = FastColor.ARGB32.color(200, r, g, b);

            drawSphere(consumer, pose, headPos, radius, colorVal);
        }
    }

    private void drawSphere(VertexConsumer consumer, Matrix4f pose, Vec3 center, float radius, int color) {
        int rings = 8;
        int sectors = 8;
        for (int i = 0; i < rings; i++) {
            float phi1 = (float) (Math.PI * (float) i / rings);
            float phi2 = (float) (Math.PI * (float) (i + 1) / rings);
            for (int j = 0; j < sectors; j++) {
                float theta1 = (float) (2.0 * Math.PI * (float) j / sectors);
                float theta2 = (float) (2.0 * Math.PI * (float) (j + 1) / sectors);

                Vector3f v1 = getSphereVertex(center, radius, theta1, phi1);
                Vector3f v2 = getSphereVertex(center, radius, theta2, phi1);
                Vector3f v3 = getSphereVertex(center, radius, theta2, phi2);
                Vector3f v4 = getSphereVertex(center, radius, theta1, phi2);

                consumer.addVertex(pose, v1.x(), v1.y(), v1.z()).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, v2.x(), v2.y(), v2.z()).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, v3.x(), v3.y(), v3.z()).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, v4.x(), v4.y(), v4.z()).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            }
        }
    }

    private Vector3f getSphereVertex(Vec3 center, float radius, float theta, float phi) {
        float x = (float) (center.x + radius * Math.sin(phi) * Math.cos(theta));
        float y = (float) (center.y + radius * Math.cos(phi));
        float z = (float) (center.z + radius * Math.sin(phi) * Math.sin(theta));
        return new Vector3f(x, y, z);
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(state);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.state = buf.readEnum(SanguineState.class);
    }

    @Override
    public ServantType<? extends Servant> getType() {
        return ServantRegister.SanguineBat.get();
    }
}