package first.summoner.network;

import first.summoner.Summoner;
import first.summoner.common.attachment.ZenithData;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ZenithPacket() implements CustomPacketPayload {

    public static final Type<ZenithPacket> TYPE = new Type<>(Summoner.rl("zenith_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ZenithPacket> STREAM_CODEC = StreamCodec.unit(new ZenithPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(ZenithPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ZenithData data = player.getData(SummonerAttachmentRegister.ZenithData);
            if (data.swing()) {
                data.addPower();
            }
        });
    }
}
