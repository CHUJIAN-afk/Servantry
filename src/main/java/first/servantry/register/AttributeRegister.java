package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AttributeRegister {

    private static final DeferredRegister<Attribute> Register = DeferredRegister.create(Registries.ATTRIBUTE, Servantry.MODID);

    public static final Holder<Attribute> ServantMaxCount = register("servant_max_count", 1, 0, 1000);
    public static final Holder<Attribute> ServantDamage = register("servant_damage", 1, 0, 1000);
    public static final Holder<Attribute> ServantKnockback = register("servant_knockback", 1, 0, 1000);

    private static Holder<Attribute> register(String name, double defaultValue, double min, double max) {
        return Register.register(name, () -> new RangedAttribute(Servantry.rl(name).toString(), defaultValue, min, max).setSyncable(true));
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
