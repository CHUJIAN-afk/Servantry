package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.network.BatchedDamageInfoPayload;
import first.servantry.network.BatchedParticlesPayload;
import first.servantry.network.MithrilAnvilPlaceRecipePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

public class ServantryNetworkPacketRegister {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ServantryNetworkPacketRegister::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(Servantry.MODID)
                .executesOn(HandlerThread.MAIN)
                .playToServer(MithrilAnvilPlaceRecipePayload.TYPE, MithrilAnvilPlaceRecipePayload.STREAM_CODEC, MithrilAnvilPlaceRecipePayload::handlePlaceRecipe)
                .playToClient(BatchedParticlesPayload.TYPE, BatchedParticlesPayload.STREAM_CODEC, BatchedParticlesPayload::handleClient)
                .playToClient(BatchedDamageInfoPayload.TYPE, BatchedDamageInfoPayload.STREAM_CODEC, BatchedDamageInfoPayload::handleClient);
    }

}
