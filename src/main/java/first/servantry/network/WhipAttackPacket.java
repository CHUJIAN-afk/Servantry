package first.servantry.network;

import first.servantry.Servantry;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.common.attachment.WhipData;
import first.servantry.register.AttachmentRegister;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record WhipAttackPacket() implements CustomPacketPayload {

    public static final Type<WhipAttackPacket> TYPE = new Type<>(Servantry.rl("whip_attack"));
    public static final StreamCodec<ByteBuf, WhipAttackPacket> STREAM_CODEC = StreamCodec.unit(new WhipAttackPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.getMainHandItem().getItem() instanceof IWhipWeapon) {
                WhipData whipData = player.getData(AttachmentRegister.WhipData);
                whipData.startAttack(player);
            }
        });
    }
}