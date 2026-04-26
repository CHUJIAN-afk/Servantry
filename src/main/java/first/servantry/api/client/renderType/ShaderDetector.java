package first.servantry.api.client.renderType;

import net.minecraft.client.Minecraft;

/**
 * 光影状态检测工具。
 * <p>
 * 用于检测当前是否启用了光影模组（Iris/Oculus）。
 * </p>
 */
public final class ShaderDetector {

    private static Boolean cachedShaderStatus = null;
    private static long lastCheckTick = -1;

    private ShaderDetector() {}

    /**
     * 检测当前是否启用了光影。
     * <p>
     * 使用缓存避免每帧重复检测，每游戏tick最多检测一次。
     * </p>
     *
     * @return true 如果光影已启用
     */
    public static boolean isShaderEnabled() {
        Minecraft mc = Minecraft.getInstance();
        long currentTick = mc.level != null ? mc.level.getGameTime() : 0;
        // 每tick最多检测一次
        if (cachedShaderStatus == null || lastCheckTick != currentTick) {
            cachedShaderStatus = detectShaderInternal();
            lastCheckTick = currentTick;
        }
        return cachedShaderStatus;
    }

    /**
     * 内部检测逻辑。
     * <p>
     * 通过反射检测 Iris API，避免硬依赖。
     * </p>
     */
    private static boolean detectShaderInternal() {
        try {
            // 尝试通过反射调用 Iris API
            // Iris 1.8+ 使用 IrisApi.getInstance()
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object irisApi = irisApiClass.getMethod("getInstance").invoke(null);
            Boolean enabled = (Boolean) irisApiClass.getMethod("isShaderPackInUse").invoke(irisApi);
            return enabled != null && enabled;
        } catch (Exception e) {
            // Iris 不存在或 API 不可用
            return false;
        }
    }

}
