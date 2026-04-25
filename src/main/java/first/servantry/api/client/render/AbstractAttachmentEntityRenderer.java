package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.PathNode;
import first.servantry.api.client.renderType.TrailRenderType;
import first.servantry.api.entity.AttachmentEntity;
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
 * 提供完整的拖尾渲染和本体渲染框架，子类只需实现两个核心方法：
 * </p>
 * <pre>{@code
 * public class MyRenderer extends AbstractAttachmentEntityRenderer<MyEntity> {
 *
 *     @Override
 *     protected RenderContext<MyEntity> createContext(MyEntity entity) {
 *         // 返回渲染配置
 *         return RenderContext.cone(entity.getTrailTimer(), 0xFF0000, 0.2f);
 *     }
 *
 *     @Override
 *     protected void renderEntity(MyEntity entity, PoseStack poseStack,
 *                                 MultiBufferSource bufferSource,
 *                                 PathNode visualNode, RenderContext<MyEntity> config) {
 *         // 渲染实体模型
 *         Minecraft.getInstance().getItemRenderer().renderStatic(...);
 *     }
 * }
 * }</pre>
 *
 * <h2>渲染流程</h2>
 * <pre>{@code
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                          渲染流程图                                      │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │   render() 入口                                                         │
 * │       │                                                                 │
 * │       ▼                                                                 │
 * │   ┌───────────────┐                                                     │
 * │   │ createContext │ ← 子类实现，返回渲染配置                             │
 * │   └───────┬───────┘                                                     │
 * │           │                                                             │
 * │           ▼                                                             │
 * │   ┌───────────────────┐                                                 │
 * │   │ 计算视觉节点       │ ← 应用 visualNodeFunction 插值                  │
 * │   └───────┬───────────┘                                                 │
 * │           │                                                             │
 * │           ▼                                                             │
 * │   ┌───────────────────┐                                                 │
 * │   │ 渲染拖尾          │ ← 根据 trailType 分发到圆锥/丝带渲染              │
 * │   │ (在模型后面)      │                                                   │
 * │   └───────┬───────────┘                                                 │
 * │           │                                                             │
 * │           ▼                                                             │
 * │   ┌───────────────────┐                                                 │
 * │   │ 应用变换          │ ← yaw/pitch/roll + 偏移 + 缩放                   │
 * │   └───────┬───────────┘                                                 │
 * │           │                                                             │
 * │           ▼                                                             │
 * │   ┌───────────────────┐                                                 │
 * │   │ renderEntity      │ ← 子类实现，渲染具体模型                         │
 * │   └───────────────────┘                                                 │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * }</pre>
 *
 * <h2>拖尾渲染原理</h2>
 *
 * <h3>历史节点收集</h3>
 * <pre>{@code
 * 实体每帧记录当前位置和旋转，形成历史节点队列：
 *
 *   历史队列（最新在前）：
 *   [当前帧] ← [帧-1] ← [帧-2] ← [帧-3] ← ...
 *      ↑
 *   visualNode (当前渲染位置)
 *
 *   trailHistoryLength 决定取多少个历史节点
 * }</pre>
 *
 * <h3>Catmull-Rom 插值</h3>
 * <pre>{@code
 * 原始节点之间通过 Catmull-Rom 样条插值生成平滑曲线：
 *
 *   原始节点:    *--------*--------*--------*
 *                P0       P1       P2       P3
 *
 *   插值后:      *-*-*-*-*-*-*-*-*-*-*-*-*
 *                ↑ trailSegmentsPerNode 控制插值密度
 * }</pre>
 *
 * <h3>圆锥拖尾渲染</h3>
 * <pre>{@code
 * 每个节点绘制一个正多边形截面，半径随进度递减：
 *
 *   节点0 (progress=0)   节点1            节点2            节点3 (progress=1)
 *       ╭──╮              ╭─╮              ╭╮               *
 *      ╱    ╲            ╱   ╲            ╱ ╲              (半径≈0)
 *     │      │          │     │          │   │
 *      ╲    ╱            ╲   ╱            ╲ ╱
 *       ╰──╯              ╰─╯              ╰╯
 *
 *   相邻截面之间用四边形面片连接，形成锥形管道
 * }</pre>
 *
 * <h3>丝带拖尾渲染</h3>
 * <pre>{@code
 * 每个节点绘制一个三角形截面，尖端朝前：
 *
 *   节点0 (大)         节点1            节点2            节点3 (小)
 *        *                 *               *               *
 *       /|\               /|\             /|\              |
 *      / | \             / | \           / | \             |
 *     *--+--*           *--+--*         *--+--*            *
 *
 *   相邻三角形之间连接形成三角带，正反两面都渲染
 * }</pre>
 *
 * <h2>性能优化</h2>
 * <ul>
 *   <li>圆形顶点缓存：预计算 cos/sin 值，避免重复计算</li>
 *   <li>向量复用：在循环中复用 Vector3f 对象</li>
 *   <li>按需渲染：trailTimer <= 0 时跳过拖尾渲染</li>
 * </ul>
 *
 * @param <T> 附件实体类型
 * @see RenderContext
 * @see IAttachmentEntityRenderer
 */
