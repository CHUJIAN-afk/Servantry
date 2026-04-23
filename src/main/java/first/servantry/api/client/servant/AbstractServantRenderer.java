package first.servantry.api.client.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.PathNode;
import first.servantry.api.client.renderType.TrailRenderType;
import first.servantry.api.servant.Servant;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 仆从渲染器抽象基类。
 * <p>
 * 实现类只需重写 {@link #createConfig(Servant)} 方法返回渲染配置，
 * 即可获得完整的拖尾和本体渲染效果。
 * </p>
 * <p>
 * 对于高级自定义需求，可重写：
 * <ul>
 *   <li>{@link #renderModel(Servant, PoseStack, MultiBufferSource, PathNode, ServantRenderConfig)} - 自定义本体渲染</li>
 *   <li>{@link #renderTrail(Servant, PoseStack, MultiBufferSource, float, PathNode, ServantRenderConfig)} - 自定义拖尾渲染</li>
 * </ul>
 * </p>
 *
 * @param <T> 仆从类型
 */
public abstract class AbstractServantRenderer<T extends Servant> implements IServantRenderer<T> {

    // ===================== 核心抽象方法 =====================

    /**
     * 为指定仆从创建渲染配置。
     * <p>
     * 这是实现类唯一需要重写的方法。返回的配置决定了拖尾类型、颜色、大小等所有渲染参数。
     * </p>
     *
     * @param servant 仆从实例
     * @return 渲染配置
     */
    protected abstract ServantRenderConfig<T> createConfig(T servant);

    // ===================== 主渲染入口 =====================

    @Override
    public void render(T servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        ServantRenderConfig<T> config = createConfig(servant);
        if (config == null) return;

        // 获取视觉节点（支持插值）
        PathNode visualNode = config.visualNodeFunction.getVisualNode(servant, partialTick, renderNode);
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);

        // 先渲染拖尾（在模型后面）
        if (config.trailType != ServantRenderConfig.TrailType.NONE && config.trailTimer > 0) {
            renderTrail(servant, poseStack, bufferSource, partialTick, visualNode, config);
        }

        // 再渲染本体模型
        renderModel(servant, poseStack, bufferSource, visualNode, config);

        poseStack.popPose();
    }

    // ===================== 本体渲染 =====================

    /**
     * 渲染仆从本体模型。
     * <p>
     * 默认实现应用缩放和旋转偏移，然后调用 {@link #renderModelItem(T, PoseStack, MultiBufferSource, PathNode, ServantRenderConfig)}。
     * 子类可重写此方法实现自定义渲染逻辑。
     * </p>
     */
    protected void renderModel(T servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, ServantRenderConfig<T> config) {
        poseStack.pushPose();

        // 应用旋转
        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));
        poseStack.mulPose(Axis.YN.rotationDegrees(config.modelYawOffset));
        poseStack.mulPose(Axis.XP.rotationDegrees(config.modelPitchOffset));
        poseStack.mulPose(Axis.ZP.rotationDegrees(config.modelRollOffset));

        // 应用缩放
        poseStack.scale(config.modelScale, config.modelScale, config.modelScale);

        // 渲染物品模型
        renderModelItem(servant, poseStack, bufferSource, visualNode, config);

        poseStack.popPose();
    }

    /**
     * 渲染仆从的物品模型。
     * <p>
     * 子类应重写此方法以渲染具体的物品模型。
     * 默认实现为空，子类需要使用 Minecraft.getItemRenderer() 渲染物品。
     * </p>
     */
    protected void renderModelItem(T servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, ServantRenderConfig<T> config) {
        // 子类重写
    }

    // ===================== 拖尾渲染 =====================

    /**
     * 渲染拖尾。
     * <p>
     * 根据配置的拖尾类型分发到具体的渲染方法。
     * </p>
     */
    protected void renderTrail(T servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, ServantRenderConfig<T> config) {
        switch (config.trailType) {
            case CONE -> renderConeTrail(servant, poseStack, bufferSource, partialTick, visualNode, config);
            case RIBBON -> renderRibbonTrail(servant, poseStack, bufferSource, partialTick, visualNode, config);
            default -> {}
        }
    }

    // ===================== 圆锥拖尾渲染 =====================

    private static final Map<Integer, CircleVertexCache> CIRCLE_CACHE = new HashMap<>();

    private void renderConeTrail(T servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, ServantRenderConfig<T> config) {
        LinkedList<PathNode> history = servant.getHistoryNodes();
        int actualLength = Math.min(history.size(), config.trailHistoryLength);
        if (actualLength < 3) return;

        // 准备节点数组
        PathNode[] nodes = new PathNode[actualLength];
        Iterator<PathNode> iterator = history.iterator();
        for (int i = 0; i < actualLength; i++) {
            nodes[i] = iterator.next();
        }
        nodes[0] = new PathNode(visualNode.pos(), visualNode.yaw(), visualNode.pitch(), visualNode.roll());

        int endIndex = nodes.length - 1;
        int startIndex = Math.max(0, Math.min(config.trailStartIndex, endIndex - 1));

        // Catmull-Rom 插值
        int segments = config.trailSegmentsPerNode;
        List<TrailNode> smoothNodes = new ArrayList<>((endIndex - startIndex) * segments + 1);
        Quaternionf tempQ = new Quaternionf();

        for (int i = startIndex; i < endIndex; i++) {
            PathNode p0 = nodes[Math.max(i - 1, startIndex)];
            PathNode p1 = nodes[i];
            PathNode p2 = nodes[i + 1];
            PathNode p3 = nodes[Math.min(i + 2, endIndex)];

            Quaternionf q1 = eulerToQuaternion(p1.yaw(), p1.pitch(), p1.roll());
            Quaternionf q2 = eulerToQuaternion(p2.yaw(), p2.pitch(), p2.roll());

            for (int j = 0; j < segments; j++) {
                float t = (float) j / segments;
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

                tempQ.set(q1).slerp(q2, t);
                smoothNodes.add(new TrailNode(pos, new Quaternionf(tempQ)));
            }
        }

        // 添加末尾节点
        PathNode lastNode = nodes[endIndex];
        smoothNodes.add(new TrailNode(lastNode.pos(), eulerToQuaternion(lastNode.yaw(), lastNode.pitch(), lastNode.roll())));

        // 绘制
        VertexConsumer consumer = bufferSource.getBuffer(TrailRenderType.getTrail());
        Matrix4f pose = poseStack.last().pose();
        CircleVertexCache cache = CIRCLE_CACHE.computeIfAbsent(config.trailResolution, CircleVertexCache::new);

        float timeShift = (servant.getOwner().tickCount + partialTick) * 0.015f;
        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        Vector3f cV1 = new Vector3f(), cV2 = new Vector3f(), pV1 = new Vector3f(), pV2 = new Vector3f();

        for (int i = 0; i < nodeCount - 1; i++) {
            TrailNode curr = smoothNodes.get(i);
            TrailNode prev = smoothNodes.get(i + 1);

            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            float currFade = config.trailFadeOut.getFade(currProgress);
            float prevFade = config.trailFadeOut.getFade(prevProgress);

            float currRadius = config.trailMaxRadius * currFade;
            float prevRadius = config.trailMaxRadius * prevFade;

            int currColor = config.trailColorFunction.getColor(servant, currProgress, timeShift);
            int prevColor = config.trailColorFunction.getColor(servant, prevProgress, timeShift);

            int cA = Math.round(currFade * 200);
            int pA = Math.round(prevFade * 200);

            int cColorVal = FastColor.ARGB32.color(cA, (currColor >> 16) & 0xFF, (currColor >> 8) & 0xFF, currColor & 0xFF);
            int pColorVal = FastColor.ARGB32.color(pA, (prevColor >> 16) & 0xFF, (prevColor >> 8) & 0xFF, prevColor & 0xFF);

            Vec3 cRel = curr.pos.subtract(renderPos);
            Vec3 pRel = prev.pos.subtract(renderPos);

            for (int j = 0; j < config.trailResolution; j++) {
                float cos1 = cache.cos(j), sin1 = cache.sin(j);
                float cos2 = cache.cos(j + 1), sin2 = cache.sin(j + 1);

                cV1.set(cos1 * currRadius, sin1 * currRadius, 0).rotate(curr.rot);
                cV2.set(cos2 * currRadius, sin2 * currRadius, 0).rotate(curr.rot);
                pV1.set(cos1 * prevRadius, sin1 * prevRadius, 0).rotate(prev.rot);
                pV2.set(cos2 * prevRadius, sin2 * prevRadius, 0).rotate(prev.rot);

                consumer.addVertex(pose, (float) cRel.x + cV1.x, (float) cRel.y + cV1.y, (float) cRel.z + cV1.z).setColor(cColorVal).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float) cRel.x + cV2.x, (float) cRel.y + cV2.y, (float) cRel.z + cV2.z).setColor(cColorVal).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float) pRel.x + pV2.x, (float) pRel.y + pV2.y, (float) pRel.z + pV2.z).setColor(pColorVal).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float) pRel.x + pV1.x, (float) pRel.y + pV1.y, (float) pRel.z + pV1.z).setColor(pColorVal).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            }
        }
    }

    // ===================== 丝带拖尾渲染 =====================

    private void renderRibbonTrail(T servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, ServantRenderConfig<T> config) {
        LinkedList<PathNode> history = servant.getHistoryNodes();
        int actualLength = Math.min(history.size(), config.trailHistoryLength);
        if (actualLength < 3) return;

        // 准备节点数组
        PathNode[] nodes = new PathNode[actualLength];
        Iterator<PathNode> iterator = history.iterator();
        for (int i = 0; i < actualLength; i++) {
            nodes[i] = iterator.next();
        }
        nodes[0] = new PathNode(visualNode.pos(), visualNode.yaw(), visualNode.pitch(), visualNode.roll());

        int endIndex = nodes.length - 1;
        int startIndex = Math.max(0, Math.min(config.trailStartIndex, endIndex - 1));

        // Catmull-Rom 插值
        int segments = config.trailSegmentsPerNode;
        List<RibbonNode> smoothNodes = new ArrayList<>((endIndex - startIndex) * segments + 1);
        Quaternionf tempQ = new Quaternionf();

        for (int i = startIndex; i < endIndex; i++) {
            PathNode p0 = nodes[Math.max(i - 1, startIndex)];
            PathNode p1 = nodes[i];
            PathNode p2 = nodes[i + 1];
            PathNode p3 = nodes[Math.min(i + 2, endIndex)];

            Quaternionf q1 = eulerToQuaternion(p1.yaw(), p1.pitch(), p1.roll());
            Quaternionf q2 = eulerToQuaternion(p2.yaw(), p2.pitch(), p2.roll());

            for (int j = 0; j < segments; j++) {
                float t = (float) j / segments;
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

                tempQ.set(q1).slerp(q2, t);
                smoothNodes.add(new RibbonNode(pos, new Quaternionf(tempQ)));
            }
        }

        PathNode lastNode = nodes[endIndex];
        smoothNodes.add(new RibbonNode(lastNode.pos(), eulerToQuaternion(lastNode.yaw(), lastNode.pitch(), lastNode.roll())));

        // 绘制丝带
        VertexConsumer consumer = bufferSource.getBuffer(TrailRenderType.getTrail());
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose last = poseStack.last();

        float timeShift = (servant.getOwner().tickCount + partialTick) * 0.015f;
        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        for (int i = 0; i < nodeCount - 1; i++) {
            RibbonNode curr = smoothNodes.get(i);
            RibbonNode prev = smoothNodes.get(i + 1);

            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            float cScale = Math.max(0.0f, 1.0f - currProgress);
            float pScale = Math.max(0.0f, 1.0f - prevProgress);

            // 计算截面
            Vector3f cTip = new Vector3f(0, 0, 0.6f).rotate(curr.rot);
            Vector3f cR = new Vector3f(0.3f * cScale, 0, -0.2f).rotate(curr.rot);
            Vector3f cL = new Vector3f(-0.3f * cScale, 0, -0.2f).rotate(curr.rot);
            Vector3f cT = new Vector3f(0, 0.15f * cScale, -0.2f).rotate(curr.rot);
            Vector3f cB = new Vector3f(0, -0.15f * cScale, -0.2f).rotate(curr.rot);

            Vector3f pTip = new Vector3f(0, 0, 0.6f).rotate(prev.rot);
            Vector3f pR = new Vector3f(0.3f * pScale, 0, -0.2f).rotate(prev.rot);
            Vector3f pL = new Vector3f(-0.3f * pScale, 0, -0.2f).rotate(prev.rot);
            Vector3f pT = new Vector3f(0, 0.15f * pScale, -0.2f).rotate(prev.rot);
            Vector3f pB = new Vector3f(0, -0.15f * pScale, -0.2f).rotate(prev.rot);

            int cColorRGB = config.trailColorFunction.getColor(servant, currProgress, timeShift);
            int pColorRGB = config.trailColorFunction.getColor(servant, prevProgress, timeShift);

            float cAlphaBoost = config.trailTipAlphaBoost.getBoost(servant, currProgress);
            float cBrightBoost = config.trailTipBrightnessBoost.getBoost(servant, currProgress);
            float pAlphaBoost = config.trailTipAlphaBoost.getBoost(servant, prevProgress);
            float pBrightBoost = config.trailTipBrightnessBoost.getBoost(servant, prevProgress);

            int cr = Math.min(255, Math.round(((cColorRGB >> 16) & 0xFF) * cBrightBoost));
            int cg = Math.min(255, Math.round(((cColorRGB >> 8) & 0xFF) * cBrightBoost));
            int cb = Math.min(255, Math.round((cColorRGB & 0xFF) * cBrightBoost));
            int pr = Math.min(255, Math.round(((pColorRGB >> 16) & 0xFF) * pBrightBoost));
            int pg = Math.min(255, Math.round(((pColorRGB >> 8) & 0xFF) * pBrightBoost));
            int pb = Math.min(255, Math.round((pColorRGB & 0xFF) * pBrightBoost));

            int cTipA = Math.min(255, Math.round(cScale * 0.1f * 255 * cAlphaBoost));
            int cBaseA = Math.min(255, Math.round(Math.max(0f, 1.0f - currProgress * 2.5f) * 0.04f * 255 * cAlphaBoost));
            int pTipA = Math.min(255, Math.round(pScale * 0.1f * 255 * pAlphaBoost));
            int pBaseA = Math.min(255, Math.round(Math.max(0f, 1.0f - prevProgress * 2.5f) * 0.04f * 255 * pAlphaBoost));

            int cTipC = FastColor.ARGB32.color(cTipA, cr, cg, cb);
            int cBaseC = FastColor.ARGB32.color(cBaseA, cr, cg, cb);
            int pTipC = FastColor.ARGB32.color(pTipA, pr, pg, pb);
            int pBaseC = FastColor.ARGB32.color(pBaseA, pr, pg, pb);

            Vec3 cRel = curr.pos.subtract(renderPos);
            Vec3 pRel = prev.pos.subtract(renderPos);

            // 绘制四个方向的面片
            buildRibbon(consumer, pose, last, cRel, pRel, cTip, pTip, cR, pR, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, cRel, pRel, cTip, pTip, cL, pL, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, cRel, pRel, cTip, pTip, cT, pT, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, cRel, pRel, cTip, pTip, cB, pB, cTipC, pTipC, cBaseC, pBaseC);
        }
    }

    private void buildRibbon(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose last, Vec3 cRel, Vec3 pRel, Vector3f cTip, Vector3f pTip, Vector3f cBase, Vector3f pBase, int cTipC, int pTipC, int cBaseC, int pBaseC) {
        consumer.addVertex(pose, (float) cRel.x + cBase.x, (float) cRel.y + cBase.y, (float) cRel.z + cBase.z).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) cRel.x + cTip.x, (float) cRel.y + cTip.y, (float) cRel.z + cTip.z).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x, (float) pRel.y + pTip.y, (float) pRel.z + pTip.z).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) pRel.x + pBase.x, (float) pRel.y + pBase.y, (float) pRel.z + pBase.z).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        consumer.addVertex(pose, (float) cRel.x + cBase.x, (float) cRel.y + cBase.y, (float) cRel.z + cBase.z).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pBase.x, (float) pRel.y + pBase.y, (float) pRel.z + pBase.z).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x, (float) pRel.y + pTip.y, (float) pRel.z + pTip.z).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) cRel.x + cTip.x, (float) cRel.y + cTip.y, (float) cRel.z + cTip.z).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
    }

    // ===================== 辅助方法 =====================

    private Quaternionf eulerToQuaternion(float yaw, float pitch, float roll) {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(-yaw))
                .rotateX((float) Math.toRadians(pitch))
                .rotateZ((float) Math.toRadians(roll));
    }

    // ===================== 内部数据类 =====================

    private record TrailNode(Vec3 pos, Quaternionf rot) {}
    private record RibbonNode(Vec3 pos, Quaternionf rot) {}

    /** 圆形顶点缓存 */
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