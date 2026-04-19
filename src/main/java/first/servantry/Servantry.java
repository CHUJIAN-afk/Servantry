package first.servantry;

import first.servantry.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Servantry.MODID)
public class Servantry {

    public static final String MODID = "servantry";

    public Servantry(IEventBus eventBus) {
        AdvancedProjectileRegister.register(eventBus);
        ArmorMaterialRegister.register(eventBus);
        AttachmentRegister.register(eventBus);
        AttributeRegister.register(eventBus);
        CreativeTabRegister.register(eventBus);
        ItemRegister.register(eventBus);
        MarkerRegister.register(eventBus);
        MobEffectRegister.register(eventBus);
        ParticleRegister.register(eventBus);
        PotionRegister.register(eventBus);
        ServantRegister.register(eventBus);
        SoundRegister.register(eventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }

}
