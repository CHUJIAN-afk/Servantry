package first.servantry.api.register;

import first.servantry.api.marker.ActiveMarker;
import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MarkerType {

    private final float extraDamage;
    private final float critRate;
    private final int durationTicks;

    public MarkerType(float extraDamage, float critRate, int durationTicks) {
        this.extraDamage = extraDamage;
        this.critRate = critRate;
        this.durationTicks = durationTicks;
    }

    public float getExtraDamage() { return extraDamage; }
    public int getDurationTicks() { return durationTicks; }
    public float getCritRate() { return critRate; }

    /**
     * 仆从命中带有该标记的目标时触发
     */
    public void onServantHit(LivingEntity target, Servant servant, Player owner, ActiveMarker activeMarker, float damageAmount) {

    }

}