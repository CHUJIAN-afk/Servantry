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
 * 通用粒子选项，支持配置中心颜色、边缘颜色、寿命、旋转速度、阻力、大小。
 */
public record GenericParticleOptions(int color, int endColor, int edgeColor, int endEdgeColor, int lifetime, float spinSpeed, float friction, float scale, float scaleOffset) implements ParticleOptions {

    public static final MapCodec<GenericParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    com.mojang.serialization.Codec.INT.fieldOf("color").forGetter(GenericParticleOptions::color),
                    com.mojang.serialization.Codec.INT.fieldOf("endColor").forGetter(GenericParticleOptions::endColor),
                    com.mojang.serialization.Codec.INT.fieldOf("edgeColor").forGetter(GenericParticleOptions::edgeColor),
                    com.mojang.serialization.Codec.INT.fieldOf("endEdgeColor").forGetter(GenericParticleOptions::endEdgeColor),
                    com.mojang.serialization.Codec.INT.fieldOf("lifetime").forGetter(GenericParticleOptions::lifetime),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("spinSpeed").forGetter(GenericParticleOptions::spinSpeed),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("friction").forGetter(GenericParticleOptions::friction),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("scale").forGetter(GenericParticleOptions::scale),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("scaleOffset").forGetter(GenericParticleOptions::scaleOffset)
            ).apply(instance, GenericParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericParticleOptions> STREAM_CODEC = StreamCodec.of(
            GenericParticleOptions::encode,
            GenericParticleOptions::decode
    );

    private static void encode(RegistryFriendlyByteBuf buffer, GenericParticleOptions options) {
        buffer.writeInt(options.color);
        buffer.writeInt(options.endColor);
        buffer.writeInt(options.edgeColor);
        buffer.writeInt(options.endEdgeColor);
        buffer.writeInt(options.lifetime);
        buffer.writeFloat(options.spinSpeed);
        buffer.writeFloat(options.friction);
        buffer.writeFloat(options.scale);
        buffer.writeFloat(options.scaleOffset);
    }

    private static GenericParticleOptions decode(RegistryFriendlyByteBuf buffer) {
        return new GenericParticleOptions(
                buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat()
        );
    }

    @Override
    public @NotNull ParticleType<GenericParticleOptions> getType() {
        return ParticleRegister.Generic.get();
    }
}