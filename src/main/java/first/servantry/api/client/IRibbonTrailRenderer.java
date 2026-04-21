package first.servantry.api.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.servantry.Servantry;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public interface IRibbonTrailRenderer {

    Map<Servant, List<TrailNode>> TRAIL_CACHE = new WeakHashMap<>();

    class TrailNode {
        public final Vector3f pos = new Vector3f();
        public final Quaternionf rot = new Quaternionf();
        public void set(double x, double y, double z, Quaternionf q) {
            this.pos.set((float) x, (float) y, (float) z);
            this.rot.set(q);
        }
    }

    class TrailRenderType extends RenderType {
        private TrailRenderType(String name, VertexFormat fmt, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, fmt, mode, bufSize, affectsCrumbling, sort, setup, clear);
        }

        public static RenderType getTrail() {
            CompositeState state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(Servantry.rl("textures/trail.png"), false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false);
            return create("servantry_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
        }
    }

    int getTrailTimer(Servant servant);

    default int getTrailHistoryLength() { return 4; }

    default int getTrailSegmentsPerNode() { return 8; }

    default int getTrailStartIndex(Servant servant) {
        return Math.max(0, 10 - getTrailTimer(servant));
    }

    int getTrailColor(Servant servant, float progress, float timeShift);

    default PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        return rawRenderNode;
    }

    default void processRibbonTrailRender(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode rawRenderNode) {
        int timer = getTrailTimer(servant);
        if (timer <= 0) return;

        LinkedList<PathNode> history = servant.getHistoryNodes();
        int actualLength = Math.min(history.size(), getTrailHistoryLength());
        if (actualLength < 3) return;

        PathNode visualNode = getVisualRenderNode(servant, partialTick, rawRenderNode);
        Vec3 visualRenderPos = visualNode.pos();

        PathNode[] renderNodesArray = new PathNode[actualLength];
        Iterator<PathNode> iterator = history.iterator();
        for (int i = 0; i < actualLength; i++) {
            renderNodesArray[i] = iterator.next();
        }
        renderNodesArray[0] = new PathNode(visualRenderPos, visualNode.yaw(), visualNode.pitch(), visualNode.roll());

        int endIndex = renderNodesArray.length - 1;
        int startIndex = Math.max(0, getTrailStartIndex(servant));
        startIndex = Math.min(startIndex, Math.max(0, endIndex - 1));

        int segments = getTrailSegmentsPerNode();
        int requiredNodes = (endIndex - startIndex) * segments + 1;

        List<TrailNode> smoothNodes = TRAIL_CACHE.computeIfAbsent(servant, k -> new ArrayList<>(32));
        while (smoothNodes.size() < requiredNodes) {
            smoothNodes.add(new TrailNode());
        }

        Quaternionf tempQ1 = new Quaternionf();
        Quaternionf tempQ2 = new Quaternionf();
        Quaternionf tempQBlend = new Quaternionf();

        int nodeIndex = 0;

        for (int i = startIndex; i < endIndex; i++) {
            PathNode p0 = renderNodesArray[Math.max(i - 1, startIndex)];
            PathNode p1 = renderNodesArray[i];
            PathNode p2 = renderNodesArray[i + 1];
            PathNode p3 = renderNodesArray[Math.min(i + 2, endIndex)];

            tempQ1.identity().rotateY((float) Math.toRadians(-p1.yaw())).rotateX((float) Math.toRadians(p1.pitch())).rotateZ((float) Math.toRadians(p1.roll()));
            tempQ2.identity().rotateY((float) Math.toRadians(-p2.yaw())).rotateX((float) Math.toRadians(p2.pitch())).rotateZ((float) Math.toRadians(p2.roll()));

            for (int j = 0; j < segments; j++) {
                float t = (float) j / segments;
                float t2 = t * t, t3 = t2 * t;
                float f0 = -0.5f * t3 + t2 - 0.5f * t;
                float f1 = 1.5f * t3 - 2.5f * t2 + 1.0f;
                float f2 = -1.5f * t3 + 2.0f * t2 + 0.5f * t;
                float f3 = 0.5f * t3 - 0.5f * t2;

                double px = p0.pos().x * f0 + p1.pos().x * f1 + p2.pos().x * f2 + p3.pos().x * f3;
                double py = p0.pos().y * f0 + p1.pos().y * f1 + p2.pos().y * f2 + p3.pos().y * f3;
                double pz = p0.pos().z * f0 + p1.pos().z * f1 + p2.pos().z * f2 + p3.pos().z * f3;

                tempQBlend.set(tempQ1).slerp(tempQ2, t);
                smoothNodes.get(nodeIndex++).set(px, py, pz, tempQBlend);
            }
        }

        PathNode lastNode = renderNodesArray[endIndex];
        tempQBlend.identity().rotateY((float) Math.toRadians(-lastNode.yaw())).rotateX((float) Math.toRadians(lastNode.pitch())).rotateZ((float) Math.toRadians(lastNode.roll()));
        smoothNodes.get(nodeIndex++).set(lastNode.pos().x, lastNode.pos().y, lastNode.pos().z, tempQBlend);

        poseStack.pushPose();
        Vec3 offset = visualRenderPos.subtract(rawRenderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        drawRibbonVertices(poseStack, bufferSource, partialTick, servant, visualNode, smoothNodes, nodeIndex);

        poseStack.popPose();
    }

    private void drawRibbonVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode visualRenderNode, List<TrailNode> smoothNodes, int validNodeCount) {
        if (validNodeCount < 2) return;

        VertexConsumer consumer = bufferSource.getBuffer(TrailRenderType.getTrail());
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();

        Vec3 logicalAnchorPos = new Vec3(smoothNodes.getFirst().pos.x, smoothNodes.getFirst().pos.y, smoothNodes.getFirst().pos.z);
        float timeShift = (servant.getOwner().tickCount + partialTick) * 0.015f;
        int activeSegments = validNodeCount - 1;

        for (int i = 0; i < activeSegments; i++) {
            TrailNode curr = smoothNodes.get(i);
            TrailNode prev = smoothNodes.get(i + 1);
            TrailNode nextOfPrev = (i + 2 < validNodeCount) ? smoothNodes.get(i + 2) : prev;

            float currProgress = (float) i / activeSegments;
            float prevProgress = (float) (i + 1) / activeSegments;

            // 完美还原原版的稳定度矩阵和缩放
            Vector3f currFwd = new Vector3f(curr.pos).sub(prev.pos);
            if (currFwd.lengthSquared() > 1e-5) currFwd.normalize(); else currFwd.set(0, 1, 0);
            Vector3f cTipDir = new Vector3f(0, 0, 1).rotate(curr.rot);
            float cScale = Math.max(0.0f, 1.0f - currProgress);
            float cStab = Math.abs(currFwd.dot(cTipDir));
            float cXBase = (0.05f + 0.25f * (1.0f - cStab) + 0.25f * cStab) * cScale;
            float cYBase = (0.05f + 0.15f * cStab) * cScale;

            Vector3f prevFwd = new Vector3f(prev.pos).sub(nextOfPrev.pos);
            if (prevFwd.lengthSquared() > 1e-5) prevFwd.normalize(); else prevFwd.set(0, 1, 0);
            Vector3f pTipDir = new Vector3f(0, 0, 1).rotate(prev.rot);
            float pScale = Math.max(0.0f, 1.0f - prevProgress);
            float pStab = Math.abs(prevFwd.dot(pTipDir));
            float pXBase = (0.05f + 0.25f * (1.0f - pStab) + 0.25f * pStab) * pScale;
            float pYBase = (0.05f + 0.15f * pStab) * pScale;

            Vec3 currRel = new Vec3(curr.pos.x, curr.pos.y, curr.pos.z).subtract(logicalAnchorPos);
            Vec3 prevRel = new Vec3(prev.pos.x, prev.pos.y, prev.pos.z).subtract(logicalAnchorPos);

            // 原版的四面菱形（Diamond Topology）
            Vector3f cTip = new Vector3f(0, 0, 0.6f).rotate(curr.rot);
            Vector3f cR = new Vector3f(cXBase, 0, -0.2f).rotate(curr.rot);
            Vector3f cL = new Vector3f(-cXBase, 0, -0.2f).rotate(curr.rot);
            Vector3f cT = new Vector3f(0, cYBase, -0.2f).rotate(curr.rot);
            Vector3f cB = new Vector3f(0, -cYBase, -0.2f).rotate(curr.rot);

            Vector3f pTip = new Vector3f(0, 0, 0.6f).rotate(prev.rot);
            Vector3f pR = new Vector3f(pXBase, 0, -0.2f).rotate(prev.rot);
            Vector3f pL = new Vector3f(-pXBase, 0, -0.2f).rotate(prev.rot);
            Vector3f pT = new Vector3f(0, pYBase, -0.2f).rotate(prev.rot);
            Vector3f pB = new Vector3f(0, -pYBase, -0.2f).rotate(prev.rot);

            int cColorRGB = getTrailColor(servant, currProgress, timeShift);
            int pColorRGB = getTrailColor(servant, prevProgress, timeShift);
            int cr = (cColorRGB >> 16) & 0xFF, cg = (cColorRGB >> 8) & 0xFF, cb = cColorRGB & 0xFF;
            int pr = (pColorRGB >> 16) & 0xFF, pg = (pColorRGB >> 8) & 0xFF, pb = pColorRGB & 0xFF;

            // 完美还原半透明独立衰减通道
            int cTipA = Math.round(cScale * 0.1f * 255);
            int cBaseA = Math.round(Math.max(0f, 1.0f - currProgress * 2.5f) * 0.04f * 255);
            int pTipA = Math.round(pScale * 0.1f * 255);
            int pBaseA = Math.round(Math.max(0f, 1.0f - prevProgress * 2.5f) * 0.04f * 255);

            int cTipC = FastColor.ARGB32.color(cTipA, cr, cg, cb);
            int cBaseC = FastColor.ARGB32.color(cBaseA, cr, cg, cb);
            int pTipC = FastColor.ARGB32.color(pTipA, pr, pg, pb);
            int pBaseC = FastColor.ARGB32.color(pBaseA, pr, pg, pb);

            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cR, pR, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cL, pL, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cT, pT, cTipC, pTipC, cBaseC, pBaseC);
            buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cB, pB, cTipC, pTipC, cBaseC, pBaseC);
        }
    }

    private void buildRibbon(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose last, Vec3 cRel, Vec3 pRel, Vector3f cTip, Vector3f pTip, Vector3f cBase, Vector3f pBase, int cTipC, int pTipC, int cBaseC, int pBaseC) {
        // 【核心修复】：还原强制固定 UV，让贴图在每段渲染面上完整贴合而不是拉伸
        consumer.addVertex(pose, (float) cRel.x + cBase.x(), (float) cRel.y + cBase.y(), (float) cRel.z + cBase.z()).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) cRel.x + cTip.x(), (float) cRel.y + cTip.y(), (float) cRel.z + cTip.z()).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x(), (float) pRel.y + pTip.y(), (float) pRel.z + pTip.z()).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) pRel.x + pBase.x(), (float) pRel.y + pBase.y(), (float) pRel.z + pBase.z()).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        consumer.addVertex(pose, (float) cRel.x + cBase.x(), (float) cRel.y + cBase.y(), (float) cRel.z + cBase.z()).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pBase.x(), (float) pRel.y + pBase.y(), (float) pRel.z + pBase.z()).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x(), (float) pRel.y + pTip.y(), (float) pRel.z + pTip.z()).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) cRel.x + cTip.x(), (float) cRel.y + cTip.y(), (float) cRel.z + cTip.z()).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
    }
}