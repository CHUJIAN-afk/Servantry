package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.network.MithrilAnvilPlaceRecipePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

public class SummonerNetworkPacketRegister {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(SummonerNetworkPacketRegister::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // BatchedParticlesPayload / BatchedDamageInfoPayload 由 Lyra 在 lyra 通道注册
        event.registrar(Summoner.MODID)
                .executesOn(HandlerThread.MAIN)
                .playToServer(MithrilAnvilPlaceRecipePayload.TYPE, MithrilAnvilPlaceRecipePayload.STREAM_CODEC, MithrilAnvilPlaceRecipePayload::handlePlaceRecipe);
    }

}
