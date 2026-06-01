package first.servantry.network;

import first.servantry.Servantry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
