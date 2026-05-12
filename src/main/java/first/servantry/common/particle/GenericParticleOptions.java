package first.servantry.common.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import first.servantry.register.ParticleRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * 通用粒子选项，支持服务端配置颜色、寿命、旋转速度、阻力和最终颜色。
 */
public record GenericParticleOptions(int color, int endColor, int lifetime, float spinSpeed, float friction) implements ParticleOptions {

    public static final MapCodec<GenericParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    com.mojang.serialization.Codec.INT.fieldOf("color").forGetter(GenericParticleOptions::color),
                    com.mojang.serialization.Codec.INT.fieldOf("endColor").forGetter(GenericParticleOptions::endColor),
                    com.mojang.serialization.Codec.INT.fieldOf("lifetime").forGetter(GenericParticleOptions::lifetime),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("spinSpeed").forGetter(GenericParticleOptions::spinSpeed),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("friction").forGetter(GenericParticleOptions::friction)
            ).apply(instance, GenericParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericParticleOptions> STREAM_CODEC = StreamCodec.of(
            GenericParticleOptions::encode,
            GenericParticleOptions::decode
    );

    private static void encode(RegistryFriendlyByteBuf buffer, GenericParticleOptions options) {
        buffer.writeInt(options.color);
        buffer.writeInt(options.endColor);
        buffer.writeInt(options.lifetime);
        buffer.writeFloat(options.spinSpeed);
        buffer.writeFloat(options.friction);
    }

    private static GenericParticleOptions decode(RegistryFriendlyByteBuf buffer) {
        return new GenericParticleOptions(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public @NotNull ParticleType<GenericParticleOptions> getType() {
        return ParticleRegister.Generic.get();
    }
}