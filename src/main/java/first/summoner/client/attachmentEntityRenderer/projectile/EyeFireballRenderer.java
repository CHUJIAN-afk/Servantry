package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.IAttachmentEntityRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.EyeFireball;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 眼球激光塔火球渲染器 - 刻意不绘制任何几何体。
 * <p>
 * 火球完全由 {@link EyeFireball#tick()} 发射的粒子表现，因此本渲染器为空实现；
 * 实体类型 {@code eye_fireball} 本身仍然保留（伤害、命中判定照常）。
 */
public class EyeFireballRenderer implements IAttachmentEntityRenderer<EyeFireball> {

    @Override
    public void render(EyeFireball fireball, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        // 不绘制任何内容（仅粒子表现）
    }
}
