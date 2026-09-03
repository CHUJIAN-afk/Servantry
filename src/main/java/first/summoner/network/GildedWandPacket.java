package first.summoner.network;

import first.lyra.common.attachment.InvincibleData;
import first.summoner.Summoner;
import first.summoner.common.attachment.SummonMarkData;
import first.summoner.common.item.GildedWandItem;
import first.summoner.common.summonMark.SummonMarkType;
import first.summoner.register.SummonerAttachmentRegister;
import first.summoner.register.SummonerEnchantmentSummonMarkTypeRegister;
import first.summoner.register.SummonerSummonMarkRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record GildedWandPacket(int id, InteractionHand hand) implements CustomPacketPayload {

    public static final Type<GildedWandPacket> TYPE = new Type<>(Summoner.rl("gilded_wand"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GildedWandPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            GildedWandPacket::id,
            StreamCodec.of(
                    (buf, hand) -> buf.writeEnum(hand),
                    buf -> buf.readEnum(InteractionHand.class)
            ),
            GildedWandPacket::hand,
            GildedWandPacket::new
    );

    public static void handleServer(GildedWandPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            Entity entity = level.getEntity(payload.id());
            ItemStack itemStack = player.getItemInHand(payload.hand());
            if (!level.isClientSide() && itemStack.getItem() instanceof GildedWandItem && entity instanceof LivingEntity living) {
                SummonMarkData data = player.getData(SummonerAttachmentRegister.SummonMarkData);
                Set<SummonMarkType> markTypes = SummonerEnchantmentSummonMarkTypeRegister.resolveActiveMarkType(itemStack);
                data.setMarkTarget(living);
                int baseDuration = 80;
                // 沉默的终时：标记持续时间 +200%
                if (markTypes.contains(SummonerSummonMarkRegister.SilentEnd.get())) {
                    baseDuration *= 3;
                }
                data.setDuration(baseDuration);
                // 银河/弑神者：标记能量积累速度 +33%
                float basePower = 1;
                if (markTypes.contains(SummonerSummonMarkRegister.Galaxy.get())) {
                    basePower *= 1.33f;
                }
                if (markTypes.contains(SummonerSummonMarkRegister.GodSlayer.get())) {
                    basePower *= 1.33f;
                }
                data.setPower(data.getPower() + basePower);
                data.setMarkTypes(markTypes);
                InvincibleData.get(living).recordHit(player.getUUID(), baseDuration);
                if (!player.isCreative()) {
                    itemStack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
