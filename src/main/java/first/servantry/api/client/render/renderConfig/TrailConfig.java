package first.servantry.api.client.render.renderConfig;

import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.AttachmentEntity;

/**
 * 拖尾渲染配置基类。
 * <p>
 * 使用强类型区分不同拖尾类型的配置参数，避免运行时类型检查。
 * 子类通过泛型自引用实现链式调用返回正确类型。
 * </p>
 *
 * @param <T>    实体类型
 * @param <SELF> 配置类自身类型（用于链式调用）
 */
public abstract class TrailConfig<T extends AttachmentEntity, SELF extends TrailConfig<T, SELF>> {

    // ===================== 基础参数 =====================

    /**
     * 拖尾计时器值，>0 时显示拖尾
     */
    public int timer = 0;

    /**
     * 历史节点数量，默认 4
     */
    public int historyLength = 4;

    /**
     * 每节点插值分段数，默认 8
     */
    public int segmentsPerNode = 8;

    /**
     * 拖尾起始索引，默认 0
     */
    public int startIndex = 0;

    // ===================== 颜色配置 =====================

    /**
     * 基础颜色 RGB
     */
    public int colorRGB = 0xFF0000;

    /**
     * 颜色函数
     */
    public RenderContext.ColorFunction<T> colorFunction = (entity, progress, timeShift) -> colorRGB;

    /**
     * 淡出函数
     */
    public RenderContext.FadeFunction fadeOut = progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);

    // ===================== 链式配置方法 =====================

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    public SELF timer(int timer) {
        this.timer = timer;
        return self();
    }

    public SELF historyLength(int length) {
        this.historyLength = length;
        return self();
    }

    public SELF segmentsPerNode(int segments) {
        this.segmentsPerNode = segments;
        return self();
    }

    public SELF startIndex(int index) {
        this.startIndex = index;
        return self();
    }

    public SELF colorRGB(int color) {
        this.colorRGB = color;
        return self();
    }

    public SELF colorFunction(RenderContext.ColorFunction<T> function) {
        this.colorFunction = function;
        return self();
    }

    public SELF fadeOut(RenderContext.FadeFunction function) {
        this.fadeOut = function;
        return self();
    }

    // ===================== 抽象方法 =====================

    /**
     * 获取拖尾类型
     */
    public abstract RenderContext.TrailType getType();
}
