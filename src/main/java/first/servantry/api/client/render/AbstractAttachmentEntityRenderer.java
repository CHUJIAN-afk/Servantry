package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.renderConfig.*;
import first.servantry.api.client.renderType.TrailRenderType;
import first.servantry.api.entity.AttachmentEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

/**
 * 附件实体渲染器抽象基类。
 * <p>
 * 提供完整的拖尾渲染和本体渲染框架，支持强类型配置分离。
 * </p>
 *
 * @param <T> 附件实体类型
 * @see RenderContext
 * @see TrailConfig
 * @see ModelConfig
 */
public abstract class AbstractAttachmentEntityRenderer<T extends AttachmentEntity> implements IAttachmentEntityRenderer<T> {

    // ===================== 核心抽象方法 =====================

    /**
     * 为指定附件实体创建渲染上下文。
     */
    protected abstract RenderContext<T> createContext(T entity);

    /**
     * 渲染附件实体本体。
     */
    protected void renderEntity(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<T> config) {
    }

    // ===================== 主渲染入口 =====================

    @Override
    public void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        RenderContext<T> config = createContext(entity);
        if (config == null) return;

        poseStack.pushPose();
        PathNode visualNode = config.model.visualNodeFunction.getVisualNode(entity, partialTick, renderNode);

        AlphaBufferSource alphaBufferSource = new AlphaBufferSource(bufferSource);
        alphaBufferSource.setAlpha(calculateFirstPersonAlpha(config, visualNode, partialTick));

        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        if (config.hasTrail()) {
            renderTrail(entity, poseStack, alphaBufferSource, partialTick, visualNode, config);
        }

