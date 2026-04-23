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

public interface IServantRibbonTrailRenderer {

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

        /**
         * 获取拖尾渲染类型。
         * <p>
         * 使用自定义 CompositeState 确保透明度正确渲染：
         * <ul>
         *   <li>使用实体半透明着色器</li>
         *   <li>禁用背面剔除实现双面渲染</li>
         *   <li>启用光照图支持全亮度</li>
         *   <li>透明度混合模式确保正确的 Alpha 混合</li>
         * </ul>
         * </p>
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
            return create("ribbon_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
        }
    }

    /**
     * 获取指定仆从的当前拖尾计时器数值。
     * <p>
     * 拖尾计时器用于控制拖尾的激活状态和长度：
     * <ul>
     *   <li><b>大于 0</b>：拖尾可见，数值越大通常拖尾越长或越亮；</li>
     *   <li><b>小于等于 0</b>：拖尾不渲染，主流程将跳过所有绘制逻辑。</li>
     * </ul>
     * 该计时器通常由仆从的动作（如冲刺、技能释放）触发增加，并随时间衰减。
     * </p>
     *
     * @param servant 要查询的仆从实体
     * @return 当前的拖尾计时器值（非负数）
     */
    int getTrailTimer(Servant servant);

    /**
     * 获取拖尾渲染时使用的历史节点数量。
     * <p>
     * 历史节点来自仆从的运动轨迹缓存。该值决定了参与样条插值的原始节点个数。
     * 节点数越多，拖尾的路径越平滑且能表现更长的历史轨迹，但会略微增加计算量。
     * 默认值为 {@code 4}，表示使用最近 4 个历史节点。
     * </p>
     *
     * @return 历史节点列表的最大使用长度
     */
    default int getTrailHistoryLength() {
        return 4;
    }

    /**
     * 获取每对历史节点之间插入的平滑分段数量。
     * <p>
     * 为消除节点间的硬边，渲染器会在每两个历史节点之间进行 Catmull-Rom 样条插值。
     * 该值决定了插值的精细程度。分段数越多，拖尾曲线越光滑，但顶点数量也会成倍增加。
     * 默认值为 {@code 4}。
     * </p>
     *
     * @return 每对历史节点间的插值段数
     */
    default int getTrailSegmentsPerNode() {
        return 4;
    }

    /**
     * 计算拖尾渲染的起始节点索引。
     * <p>
     * 拖尾并非总是从历史缓存的最旧节点开始绘制，而是根据计时器动态缩短起始位置。
     * 当计时器较小时（如即将消失），起始索引会向前移动，使拖尾长度逐渐收缩，
     * 产生自然淡出的视觉效果。默认公式为 {@code max(0, 10 - getTrailTimer(servant))}，
     * 意味着计时器每减少 1，拖尾就会从尾部缩短一个历史节点的跨度。
     * </p>
     *
     * @param servant 仆从实体
     * @return 历史节点数组中的起始下标（包含）
     */
    default int getTrailStartIndex(Servant servant) {
        return Math.max(0, 10 - getTrailTimer(servant));
    }

    /**
     * 获取拖尾在指定进度和时间偏移下的颜色值（RGB）。
     * <p>
     * 该方法在绘制每一段丝带面片时被调用，用于计算顶点颜色。
     * <ul>
     *   <li>{@code progress}：取值范围 {@code 0.0 ~ 1.0}，0 表示拖尾头部（最新位置），1 表示尾部（最旧位置）；</li>
     *   <li>{@code timeShift}：随时间线性变化的浮点数，可用于实现流光或颜色循环效果；</li>
     *   <li>返回值：一个 RGB 整型颜色，不包含 Alpha 通道，透明度由渲染器根据进度另行计算。</li>
     * </ul>
     * 实现者可以基于进度实现渐变，或基于时间偏移实现动态变色。
     * </p>
     *
     * @param servant   仆从实体
     * @param progress  当前节点在整体拖尾中的归一化进度（0=头，1=尾）
     * @param timeShift 基于游戏时间的动态偏移量
     * @return RGB 颜色值（例如 {@code 0xFFAA00}）
     */
    int getTrailColor(Servant servant, float progress, float timeShift);

