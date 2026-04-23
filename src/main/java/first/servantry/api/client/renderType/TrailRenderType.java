package first.servantry.api.client.renderType;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.servantry.Servantry;
import net.minecraft.client.renderer.RenderType;

public class TrailRenderType extends RenderType {
    private TrailRenderType(String name, VertexFormat fmt, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sort, Runnable setup, Runnable clear) {
        super(name, fmt, mode, bufSize, affectsCrumbling, sort, setup, clear);
    }

    /**
     * 获取拖尾渲染类型。
     * <p>
     * 使用眼睛发光效果的着色器，光影模组通常对此有良好适配：
     * <ul>
     *   <li>使用眼睛发光着色器（Eyes）</li>
     *   <li>禁用背面剔除实现双面渲染</li>
     *   <li>启用光照图支持全亮度</li>
     *   <li>透明度混合模式确保正确的 Alpha 混合</li>
     * </ul>
     * </p>
     */
    public static RenderType getTrail() {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new TextureStateShard(Servantry.rl("textures/trail.png"), false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return create("ribbon_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
    }
}