        renderEntityModel(entity, poseStack, alphaBufferSource, visualNode, config);
        poseStack.popPose();
    }

    // ===================== 模型渲染 =====================

    private void renderEntityModel(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode node, RenderContext<T> config) {
        ModelConfig<T> model = config.model;
        poseStack.pushPose();

        poseStack.mulPose(Axis.YN.rotationDegrees(node.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(node.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(node.roll()));

        poseStack.mulPose(Axis.YN.rotationDegrees(model.yawOffset));
        poseStack.mulPose(Axis.XP.rotationDegrees(model.pitchOffset));
        poseStack.mulPose(Axis.ZP.rotationDegrees(model.rollOffset));

        poseStack.scale(model.scale, model.scale, model.scale);
        poseStack.translate(model.translateX, model.translateY, model.translateZ);

        renderEntity(entity, poseStack, bufferSource, node, config);
        poseStack.popPose();
    }

    // ===================== 拖尾渲染分发 =====================

    protected RenderType getTrailRenderType() {
        return TrailRenderType.getTrail();
    }

    @SuppressWarnings("unchecked")
    private void renderTrail(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, RenderContext<T> config) {
        TrailConfig<T, ?> trail = config.trail;
        switch (trail.getType()) {
            case CONE ->
                    renderConeTrail(entity, poseStack, bufferSource, partialTick, visualNode, (ConeTrailConfig<T>) trail);
            case DROPLET ->
                    renderDropletTrail(entity, poseStack, bufferSource, partialTick, visualNode, (DropletTrailConfig<T>) trail);
            case RIBBON ->
                    renderRibbonTrail(entity, poseStack, bufferSource, partialTick, visualNode, (RibbonTrailConfig<T>) trail);
            default -> {}
        }
    }

    // ===================== 圆锥拖尾渲染 =====================

    private static final Map<Integer, CircleVertexCache> CIRCLE_CACHE = new HashMap<>();

    private void renderConeTrail(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, ConeTrailConfig<T> trail) {
        List<InterpolatedNode> smoothNodes = buildSmoothNodes(entity, visualNode, trail);
        if (smoothNodes.size() < 2) return;

        VertexConsumer consumer = bufferSource.getBuffer(getTrailRenderType());
        Matrix4f pose = poseStack.last().pose();
        CircleVertexCache cache = CIRCLE_CACHE.computeIfAbsent(trail.resolution, CircleVertexCache::new);

        Player owner = entity.getOwner();
        float timeShift = owner != null ? (owner.tickCount + partialTick) * 0.015f : 0f;

        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        Vector3f currV1 = new Vector3f(), currV2 = new Vector3f();
        Vector3f prevV1 = new Vector3f(), prevV2 = new Vector3f();

        for (int i = 0; i < nodeCount - 1; i++) {
            InterpolatedNode curr = smoothNodes.get(i);
            InterpolatedNode prev = smoothNodes.get(i + 1);

            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            float currFade = trail.fadeOut.getFade(currProgress);
            float prevFade = trail.fadeOut.getFade(prevProgress);
            float currRadius = trail.maxRadius * (trail.minRadiusRatio + (1 - trail.minRadiusRatio) * currFade);
            float prevRadius = trail.maxRadius * (trail.minRadiusRatio + (1 - trail.minRadiusRatio) * prevFade);

            int currColor = trail.colorFunction.getColor(entity, currProgress, timeShift);
            int prevColor = trail.colorFunction.getColor(entity, prevProgress, timeShift);

            int currAlpha = Math.round(currFade * 200);
            int prevAlpha = Math.round(prevFade * 200);
            int currARGB = FastColor.ARGB32.color(currAlpha, (currColor >> 16) & 0xFF, (currColor >> 8) & 0xFF, currColor & 0xFF);
            int prevARGB = FastColor.ARGB32.color(prevAlpha, (prevColor >> 16) & 0xFF, (prevColor >> 8) & 0xFF, prevColor & 0xFF);

            Vec3 currRel = curr.pos.subtract(renderPos);
            Vec3 prevRel = prev.pos.subtract(renderPos);

            for (int j = 0; j < trail.resolution; j++) {
                float cos1 = cache.cos(j), sin1 = cache.sin(j);
                float cos2 = cache.cos(j + 1), sin2 = cache.sin(j + 1);

                currV1.set(cos1 * currRadius, sin1 * currRadius, 0).rotate(curr.rot);
                currV2.set(cos2 * currRadius, sin2 * currRadius, 0).rotate(curr.rot);
                prevV1.set(cos1 * prevRadius, sin1 * prevRadius, 0).rotate(prev.rot);
                prevV2.set(cos2 * prevRadius, sin2 * prevRadius, 0).rotate(prev.rot);

                emitQuad(consumer, pose,
                        (float) currRel.x + currV1.x, (float) currRel.y + currV1.y, (float) currRel.z + currV1.z,
                        (float) currRel.x + currV2.x, (float) currRel.y + currV2.y, (float) currRel.z + currV2.z,
                        (float) prevRel.x + prevV2.x, (float) prevRel.y + prevV2.y, (float) prevRel.z + prevV2.z,
                        (float) prevRel.x + prevV1.x, (float) prevRel.y + prevV1.y, (float) prevRel.z + prevV1.z,
                        currARGB, prevARGB);
            }
        }
    }

    // ===================== 水滴拖尾渲染 =====================

    private void renderDropletTrail(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, DropletTrailConfig<T> trail) {
        List<InterpolatedNode> smoothNodes = buildSmoothNodes(entity, visualNode, trail);
        if (smoothNodes.size() < 2) return;

        VertexConsumer consumer = bufferSource.getBuffer(getTrailRenderType());
        Matrix4f pose = poseStack.last().pose();
        CircleVertexCache cache = CIRCLE_CACHE.computeIfAbsent(trail.resolution, CircleVertexCache::new);

        Player owner = entity.getOwner();
        float timeShift = owner != null ? (owner.tickCount + partialTick) * 0.015f : 0f;

        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        Vector3f currV1 = new Vector3f(), currV2 = new Vector3f();
        Vector3f prevV1 = new Vector3f(), prevV2 = new Vector3f();

        for (int i = 0; i < nodeCount - 1; i++) {
            InterpolatedNode curr = smoothNodes.get(i);
            InterpolatedNode prev = smoothNodes.get(i + 1);

            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            float currFade = trail.fadeOut.getFade(currProgress);
            float prevFade = trail.fadeOut.getFade(prevProgress);
            float currRadius = trail.maxRadius * (trail.minRadiusRatio + (1 - trail.minRadiusRatio) * currFade);
            float prevRadius = trail.maxRadius * (trail.minRadiusRatio + (1 - trail.minRadiusRatio) * prevFade);

            int currColor = trail.colorFunction.getColor(entity, currProgress, timeShift);
            int prevColor = trail.colorFunction.getColor(entity, prevProgress, timeShift);

            int currAlpha = Math.round(currFade * 200);
            int prevAlpha = Math.round(prevFade * 200);
            int currARGB = FastColor.ARGB32.color(currAlpha, (currColor >> 16) & 0xFF, (currColor >> 8) & 0xFF, currColor & 0xFF);
            int prevARGB = FastColor.ARGB32.color(prevAlpha, (prevColor >> 16) & 0xFF, (prevColor >> 8) & 0xFF, prevColor & 0xFF);

            Vec3 currRel = curr.pos.subtract(renderPos);
            Vec3 prevRel = prev.pos.subtract(renderPos);

            for (int j = 0; j < trail.resolution; j++) {
                float cos1 = cache.cos(j), sin1 = cache.sin(j);
                float cos2 = cache.cos(j + 1), sin2 = cache.sin(j + 1);

                currV1.set(cos1 * currRadius, sin1 * currRadius, 0).rotate(curr.rot);
                currV2.set(cos2 * currRadius, sin2 * currRadius, 0).rotate(curr.rot);
                prevV1.set(cos1 * prevRadius, sin1 * prevRadius, 0).rotate(prev.rot);
                prevV2.set(cos2 * prevRadius, sin2 * prevRadius, 0).rotate(prev.rot);

                emitQuad(consumer, pose,
                        (float) currRel.x + currV1.x, (float) currRel.y + currV1.y, (float) currRel.z + currV1.z,
                        (float) currRel.x + currV2.x, (float) currRel.y + currV2.y, (float) currRel.z + currV2.z,
                        (float) prevRel.x + prevV2.x, (float) prevRel.y + prevV2.y, (float) prevRel.z + prevV2.z,
                        (float) prevRel.x + prevV1.x, (float) prevRel.y + prevV1.y, (float) prevRel.z + prevV1.z,
                        currARGB, prevARGB);
            }
        }

        // 渲染头部半球
        InterpolatedNode headNode = smoothNodes.getFirst();
        float headFade = trail.fadeOut.getFade(0);
        float headRadius = trail.maxRadius * headFade;
        int headColor = trail.colorFunction.getColor(entity, 0, timeShift);
        int headAlpha = Math.round(headFade * 200);
        int headARGB = FastColor.ARGB32.color(headAlpha, (headColor >> 16) & 0xFF, (headColor >> 8) & 0xFF, headColor & 0xFF);

        Vec3 headRel = headNode.pos.subtract(renderPos);
        int hemisphereSegments = Math.max(2, trail.resolution / 2);

        for (int lat = 0; lat < hemisphereSegments; lat++) {
            float latAngle1 = (float) (Math.PI / 2 * lat / hemisphereSegments);
            float latAngle2 = (float) (Math.PI / 2 * (lat + 1) / hemisphereSegments);
            float r1 = (float) Math.cos(latAngle1) * headRadius;
            float r2 = (float) Math.cos(latAngle2) * headRadius;
            float h1 = (float) Math.sin(latAngle1) * headRadius;
            float h2 = (float) Math.sin(latAngle2) * headRadius;

            for (int lon = 0; lon < trail.resolution; lon++) {
                float cos1 = cache.cos(lon), sin1 = cache.sin(lon);
                float cos2 = cache.cos(lon + 1), sin2 = cache.sin(lon + 1);

                Vector3f v1 = new Vector3f(cos1 * r1, sin1 * r1, h1).rotate(headNode.rot);
                Vector3f v2 = new Vector3f(cos2 * r1, sin2 * r1, h1).rotate(headNode.rot);
                Vector3f v3 = new Vector3f(cos2 * r2, sin2 * r2, h2).rotate(headNode.rot);
                Vector3f v4 = new Vector3f(cos1 * r2, sin1 * r2, h2).rotate(headNode.rot);

                emitQuad(consumer, pose,
                        (float) headRel.x + v1.x, (float) headRel.y + v1.y, (float) headRel.z + v1.z,
                        (float) headRel.x + v2.x, (float) headRel.y + v2.y, (float) headRel.z + v2.z,
                        (float) headRel.x + v3.x, (float) headRel.y + v3.y, (float) headRel.z + v3.z,
                        (float) headRel.x + v4.x, (float) headRel.y + v4.y, (float) headRel.z + v4.z,
                        headARGB, headARGB);
            }
        }
    }

    // ===================== 丝带拖尾渲染 =====================

    private void renderRibbonTrail(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, RibbonTrailConfig<T> trail) {
        List<InterpolatedNode> smoothNodes = buildSmoothNodes(entity, visualNode, trail);
        if (smoothNodes.size() < 2) return;

        VertexConsumer consumer = bufferSource.getBuffer(getTrailRenderType());
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose last = poseStack.last();

        Player owner = entity.getOwner();
        float timeShift = owner != null ? (owner.tickCount + partialTick) * 0.015f : 0f;

        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        for (int i = 0; i < nodeCount - 1; i++) {
            InterpolatedNode curr = smoothNodes.get(i);
            InterpolatedNode prev = smoothNodes.get(i + 1);

            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            float currScale = Math.max(0.0f, 1.0f - currProgress);
            float prevScale = Math.max(0.0f, 1.0f - prevProgress);

            float height = trail.width;
            float baseWidth = trail.diamondSize;

            Vector3f currTip = new Vector3f(0, 0, height).rotate(curr.rot);
            Vector3f currLeft = new Vector3f(-baseWidth * 0.5f * currScale, 0, 0).rotate(curr.rot);
            Vector3f currRight = new Vector3f(baseWidth * 0.5f * currScale, 0, 0).rotate(curr.rot);

            Vector3f prevTip = new Vector3f(0, 0, height).rotate(prev.rot);
            Vector3f prevLeft = new Vector3f(-baseWidth * 0.5f * prevScale, 0, 0).rotate(prev.rot);
            Vector3f prevRight = new Vector3f(baseWidth * 0.5f * prevScale, 0, 0).rotate(prev.rot);

            int currColorRGB = trail.colorFunction.getColor(entity, currProgress, timeShift);
            int prevColorRGB = trail.colorFunction.getColor(entity, prevProgress, timeShift);
            float currAlphaBoost = trail.tipAlphaBoost.getBoost(entity, currProgress);
            float currBrightBoost = trail.tipBrightnessBoost.getBoost(entity, currProgress);
            float prevAlphaBoost = trail.tipAlphaBoost.getBoost(entity, prevProgress);
            float prevBrightBoost = trail.tipBrightnessBoost.getBoost(entity, prevProgress);

            int currR = Math.min(255, Math.round(((currColorRGB >> 16) & 0xFF) * currBrightBoost));
            int currG = Math.min(255, Math.round(((currColorRGB >> 8) & 0xFF) * currBrightBoost));
            int currB = Math.min(255, Math.round((currColorRGB & 0xFF) * currBrightBoost));
            int prevR = Math.min(255, Math.round(((prevColorRGB >> 16) & 0xFF) * prevBrightBoost));
            int prevG = Math.min(255, Math.round(((prevColorRGB >> 8) & 0xFF) * prevBrightBoost));
            int prevB = Math.min(255, Math.round((prevColorRGB & 0xFF) * prevBrightBoost));

            int currTipAlpha = Math.min(255, Math.round(currScale * 0.1f * 255 * currAlphaBoost));
            int currBaseAlpha = Math.min(255, Math.round(Math.max(0f, 1.0f - currProgress * 2.5f) * 0.04f * 255 * currAlphaBoost));
            int prevTipAlpha = Math.min(255, Math.round(prevScale * 0.1f * 255 * prevAlphaBoost));
            int prevBaseAlpha = Math.min(255, Math.round(Math.max(0f, 1.0f - prevProgress * 2.5f) * 0.04f * 255 * prevAlphaBoost));

            int currTipColor = FastColor.ARGB32.color(currTipAlpha, currR, currG, currB);
            int currBaseColor = FastColor.ARGB32.color(currBaseAlpha, currR, currG, currB);
            int prevTipColor = FastColor.ARGB32.color(prevTipAlpha, prevR, prevG, prevB);
            int prevBaseColor = FastColor.ARGB32.color(prevBaseAlpha, prevR, prevG, prevB);

            Vec3 currRel = curr.pos.subtract(renderPos);
            Vec3 prevRel = prev.pos.subtract(renderPos);

            emitTriangleStrip(consumer, pose, last, currRel, prevRel,
                    currTip, prevTip, currLeft, prevLeft, currRight, prevRight,
                    currTipColor, prevTipColor, currBaseColor, prevBaseColor);
        }
    }

    // ===================== 节点插值计算 =====================

    private List<InterpolatedNode> buildSmoothNodes(T entity, PathNode visualNode, TrailConfig<T, ?> trail) {
        ArrayList<PathNode> history = entity.getHistoryNodes();
        int actualLength = Math.min(history.size(), trail.historyLength);
        if (actualLength < 2) return List.of();

        PathNode[] nodes = new PathNode[actualLength];
        Iterator<PathNode> iterator = history.iterator();
        for (int i = 0; i < actualLength; i++) {
            nodes[i] = iterator.next();
        }
        nodes[0] = new PathNode(visualNode.pos(), visualNode.yaw(), visualNode.pitch(), visualNode.roll());

        int endIndex = nodes.length - 1;
        int startIndex = Math.max(0, Math.min(trail.startIndex, endIndex - 1));

        int segments = trail.segmentsPerNode;
        List<InterpolatedNode> result = new ArrayList<>((endIndex - startIndex) * segments + 1);
        Quaternionf tempQuat = new Quaternionf();

        for (int i = startIndex; i < endIndex; i++) {
            PathNode p0 = nodes[Math.max(i - 1, startIndex)];
            PathNode p1 = nodes[i];
            PathNode p2 = nodes[i + 1];
            PathNode p3 = nodes[Math.min(i + 2, endIndex)];

            Quaternionf q1 = eulerToQuaternion(p1.yaw(), p1.pitch(), p1.roll());
            Quaternionf q2 = eulerToQuaternion(p2.yaw(), p2.pitch(), p2.roll());

            for (int j = 0; j < segments; j++) {
                float t = (float) j / segments;
                InterpolatedNode node = catmullRomInterpolate(p0, p1, p2, p3, q1, q2, t, tempQuat);
                result.add(node);
            }
        }

        PathNode lastNode = nodes[endIndex];
        result.add(new InterpolatedNode(lastNode.pos(), eulerToQuaternion(lastNode.yaw(), lastNode.pitch(), lastNode.roll())));

        return result;
    }

    private InterpolatedNode catmullRomInterpolate(PathNode p0, PathNode p1, PathNode p2, PathNode p3,
                                                   Quaternionf q1, Quaternionf q2, float t, Quaternionf tempQuat) {
        float t2 = t * t, t3 = t2 * t;
        float f0 = -0.5f * t3 + t2 - 0.5f * t;
        float f1 = 1.5f * t3 - 2.5f * t2 + 1.0f;
        float f2 = -1.5f * t3 + 2.0f * t2 + 0.5f * t;
        float f3 = 0.5f * t3 - 0.5f * t2;

        Vec3 pos = new Vec3(
                p0.pos().x * f0 + p1.pos().x * f1 + p2.pos().x * f2 + p3.pos().x * f3,
                p0.pos().y * f0 + p1.pos().y * f1 + p2.pos().y * f2 + p3.pos().y * f3,
                p0.pos().z * f0 + p1.pos().z * f1 + p2.pos().z * f2 + p3.pos().z * f3
        );

        tempQuat.set(q1).slerp(q2, t);

        return new InterpolatedNode(pos, new Quaternionf(tempQuat));
    }

    // ===================== 顶点发射辅助方法 =====================

    private void emitQuad(VertexConsumer consumer, Matrix4f pose,
                          float x1, float y1, float z1,
                          float x2, float y2, float z2,
                          float x3, float y3, float z3,
                          float x4, float y4, float z4,
                          int color1, int color2) {
        consumer.addVertex(pose, x1, y1, z1).setColor(color1).setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        consumer.addVertex(pose, x2, y2, z2).setColor(color1).setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        consumer.addVertex(pose, x3, y3, z3).setColor(color2).setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        consumer.addVertex(pose, x4, y4, z4).setColor(color2).setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
    }

    private void emitTriangleStrip(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose last,
                                   Vec3 currRel, Vec3 prevRel,
                                   Vector3f currTip, Vector3f prevTip,
                                   Vector3f currLeft, Vector3f prevLeft,
                                   Vector3f currRight, Vector3f prevRight,
                                   int currTipColor, int prevTipColor,
                                   int currBaseColor, int prevBaseColor) {
        // 左侧三角形正面
        consumer.addVertex(pose, (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z)
                .setColor(currTipColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) currRel.x + currLeft.x, (float) currRel.y + currLeft.y, (float) currRel.z + currLeft.z)
                .setColor(currBaseColor).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevLeft.x, (float) prevRel.y + prevLeft.y, (float) prevRel.z + prevLeft.z)
                .setColor(prevBaseColor).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z)
                .setColor(prevTipColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        // 左侧三角形反面
        consumer.addVertex(pose, (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z)
                .setColor(currTipColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z)
                .setColor(prevTipColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevLeft.x, (float) prevRel.y + prevLeft.y, (float) prevRel.z + prevLeft.z)
                .setColor(prevBaseColor).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) currRel.x + currLeft.x, (float) currRel.y + currLeft.y, (float) currRel.z + currLeft.z)
                .setColor(currBaseColor).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);

        // 右侧三角形正面
        consumer.addVertex(pose, (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z)
                .setColor(currTipColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z)
                .setColor(prevTipColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevRight.x, (float) prevRel.y + prevRight.y, (float) prevRel.z + prevRight.z)
                .setColor(prevBaseColor).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) currRel.x + currRight.x, (float) currRel.y + currRight.y, (float) currRel.z + currRight.z)
                .setColor(currBaseColor).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        // 右侧三角形反面
        consumer.addVertex(pose, (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z)
                .setColor(currTipColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) currRel.x + currRight.x, (float) currRel.y + currRight.y, (float) currRel.z + currRight.z)
                .setColor(currBaseColor).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevRight.x, (float) prevRel.y + prevRight.y, (float) prevRel.z + prevRight.z)
                .setColor(prevBaseColor).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z)
                .setColor(prevTipColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
    }

    // ===================== 数学辅助方法 =====================

    private Quaternionf eulerToQuaternion(float yaw, float pitch, float roll) {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(-yaw))
                .rotateX((float) Math.toRadians(pitch))
                .rotateZ((float) Math.toRadians(roll));
    }

    private float calculateFirstPersonAlpha(RenderContext<T> config, PathNode visualNode, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson()) {
            return 1.0f;
        }

        Vec3 entityPos = visualNode.pos();
        Vec3 eyePos = player.getEyePosition(partialTick);
        double distance = entityPos.distanceTo(eyePos);

        float minDistance = 0.5f * config.model.alphaDistanceFactor;
        float maxDistance = 4.0f * config.model.alphaDistanceFactor;

        if (distance <= minDistance) {
            return 0.0f;
        }
        if (distance >= maxDistance) {
            return 1.0f;
        }

        float alpha = (float) ((distance - minDistance) / (maxDistance - minDistance));
        return Math.max(0.102f, Math.min(1.0f, alpha));
    }

    // ===================== 内部数据类 =====================

    private record InterpolatedNode(Vec3 pos, Quaternionf rot) {}

    private static final class CircleVertexCache {
        private final float[] cos, sin;

        CircleVertexCache(int resolution) {
            cos = new float[resolution + 1];
            sin = new float[resolution + 1];
            float delta = (float) (2.0 * Math.PI / resolution);
            for (int i = 0; i <= resolution; i++) {
                float angle = i * delta;
                cos[i] = (float) Math.cos(angle);
                sin[i] = (float) Math.sin(angle);
            }
        }

        float cos(int i) { return cos[i]; }
        float sin(int i) { return sin[i]; }
    }
}