    /**
     * 获取轨迹剑尖处的额外不透明度增强系数。
     * <p>
     * 默认返回 1.0，即不增强。实现者可重写此方法，
     * 使剑尖处（progress 较小）的不透明度更高，轨迹头部更加醒目。
     * </p>
     *
     * @param servant  仆从实体
     * @param progress 当前节点在整体拖尾中的归一化进度（0=头，1=尾）
     * @return 不透明度增强系数（1.0 = 无增强）
     */
    default float getTrailTipAlphaBoost(Servant servant, float progress) {
        return 1.0f;
    }

    /**
     * 获取轨迹剑尖处的额外亮度增强系数。
     * <p>
     * 默认返回 1.0，即不增强。实现者可重写此方法，
     * 使剑尖处的亮度额外提升，轨迹头部更加明亮醒目。
     * </p>
     *
     * @param servant  仆从实体
     * @param progress 当前节点在整体拖尾中的归一化进度（0=头，1=尾）
     * @return 亮度增强系数（1.0 = 无增强）
     */
    default float getTrailTipBrightnessBoost(Servant servant, float progress) {
        return 1.0f;
    }

    /**
     * 获取用于渲染的视觉节点（可选插值）。
     * <p>
     * 在每一帧渲染时，原始渲染节点（{@code rawRenderNode}）通常代表仆从上一 tick 的精确位置。
     * 通过该方法，可以实现对视觉位置的额外平滑处理（如基于 {@code partialTick} 的线性插值）。
     * 默认实现直接返回原始节点，即不做任何调整。
     * </p>
     *
     * @param servant       仆从实体
     * @param partialTick   当前帧的部分 tick 进度（0.0 ~ 1.0）
     * @param rawRenderNode 未经插值的原始渲染节点
     * @return 最终用于定位拖尾头部的视觉节点
     */
    default PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        return rawRenderNode;
    }

    /**
     * 丝带拖尾的主渲染入口。
     * <p>
     * 根据历史路径节点，通过 Catmull-Rom 样条插值和球面线性插值生成平滑的轨迹节点序列，
     * 然后为每对相邻节点绘制四条半透明的菱形带状四边形，组成完整的拖尾效果。
     * </p>
     *
     * @param poseStack     矩阵栈，用于变换到世界坐标
     * @param bufferSource  渲染缓冲源
     * @param partialTick   当前帧的部分 tick 插值
     * @param servant       所属的仆从实体
     * @param rawRenderNode 未经插值的原始渲染节点
     */
    default void processRibbonTrailRender(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode rawRenderNode) {
        int timer = getTrailTimer(servant);
        // 只有当计时器有效时才进行后续处理，避免提前返回
        if (timer > 0) {
            LinkedList<PathNode> history = servant.getHistoryNodes();
            int actualLength = Math.min(history.size(), getTrailHistoryLength());
            if (actualLength >= 3) {
                PathNode visualNode = getVisualRenderNode(servant, partialTick, rawRenderNode);
                Vec3 visualRenderPos = visualNode.pos();

                // 构建历史节点数组，并用当前视觉位置覆盖最近的一个节点
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

                List<TrailNode> smoothNodes = new ArrayList<>(requiredNodes);
                for (int i = 0; i < requiredNodes; i++) {
                    smoothNodes.add(new TrailNode());
                }

                Quaternionf tempQ1 = new Quaternionf();
                Quaternionf tempQ2 = new Quaternionf();
                Quaternionf tempQBlend = new Quaternionf();

                int nodeIndex = 0;
                // 对每一对相邻的历史节点进行 Catmull-Rom 样条插值，生成平滑的位置与旋转
                for (int i = startIndex; i < endIndex; i++) {
                    PathNode p0 = renderNodesArray[Math.max(i - 1, startIndex)];
                    PathNode p1 = renderNodesArray[i];
                    PathNode p2 = renderNodesArray[i + 1];
                    PathNode p3 = renderNodesArray[Math.min(i + 2, endIndex)];

                    // 将欧拉角转换为四元数表示
                    tempQ1.identity().rotateY((float) Math.toRadians(-p1.yaw())).rotateX((float) Math.toRadians(p1.pitch())).rotateZ((float) Math.toRadians(p1.roll()));
                    tempQ2.identity().rotateY((float) Math.toRadians(-p2.yaw())).rotateX((float) Math.toRadians(p2.pitch())).rotateZ((float) Math.toRadians(p2.roll()));

                    for (int j = 0; j < segments; j++) {
                        float t = (float) j / segments;
                        float t2 = t * t, t3 = t2 * t;
                        // Catmull-Rom 基函数系数
                        float f0 = -0.5f * t3 + t2 - 0.5f * t;
                        float f1 = 1.5f * t3 - 2.5f * t2 + 1.0f;
                        float f2 = -1.5f * t3 + 2.0f * t2 + 0.5f * t;
                        float f3 = 0.5f * t3 - 0.5f * t2;

                        double px = p0.pos().x * f0 + p1.pos().x * f1 + p2.pos().x * f2 + p3.pos().x * f3;
                        double py = p0.pos().y * f0 + p1.pos().y * f1 + p2.pos().y * f2 + p3.pos().y * f3;
                        double pz = p0.pos().z * f0 + p1.pos().z * f1 + p2.pos().z * f2 + p3.pos().z * f3;

                        // 球面线性插值旋转
                        tempQBlend.set(tempQ1).slerp(tempQ2, t);
                        smoothNodes.get(nodeIndex++).set(px, py, pz, tempQBlend);
                    }
                }

                // 添加最后一个节点
                PathNode lastNode = renderNodesArray[endIndex];
                tempQBlend.identity().rotateY((float) Math.toRadians(-lastNode.yaw())).rotateX((float) Math.toRadians(lastNode.pitch())).rotateZ((float) Math.toRadians(lastNode.roll()));
                smoothNodes.get(nodeIndex++).set(lastNode.pos().x, lastNode.pos().y, lastNode.pos().z, tempQBlend);

                poseStack.pushPose();
                Vec3 offset = visualRenderPos.subtract(rawRenderNode.pos());
                poseStack.translate(offset.x, offset.y, offset.z);

                // 绘制平滑后的节点序列构成的丝带
                drawRibbonVertices(poseStack, bufferSource, partialTick, servant, smoothNodes, nodeIndex);

                poseStack.popPose();
            }
        }
    }

    /**
     * 为相邻的轨迹节点生成四边形顶点，形成四条菱形带状面片。
     * <p>
     * 每条边带由四个顶点组成两个三角形（双面渲染），分别对应上下左右四个方向。
     * 颜色和透明度根据节点进度以及仆从的 tick 时间动态计算。
     * </p>
     *
     * @param poseStack      矩阵栈
     * @param bufferSource   渲染缓冲源
     * @param partialTick    部分 tick 插值
     * @param servant        仆从实体
     * @param smoothNodes    平滑后的节点列表
     * @param validNodeCount 有效节点数量
     */
    private void drawRibbonVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, List<TrailNode> smoothNodes, int validNodeCount) {
        if (validNodeCount >= 2) {
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

                // 计算前进方向与朝向的点积，用于决定截面缩放
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

                // 计算当前和上一个节点的菱形截面顶点（相对于节点位置）
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

                // 获取剑尖处的增强系数
                float cAlphaBoost = getTrailTipAlphaBoost(servant, currProgress);
                float cBrightnessBoost = getTrailTipBrightnessBoost(servant, currProgress);
                float pAlphaBoost = getTrailTipAlphaBoost(servant, prevProgress);
                float pBrightnessBoost = getTrailTipBrightnessBoost(servant, prevProgress);

                // 应用亮度增强到颜色
                int cr = Math.min(255, Math.round(((cColorRGB >> 16) & 0xFF) * cBrightnessBoost));
                int cg = Math.min(255, Math.round(((cColorRGB >> 8) & 0xFF) * cBrightnessBoost));
                int cb = Math.min(255, Math.round((cColorRGB & 0xFF) * cBrightnessBoost));
                int pr = Math.min(255, Math.round(((pColorRGB >> 16) & 0xFF) * pBrightnessBoost));
                int pg = Math.min(255, Math.round(((pColorRGB >> 8) & 0xFF) * pBrightnessBoost));
                int pb = Math.min(255, Math.round((pColorRGB & 0xFF) * pBrightnessBoost));

                // 透明度随进度衰减，剑尖处应用额外增强
                int cTipA = Math.min(255, Math.round(cScale * 0.1f * 255 * cAlphaBoost));
                int cBaseA = Math.min(255, Math.round(Math.max(0f, 1.0f - currProgress * 2.5f) * 0.04f * 255 * cAlphaBoost));
                int pTipA = Math.min(255, Math.round(pScale * 0.1f * 255 * pAlphaBoost));
                int pBaseA = Math.min(255, Math.round(Math.max(0f, 1.0f - prevProgress * 2.5f) * 0.04f * 255 * pAlphaBoost));

                int cTipC = FastColor.ARGB32.color(cTipA, cr, cg, cb);
                int cBaseC = FastColor.ARGB32.color(cBaseA, cr, cg, cb);
                int pTipC = FastColor.ARGB32.color(pTipA, pr, pg, pb);
                int pBaseC = FastColor.ARGB32.color(pBaseA, pr, pg, pb);

                // 依次绘制四个方向的面片（右、左、上、下）
                buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cR, pR, cTipC, pTipC, cBaseC, pBaseC);
                buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cL, pL, cTipC, pTipC, cBaseC, pBaseC);
                buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cT, pT, cTipC, pTipC, cBaseC, pBaseC);
                buildRibbon(consumer, pose, last, currRel, prevRel, cTip, pTip, cB, pB, cTipC, pTipC, cBaseC, pBaseC);
            }
        }
    }

    /**
     * 构建一段丝带四边形，由两个三角形组成（正反面均可见）。
     *
     * @param consumer 顶点消费者
     * @param pose     模型矩阵
     * @param last     矩阵栈的当前姿态
     * @param cRel     当前节点的相对位置
     * @param pRel     前一个节点的相对位置
     * @param cTip     当前节点尖端偏移
     * @param pTip     前一个节点尖端偏移
     * @param cBase    当前节点基底偏移
     * @param pBase    前一个节点基底偏移
     * @param cTipC    当前节点尖端颜色
     * @param pTipC    前一个节点尖端颜色
     * @param cBaseC   当前节点基底颜色
     * @param pBaseC   前一个节点基底颜色
     */
    private void buildRibbon(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose last, Vec3 cRel, Vec3 pRel, Vector3f cTip, Vector3f pTip, Vector3f cBase, Vector3f pBase, int cTipC, int pTipC, int cBaseC, int pBaseC) {
        // 正面三角形1: 当前基底 -> 当前尖端 -> 前一尖端
        consumer.addVertex(pose, (float) cRel.x + cBase.x(), (float) cRel.y + cBase.y(), (float) cRel.z + cBase.z()).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) cRel.x + cTip.x(), (float) cRel.y + cTip.y(), (float) cRel.z + cTip.z()).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x(), (float) pRel.y + pTip.y(), (float) pRel.z + pTip.z()).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        // 正面三角形2: 当前基底 -> 前一尖端 -> 前一基底
        consumer.addVertex(pose, (float) pRel.x + pBase.x(), (float) pRel.y + pBase.y(), (float) pRel.z + pBase.z()).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        // 反面三角形1（法线反向，实现双面渲染）
        consumer.addVertex(pose, (float) cRel.x + cBase.x(), (float) cRel.y + cBase.y(), (float) cRel.z + cBase.z()).setColor(cBaseC).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pBase.x(), (float) pRel.y + pBase.y(), (float) pRel.z + pBase.z()).setColor(pBaseC).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) pRel.x + pTip.x(), (float) pRel.y + pTip.y(), (float) pRel.z + pTip.z()).setColor(pTipC).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        // 反面三角形2
        consumer.addVertex(pose, (float) cRel.x + cTip.x(), (float) cRel.y + cTip.y(), (float) cRel.z + cTip.z()).setColor(cTipC).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
    }
}