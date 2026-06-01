package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.mithrilAnvil.MithrilAnvilCraftingRecipe;
import first.servantry.client.screen.MithrilAnvilGui;
import first.servantry.common.menu.MithrilAnvilPlaceRecipeHandler;
import first.servantry.network.MithrilAnvilPlaceRecipePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;

public class NetworkPacketRegister {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(NetworkPacketRegister::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(Servantry.MODID)
                .executesOn(HandlerThread.MAIN)
                .playToServer(
                        MithrilAnvilPlaceRecipePayload.TYPE,
                        MithrilAnvilPlaceRecipePayload.STREAM_CODEC,
                        NetworkPacketRegister::handlePlaceRecipe
                );
    }

    private static void handlePlaceRecipe(MithrilAnvilPlaceRecipePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        context.enqueueWork(() -> {
            if (player.containerMenu instanceof MithrilAnvilGui.MithrilAnvilMenu menu && menu.containerId == payload.containerId()) {
                player.getServer().getRecipeManager()
                        .byKey(payload.recipeId())
                        .ifPresent(holder -> {
                            if (holder.value() instanceof MithrilAnvilCraftingRecipe) {
                                MithrilAnvilPlaceRecipeHandler.handleRecipeClick(
                                        player, menu, holder, payload.craftAll());
                            }
                        });
            }
        });
    }
}
