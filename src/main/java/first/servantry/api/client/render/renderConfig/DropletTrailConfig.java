package first.servantry.api.client.render.renderConfig;

import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.AttachmentEntity;

/**
 * 水滴拖尾配置（圆锥 + 头部半球）。
 * <p>
 * 适用于需要圆润头部的拖尾效果。
 * </p>
 * <pre>{@code
 * 效果示意：
 *       ╭─╮  ← 半球顶部
 *      ╱   ╲
 *     │     │
 *      ╲   ╱
 *       ╰─╯  ← 圆锥尾部
 * 头→尾
 * }</pre>
 *
 * @param <T> 实体类型
 */
public class DropletTrailConfig<T extends AttachmentEntity> extends TrailConfig<T, DropletTrailConfig<T>> {

    /**
     * 拖尾头部最大半径
     */
    public float maxRadius = 0.2f;

    /**
     * 最小半径比例，控制尾端不会完全缩成一点
     */
    public float minRadiusRatio = 0.0f;

    /**
     * 圆锥截面正多边形边数
     */
    public int resolution = 6;

    public DropletTrailConfig<T> maxRadius(float radius) {
        this.maxRadius = radius;
        return this;
    }

    public DropletTrailConfig<T> minRadiusRatio(float ratio) {
        this.minRadiusRatio = ratio;
        return this;
    }

    public DropletTrailConfig<T> resolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    @Override
    public RenderContext.TrailType getType() {
        return RenderContext.TrailType.DROPLET;
    }
}
