package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SoundRegister {

    private static final DeferredRegister<SoundEvent> Register = DeferredRegister.create(Registries.SOUND_EVENT, Servantry.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> UseTerraprism = create("use_terraprism");
    public static final DeferredHolder<SoundEvent, SoundEvent> UseServantWeapon = create("use_servant_weapon");

    private static DeferredHolder<SoundEvent, SoundEvent> create(String name) {
        return Register.register(name, () -> SoundEvent.createVariableRangeEvent(Servantry.rl(name)));
    }

    public static void register(IEventBus bus) {
        Register.register(bus);
    }

}
