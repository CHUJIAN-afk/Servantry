package first.servantry.api.client.render;

import first.servantry.api.PathNode;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.TrailConfig;
import first.servantry.api.entity.AttachmentEntity;

/**
 * 渲染上下文，封装附件实体渲染所需的所有参数和配置。
 * <p>
 * 采用强类型配置分离模式：
 * <ul>
 *   <li>{@link TrailConfig} - 拖尾渲染配置（通过子类区分类型）</li>
 *   <li>{@link ModelConfig} - 模型渲染配置</li>
 * </ul>
 * </p>
 *
 * @param <T> 附件实体类型
 * @see TrailConfig
 * @see ModelConfig
 * @see AbstractAttachmentEntityRenderer
 */
public class RenderContext<T extends AttachmentEntity> {

    /**
     * 拖尾配置，null 表示无拖尾
     */
    public final TrailConfig<T, ?> trail;

    /** 模型配置 */
    public final ModelConfig<T> model;

    private RenderContext(TrailConfig<T, ?> trail, ModelConfig<T> model) {
        this.trail = trail;
        this.model = model;
    }

    /** 创建 Builder 实例 */
    public static <T extends AttachmentEntity> Builder<T> builder() {
        return new Builder<>();
    }

    // ===================== 函数式接口定义 =====================

    /**
     * 是否有拖尾
     */
    public boolean hasTrail() {
        return trail != null && trail.timer > 0;
    }

    /** 颜色计算函数 */
    @FunctionalInterface
    public interface ColorFunction<T extends AttachmentEntity> {
        int getColor(T entity, float progress, float timeShift);
    }

    /** 淡出函数 */
    @FunctionalInterface
    public interface FadeFunction {
        float getFade(float progress);
    }

    /** 透明度增强函数 */
    @FunctionalInterface
    public interface AlphaBoostFunction<T extends AttachmentEntity> {
        float getBoost(T entity, float progress);
    }

    /** 亮度增强函数 */
    @FunctionalInterface
    public interface BrightnessBoostFunction<T extends AttachmentEntity> {
        float getBoost(T entity, float progress);
    }

    // ===================== Builder =====================

    /** 视觉节点计算函数 */
    @FunctionalInterface
    public interface VisualNodeFunction<T extends AttachmentEntity> {
        PathNode getVisualNode(T entity, float partialTick, PathNode rawNode);
    }

    /** 渲染上下文构建器 */
    public static class Builder<T extends AttachmentEntity> {
        private TrailConfig<T, ?> trail;
        private ModelConfig<T> model = new ModelConfig<>();

        /** 设置拖尾配置 */
        public Builder<T> trail(TrailConfig<T, ?> trail) {
            this.trail = trail;
            return this;
        }

        /** 设置模型配置 */
        public Builder<T> model(ModelConfig<T> model) {
            this.model = model;
            return this;
        }

        /** 构建渲染上下文 */
        public RenderContext<T> build() {
            return new RenderContext<>(trail, model);
        }
    }
}
