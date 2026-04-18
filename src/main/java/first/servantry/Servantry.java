package first.servantry;

import first.servantry.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Servantry.MODID)
public class Servantry {

    public static final String MODID = "servantry";

    public Servantry(IEventBus eventBus) {
        AttributeRegister.register(eventBus);
        AttachmentRegister.register(eventBus);
        ServantRegister.register(eventBus);
        ItemRegister.register(eventBus);
        CreativeTabRegister.register(eventBus);
        SoundRegister.register(eventBus);
        MarkerRegister.register(eventBus);
        AdvancedProjectileRegister.register(eventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }

}
