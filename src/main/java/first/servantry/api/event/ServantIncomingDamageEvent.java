package first.servantry.api.event;

import first.servantry.api.servant.ServantDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;

public class ServantIncomingDamageEvent extends LivingIncomingDamageEvent {

    public ServantIncomingDamageEvent(LivingEntity entity, DamageContainer container) {
        super(entity, container);
    }

    @Override
    public @NotNull ServantDamageSource getSource() {
        return (ServantDamageSource) super.getSource();
    }

}
