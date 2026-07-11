package first.servantry.mixin;

import first.servantry.api.damageInfo.IDamageSourceCritical;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements IDamageSourceCritical {

    @Unique
    private boolean servantry$isCritical = false;

    @Override
    public boolean servantry$isCritical() {
        return servantry$isCritical;
    }

    @Override
    public void servantry$setCritical(boolean isCritical) {
        this.servantry$isCritical = isCritical;
    }
}
