package first.summoner.mixin;

import first.summoner.client.render.ArenaTerrainShaders;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderInstance.class)
public class ArenaShaderMixin {

    @Shadow @Final private int programId;
    @Unique private ArenaTerrainShaders.Uniforms summoner$terrain;

    @Inject(method = "updateLocations", at = @At("TAIL"))
    private void summoner$locate(CallbackInfo ci) {
        summoner$terrain = new ArenaTerrainShaders.Uniforms(programId);
    }

    @Inject(method = "apply", at = @At("TAIL"))
    private void summoner$upload(CallbackInfo ci) {
        summoner$terrain.upload();
    }
}
