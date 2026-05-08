package first.servantry;

import first.servantry.register.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Servantry.MODID)
public class Servantry {

    public static final String MODID = "servantry";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public Servantry(IEventBus eventBus) {
        ArmorMaterialRegister.register(eventBus);
        AttachmentRegister.register(eventBus);
        AttributeRegister.register(eventBus);
        CreativeTabRegister.register(eventBus);
        ItemRegister.register(eventBus);
        MobEffectRegister.register(eventBus);
        ParticleRegister.register(eventBus);
        PotionRegister.register(eventBus);
        ProjectileRegister.register(eventBus);
        ServantRegister.register(eventBus);
        SoundRegister.register(eventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }

}
