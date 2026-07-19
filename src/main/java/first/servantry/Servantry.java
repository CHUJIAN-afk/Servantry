package first.servantry;

import first.servantry.config.ClientConfig;
import first.servantry.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Servantry.MODID)
public class Servantry {

    public static final String MODID = "servantry";

    public Servantry(IEventBus eventBus, Dist dist, ModContainer container) {
        ServantryArmorMaterialRegister.register(eventBus);
        ServantryAttachmentRegister.register(eventBus);
        ServantryAttributeRegister.register(eventBus);
        ServantryCreativeTabRegister.register(eventBus);
        ServantryDataComponentRegister.register(eventBus);
        ServantryAttachmentEntityRegister.register(eventBus);
        ServantryItemRegister.register();
        ServantryArmorRegister.register();
        ServantryCurioRegister.register();
        ServantryServantWeaponRegister.register();
        ServantryItemRegisterBuilder.register(eventBus);
        ServantryMobEffectRegister.register(eventBus);
        ServantryParticleRegister.register(eventBus);
        ServantryPotionRegister.register(eventBus);
        ServantrySoundRegister.register(eventBus);
        ServantryArmorSetRegister.register(eventBus);
        ServantryBlockRegister.register(eventBus);
        ServantryMenuRegister.register(eventBus);
        ServantryMithrilAnvilRecipeRegister.register(eventBus);
        ServantryNetworkPacketRegister.register(eventBus);
        if (dist.isClient()) {
            container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.Spec);
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
