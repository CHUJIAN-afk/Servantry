package first.servantry.api.client.render.renderConfig;

import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.AttachmentEntity;

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

    /**
     * 丝带棱形大小（三角形底边长度）
     */
    public float diamondSize = 0.3f;

    /**
     * 尖端透明度增强函数
     */
    public RenderContext.AlphaBoostFunction<T> tipAlphaBoost = (entity, progress) -> 1.0f;

    /**
     * 尖端亮度增强函数
     */
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
    public RenderContext.TrailType getType() {
        return RenderContext.TrailType.RIBBON;
    }
}