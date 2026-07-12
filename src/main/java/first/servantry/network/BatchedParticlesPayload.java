package first.servantry.network;

import first.servantry.Servantry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 批量粒子网络包（服务端 → 客户端）。
 * <p>
 * 单 tick 内服务端累积的所有粒子记录一次性下发，避免每个粒子单独发送
 * {@code ClientboundLevelParticlesPacket}。客户端收到后逐条调用
 * {@link net.minecraft.world.level.Level#addParticle} 生成粒子，视觉效果与原版一致。
 * </p>
 * <p>
 * 粒子类型序列化复刻原版 {@code ClientboundLevelParticlesPacket}：先写
 * {@link net.minecraft.core.registries.BuiltInRegistries#PARTICLE_TYPE} 的注册表 id，
 * 再用该类型自有的 {@link ParticleType#streamCodec()} 编码具体参数，
 * 兼容原版与自定义粒子（如 {@link first.servantry.api.common.particle.GenericParticleOptions}）。
 * </p>
 *
 * @param entries 粒子记录列表
 */
public record BatchedParticlesPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final Type<BatchedParticlesPayload> TYPE = new Type<>(Servantry.rl("batched_particles"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BatchedParticlesPayload> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BatchedParticlesPayload::entries,
            BatchedParticlesPayload::new
    );

    /**
     * 客户端处理：逐条调用 {@link Level#addParticle} 生成粒子，复刻原版视觉效果。
     */
    public static void handleClient(BatchedParticlesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            for (Entry entry : payload.entries()) {
                level.addParticle(entry.options(), false, entry.x(), entry.y(), entry.z(), entry.vx(), entry.vy(), entry.vz());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 单条粒子记录：类型 + 位置 + 速度。
     * <p>
     * 序列化复刻原版：写 {@link net.minecraft.core.registries.BuiltInRegistries#PARTICLE_TYPE} 注册表 id，
     * 再用 {@link ParticleType#streamCodec()} 编码具体参数。位置/速度用三个 double 存储。
     * </p>
     */
    public record Entry(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.ofMember(
                Entry::write,
                Entry::read
        );

        // 写：先写粒子类型注册表 id，再用该类型自有 codec 编码具体参数，最后写位置/速度
        private void write(RegistryFriendlyByteBuf buf) {
            ParticleType<?> type = options.getType();
            buf.writeVarInt(BuiltInRegistries.PARTICLE_TYPE.getId(type));
            writeOptions(buf, type, options);
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
            buf.writeDouble(vx);
            buf.writeDouble(vy);
            buf.writeDouble(vz);
        }

        // 读：先读类型 id 反查 ParticleType，再用其 codec 解码具体参数
        private static Entry read(RegistryFriendlyByteBuf buf) {
            int id = buf.readVarInt();
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.byId(id);
            assert type != null;
            ParticleOptions options = readOptions(buf, type);
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            double vx = buf.readDouble();
            double vy = buf.readDouble();
            double vz = buf.readDouble();
            return new Entry(options, x, y, z, vx, vy, vz);
        }

        @SuppressWarnings("unchecked")
        private static <T extends ParticleOptions> void writeOptions(RegistryFriendlyByteBuf buf, ParticleType<T> type, ParticleOptions options) {
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec = type.streamCodec();
            streamCodec.encode(buf, (T) options);
        }

        private static <T extends ParticleOptions> ParticleOptions readOptions(RegistryFriendlyByteBuf buf, ParticleType<T> type) {
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec = type.streamCodec();
            return streamCodec.decode(buf);
        }
    }
}
