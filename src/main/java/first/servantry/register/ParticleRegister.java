package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ParticleRegister {

    private static final DeferredRegister<ParticleType<?>> Register = DeferredRegister.create(Registries.PARTICLE_TYPE, Servantry.MODID);

    // false 代表粒子不会无视距离强制渲染（优化性能）
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> StardustScatter = Register.register("stardust_scatter", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}