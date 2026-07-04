package first.servantry.network;

import first.servantry.Servantry;
import first.servantry.client.screen.MithrilAnvilGui;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MithrilAnvilPlaceRecipePayload(int containerId, ResourceLocation recipeId, boolean craftAll) implements CustomPacketPayload {

    public static final Type<MithrilAnvilPlaceRecipePayload> TYPE = new Type<>(Servantry.rl("mithril_anvil_place_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MithrilAnvilPlaceRecipePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MithrilAnvilPlaceRecipePayload::containerId,
            ResourceLocation.STREAM_CODEC,
            MithrilAnvilPlaceRecipePayload::recipeId,
            ByteBufCodecs.BOOL,
            MithrilAnvilPlaceRecipePayload::craftAll,
            MithrilAnvilPlaceRecipePayload::new
    );

    public static void handlePlaceRecipe(MithrilAnvilPlaceRecipePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.getServer() instanceof MinecraftServer server) {
            context.enqueueWork(() -> {
                if (player.containerMenu instanceof MithrilAnvilGui.MithrilAnvilMenu menu && menu.containerId == payload.containerId()) {
                    RecipeHolder<?> holder = server.getRecipeManager()
                            .byKey(payload.recipeId())
                            .orElse(null);
                    if (holder != null && holder.value() instanceof MithrilAnvilRecipe && menu.stillValid(player)) {
                        @SuppressWarnings("unchecked")
                        RecipeHolder<MithrilAnvilRecipe> recipe = (RecipeHolder<MithrilAnvilRecipe>) holder;
                        menu.setSelectedRecipe(recipe);
                    }
                }
            });
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
