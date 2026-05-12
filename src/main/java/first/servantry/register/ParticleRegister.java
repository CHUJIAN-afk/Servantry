package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.common.particle.GenericParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class ParticleRegister {

    private static final DeferredRegister<ParticleType<?>> Register = DeferredRegister.create(Registries.PARTICLE_TYPE, Servantry.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<GenericParticleOptions>> Generic = Register.register("generic", () ->
            new ParticleType<>(false) {
                @Override
                public com.mojang.serialization.@NotNull MapCodec<GenericParticleOptions> codec() {
                    return GenericParticleOptions.CODEC;
                }

                @Override
                public net.minecraft.network.codec.@NotNull StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, GenericParticleOptions> streamCodec() {
                    return GenericParticleOptions.STREAM_CODEC;
                }
            });

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}