package first.summoner.register;

import com.mojang.serialization.MapCodec;
import first.summoner.Summoner;
import first.summoner.common.particle.zenithParticle.ZenithParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class SummonerParticleRegister {

    private static final DeferredRegister<ParticleType<?>> Register = DeferredRegister.create(Registries.PARTICLE_TYPE, Summoner.MODID);

    // false 代表粒子不会无视距离强制渲染（优化性能）
    public static final DeferredHolder<ParticleType<?>, ParticleType<ZenithParticleOptions>> Zenith = Register.register("zenith", () -> new ParticleType<>(false) {
        @Override
        public @NotNull MapCodec<ZenithParticleOptions> codec() {
            return ZenithParticleOptions.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ZenithParticleOptions> streamCodec() {
            return ZenithParticleOptions.STREAM_CODEC;
        }
    });

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
