package first.servantry.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.PathNode;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 射弹渲染调度器，统一管理所有射弹的渲染。
 * <p>
 * 类似于 {@link ServantRenderDispatcher}，在客户端渲染事件中调用。
 * 支持通过 {@link #register(ProjectileType, IProjectileRenderer)} 注册不同类型射弹的渲染器。
 * </p>
 */
public class ProjectileRenderDispatcher {

    /** 渲染器映射表，按射弹类型存储对应的渲染器 */
    private static final Map<ProjectileType<?>, IProjectileRenderer<?>> renderers = new HashMap<>();

    /**
     * 渲染玩家的所有射弹。
     * <p>
     * 此方法遍历玩家的射弹列表，为每个射弹调用对应的渲染器进行渲染。
     * </p>
     *
     * @param player        玩家
     * @param poseStack     矩阵栈
     * @param bufferSource  渲染缓冲源
     * @param partialTick   部分 tick 插值进度
     */
    public static void render(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        List<Projectile> projectiles = player.getData(AttachmentRegister.ProjectileData).getProjectiles();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        for (Projectile projectile : projectiles) {
            IProjectileRenderer<Projectile> renderer = getRenderer(projectile);
            if (renderer == null) continue;

            poseStack.pushPose();
            PathNode renderNode = projectile.getRenderNode(partialTick);
            poseStack.translate(
                    renderNode.pos().x() - cameraPos.x,
                    renderNode.pos().y() - cameraPos.y,
                    renderNode.pos().z() - cameraPos.z
            );

            int packedLight = LevelRenderer.getLightColor(player.level(), BlockPos.containing(renderNode.pos().x(), renderNode.pos().y(), renderNode.pos().z()));

            renderer.render(projectile, poseStack, bufferSource, partialTick, packedLight, renderNode);

            // 如果渲染器实现了拖尾渲染接口，处理拖尾渲染
            if (renderer instanceof IProjectileTrailRenderer trailRenderer) {
                trailRenderer.processTrailRender(poseStack, bufferSource, partialTick, projectile, renderNode);
            }

            poseStack.popPose();
        }
    }

    /**
     * 获取射弹对应的渲染器。
     *
     * @param projectile 射弹实例
     * @return 对应的渲染器，若未注册则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T extends Projectile> IProjectileRenderer<T> getRenderer(T projectile) {
        ProjectileType<T> type = (ProjectileType<T>) projectile.getType();
        return (IProjectileRenderer<T>) renderers.get(type);
    }

    /**
     * 注册射弹类型的渲染器。
     * <p>
     * 每种射弹类型只能注册一个渲染器，重复注册将被忽略。
     * </p>
     *
     * @param type     射弹类型
     * @param renderer 渲染器实例
     */
    public static <T extends Projectile> void register(ProjectileType<T> type, IProjectileRenderer<T> renderer) {
        if (!renderers.containsKey(type)) {
            renderers.put(type, renderer);
        }
    }

}