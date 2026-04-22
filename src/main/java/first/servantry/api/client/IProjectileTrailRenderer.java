package first.servantry.api.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.servantry.Servantry;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.PathNode;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

/**
 * 射弹圆锥形拖尾渲染接口。
 * <p>
 * 该接口为射弹实体提供一种末端细、前端粗的拖尾效果，拖尾沿运动轨迹生成，
 * 并通过 Catmull-Rom 样条插值和球面线性插值实现平滑的路径与旋转过渡。
 * 拖尾颜色、半径和透明度随进度渐变，末端自然消散。
 * </p>
 * <p>
 * 与 {@link IConeTrailRenderer} 功能相同，但专为射弹设计，不依赖仆从类型。
 * </p>
 * <p>
 * 渲染流程：
 * <ol>
 *   <li>检查拖尾计时器，若有效则继续；</li>
 *   <li>获取历史节点并构建插值数组；</li>
 *   <li>计算样条插值生成平滑节点序列；</li>
 *   <li>遍历相邻节点，构建多边形截面并绘制四边形带；</li>
 *   <li>在最前端绘制半球形头部，封闭拖尾末端。</li>
 * </ol>
 * </p>
 */
public interface IProjectileTrailRenderer {

    /**
     * 单个平滑后的拖尾节点，包含世界坐标和旋转四元数。
     */
    record TrailNode(Vec3 pos, Quaternionf rot) {
    }

    // ===================== 核心控制方法 =====================

    /**
     * 获取拖尾计时器的当前值。
     * <p>
     * 计时器大于 0 时拖尾可见，小于等于 0 时跳过所有渲染计算。
     * </p>
     *
     * @param projectile 射弹实体
     * @return 拖尾计时器值（非负数）
     */
    int getTrailTimer(Projectile projectile);

    /**
     * 获取拖尾渲染时使用的历史节点数量。
     * <p>
     * 该值决定了参与样条插值的原始轨迹节点个数。默认返回 4。
     * </p>
     *
     * @return 历史节点缓存长度
     */
    default int getTrailHistoryLength() {
        return 4;
    }

    /**
     * 获取每对相邻历史节点之间插入的平滑分段数。
     * <p>
     * 分段数越高，拖尾曲线越光滑，但顶点数量线性增加。默认值为 4。
     * </p>
     *
     * @return 插值分段数
     */
    default int getTrailSegmentsPerNode() {
        return 4;
    }

    /**
     * 获取拖尾渲染的起始节点索引。
     * <p>
     * 用于动态控制拖尾长度。当计时器减小时，起始索引前移，使拖尾尾部收缩。
     * 默认返回 0，表示始终从第一个历史节点开始绘制。
     * </p>
     *
     * @return 历史节点数组中的起始下标（包含）
     */
    default int getTrailStartIndex() {
        return 0;
    }

    /**
     * 获取用于渲染的视觉节点（支持 partialTick 插值）。
     * <p>
     * 默认直接返回原始节点。子类可重写以实现更平滑的视觉位置。
     * </p>
     *
     * @param projectile     射弹实体
     * @param partialTick    当前帧插值进度（0~1）
     * @param rawRenderNode  原始渲染节点
     * @return 实际用于定位拖尾头部的节点
     */
    default PathNode getVisualRenderNode(Projectile projectile, float partialTick, PathNode rawRenderNode) {
        return rawRenderNode;
    }

    /**
     * 获取拖尾横截面的最大半径（头部最粗处）。
     *
     * @return 最大半径（世界单位），默认 0.2
     */
    default float getTrailMaxRadius() {
        return 0.2f;
    }

