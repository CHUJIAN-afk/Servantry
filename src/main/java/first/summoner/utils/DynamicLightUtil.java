package first.summoner.utils;

import first.lyra.api.LyraAPI;
import first.lyra.common.attachmentEntity.PathNode;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 动态光源工具。
 * <p>
 * Lyra 1.21.1.11（HEAD 3fe87d7「优化动态光照」）删除了静态的
 * {@code DynamicLightDispatcher.addLightSources(Vec3,int)} 与
 * {@code addLightSources(PathNode,AABB,int)}，改为实例方法
 * {@code INSTANCE.addLightSource(LightSource)}。本类在 Lyra 新 API 之上保留 Summoner
 * 原本的两种用法：单点光源，以及沿 {@link PathNode} 局部 AABB 铺开的点光源序列。
 * </p>
 */
public final class DynamicLightUtil {

    /** 相邻点光源的最大间距（格） */
    private static final double SPACING = 0.5;

    /**
     * 沿 {@link PathNode} 路径分布多个点光源（局部 AABB → 世界旋转）。
     * <p>
     * Z 轴方向最长，沿 Z 轴等间隔放置点光源（间隔 ≤ {@value #SPACING} 格），
     * 局部→世界旋转为：先绕 Y 旋转 yaw，再绕 X 旋转 pitch。
     * </p>
     */
    public static void addLight(PathNode pathNode, AABB aabb, int light) {
        Vec3 pos = pathNode.pos();
        double minX = aabb.minX;
        double maxX = aabb.maxX;
        double minY = aabb.minY;
        double maxY = aabb.maxY;
        double minZ = aabb.minZ;
        double maxZ = aabb.maxZ;

        double zLength = maxZ - minZ;
        int count = Math.max(1, Mth.ceil(zLength / SPACING));
        double step = zLength / count;

        float yawRad = (float) Math.toRadians(-pathNode.yaw());
        float pitchRad = (float) Math.toRadians(pathNode.pitch());
        double cosY = Math.cos(yawRad);
        double sinY = Math.sin(yawRad);
        double cosP = Math.cos(pitchRad);
        double sinP = Math.sin(pitchRad);

        double localX = (minX + maxX) * 0.5;
        double localY = (minY + maxY) * 0.5;

        for (int i = 0; i <= count; i++) {
            double localZ = minZ + step * i;

            double rotatedY = localY * cosP - localZ * sinP;
            double rotatedZ = localY * sinP + localZ * cosP;

            double worldX = localX * cosY + rotatedZ * sinY;
            double worldZ = -localX * sinY + rotatedZ * cosY;

            Vec3 pos1 = pos.add(worldX, rotatedY, worldZ);
            LyraAPI.light(pos1, light);
        }
    }
}
