package first.servantry.api.client.renderType;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

/**
 * 轨迹着色器管理类。
 * <p>
 * 管理自定义轨迹渲染着色器的注册和引用。
 * 所有轨迹都是自发光效果，不受世界光照影响。
 * </p>
 */
public class TrailShaders {

    /** 标准透明轨迹着色器 */
    public static ShaderInstance trailShader;

    /** 加法混合轨迹着色器 */
    public static ShaderInstance trailAdditiveShader;

    /** 无光照轨迹着色器（光影兼容性最佳） */
    public static ShaderInstance trailUnlitShader;

    /** 轨迹顶点格式：Position + Color + UV0 + UV1 + UV2 + Normal */
    public static final VertexFormat TRAIL_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("UV1", VertexFormatElement.UV1)
            .add("UV2", VertexFormatElement.UV2)
            .add("Normal", VertexFormatElement.NORMAL)
            .padding(1)
            .build();

    /**
     * 在 RegisterShadersEvent 中调用此方法注册着色器。
     *
     * @param event 着色器注册事件
     */
    public static void register(RegisterShadersEvent event) throws IOException {
        // 标准透明轨迹着色器
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.parse("servantry:trail"),
                        TRAIL_FORMAT
                ),
                shader -> trailShader = shader
        );

        // 加法混合轨迹着色器
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.parse("servantry:trail_additive"),
                        TRAIL_FORMAT
                ),
                shader -> trailAdditiveShader = shader
        );

        // 无光照轨迹着色器
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.parse("servantry:trail_unlit"),
                        TRAIL_FORMAT
                ),
                shader -> trailUnlitShader = shader
        );
    }
}
