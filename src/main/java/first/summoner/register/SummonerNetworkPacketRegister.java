package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.network.GildedWandPacket;
import first.summoner.network.SyncFieldPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

@EventBusSubscriber(modid = Summoner.MODID)
public class SummonerNetworkPacketRegister {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(Summoner.MODID)
                .executesOn(HandlerThread.MAIN)
                .playToServer(GildedWandPacket.TYPE, GildedWandPacket.STREAM_CODEC, GildedWandPacket::handleServer)
                .playToClient(SyncFieldPacket.TYPE, SyncFieldPacket.STREAM_CODEC, SyncFieldPacket::handleClient);
    }
}
