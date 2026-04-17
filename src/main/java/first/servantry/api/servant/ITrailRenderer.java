package first.servantry.api.servant;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.servantry.Servantry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 为仆从实现此接口让仆从拥有轨迹光效渲染能力
 */
public interface ITrailRenderer {

    class TrailNode {
        public Vec3 pos;
        public Quaternionf rot;

        public TrailNode(Vec3 pos, Quaternionf rot) {
            this.pos = pos;
            this.rot = rot;
        }
    }

    /**
     * 决定是否渲染轨迹
     * @return 轨迹计时器，小于等于0时自动略过运算和渲染
     */
    int getTrailTimer();

    /**
     * @return 采样的历史节点长度
     */
    default int getTrailHistoryLength() { return 4; }

    /**
     * @return 贝塞尔曲线每两个节点间的平滑细分段数
     */
    default int getTrailSegmentsPerNode() { return 4; }

    /**
     * @return 轨迹起点的跳过索引（用于实现随时间缩短消散的效果）
     */
    default int getTrailStartIndex() { return 0; }

    /**
     * 允许在进行坐标计算前，利用待机混合状态替换当前的视觉渲染节点（解决收刀时的轨迹剥离问题）
     */
    default PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        return rawRenderNode;
    }

    /**
     * 核心逻辑，由 Mixin 在渲染前自动调用
     */
    default void processTrailRender(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode rawRenderNode) {
        int timer = getTrailTimer();
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
        renderNodesArray[0] = new PathNode("", visualRenderPos, visualNode.yaw(), visualNode.pitch(), visualNode.roll());

        int endIndex = renderNodesArray.length - 1;
        int startIndex = Math.max(0, getTrailStartIndex());
        startIndex = Math.min(startIndex, Math.max(0, endIndex - 1));

        int segments = getTrailSegmentsPerNode();
        List<TrailNode> smoothNodes = new ArrayList<>((endIndex - startIndex) * segments + 1);
        Quaternionf tempQ = new Quaternionf();

        // 统一 Catmull-Rom 贝塞尔平滑算法
        for (int i = startIndex; i < endIndex; i++) {
            PathNode p0 = renderNodesArray[Math.max(i - 1, startIndex)];
            PathNode p1 = renderNodesArray[i];
            PathNode p2 = renderNodesArray[i + 1];
            PathNode p3 = renderNodesArray[Math.min(i + 2, endIndex)];

            Quaternionf q1 = new Quaternionf().rotateY((float) Math.toRadians(-p1.yaw())).rotateX((float) Math.toRadians(p1.pitch())).rotateZ((float) Math.toRadians(p1.roll()));
            Quaternionf q2 = new Quaternionf().rotateY((float) Math.toRadians(-p2.yaw())).rotateX((float) Math.toRadians(p2.pitch())).rotateZ((float) Math.toRadians(p2.roll()));

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
        PathNode lastNode = renderNodesArray[endIndex];
        Quaternionf qLast = new Quaternionf().rotateY((float) Math.toRadians(-lastNode.yaw())).rotateX((float) Math.toRadians(lastNode.pitch())).rotateZ((float) Math.toRadians(lastNode.roll()));
        smoothNodes.add(new TrailNode(lastNode.pos(), qLast));

        poseStack.pushPose();
        Vec3 offset = visualRenderPos.subtract(rawRenderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        drawTrailVertices(poseStack, bufferSource, partialTick, servant, visualNode, smoothNodes);

        poseStack.popPose();
    }

    /**
     * 自定义顶点构建方法（在此使用 BufferBuilder 和从 smoothNodes 得到的数据画光效）
     */
    void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode visualRenderNode, List<TrailNode> smoothNodes);

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
            return create("trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
        }
    }

}