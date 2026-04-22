package first.servantry.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.PathNode;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 射弹渲染器接口。
 * <p>
 * 所有射弹渲染器都需要实现此接口。
 * 渲染器负责绘制射弹本体、拖尾效果等视觉元素。
 * </p>
 *
 * @param <T> 射弹类型
 */
public interface IProjectileRenderer<T extends Projectile> {

    /**
     * 渲染射弹。
     * <p>
     * 此方法在每帧渲染时调用，负责绘制射弹的视觉表现。
     * </p>
     *
     * @param projectile    射弹实例
     * @param poseStack     矩阵栈，用于变换坐标系
     * @param bufferSource  渲染缓冲源
     * @param partialTick   部分 tick 插值进度（0~1）
     * @param packedLight   光照值
     * @param renderNode    渲染节点（位置和旋转）
     */
    void render(T projectile, PoseStack poseStack, MultiBufferSource bufferSource,
                float partialTick, int packedLight, PathNode renderNode);
}