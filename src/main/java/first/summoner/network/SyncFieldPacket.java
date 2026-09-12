package first.summoner.network;

import first.summoner.Summoner;
import first.summoner.common.master.AbstractMaster;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncFieldPacket(int id, byte[] bytes) implements CustomPacketPayload {

    public static final Type<SyncFieldPacket> TYPE = new Type<>(Summoner.rl("sync_field_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFieldPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, SyncFieldPacket::id, ByteBufCodecs.BYTE_ARRAY, SyncFieldPacket::bytes, SyncFieldPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncFieldPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            Entity entity = level.getEntity(payload.id());
            if (entity instanceof AbstractMaster master) {
                master.enqueueSyncPayload(payload.bytes());
            }
        });
    }

}
