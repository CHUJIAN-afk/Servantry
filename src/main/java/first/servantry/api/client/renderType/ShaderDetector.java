package first.servantry.api.client.renderType;

import first.servantry.Servantry;
import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 光影状态检测工具。
 * <p>
 * 用于检测当前是否启用了光影模组（Iris/Oculus）。
 * </p>
 */
@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ShaderDetector {

    private static boolean isShaderPackInUse = false;

    @SubscribeEvent
    public static void client(ClientTickEvent.Post event) {
        isShaderPackInUse = ModList.get().isLoaded("iris") && IrisApi.getInstance().isShaderPackInUse();
    }

    /**
     * 检测当前是否启用了光影。
     *
     * @return true 如果光影已启用
     */
    public static boolean isShaderEnabled() {
        return isShaderPackInUse;
    }

}
