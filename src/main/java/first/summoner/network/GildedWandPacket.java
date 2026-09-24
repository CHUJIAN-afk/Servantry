package first.summoner.network;

import first.lyra.common.attachment.ImmunityData;
import first.lyra.common.attachment.SummonMarkTracker;
import first.lyra.common.summonMark.SummonMarkInstance;
import first.lyra.common.summonMark.SummonMarkType;
import first.lyra.register.LyraAttachmentRegister;
import first.summoner.Summoner;
import first.summoner.common.item.GildedWandItem;
import first.summoner.register.SummonerEnchantmentSummonMarkTypeRegister;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record GildedWandPacket(int id, InteractionHand hand) implements CustomPacketPayload {

    public static final Type<GildedWandPacket> TYPE = new Type<>(Summoner.rl("gilded_wand"));

    /** 标记刷新间隔：玩家 tick 值整除该值时才落一次标记。 */
    private static final int MARK_INTERVAL = 20;

    public static final StreamCodec<RegistryFriendlyByteBuf, GildedWandPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            GildedWandPacket::id,
            StreamCodec.of(
                    FriendlyByteBuf::writeEnum,
                    buf -> buf.readEnum(InteractionHand.class)
            ),
            GildedWandPacket::hand,
            GildedWandPacket::new
    );

    public static void handleServer(GildedWandPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            // 客户端引导时每 tick 都会发包，只取 tick 值整除 20 的那一次落标记
            if (player.tickCount % MARK_INTERVAL != 0) {
                return;
            }
            Level level = player.level();
            Entity entity = level.getEntity(payload.id());
            ItemStack itemStack = player.getItemInHand(payload.hand());
            if (!(itemStack.getItem() instanceof GildedWandItem) || !(entity instanceof LivingEntity living)) {
                return;
            }
            // 标记附魔互斥，鎏金法杖上至多命中一个标记类型
            Set<SummonMarkType> markTypes = SummonerEnchantmentSummonMarkTypeRegister.resolveActiveMarkType(itemStack);
            if (markTypes.isEmpty()) {
                return;
            }
            SummonMarkType markType = markTypes.iterator().next();
            // 标记实例由标记类型创建（持续时间由上游 createInstance 决定），再交给玩家的标记追踪器登记
            SummonMarkInstance instance = markType.createInstance(player, living);
            SummonMarkTracker tracker = player.getData(LyraAttachmentRegister.SummonMarkData);
            tracker.tracker(living, instance);
            ImmunityData.get(living).record(player.getUUID(), instance.getDuration());
            if (!player.isCreative()) {
                itemStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
