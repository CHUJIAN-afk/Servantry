package first.servantry.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.servant.IDamagingOnCollide;
import first.servantry.api.servant.Servant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Servant.class)
public class ServantMixin {

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void tick(CallbackInfo ci) {
        Servant servant = (Servant) (Object) this;
        if (servant instanceof IDamagingOnCollide iDamagingOnCollide) {
            iDamagingOnCollide.processCollision(servant);
        }
    }

    @Inject(
            method = "renderInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lfirst/servantry/api/servant/Servant;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FILfirst/servantry/api/PathNode;)V"
            )
    )
    private void render(CallbackInfo ci, @Local(argsOnly = true) PoseStack poseStack, @Local(argsOnly = true) MultiBufferSource bufferSource, @Local(name = "renderNode") PathNode renderNode) {
        Servant servant = (Servant) (Object) this;
        if (servant instanceof IDamagingOnCollide iDamagingOnCollide) {
            if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                iDamagingOnCollide.renderDebugHitbox(poseStack, bufferSource, renderNode.yaw(), renderNode.pitch(), renderNode.roll());
            }
        }
    }


}
