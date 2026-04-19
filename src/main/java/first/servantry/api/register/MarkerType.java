package first.servantry.api.register;

import first.servantry.api.marker.ActiveMarker;
import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record MarkerType(float extraDamage, float critRate, int durationTicks) {

    /**
     * 仆从命中带有该标记的目标时触发
     */
    public void onServantHit(LivingEntity target, Servant servant, Player owner, ActiveMarker activeMarker, float damageAmount) {

    }

}