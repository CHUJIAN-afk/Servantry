package first.summoner.mixin;

import first.summoner.client.render.ArenaTerrainShaders;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.shader.GlProgram", remap = false)
public abstract class ArenaSodiumProgramMixin {

    @Unique private ArenaTerrainShaders.Uniforms summoner$terrain;

    @Inject(method = "bind", at = @At("TAIL"))
    private void summoner$upload(CallbackInfo ci) {
        if (summoner$terrain == null) {
            summoner$terrain = new ArenaTerrainShaders.Uniforms(GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM));
        }
        summoner$terrain.upload();
    }
}
