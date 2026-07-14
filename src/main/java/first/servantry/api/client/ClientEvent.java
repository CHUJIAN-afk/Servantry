package first.servantry.api.client;

import first.servantry.Servantry;
import first.servantry.api.client.tooltip.TooltipHandler;
import first.servantry.api.common.particle.genericParticle.GenericParticleProvider;
import first.servantry.api.damageInfo.DamageInfoStyleManager;
import first.servantry.register.ServantryParticleRegister;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void handler(ItemTooltipEvent event) {
        TooltipHandler.handler(event);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(DamageInfoStyleManager.INSTANCE);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ServantryParticleRegister.Generic.get(), GenericParticleProvider::new);
    }
}