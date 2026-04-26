package first.servantry.api.client.renderType;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.servantry.Servantry;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * 轨迹渲染类型。
 * <p>
 * 使用自定义着色器实现自发光轨迹效果，完美兼容光影模组（Iris/Oculus）。
 * 所有轨迹都是自发光效果，不受世界光照影响。
 * </p>
 *
 * <h3>渲染类型选择指南</h3>
 * <pre>{@code
 * ┌─────────────────┬────────────────────────────────────────────┐
 * │ 类型            │ 适用场景                                     │
 * ├─────────────────┼────────────────────────────────────────────┤
 * │ getTrail()      │ 标准透明轨迹，适合大多数情况                  │
 * │ getTrailUnlit() │ 无光照轨迹，光影兼容性最佳                    │
 * │ getTrailAdditive() │ 加法混合轨迹，适合能量/魔法效果            │
 * └─────────────────┴────────────────────────────────────────────┘
 * }</pre>
 */
public class TrailRenderType extends RenderType {
    private TrailRenderType(String name, VertexFormat fmt, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sort, Runnable setup, Runnable clear) {
        super(name, fmt, mode, bufSize, affectsCrumbling, sort, setup, clear);
    }

    private static final ResourceLocation TRAIL_TEXTURE = Servantry.rl("textures/trail.png");

    // ===================== 着色器状态分片 =====================

    /** 标准轨迹着色器状态 */
    private static final ShaderStateShard TRAIL_SHADER = new ShaderStateShard(
            () -> TrailShaders.trailShader
    );

    /** 加法混合轨迹着色器状态 */
    private static final ShaderStateShard TRAIL_ADDITIVE_SHADER = new ShaderStateShard(
            () -> TrailShaders.trailAdditiveShader
    );

    /** 无光照轨迹着色器状态 */
    private static final ShaderStateShard TRAIL_UNLIT_SHADER = new ShaderStateShard(
            () -> TrailShaders.trailUnlitShader
    );

    // ===================== 透明度状态分片 =====================

    /** 加法透明度状态 */
    private static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard(
            "trail_additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );

    // ===================== 公共 API =====================

    /**
     * 获取标准透明轨迹渲染类型。
     * <p>
     * 特点：
     * <ul>
     *   <li>标准透明度混合</li>
     *   <li>自发光效果，不受世界光照影响</li>
     *   <li>光影模组兼容性良好</li>
     * </ul>
     * </p>
     */
    public static RenderType getTrail() {
        return Internal.TRAIL;
    }

    /**
     * 获取无光照轨迹渲染类型。
     * <p>
     * 特点：
     * <ul>
     *   <li>自发光效果，不受世界光照影响</li>
     *   <li>光影模组兼容性最佳</li>
     *   <li>不写入深度缓冲</li>
     * </ul>
     * </p>
     */
    public static RenderType getTrailUnlit() {
        return Internal.TRAIL_UNLIT;
    }

    /**
     * 获取加法混合轨迹渲染类型。
     * <p>
     * 特点：
     * <ul>
     *   <li>颜色叠加效果，适合能量武器、魔法效果</li>
     *   <li>自发光效果，不受世界光照影响</li>
     *   <li>不写入深度缓冲</li>
     * </ul>
     * </p>
     */
    public static RenderType getTrailAdditive() {
        return Internal.TRAIL_ADDITIVE;
    }

    // ===================== 内部实现 =====================

    /**
     * 延迟初始化的渲染类型实例。
     * 由于着色器在资源加载后才可用，需要延迟创建渲染类型。
     */
    private static class Internal {
        static final RenderType TRAIL = createTrail();
        static final RenderType TRAIL_UNLIT = createTrailUnlit();
        static final RenderType TRAIL_ADDITIVE = createTrailAdditive();

        private static RenderType createTrail() {
            CompositeState state = CompositeState.builder()
                    .setShaderState(TRAIL_SHADER)
                    .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(false);
            return create("servantry_trail", TrailShaders.TRAIL_FORMAT, VertexFormat.Mode.QUADS, 256, false, true, state);
        }

        private static RenderType createTrailUnlit() {
            CompositeState state = CompositeState.builder()
                    .setShaderState(TRAIL_UNLIT_SHADER)
                    .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false);
            return create("servantry_trail_unlit", TrailShaders.TRAIL_FORMAT, VertexFormat.Mode.QUADS, 256, false, true, state);
        }

        private static RenderType createTrailAdditive() {
            CompositeState state = CompositeState.builder()
                    .setShaderState(TRAIL_ADDITIVE_SHADER)
                    .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false);
            return create("servantry_trail_additive", TrailShaders.TRAIL_FORMAT, VertexFormat.Mode.QUADS, 256, false, true, state);
        }
    }
}
