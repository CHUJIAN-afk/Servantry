package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.IAttachmentEntityRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.ImpFireball;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 小鬼火球渲染器 - 刻意不绘制任何几何体。
 * <p>
 * 火球完全由 {@link ImpFireball#tick()} 发射的粒子表现（移植决策：火球不渲染、只保留粒子），
 * 因此本渲染器为空实现；实体类型 {@code imp_fireball} 本身仍然保留。
 * </p>
 * <p>
 * 注册方式（需替换 {@code SummonerAttachmentEntityRenderRegister} 中原有的
 * {@code register(SummonerAttachmentEntityRegister.IMP_FIREBALL, () -> simpleRenderer(Summoner.rl("lyra_model/textures/imp_fireball.png"), 0.25f, 8));} 一行）：
 * {@code register(SummonerAttachmentEntityRegister.IMP_FIREBALL, ImpFireballRenderer::new);}
 * </p>
 */
public class ImpFireballRenderer implements IAttachmentEntityRenderer<ImpFireball> {

    @Override
    public void render(ImpFireball fireball, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        // 不绘制任何内容（仅粒子表现）
    }
}
