package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServantryPotionRegister {

    private static final DeferredRegister<Potion> Register = DeferredRegister.create(Registries.POTION, Servantry.MODID);

    public static final DeferredHolder<Potion, Potion> Obsession = Register.register("obsession", () -> new Potion(new MobEffectInstance(ServantryMobEffectRegister.Obsession, 12000)));
    public static final DeferredHolder<Potion, Potion> LongObsession = Register.register("long_obsession", () -> new Potion(new MobEffectInstance(ServantryMobEffectRegister.Obsession, 24000)));
    public static final DeferredHolder<Potion, Potion> StrongObsession = Register.register("strong_obsession", () -> new Potion(new MobEffectInstance(ServantryMobEffectRegister.Obsession, 6000, 1)));

    public static void register(IEventBus eventBus){
        Register.register(eventBus);
    }

}
