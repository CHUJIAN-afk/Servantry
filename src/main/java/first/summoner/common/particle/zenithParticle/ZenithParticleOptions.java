package first.summoner.common.particle.zenithParticle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import first.summoner.register.SummonerParticleRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * 天顶剑粒子选项：颜色（RGB）、寿命、阻力、大小由服务端下发，速度随生成时的速度传入。
 */
public record ZenithParticleOptions(int color, int lifetime, float friction, float scale) implements ParticleOptions {

    public static final MapCodec<ZenithParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(ZenithParticleOptions::color),
                    Codec.INT.fieldOf("lifetime").forGetter(ZenithParticleOptions::lifetime),
                    Codec.FLOAT.fieldOf("friction").forGetter(ZenithParticleOptions::friction),
                    Codec.FLOAT.fieldOf("scale").forGetter(ZenithParticleOptions::scale)
            ).apply(instance, ZenithParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ZenithParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buffer, options) -> {
                buffer.writeInt(options.color);
                buffer.writeInt(options.lifetime);
                buffer.writeFloat(options.friction);
                buffer.writeFloat(options.scale);
            },
            buffer -> new ZenithParticleOptions(
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readFloat(),
                    buffer.readFloat()
            )
    );

    @Override
    public @NotNull ParticleType<ZenithParticleOptions> getType() {
        return SummonerParticleRegister.Zenith.get();
    }
}
