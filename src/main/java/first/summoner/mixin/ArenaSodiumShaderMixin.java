package first.summoner.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import first.summoner.client.render.ArenaTerrainShaders;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.shader.GlShader", remap = false)
public class ArenaSodiumShaderMixin {

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderWorkarounds;safeShaderSource(ILjava/lang/CharSequence;)V"), index = 1)
    private CharSequence summoner$terrain(CharSequence source, @Local(argsOnly = true) ResourceLocation name) {
        return ArenaTerrainShaders.sodium(name.toString(), source.toString());
    }
}
