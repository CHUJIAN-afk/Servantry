package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 仆从模型注册。
 * <p>
 * 注册不与物品/方块关联的独立模型文件，供渲染器直接使用。
 * </p>
 */
@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ModelRegister {

    private static final List<ModelResourceLocation> MODELS = new ArrayList<>();

    public static final ModelResourceLocation ENCHANTED_THROWING_KNIVES = standalone("enchanted_throwing_knives");
    public static final ModelResourceLocation STARDUST_CELL = standalone("stardust_cell");
    public static final ModelResourceLocation TERRAPRISM = standalone("terraprism");
    public static final ModelResourceLocation STARDUST_DRAGON_HEAD = standalone("stardust_dragon_head");
    public static final ModelResourceLocation STARDUST_DRAGON_BODY1 = standalone("stardust_dragon_body1");
    public static final ModelResourceLocation STARDUST_DRAGON_BODY2 = standalone("stardust_dragon_body2");
    public static final ModelResourceLocation STARDUST_DRAGON_BODY3 = standalone("stardust_dragon_body3");
    public static final ModelResourceLocation TWINS_LASER = standalone("twins_laser");
    public static final ModelResourceLocation TWINS_CURSED_FLAME = standalone("twins_cursed_flame");
    public static final ModelResourceLocation SHARKNADO = standalone("sharknado");
    public static final ModelResourceLocation SHARK_OPEN = standalone("shark_open");
    public static final ModelResourceLocation SHARK_CLOSE = standalone("shark_close");
    public static final ModelResourceLocation CHLOROPHYTE_CRYSTAL = standalone("chlorophyte_crystal");
    public static final ModelResourceLocation BALLISTA = standalone("ballista");
    public static final ModelResourceLocation DEADLY_SPHERE = standalone("deadly_sphere");
    public static final ModelResourceLocation MOON_PORTAL = standalone("moon_portal");
    public static final ModelResourceLocation RAINBOW_CRYSTAL = standalone("rainbow_crystal");
    public static final ModelResourceLocation SCAVENGER_FAIRY = standalone("scavenger_fairy");
    public static final ModelResourceLocation SURVEY_DRONE = standalone("survey_drone");

    private static ModelResourceLocation standalone(String path) {
        ModelResourceLocation location = ModelResourceLocation.standalone(Servantry.rl("servant/" + path));
        MODELS.add(location);
        return location;
    }

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        MODELS.forEach(event::register);
    }
}