    /**
     * 计算拖尾的淡出缩放因子。
     * <p>
     * 根据拖尾进度（0=头部，1=尾部）返回一个 0~1 之间的系数，用于缩放半径和透明度。
     * 默认实现使用幂函数实现平滑淡出。
     * </p>
     *
     * @param progress 归一化进度（0=头部，1=尾部）
     * @return 淡出因子（0~1）
     */
    default float getTrailFadeOut(float progress) {
        return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);
    }

    /**
     * 获取圆锥横截面的多边形边数。
     * <p>
     * 边数越多，圆柱体越圆滑，但顶点数量成倍增加。默认 6 边形。
     * </p>
     *
     * @return 多边形边数（至少为 3）
     */
    default int getTrailResolution() {
        return 6;
    }

    /**
     * 获取拖尾在指定进度下的基础颜色（RGB 整型）。
     * <p>
     * 透明度由 {@link #getTrailFadeOut(float)} 单独计算，此方法仅返回 RGB 通道。
     * 默认返回红色（0xFF0000）。
     * </p>
     *
     * @param progress 归一化进度（0=头部，1=尾部）
     * @return RGB 颜色值
     */
    default int getTrailColorRGB(float progress) {
        return 0xFF0000;
    }

    // ===================== 性能优化辅助类 =====================

    /**
     * 预计算正/余弦值的缓存表，用于加速圆形截面的顶点计算。
     * <p>
     * 避免每帧重复调用 Math.cos 和 Math.sin，尤其当分辨率固定时。
     * </p>
     */
    final class CircleVertexCache {
        private final float[] cos;
        private final float[] sin;
        private final int resolution;

        public CircleVertexCache(int resolution) {
            this.resolution = resolution;
            this.cos = new float[resolution + 1];
            this.sin = new float[resolution + 1];
            float delta = (float) (2.0 * Math.PI / resolution);
            for (int i = 0; i <= resolution; i++) {
                float angle = i * delta;
                cos[i] = (float) Math.cos(angle);
                sin[i] = (float) Math.sin(angle);
            }
        }

        public float cos(int index) {
            return cos[index];
        }

        public float sin(int index) {
            return sin[index];
        }

        public int resolution() {
            return resolution;
        }
    }

    // ===================== 主渲染逻辑 =====================

    /**
     * 圆锥形拖尾的主渲染入口。
     * <p>
     * 该方法完成从历史节点采集、样条插值、顶点生成到最终渲染的全部流程。
     * </p>
     *
     * @param poseStack     矩阵栈，用于变换坐标系
     * @param bufferSource  渲染缓冲源
     * @param partialTick   当前帧插值进度
     * @param projectile    射弹实体
     * @param rawRenderNode 原始渲染节点
     */
    default void processTrailRender(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Projectile projectile, PathNode rawRenderNode) {
        int timer = getTrailTimer(projectile);
        if (timer <= 0) return;

        LinkedList<PathNode> history = projectile.getHistoryNodes();
        int actualLength = Math.min(history.size(), getTrailHistoryLength());
        if (actualLength < 3) return;

        // 1. 准备节点数组，并将最新位置用视觉节点覆盖
        PathNode visualNode = getVisualRenderNode(projectile, partialTick, rawRenderNode);
        Vec3 visualRenderPos = visualNode.pos();

        PathNode[] renderNodesArray = new PathNode[actualLength];
        Iterator<PathNode> iterator = history.iterator();
        for (int i = 0; i < actualLength; i++) {
            renderNodesArray[i] = iterator.next();
        }
        renderNodesArray[0] = new PathNode(visualRenderPos, visualNode.yaw(), visualNode.pitch(), visualNode.roll());

        int endIndex = renderNodesArray.length - 1;
        int startIndex = Math.max(0, getTrailStartIndex());
        startIndex = Math.min(startIndex, Math.max(0, endIndex - 1));

        // 2. Catmull-Rom 插值生成平滑节点序列
        int segments = getTrailSegmentsPerNode();
        int estimatedNodes = (endIndex - startIndex) * segments + 1;
        List<TrailNode> smoothNodes = new ArrayList<>(estimatedNodes);
        Quaternionf tempQ = new Quaternionf();

        for (int i = startIndex; i < endIndex; i++) {
            PathNode p0 = renderNodesArray[Math.max(i - 1, startIndex)];
            PathNode p1 = renderNodesArray[i];
            PathNode p2 = renderNodesArray[i + 1];
            PathNode p3 = renderNodesArray[Math.min(i + 2, endIndex)];

            Quaternionf q1 = new Quaternionf()
                    .rotateY((float) Math.toRadians(-p1.yaw()))
                    .rotateX((float) Math.toRadians(p1.pitch()))
                    .rotateZ((float) Math.toRadians(p1.roll()));
            Quaternionf q2 = new Quaternionf()
                    .rotateY((float) Math.toRadians(-p2.yaw()))
                    .rotateX((float) Math.toRadians(p2.pitch()))
                    .rotateZ((float) Math.toRadians(p2.roll()));

            for (int j = 0; j < segments; j++) {
                float t = (float) j / segments;
                float t2 = t * t;
                float t3 = t2 * t;

                // Catmull-Rom 基函数系数
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
        PathNode lastNode = renderNodesArray[endIndex];
        Quaternionf qLast = new Quaternionf()
                .rotateY((float) Math.toRadians(-lastNode.yaw()))
                .rotateX((float) Math.toRadians(lastNode.pitch()))
                .rotateZ((float) Math.toRadians(lastNode.roll()));
        smoothNodes.add(new TrailNode(lastNode.pos(), qLast));

        // 3. 开始绘制
        poseStack.pushPose();
        Vec3 offset = visualRenderPos.subtract(rawRenderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        VertexConsumer consumer = bufferSource.getBuffer(ProjectileTrailRenderType.getTrail());
        Matrix4f pose = poseStack.last().pose();
        int resolution = getTrailResolution();
        float maxRadius = getTrailMaxRadius();
        Vec3 renderPos = visualNode.pos();

        // 获取或创建圆形顶点缓存
        CircleVertexCache cache = getOrCreateCircleCache(resolution);

        // 临时复用变量
        Vector3f cV1 = new Vector3f();
        Vector3f cV2 = new Vector3f();
        Vector3f pV1 = new Vector3f();
        Vector3f pV2 = new Vector3f();

        int nodeCount = smoothNodes.size();
        if (nodeCount > 1) {
            for (int i = 0; i < nodeCount - 1; i++) {
                TrailNode curr = smoothNodes.get(i);
                TrailNode prev = smoothNodes.get(i + 1);

                float currProgress = (float) i / (nodeCount - 1);
                float prevProgress = (float) (i + 1) / (nodeCount - 1);

                float currFade = getTrailFadeOut(currProgress);
                float prevFade = getTrailFadeOut(prevProgress);

                float currRadius = maxRadius * currFade;
                float prevRadius = maxRadius * prevFade;

                int currColor = getTrailColorRGB(currProgress);
                int prevColor = getTrailColorRGB(prevProgress);

                int cA = Math.round(currFade * 200);
                int pA = Math.round(prevFade * 200);

                int cr = (currColor >> 16) & 0xFF;
                int cg = (currColor >> 8) & 0xFF;
                int cb = currColor & 0xFF;
                int pr = (prevColor >> 16) & 0xFF;
                int pg = (prevColor >> 8) & 0xFF;
                int pb = prevColor & 0xFF;

                int cColorVal = FastColor.ARGB32.color(cA, cr, cg, cb);
                int pColorVal = FastColor.ARGB32.color(pA, pr, pg, pb);

                Vec3 cRel = curr.pos.subtract(renderPos);
                Vec3 pRel = prev.pos.subtract(renderPos);

                // 绘制多边形带
                for (int j = 0; j < resolution; j++) {
                    float cos1 = cache.cos(j);
                    float sin1 = cache.sin(j);
                    float cos2 = cache.cos(j + 1);
                    float sin2 = cache.sin(j + 1);

                    cV1.set(cos1 * currRadius, sin1 * currRadius, 0).rotate(curr.rot);
                    cV2.set(cos2 * currRadius, sin2 * currRadius, 0).rotate(curr.rot);
                    pV1.set(cos1 * prevRadius, sin1 * prevRadius, 0).rotate(prev.rot);
                    pV2.set(cos2 * prevRadius, sin2 * prevRadius, 0).rotate(prev.rot);

                    consumer.addVertex(pose, (float) cRel.x + cV1.x(), (float) cRel.y + cV1.y(), (float) cRel.z + cV1.z())
                            .setColor(cColorVal).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                    consumer.addVertex(pose, (float) cRel.x + cV2.x(), (float) cRel.y + cV2.y(), (float) cRel.z + cV2.z())
                            .setColor(cColorVal).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                    consumer.addVertex(pose, (float) pRel.x + pV2.x(), (float) pRel.y + pV2.y(), (float) pRel.z + pV2.z())
                            .setColor(pColorVal).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                    consumer.addVertex(pose, (float) pRel.x + pV1.x(), (float) pRel.y + pV1.y(), (float) pRel.z + pV1.z())
                            .setColor(pColorVal).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                }
            }
        }

        // 4. 绘制头部半球
        if (!smoothNodes.isEmpty()) {
            TrailNode first = smoothNodes.get(0);
            Vec3 headRel = first.pos.subtract(renderPos);
            float headRadius = getTrailMaxRadius();
            int headColorRGB = getTrailColorRGB(0f);
            int hr = (headColorRGB >> 16) & 0xFF;
            int hg = (headColorRGB >> 8) & 0xFF;
            int hb = headColorRGB & 0xFF;
            int headColorVal = FastColor.ARGB32.color(200, hr, hg, hb);

            int rings = 8;
            int sectors = 8;
            float deltaPhi = (float) Math.PI / rings;
            float deltaTheta = (float) (2.0 * Math.PI / sectors);

            Vector3f v1 = new Vector3f();
            Vector3f v2 = new Vector3f();
            Vector3f v3 = new Vector3f();
            Vector3f v4 = new Vector3f();

            for (int i = 0; i < rings; i++) {
                float phi1 = i * deltaPhi;
                float phi2 = (i + 1) * deltaPhi;
                float sinPhi1 = (float) Math.sin(phi1);
                float cosPhi1 = (float) Math.cos(phi1);
                float sinPhi2 = (float) Math.sin(phi2);
                float cosPhi2 = (float) Math.cos(phi2);

                for (int j = 0; j < sectors; j++) {
                    float theta1 = j * deltaTheta;
                    float theta2 = (j + 1) * deltaTheta;
                    float cosTheta1 = (float) Math.cos(theta1);
                    float sinTheta1 = (float) Math.sin(theta1);
                    float cosTheta2 = (float) Math.cos(theta2);
                    float sinTheta2 = (float) Math.sin(theta2);

                    setSphereVertex(v1, headRel, headRadius, sinPhi1, cosPhi1, cosTheta1, sinTheta1);
                    setSphereVertex(v2, headRel, headRadius, sinPhi1, cosPhi1, cosTheta2, sinTheta2);
                    setSphereVertex(v3, headRel, headRadius, sinPhi2, cosPhi2, cosTheta2, sinTheta2);
                    setSphereVertex(v4, headRel, headRadius, sinPhi2, cosPhi2, cosTheta1, sinTheta1);

                    consumer.addVertex(pose, v1.x(), v1.y(), v1.z()).setColor(headColorVal).setUv(0, 0)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                    consumer.addVertex(pose, v2.x(), v2.y(), v2.z()).setColor(headColorVal).setUv(1, 0)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                    consumer.addVertex(pose, v3.x(), v3.y(), v3.z()).setColor(headColorVal).setUv(1, 1)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                    consumer.addVertex(pose, v4.x(), v4.y(), v4.z()).setColor(headColorVal).setUv(0, 1)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                }
            }
        }

        poseStack.popPose();
    }

    // ===================== 缓存与辅助 =====================

    /** 圆形顶点缓存映射 */
    Map<Integer, CircleVertexCache> CACHE_MAP = new java.util.HashMap<>();

    /**
     * 获取或创建指定分辨率的圆形顶点缓存。
     */
    private CircleVertexCache getOrCreateCircleCache(int resolution) {
        return CACHE_MAP.computeIfAbsent(resolution, CircleVertexCache::new);
    }

    /**
     * 辅助方法：将球面坐标转换为直角坐标。
     */
    private void setSphereVertex(Vector3f out, Vec3 center, float radius, float sinPhi, float cosPhi, float cosTheta, float sinTheta) {
        out.x = (float) center.x + radius * sinPhi * cosTheta;
        out.y = (float) center.y + radius * cosPhi;
        out.z = (float) center.z + radius * sinPhi * sinTheta;
    }

    // ===================== 渲染类型定义 =====================

    /**
     * 射弹圆锥拖尾专用渲染类型。
     * <p>
     * 使用实体半透明着色器、自定义纹理、双面渲染、无剔除、全亮度光照。
     * </p>
     */
    class ProjectileTrailRenderType extends RenderType {

        private ProjectileTrailRenderType(String name, VertexFormat fmt, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, fmt, mode, bufSize, affectsCrumbling, sort, setup, clear);
        }

        /**
         * 获取拖尾渲染类型。
         *
         * @return 渲染类型实例
         */
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
            return create("projectile_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                    256, false, true, state);
        }
    }
}