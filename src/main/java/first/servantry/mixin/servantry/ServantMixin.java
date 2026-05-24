package first.servantry.mixin.servantry;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import first.servantry.api.servant.Servant;
import first.servantry.register.ItemRegister;
import first.servantry.utils.CuriosUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Servant.class)
public class ServantMixin {

    @ModifyExpressionValue(
            method = "searchTarget",
            at = @At(
                    value = "INVOKE",
                    target = "Lfirst/servantry/api/servant/Servant;getTargetDistance()I"
            )
    )
    private int getTargetDistance(int original) {
        Servant servant = (Servant) (Object) this;
        if (CuriosUtil.isEquipped(servant.getOwner(), ItemRegister.ThreatAnalyzer.get())) {
            original = (int) (original * 1.15);
        }
        return original;
    }
}
