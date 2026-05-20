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

    /**
     * 附魔飞刀模型
     */
    public static final ModelResourceLocation ENCHANTED_THROWING_KNIVES = standalone("servant/enchanted_throwing_knives"),

    /**
     * 星尘细胞模型
     */
    STARDUST_CELL = standalone("servant/stardust_cell"),

    /**
     * 泰拉棱镜模型
     */
    TERRAPRISM = standalone("servant/terraprism"),

    /**
     * 星尘龙头部模型
     */
    STARDUST_DRAGON_HEAD = standalone("servant/stardust_dragon_head"),

    /**
     * 星尘龙身体1模型
     */
    STARDUST_DRAGON_BODY1 = standalone("servant/stardust_dragon_body1"),

    /**
     * 星尘龙身体2模型
     */
    STARDUST_DRAGON_BODY2 = standalone("servant/stardust_dragon_body2"),

    /**
     * 星尘龙尾部模型
     */
    STARDUST_DRAGON_BODY3 = standalone("servant/stardust_dragon_body3"),

    /**
     * 激光眼模型
     */
    TwinsLaser = standalone("servant/twins_laser"),

    /**
     * 魔焰眼模型
     */
    TwinsCursedFlame = standalone("servant/twins_cursed_flame");

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        MODELS.forEach(event::register);
    }

    private static ModelResourceLocation standalone(String path) {
        ModelResourceLocation location = ModelResourceLocation.standalone(Servantry.rl(path));
        MODELS.add(location);
        return location;
    }

}
