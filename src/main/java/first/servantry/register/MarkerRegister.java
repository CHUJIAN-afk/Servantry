package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.register.MarkerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MarkerRegister {

    private static final DeferredRegister<MarkerType> Register = DeferredRegister.create(first.servantry.api.register.Registries.MARKER_TYPES, Servantry.MODID);

    public static final DeferredHolder<MarkerType, MarkerType> CobwebMark = Register.register("cobweb", () -> new MarkerType(0.6f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> SlimeMark = Register.register("slime_whip", () -> new MarkerType(0.6f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> LeatherMark = Register.register("leather_whip", () -> new MarkerType(0.8f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> SoulscourgeMark = Register.register("soulscourge", () -> new MarkerType(1f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> VasculashMark = Register.register("vasculash", () -> new MarkerType(1f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> StarcrashMark = Register.register("starcrash", () -> new MarkerType(0.4f, 0, 40));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}