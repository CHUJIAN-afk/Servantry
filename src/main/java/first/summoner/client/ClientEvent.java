package first.summoner.client;

import first.summoner.Summoner;
import first.summoner.common.particle.zenithParticle.ZenithParticleProvider;
import first.summoner.register.SummonerParticleRegister;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void handler(ClientTickEvent.Post event) {
        MusicManager.handler(event);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SummonerParticleRegister.Zenith.get(), ZenithParticleProvider::new);
    }
}
