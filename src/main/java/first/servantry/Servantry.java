package first.servantry;

import first.servantry.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Servantry.MODID)
public class Servantry {

    public static final String MODID = "servantry";

    public Servantry(IEventBus eventBus) {
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
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
