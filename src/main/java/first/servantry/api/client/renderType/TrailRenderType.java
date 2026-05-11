package first.servantry.api.client.renderType;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.servantry.Servantry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * 轨迹渲染类型。
 * <p>
 * 自动检测光影状态并选择最佳渲染方案：
 * <ul>
 *   <li>无光影：使用自定义着色器，效果最佳</li>
 *   <li>有光影：使用原版自发光着色器，完美兼容 Iris/Oculus</li>
 * </ul>
 * 所有轨迹都是自发光效果，不受世界光照影响。
 * </p>
 */
public class TrailRenderType extends RenderType {
    private TrailRenderType(String name, VertexFormat fmt, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sort, Runnable setup, Runnable clear) {
        super(name, fmt, mode, bufSize, affectsCrumbling, sort, setup, clear);
    }

    private static final ResourceLocation TRAIL_TEXTURE = Servantry.rl("textures/trail.png");

    // ===================== 公共 API =====================

    /**
     * 获取轨迹渲染类型。
     * <p>
     * 自动检测光影状态：
     * <ul>
     *   <li>无光影：使用自定义着色器，效果最佳</li>
     *   <li>有光影：使用原版自发光着色器，光影兼容</li>
     * </ul>
     * </p>
     *
     * @return 适合当前环境的渲染类型
     */
    public static RenderType getTrail() {
        // 光影启用时使用原版着色器，否则使用自定义着色器
        if (ShaderDetector.isShaderEnabled()) {
            return Internal.TRAIL_VANILLA;
        } else {
            return Internal.TRAIL_CUSTOM;
        }
    }

    // ===================== 内部实现 =====================

    /**
     * 延迟初始化的渲染类型实例。
     */
    private static class Internal {
        // 原版自发光着色器（光影兼容）
        static final RenderType TRAIL_VANILLA = createTrailVanilla();
        // 自定义着色器（无光影时效果最佳）
        static final RenderType TRAIL_CUSTOM = createTrailCustom();

        private static RenderType createTrailVanilla() {
            // 使用原版自发光着色器，Iris 会正确处理
            CompositeState state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)  // 写颜色，写深度
                    .setOverlayState(OVERLAY)
                    .createCompositeState(false);
            return create("servantry_trail_vanilla", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
        }

        private static RenderType createTrailCustom() {
            // 使用自定义着色器，无光影时效果最佳
            ShaderStateShard customShader = new ShaderStateShard(() -> TrailShaders.trailShader);
            CompositeState state = CompositeState.builder()
                    .setShaderState(customShader)
                    .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)  // 只写颜色，不写深度
                    .setOverlayState(OVERLAY)
                    .createCompositeState(false);
            return create("servantry_trail_custom", TrailShaders.TRAIL_FORMAT, VertexFormat.Mode.QUADS, 256, false, true, state);
        }
    }
}
