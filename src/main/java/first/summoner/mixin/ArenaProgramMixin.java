package first.summoner.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Program;
import first.summoner.client.render.ArenaTerrainShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Program.class)
public class ArenaProgramMixin {

    @ModifyArg(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;process(Ljava/lang/String;)Ljava/util/List;"))
    private static String summoner$terrain(String source, @Local(argsOnly = true) Program.Type type, @Local(argsOnly = true, ordinal = 0) String name) {
        return ArenaTerrainShaders.vanilla(name, type == Program.Type.VERTEX, source);
    }
}
