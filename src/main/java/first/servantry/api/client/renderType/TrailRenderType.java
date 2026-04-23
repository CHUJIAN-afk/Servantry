package first.servantry.api.client.renderType;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.servantry.Servantry;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * 拖尾渲染类型。
 * <p>
 * 提供多种渲染方案以兼容原版和光影模组环境：
 * <ul>
 *   <li>{@link #getTrail()} - 标准透明拖尾（推荐）</li>
 *   <li>{@link #getTrailUnlit()} - 无光照透明拖尾，光影兼容性更好</li>
 *   <li>{@link #getTrailAdditive()} - 加法混合拖尾，适合发光效果</li>
 * </ul>
 * </p>
 */
public class TrailRenderType extends RenderType {
    private TrailRenderType(String name, VertexFormat fmt, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sort, Runnable setup, Runnable clear) {
        super(name, fmt, mode, bufSize, affectsCrumbling, sort, setup, clear);
    }

    private static final ResourceLocation TRAIL_TEXTURE = Servantry.rl("textures/trail.png");

    // 缓存渲染类型实例
    private static final RenderType TRAIL = createTrail();
    private static final RenderType TRAIL_UNLIT = createTrailUnlit();
    private static final RenderType TRAIL_ADDITIVE = createTrailAdditive();

    /**
     * 获取标准透明拖尾渲染类型。
     * <p>
     * 使用标准实体透明着色器，特点：
     * <ul>
     *   <li>标准透明度混合</li>
     *   <li>禁用背面剔除实现双面渲染</li>
     *   <li>启用光照图支持全亮度</li>
     *   <li>启用深度排序</li>
     * </ul>
     * 适用于大多数情况，光影兼容性良好。
     * </p>
     */
    public static RenderType getTrail() {
        return TRAIL;
    }

    /**
     * 获取无光照透明拖尾渲染类型。
     * <p>
     * 使用自定义无光照着色器，特点：
     * <ul>
     *   <li>禁用漫反射光照，全亮度四边形显示正确</li>
     *   <li>标准透明度混合</li>
     *   <li>光影模组兼容性最佳</li>
     * </ul>
     * 推荐在光影环境下使用。
     * </p>
     */
    public static RenderType getTrailUnlit() {
        return TRAIL_UNLIT;
    }

    /**
     * 获取加法混合拖尾渲染类型。
     * <p>
     * 使用加法透明度混合，特点：
     * <ul>
     *   <li>颜色叠加效果，适合发光拖尾</li>
     *   <li>不写入深度缓冲，避免深度冲突</li>
     *   <li>光影模组通常对加法混合有良好支持</li>
     * </ul>
     * 适合能量武器、魔法效果等发光拖尾。
     * </p>
     */
    public static RenderType getTrailAdditive() {
        return TRAIL_ADDITIVE;
    }

    // ===================== 内部创建方法 =====================

    private static RenderType createTrail() {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(false);
        return create("servantry_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    private static RenderType createTrailUnlit() {
        // 无光照着色器状态 - 使用实体透明着色器但禁用漫反射
        // 注意：这里使用 RENDERTYPE_ENTITY_TRANSLUCENT_SHADER，但通过 FULL_BRIGHT 光照实现无光照效果
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                // 禁用深度写入，避免透明物体遮挡问题
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return create("servantry_trail_unlit", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    private static RenderType createTrailAdditive() {
        // 加法透明度状态
        RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
                "additive_transparency",
                () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                },
                () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }
        );

        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(TRAIL_TEXTURE, false, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return create("servantry_trail_additive", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
    }
}
