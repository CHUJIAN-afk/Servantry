package first.summoner.mixin;

import first.summoner.client.render.AstrageldonArenaEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class ArenaLevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void summoner$arena(DeltaTracker delta, boolean outline, Camera camera, GameRenderer renderer, LightTexture light, Matrix4f view, Matrix4f projection, CallbackInfo ci) {
        AstrageldonArenaEffect.beginFrame(camera, view, projection, delta.getGameTimeDeltaPartialTick(false));
    }
}
