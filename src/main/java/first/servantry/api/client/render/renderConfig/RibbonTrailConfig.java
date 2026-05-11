package first.servantry.api.client.render.renderConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.AttachmentEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * 丝带拖尾配置。
 * <p>
 * 适用于剑状、刀锋、扁平物体、有明显方向性的实体。
 * </p>
 * <pre>{@code
 * 三角形截面：
 *        * 尖端（朝前）
 *       /|\
 *      / | \
 *     *--+--*
 *    左  基部  右
 * }</pre>
 *
 * @param <T> 实体类型
 */
public class RibbonTrailConfig<T extends AttachmentEntity> extends TrailConfig<T, RibbonTrailConfig<T>> {

    /**
     * 丝带宽度（三角形高度）
     */
    public float width = 0.15f;

    /** 丝带棱形大小（三角形底边长度） */
    public float diamondSize = 0.3f;

    /** 尖端透明度增强函数 */
    public RenderContext.AlphaBoostFunction<T> tipAlphaBoost = (entity, progress) -> 1.0f;

    /** 尖端亮度增强函数 */
    public RenderContext.BrightnessBoostFunction<T> tipBrightnessBoost = (entity, progress) -> 1.0f;

    public RibbonTrailConfig<T> width(float width) {
        this.width = width;
        return this;
    }

    public RibbonTrailConfig<T> diamondSize(float size) {
        this.diamondSize = size;
        return this;
    }

    public RibbonTrailConfig<T> tipAlphaBoost(RenderContext.AlphaBoostFunction<T> function) {
        this.tipAlphaBoost = function;
        return this;
    }

    public RibbonTrailConfig<T> tipBrightnessBoost(RenderContext.BrightnessBoostFunction<T> function) {
        this.tipBrightnessBoost = function;
        return this;
    }

    @Override
    public void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource,
                       float partialTick, PathNode visualNode, RenderType renderType) {
        List<InterpolatedNode> smoothNodes = buildSmoothNodes(entity, visualNode);
        if (smoothNodes.size() < 2) return;

        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Matrix4f pose = poseStack.last().pose();

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

            Vector3f currTip = new Vector3f(0, 0, width).rotate(curr.rot());
            Vector3f currLeft = new Vector3f(-diamondSize * 0.5f * currScale, 0, 0).rotate(curr.rot());
            Vector3f currRight = new Vector3f(diamondSize * 0.5f * currScale, 0, 0).rotate(curr.rot());

            Vector3f prevTip = new Vector3f(0, 0, width).rotate(prev.rot());
            Vector3f prevLeft = new Vector3f(-diamondSize * 0.5f * prevScale, 0, 0).rotate(prev.rot());
            Vector3f prevRight = new Vector3f(diamondSize * 0.5f * prevScale, 0, 0).rotate(prev.rot());

            int currColorRGB = colorFunction.getColor(entity, currProgress, timeShift);
            int prevColorRGB = colorFunction.getColor(entity, prevProgress, timeShift);
            float currAlphaBoost = tipAlphaBoost.getBoost(entity, currProgress);
            float currBrightBoost = tipBrightnessBoost.getBoost(entity, currProgress);
            float prevAlphaBoost = tipAlphaBoost.getBoost(entity, prevProgress);
            float prevBrightBoost = tipBrightnessBoost.getBoost(entity, prevProgress);

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

            Vec3 currRel = curr.pos().subtract(renderPos);
            Vec3 prevRel = prev.pos().subtract(renderPos);

            emitTriangleStrip(consumer, pose, currRel, prevRel,
                    currTip, prevTip, currLeft, prevLeft, currRight, prevRight,
                    currTipColor, prevTipColor, currBaseColor, prevBaseColor);
        }
    }

    private void emitTriangleStrip(VertexConsumer consumer, Matrix4f pose,
                                   Vec3 currRel, Vec3 prevRel,
                                   Vector3f currTip, Vector3f prevTip,
                                   Vector3f currLeft, Vector3f prevLeft,
                                   Vector3f currRight, Vector3f prevRight,
                                   int currTipColor, int prevTipColor,
                                   int currBaseColor, int prevBaseColor) {
        // 左侧三角形正面
        emitQuad(consumer, pose,
                (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z, currTipColor,
                (float) currRel.x + currLeft.x, (float) currRel.y + currLeft.y, (float) currRel.z + currLeft.z, currBaseColor,
                (float) prevRel.x + prevLeft.x, (float) prevRel.y + prevLeft.y, (float) prevRel.z + prevLeft.z, prevBaseColor,
                (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z, prevTipColor);

        // 左侧三角形反面
        emitQuad(consumer, pose,
                (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z, currTipColor,
                (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z, prevTipColor,
                (float) prevRel.x + prevLeft.x, (float) prevRel.y + prevLeft.y, (float) prevRel.z + prevLeft.z, prevBaseColor,
                (float) currRel.x + currLeft.x, (float) currRel.y + currLeft.y, (float) currRel.z + currLeft.z, currBaseColor);

        // 右侧三角形正面
        emitQuad(consumer, pose,
                (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z, currTipColor,
                (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z, prevTipColor,
                (float) prevRel.x + prevRight.x, (float) prevRel.y + prevRight.y, (float) prevRel.z + prevRight.z, prevBaseColor,
                (float) currRel.x + currRight.x, (float) currRel.y + currRight.y, (float) currRel.z + currRight.z, currBaseColor);

        // 右侧三角形反面
        emitQuad(consumer, pose,
                (float) currRel.x + currTip.x, (float) currRel.y + currTip.y, (float) currRel.z + currTip.z, currTipColor,
                (float) currRel.x + currRight.x, (float) currRel.y + currRight.y, (float) currRel.z + currRight.z, currBaseColor,
                (float) prevRel.x + prevRight.x, (float) prevRel.y + prevRight.y, (float) prevRel.z + prevRight.z, prevBaseColor,
                (float) prevRel.x + prevTip.x, (float) prevRel.y + prevTip.y, (float) prevRel.z + prevTip.z, prevTipColor);
    }
}
