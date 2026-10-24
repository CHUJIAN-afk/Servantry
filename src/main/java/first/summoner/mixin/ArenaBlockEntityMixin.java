package first.summoner.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import first.summoner.client.render.AstrageldonArenaEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class ArenaBlockEntityMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void summoner$outline(BlockEntity entity, float partialTick, PoseStack poses, MultiBufferSource buffers, CallbackInfo ci) {
        if (AstrageldonArenaEffect.outlineBlockEntity(entity, poses, buffers)) {
            ci.cancel();
        }
    }
}