public abstract class AbstractAttachmentEntityRenderer<T extends AttachmentEntity> implements IAttachmentEntityRenderer<T> {

    // ===================== 核心抽象方法 =====================

    /**
     * 为指定附件实体创建渲染上下文。
     * <p>
     * 返回的上下文决定了拖尾类型、颜色、大小等所有渲染参数。
     * </p>
     *
     * @param entity 附件实体实例
     * @return 渲染上下文，若返回 null 则跳过渲染
     */
    protected abstract RenderContext<T> createContext(T entity);

    /**
     * 渲染附件实体本体。
     * <p>
     * 在调用此方法前，PoseStack 已完成以下变换：
     * <ul>
     *   <li>平移到视觉位置（支持插值）</li>
     *   <li>应用旋转（yaw/pitch/roll + 配置中的偏移）</li>
     *   <li>应用缩放</li>
     * </ul>
     * 子类只需在此方法中绘制具体的模型即可。
     * </p>
     *
     * @param entity      附件实体实例
     * @param poseStack   变换矩阵栈
     * @param bufferSource 顶点缓冲源
     * @param visualNode  视觉节点（插值后的位置和朝向）
     * @param config      渲染配置
     */
    protected abstract void renderEntity(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<T> config);

    // ===================== 主渲染入口 =====================

    /**
     * 主渲染方法，由渲染调度器调用。
     * <p>
     * 执行流程：
     * <ol>
     *   <li>创建渲染配置</li>
     *   <li>计算视觉节点（支持位置插值）</li>
     *   <li>渲染拖尾（在模型后面）</li>
     *   <li>渲染本体模型</li>
     * </ol>
     * </p>
     */
    @Override
    public void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        // 1. 创建渲染配置
        RenderContext<T> config = createContext(entity);
        if (config == null) return;

