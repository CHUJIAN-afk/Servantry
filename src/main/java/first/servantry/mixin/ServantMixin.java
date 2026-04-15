package first.servantry.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.servant.IBlockCollider;
import first.servantry.api.servant.IDamagingOnCollide;
import first.servantry.api.servant.ITrailRenderer;
import first.servantry.api.servant.Servant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Servant.class)
public class ServantMixin {

    // 【新增】：在 Tick 头部执行方块碰撞预测
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void preTick(CallbackInfo ci) {
        Servant servant = (Servant) (Object) this;
        // 如果实现了方块碰撞接口，在应用路径节点前进行修正滑动
        if (servant instanceof IBlockCollider iBlockCollider) {
            iBlockCollider.processBlockCollision(servant);
        }
    }

    // 【保留原本的尾部注入】：伤害碰撞判定（需要在移动之后判定）
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void postTick(CallbackInfo ci) {
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
    private void renderHook(CallbackInfo ci, @Local(argsOnly = true) float partialTick, @Local(argsOnly = true) PoseStack poseStack, @Local(argsOnly = true) MultiBufferSource bufferSource, @Local(name = "renderNode") PathNode renderNode) {
        Servant servant = (Servant) (Object) this;

        if (servant instanceof ITrailRenderer trailRenderer) {
            trailRenderer.processTrailRender(poseStack, bufferSource, partialTick, servant, renderNode);
        }

        if (servant instanceof IDamagingOnCollide iDamagingOnCollide) {
            if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                iDamagingOnCollide.renderDebugHitbox(poseStack, bufferSource, renderNode.yaw(), renderNode.pitch(), renderNode.roll());
            }
        }
    }
}