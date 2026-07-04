package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.network.BatchedParticlesPayload;
import first.servantry.network.MithrilAnvilPlaceRecipePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkPacketRegister {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(NetworkPacketRegister::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Servantry.MODID);
        registrar.executesOn(HandlerThread.MAIN)
                .playToServer(
                        MithrilAnvilPlaceRecipePayload.TYPE,
                        MithrilAnvilPlaceRecipePayload.STREAM_CODEC,
                        MithrilAnvilPlaceRecipePayload::handlePlaceRecipe
                )
                .playToClient(
                        BatchedParticlesPayload.TYPE,
                        BatchedParticlesPayload.STREAM_CODEC,
                        BatchedParticlesPayload::handleClient
                );
    }

}
