package first.servantry.api.client.render.renderConfig;

import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.AttachmentEntity;

/**
 * 圆锥拖尾配置。
 * <p>
 * 适用于球状、圆形、能量弹、魔法球等实体。
 * </p>
 * <pre>{@code
 * 效果示意：
 *     ╭──╮
 *    ╱    ╲
 *   │      │
 *    ╲    ╱
 *     ╰──╯
 * 头→尾
 * }</pre>
 *
 * @param <T> 实体类型
 */
public class ConeTrailConfig<T extends AttachmentEntity> extends TrailConfig<T, ConeTrailConfig<T>> {

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

    public ConeTrailConfig<T> maxRadius(float radius) {
        this.maxRadius = radius;
        return this;
    }

    public ConeTrailConfig<T> minRadiusRatio(float ratio) {
        this.minRadiusRatio = ratio;
        return this;
    }

    public ConeTrailConfig<T> resolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    @Override
    public RenderContext.TrailType getType() {
        return RenderContext.TrailType.CONE;
    }
}
