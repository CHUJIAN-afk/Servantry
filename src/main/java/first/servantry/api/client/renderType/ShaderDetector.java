package first.servantry.api.client.renderType;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;

public class ShaderDetector {

    /**
     * 检测当前是否启用了光影。
     *
     * @return true 如果光影已启用
     */
    public static boolean isShaderEnabled() {
        return ModList.get().isLoaded("iris") && IrisApi.getInstance().isShaderPackInUse();
    }
}
