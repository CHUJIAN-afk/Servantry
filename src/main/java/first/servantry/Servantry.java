package first.servantry;

import first.servantry.config.StartupConfig;
import first.servantry.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Servantry.MODID)
public class Servantry {

    public static final String MODID = "servantry";

    public Servantry(IEventBus eventBus, ModContainer container) {
        ServantryArmorMaterialRegister.register(eventBus);
        ServantryCreativeTabRegister.register(eventBus);
        ServantryDataComponentRegister.register(eventBus);
        ServantryAttachmentEntityRegister.register(eventBus);
        ServantryItemRegister.register(eventBus);
        ServantryArmorRegister.register();
        ServantryCurioRegister.register();
        ServantryServantWeaponRegister.register();
        ServantryMobEffectRegister.register(eventBus);
        ServantryPotionRegister.register(eventBus);
        ServantrySoundRegister.register(eventBus);
        ServantryArmorSetRegister.register(eventBus);
        ServantryBlockRegister.register(eventBus);
        ServantryMenuRegister.register(eventBus);
        ServantryMithrilAnvilRecipeRegister.register(eventBus);
        ServantryNetworkPacketRegister.register(eventBus);
        container.registerConfig(ModConfig.Type.STARTUP, StartupConfig.Spec);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
