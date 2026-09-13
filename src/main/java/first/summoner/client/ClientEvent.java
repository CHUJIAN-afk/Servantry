package first.summoner.client;

import first.summoner.Summoner;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void handler(ClientTickEvent.Post event) {
        MusicManager.handler(event);
    }
}
