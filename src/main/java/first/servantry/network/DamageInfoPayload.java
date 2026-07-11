package first.servantry.network;

import first.servantry.Servantry;
import first.servantry.api.damageInfo.DamageInfo;
import first.servantry.api.damageInfo.DamageInfoStyle;
import first.servantry.api.damageInfo.DamageInfoStyleManager;
import first.servantry.register.AttachmentRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 批量伤害数字网络包（服务端 → 客户端）。
 * <p>
 * 服务端只发送最小数据（伤害类型、伤害值、位置、速度、暴击标记），
 * 客户端收到后根据伤害类型从 {@link DamageInfoStyleManager} 查询样式重建完整渲染参数。
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
     * 单条伤害数字记录：伤害类型 + 伤害值 + 位置 + 速度 + 暴击标记。
     * <p>
     * 渲染参数（贴图、颜色、尺寸等）由客户端根据 damageType 从 JSON 样式表查询，
     * 不通过网络同步，大幅减少流量。
     * </p>
     */
    public record Entry(String damageType, float damageAmount, double x, double y, double z, double vx, double vy, double vz, boolean critical) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.ofMember(
                (entry, buf) -> {
                    buf.writeUtf(entry.damageType);
                    buf.writeFloat(entry.damageAmount);
                    buf.writeDouble(entry.x);
                    buf.writeDouble(entry.y);
                    buf.writeDouble(entry.z);
                    buf.writeDouble(entry.vx);
                    buf.writeDouble(entry.vy);
                    buf.writeDouble(entry.vz);
                    buf.writeBoolean(entry.critical);
                },
                buf -> new Entry(
                        buf.readUtf(),
                        buf.readFloat(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readBoolean()
                )
        );

        /** 转为客户端渲染数据：根据 damageType 查询样式重建 DamageInfo，样式未定义时返回 null */
        @Nullable
        public DamageInfo toDamageInfo() {
            if (damageAmount >= 0.01) {
                DamageInfoStyle style = DamageInfoStyleManager.INSTANCE.getStyle(ResourceLocation.parse(damageType));
                if (style != null) {
                    return new DamageInfo(style, damageAmount, new Vec3(x, y, z), new Vec3(vx, vy, vz), critical);
                }
            }
            return null;
        }
    }
}
