package first.summoner.mixin;

import first.summoner.client.AstrageldonArenaClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LocalPlayer.class)
public class ArenaPlayerMixin {

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 summoner$arenaMovement(Vec3 movement) {
        return AstrageldonArenaClient.constrainMovement((LocalPlayer) (Object) this, movement);
    }
}