        // 2. 获取视觉节点（支持插值平滑）
        PathNode visualNode = config.visualNodeFunction.getVisualNode(entity, partialTick, renderNode);
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);


        // 3. 先渲染拖尾（在模型后面，避免遮挡）
        if (config.trailType != RenderContext.TrailType.NONE && config.trailTimer > 0) {
            renderTrail(entity, poseStack, bufferSource, partialTick, visualNode, config);
        }
        // 4. 渲染本体模型
        poseStack.pushPose();

        // 应用旋转：先应用实体的朝向
        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));

        // 应用配置中的旋转偏移
        poseStack.mulPose(Axis.YN.rotationDegrees(config.modelYawOffset));
        poseStack.mulPose(Axis.XP.rotationDegrees(config.modelPitchOffset));
        poseStack.mulPose(Axis.ZP.rotationDegrees(config.modelRollOffset));

        // 应用平移偏移（修正模型旋转中心）
        poseStack.translate(config.modelTranslateX, config.modelTranslateY, config.modelTranslateZ);

        // 应用缩放
        poseStack.scale(config.modelScale, config.modelScale, config.modelScale);

        // 调用子类实现的具体渲染
        renderEntity(entity, poseStack, bufferSource, visualNode, config);

        poseStack.popPose();
        poseStack.popPose();
    }

    // ===================== 拖尾渲染 =====================

    /**
     * 根据配置获取拖尾渲染类型。
     *
     * @param config 渲染配置
     * @return 对应的渲染类型
     */
    protected RenderType getTrailRenderType(RenderContext<T> config) {
        return switch (config.trailShaderType) {
            case UNLIT -> TrailRenderType.getTrailUnlit();
            case ADDITIVE -> TrailRenderType.getTrailAdditive();
            default -> TrailRenderType.getTrail();
        };
    }

    /**
     * 渲染拖尾，根据配置的拖尾类型分发到具体渲染方法。
     */
    protected void renderTrail(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, RenderContext<T> config) {
        switch (config.trailType) {
            case CONE -> renderConeTrail(entity, poseStack, bufferSource, partialTick, visualNode, config);
            case RIBBON -> renderRibbonTrail(entity, poseStack, bufferSource, partialTick, visualNode, config);
            default -> {}
        }
    }

    // ===================== 圆锥拖尾渲染 =====================

    /** 圆形顶点缓存，避免重复计算三角函数 */
    private static final Map<Integer, CircleVertexCache> CIRCLE_CACHE = new HashMap<>();

    /**
     * 渲染圆锥形拖尾。
     * <p>
     * 特点：
     * <ul>
     *   <li>使用 Catmull-Rom 样条插值实现平滑曲线</li>
     *   <li>每个节点处绘制一个正多边形截面</li>
     *   <li>截面半径随进度递减，形成锥形效果</li>
     *   <li>支持颜色渐变和透明度淡出</li>
     * </ul>
     * </p>
     */
    private void renderConeTrail(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, RenderContext<T> config) {
        // 准备平滑节点列表
        List<InterpolatedNode> smoothNodes = buildSmoothNodes(entity, visualNode, config);
        if (smoothNodes.size() < 2) return;

        // 根据配置选择渲染类型
        VertexConsumer consumer = bufferSource.getBuffer(getTrailRenderType(config));
        Matrix4f pose = poseStack.last().pose();
        CircleVertexCache cache = CIRCLE_CACHE.computeIfAbsent(config.trailResolution, CircleVertexCache::new);

        // 计算时间偏移（用于动态颜色效果）
        Player owner = entity.getOwner();
        float timeShift = owner != null ? (owner.tickCount + partialTick) * 0.015f : 0f;

        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        // 可复用的向量，避免在循环中创建
        Vector3f currV1 = new Vector3f(), currV2 = new Vector3f();
        Vector3f prevV1 = new Vector3f(), prevV2 = new Vector3f();

        // 遍历相邻节点对，绘制连接的圆环面片
        for (int i = 0; i < nodeCount - 1; i++) {
            InterpolatedNode curr = smoothNodes.get(i);
            InterpolatedNode prev = smoothNodes.get(i + 1);

            // 计算进度（0 = 头部，1 = 尾部）
            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            // 计算淡出系数和半径
            float currFade = config.trailFadeOut.getFade(currProgress);
            float prevFade = config.trailFadeOut.getFade(prevProgress);
            float currRadius = config.trailMaxRadius * currFade;
            float prevRadius = config.trailMaxRadius * prevFade;

            // 获取颜色
            int currColor = config.trailColorFunction.getColor(entity, currProgress, timeShift);
            int prevColor = config.trailColorFunction.getColor(entity, prevProgress, timeShift);

            // 计算最终颜色（带透明度）
            int currAlpha = Math.round(currFade * 200);
            int prevAlpha = Math.round(prevFade * 200);
            int currARGB = FastColor.ARGB32.color(currAlpha, (currColor >> 16) & 0xFF, (currColor >> 8) & 0xFF, currColor & 0xFF);
            int prevARGB = FastColor.ARGB32.color(prevAlpha, (prevColor >> 16) & 0xFF, (prevColor >> 8) & 0xFF, prevColor & 0xFF);

            // 计算相对位置
            Vec3 currRel = curr.pos.subtract(renderPos);
            Vec3 prevRel = prev.pos.subtract(renderPos);

            // 绘制圆环的每个扇形
            for (int j = 0; j < config.trailResolution; j++) {
                float cos1 = cache.cos(j), sin1 = cache.sin(j);
                float cos2 = cache.cos(j + 1), sin2 = cache.sin(j + 1);

                // 计算当前节点圆周上的两点
                currV1.set(cos1 * currRadius, sin1 * currRadius, 0).rotate(curr.rot);
                currV2.set(cos2 * currRadius, sin2 * currRadius, 0).rotate(curr.rot);

                // 计算前一个节点圆周上的两点
                prevV1.set(cos1 * prevRadius, sin1 * prevRadius, 0).rotate(prev.rot);
                prevV2.set(cos2 * prevRadius, sin2 * prevRadius, 0).rotate(prev.rot);

                // 绘制四边形面片
                emitQuad(consumer, pose,
                        (float) currRel.x + currV1.x, (float) currRel.y + currV1.y, (float) currRel.z + currV1.z,
                        (float) currRel.x + currV2.x, (float) currRel.y + currV2.y, (float) currRel.z + currV2.z,
                        (float) prevRel.x + prevV2.x, (float) prevRel.y + prevV2.y, (float) prevRel.z + prevV2.z,
                        (float) prevRel.x + prevV1.x, (float) prevRel.y + prevV1.y, (float) prevRel.z + prevV1.z,
                        currARGB, prevARGB);
            }
        }
    }

    // ===================== 丝带拖尾渲染 =====================

    /**
     * 渲染丝带形拖尾。
     * <p>
     * 特点：
     * <ul>
     *   <li>使用 Catmull-Rom 样条插值实现平滑曲线</li>
     *   <li>每个节点处绘制一个三角形截面（尖端向前）</li>
     *   <li>三角形正反两面都渲染，确保任意视角可见</li>
     *   <li>支持颜色渐变、亮度增强和透明度控制</li>
     * </ul>
     * </p>
     *
     * <h3>几何结构</h3>
     * <pre>{@code
     *      三角形截面（俯视图）：
     *
     *           tip (尖端，朝前)
     *            *
     *           /|\
     *          / | \
     *         /  |  \
     *        /   |   \
     *       *----+----*
     *     left  center right (基部)
     *
     *      参数说明：
     *      - ribbonWidth: 三角形的高（tip 到基线的距离，Z方向）
     *      - ribbonDiamondSize: 基线长度（left 到 right 的距离）
     * }</pre>
     *
     * <h3>渲染效果</h3>
     * <p>
     * 三角形从头部（进度 0）到尾部（进度 1）逐渐缩小并淡出，
     * 形成剑刃拖尾效果。尖端透明度较高，基部透明度较低，
     * 使拖尾看起来像剑刃的光芒。
     * </p>
     */
    private void renderRibbonTrail(T entity, PoseStack poseStack, MultiBufferSource bufferSource,
                                   float partialTick, PathNode visualNode, RenderContext<T> config) {
        // 准备平滑节点列表
        List<InterpolatedNode> smoothNodes = buildSmoothNodes(entity, visualNode, config);
        if (smoothNodes.size() < 2) return;

        // 根据配置选择渲染类型
        VertexConsumer consumer = bufferSource.getBuffer(getTrailRenderType(config));
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose last = poseStack.last();

        // 计算时间偏移
        Player owner = entity.getOwner();
        float timeShift = owner != null ? (owner.tickCount + partialTick) * 0.015f : 0f;

        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        // 遍历相邻节点对
        for (int i = 0; i < nodeCount - 1; i++) {
            InterpolatedNode curr = smoothNodes.get(i);
            InterpolatedNode prev = smoothNodes.get(i + 1);

            // 计算进度
            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            // 计算缩放（从头到尾递减）
            float currScale = Math.max(0.0f, 1.0f - currProgress);
            float prevScale = Math.max(0.0f, 1.0f - prevProgress);

            // 使用配置参数计算三角形截面
            // ribbonWidth: 三角形的高（从尖端到基部的距离，Z方向）
            // ribbonDiamondSize: 三角形的底边长度（左右方向的宽度）
            float height = config.ribbonWidth;
            float baseWidth = config.ribbonDiamondSize;

            // 计算当前节点的三角形顶点
            // 尖端在Z正方向（前方），基部在Z负方向（后方）
            Vector3f currTip = new Vector3f(0, 0, height).rotate(curr.rot);
            Vector3f currLeft = new Vector3f(-baseWidth * 0.5f * currScale, 0, 0).rotate(curr.rot);
            Vector3f currRight = new Vector3f(baseWidth * 0.5f * currScale, 0, 0).rotate(curr.rot);

            // 计算前一个节点的三角形顶点
            Vector3f prevTip = new Vector3f(0, 0, height).rotate(prev.rot);
            Vector3f prevLeft = new Vector3f(-baseWidth * 0.5f * prevScale, 0, 0).rotate(prev.rot);
            Vector3f prevRight = new Vector3f(baseWidth * 0.5f * prevScale, 0, 0).rotate(prev.rot);

            // 获取颜色和增强参数
            int currColorRGB = config.trailColorFunction.getColor(entity, currProgress, timeShift);
            int prevColorRGB = config.trailColorFunction.getColor(entity, prevProgress, timeShift);
            float currAlphaBoost = config.trailTipAlphaBoost.getBoost(entity, currProgress);
            float currBrightBoost = config.trailTipBrightnessBoost.getBoost(entity, currProgress);
            float prevAlphaBoost = config.trailTipAlphaBoost.getBoost(entity, prevProgress);
            float prevBrightBoost = config.trailTipBrightnessBoost.getBoost(entity, prevProgress);

            // 计算最终颜色
            int currR = Math.min(255, Math.round(((currColorRGB >> 16) & 0xFF) * currBrightBoost));
            int currG = Math.min(255, Math.round(((currColorRGB >> 8) & 0xFF) * currBrightBoost));
            int currB = Math.min(255, Math.round((currColorRGB & 0xFF) * currBrightBoost));
            int prevR = Math.min(255, Math.round(((prevColorRGB >> 16) & 0xFF) * prevBrightBoost));
            int prevG = Math.min(255, Math.round(((prevColorRGB >> 8) & 0xFF) * prevBrightBoost));
            int prevB = Math.min(255, Math.round((prevColorRGB & 0xFF) * prevBrightBoost));

            // 计算透明度（尖端较亮，基部较暗）
            int currTipAlpha = Math.min(255, Math.round(currScale * 0.1f * 255 * currAlphaBoost));
            int currBaseAlpha = Math.min(255, Math.round(Math.max(0f, 1.0f - currProgress * 2.5f) * 0.04f * 255 * currAlphaBoost));
            int prevTipAlpha = Math.min(255, Math.round(prevScale * 0.1f * 255 * prevAlphaBoost));
            int prevBaseAlpha = Math.min(255, Math.round(Math.max(0f, 1.0f - prevProgress * 2.5f) * 0.04f * 255 * prevAlphaBoost));

            int currTipColor = FastColor.ARGB32.color(currTipAlpha, currR, currG, currB);
            int currBaseColor = FastColor.ARGB32.color(currBaseAlpha, currR, currG, currB);
            int prevTipColor = FastColor.ARGB32.color(prevTipAlpha, prevR, prevG, prevB);
            int prevBaseColor = FastColor.ARGB32.color(prevBaseAlpha, prevR, prevG, prevB);

            // 计算相对位置
            Vec3 currRel = curr.pos.subtract(renderPos);
            Vec3 prevRel = prev.pos.subtract(renderPos);

            // 绘制三角形截面（正反两面）
            // 将两个相邻的三角形连接成一个三角带
            emitTriangleStrip(consumer, pose, last, currRel, prevRel,
                    currTip, prevTip, currLeft, prevLeft, currRight, prevRight,
                    currTipColor, prevTipColor, currBaseColor, prevBaseColor);
        }
    }

    // ===================== 节点插值计算 =====================

    /**
     * 构建平滑插值节点列表。
     * <p>
     * 使用 Catmull-Rom 样条插值在历史节点之间生成平滑曲线。
     * </p>
     *
     * @param entity     附件实体
     * @param visualNode 当前视觉节点
     * @param config     渲染配置
     * @return 插值后的节点列表
     */
    private List<InterpolatedNode> buildSmoothNodes(T entity, PathNode visualNode, RenderContext<T> config) {
        LinkedList<PathNode> history = entity.getHistoryNodes();
        int actualLength = Math.min(history.size(), config.trailHistoryLength);
        if (actualLength < 2) return List.of();

        // 将历史节点复制到数组，并用视觉节点替换第一个节点
        PathNode[] nodes = new PathNode[actualLength];
        Iterator<PathNode> iterator = history.iterator();
        for (int i = 0; i < actualLength; i++) {
            nodes[i] = iterator.next();
        }
        nodes[0] = new PathNode(visualNode.pos(), visualNode.yaw(), visualNode.pitch(), visualNode.roll());

        // 确定插值范围
        int endIndex = nodes.length - 1;
        int startIndex = Math.max(0, Math.min(config.trailStartIndex, endIndex - 1));

        // 执行 Catmull-Rom 插值
        int segments = config.trailSegmentsPerNode;
        List<InterpolatedNode> result = new ArrayList<>((endIndex - startIndex) * segments + 1);
        Quaternionf tempQuat = new Quaternionf();

        for (int i = startIndex; i < endIndex; i++) {
            // 获取四个控制点（边界处使用边界点）
            PathNode p0 = nodes[Math.max(i - 1, startIndex)];
            PathNode p1 = nodes[i];
            PathNode p2 = nodes[i + 1];
            PathNode p3 = nodes[Math.min(i + 2, endIndex)];

            // 预计算端点的四元数
            Quaternionf q1 = eulerToQuaternion(p1.yaw(), p1.pitch(), p1.roll());
            Quaternionf q2 = eulerToQuaternion(p2.yaw(), p2.pitch(), p2.roll());

            // 在 p1 和 p2 之间插值
            for (int j = 0; j < segments; j++) {
                float t = (float) j / segments;
                InterpolatedNode node = catmullRomInterpolate(p0, p1, p2, p3, q1, q2, t, tempQuat);
                result.add(node);
            }
        }

        // 添加最后一个节点
        PathNode lastNode = nodes[endIndex];
        result.add(new InterpolatedNode(lastNode.pos(), eulerToQuaternion(lastNode.yaw(), lastNode.pitch(), lastNode.roll())));

        return result;
    }

    /**
     * Catmull-Rom 样条插值。
     *
     * @param p0, p1, p2, p3 四个控制点
     * @param q1, q2        p1 和 p2 的四元数
     * @param t             插值参数 [0, 1)
     * @param tempQuat      临时四元数（用于旋转插值）
     * @return 插值结果节点
     */
    private InterpolatedNode catmullRomInterpolate(PathNode p0, PathNode p1, PathNode p2, PathNode p3,
                                                   Quaternionf q1, Quaternionf q2, float t, Quaternionf tempQuat) {
        // Catmull-Rom 基函数系数
        float t2 = t * t, t3 = t2 * t;
        float f0 = -0.5f * t3 + t2 - 0.5f * t;
        float f1 = 1.5f * t3 - 2.5f * t2 + 1.0f;
        float f2 = -1.5f * t3 + 2.0f * t2 + 0.5f * t;
        float f3 = 0.5f * t3 - 0.5f * t2;

        // 位置插值
        Vec3 pos = new Vec3(
                p0.pos().x * f0 + p1.pos().x * f1 + p2.pos().x * f2 + p3.pos().x * f3,
                p0.pos().y * f0 + p1.pos().y * f1 + p2.pos().y * f2 + p3.pos().y * f3,
                p0.pos().z * f0 + p1.pos().z * f1 + p2.pos().z * f2 + p3.pos().z * f3
        );

        // 旋转插值（球面线性插值）
        tempQuat.set(q1).slerp(q2, t);

        return new InterpolatedNode(pos, new Quaternionf(tempQuat));
    }

    // ===================== 顶点发射辅助方法 =====================

    /**
     * 发射一个四边形面片（用于圆锥拖尾）。
     */
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

    /**
     * 发射一个三角形带（正反两面）。
     * <p>
     * 将两个相邻节点的三角形截面连接成一个三角带，形成丝带拖尾效果。
     * </p>
     *
     * <h3>几何结构</h3>
     * <pre>{@code
     *      curr节点              prev节点
     *      (进度 i)             (进度 i+1)
     *
     *        tip *                 tip *
     *           /|\                   /|\
     *          / | \                 / | \
     *         /  |  \               /  |  \
     *   left *----+----* right  left *----+----* right
     *
     *      连接方式：
     *      - 三角形1: curr.tip -> curr.left -> prev.left -> prev.tip
     *      - 三角形2: curr.tip -> prev.tip -> prev.right -> curr.right
     * }</pre>
     *
     * @param consumer       顶点消费者
     * @param pose           变换矩阵
     * @param last           PoseStack 状态
     * @param currRel        当前节点相对位置
     * @param prevRel        前一节点相对位置
     * @param currTip        当前节点尖端顶点
     * @param prevTip        前一节点尖端顶点
     * @param currLeft       当前节点左侧顶点
     * @param prevLeft       前一节点左侧顶点
     * @param currRight      当前节点右侧顶点
     * @param prevRight      前一节点右侧顶点
     * @param currTipColor   当前尖端颜色
     * @param prevTipColor   前一尖端颜色
     * @param currBaseColor  当前基部颜色
     * @param prevBaseColor  前一基部颜色
     */
    private void emitTriangleStrip(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose last,
                                   Vec3 currRel, Vec3 prevRel,
                                   Vector3f currTip, Vector3f prevTip,
                                   Vector3f currLeft, Vector3f prevLeft,
                                   Vector3f currRight, Vector3f prevRight,
                                   int currTipColor, int prevTipColor,
                                   int currBaseColor, int prevBaseColor) {
        // === 左侧三角形（tip -> left） ===
        // 正面
        consumer.addVertex(pose, (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z)
                .setColor(currTipColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) currRel.x + currLeft.x, (float) currRel.y + currLeft.y, (float) currRel.z + currLeft.z)
                .setColor(currBaseColor).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevLeft.x, (float) prevRel.y + prevLeft.y, (float) prevRel.z + prevLeft.z)
                .setColor(prevBaseColor).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z)
                .setColor(prevTipColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        // 反面
        consumer.addVertex(pose, (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z)
                .setColor(currTipColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z)
                .setColor(prevTipColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevLeft.x, (float) prevRel.y + prevLeft.y, (float) prevRel.z + prevLeft.z)
                .setColor(prevBaseColor).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);
        consumer.addVertex(pose, (float) currRel.x + currLeft.x, (float) currRel.y + currLeft.y, (float) currRel.z + currLeft.z)
                .setColor(currBaseColor).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, -1, 0);

        // === 右侧三角形（tip -> right） ===
        // 正面
        consumer.addVertex(pose, (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z)
                .setColor(currTipColor).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z)
                .setColor(prevTipColor).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) prevRel.x + prevRight.x, (float) prevRel.y + prevRight.y, (float) prevRel.z + prevRight.z)
                .setColor(prevBaseColor).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);
        consumer.addVertex(pose, (float) currRel.x + currRight.x, (float) currRel.y + currRight.y, (float) currRel.z + currRight.z)
                .setColor(currBaseColor).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(last, 0, 1, 0);

        // 反面
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

    /**
     * 将欧拉角转换为四元数。
     */
    private Quaternionf eulerToQuaternion(float yaw, float pitch, float roll) {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(-yaw))
                .rotateX((float) Math.toRadians(pitch))
                .rotateZ((float) Math.toRadians(roll));
    }

    // ===================== 内部数据类 =====================

    /**
     * 插值后的轨迹节点，包含位置和旋转。
     */
    private record InterpolatedNode(Vec3 pos, Quaternionf rot) {}

    /**
     * 圆形顶点缓存。
     * <p>
     * 预计算正多边形顶点的 cos/sin 值，避免在每帧渲染中重复计算。
     * </p>
     */
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
