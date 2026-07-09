package first.servantry.network;

import first.servantry.Servantry;
import first.servantry.api.damageInfo.DamageInfo;
import first.servantry.register.AttachmentRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 批量伤害数字网络包（服务端 → 客户端）。
 * <p>
 * 单 tick 内服务端累积的所有伤害数字记录一次性下发，
 * 客户端收到后逐条转为 {@link DamageInfo} 渲染数据。
 * </p>
 *
 * @param entries 伤害数字记录列表
 */
public record DamageInfoPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final Type<DamageInfoPayload> TYPE = new Type<>(Servantry.rl("damage_info"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageInfoPayload> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DamageInfoPayload::entries,
            DamageInfoPayload::new
    );

    /**
     * 客户端处理：逐条转为 {@link DamageInfo} 写入客户端 Level 附件。
     */
    public static void handleClient(DamageInfoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            level.getData(AttachmentRegister.DamageInfoData).receive(payload.entries());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 单条伤害数字记录：伤害值 + 位置 + 速度 + 阻力 + 暴击 + 颜色。
     */
    public record Entry(float damageAmount, double x, double y, double z, double vx, double vy, double vz, float drag, boolean critical, int color, int endColor, float roll) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.ofMember(
                (entry, buf) -> {
                    buf.writeFloat(entry.damageAmount);
                    buf.writeDouble(entry.x);
                    buf.writeDouble(entry.y);
                    buf.writeDouble(entry.z);
                    buf.writeDouble(entry.vx);
                    buf.writeDouble(entry.vy);
                    buf.writeDouble(entry.vz);
                    buf.writeFloat(entry.drag);
                    buf.writeBoolean(entry.critical);
                    buf.writeInt(entry.color);
                    buf.writeInt(entry.endColor);
                    buf.writeFloat(entry.roll);
                },
                buf -> new Entry(
                        buf.readFloat(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readFloat(),
                        buf.readBoolean(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readFloat()
                )
        );

        /** 转为客户端渲染数据 */
        public DamageInfo toDamageInfo() {
            return new DamageInfo(damageAmount, new Vec3(x, y, z), new Vec3(vx, vy, vz), drag, critical, color, endColor, roll);
        }
    }
}
