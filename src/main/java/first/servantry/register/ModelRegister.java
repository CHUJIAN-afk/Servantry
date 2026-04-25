package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * 仆从模型注册。
 * <p>
 * 注册不与物品/方块关联的独立模型文件，供渲染器直接使用。
 * </p>
 */
@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public final class ModelRegister {

    /** 附魔飞刀模型 */
    public static final ModelResourceLocation ENCHANTED_THROWING_KNIVES = standalone("servant/enchanted_throwing_knives");

    /** 星尘细胞模型 */
    public static final ModelResourceLocation STARDUST_CELL = standalone("servant/stardust_cell");

    /** 泰拉棱镜模型 */
    public static final ModelResourceLocation TERRAPRISM = standalone("servant/terraprism");

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(ENCHANTED_THROWING_KNIVES);
        event.register(STARDUST_CELL);
        event.register(TERRAPRISM);
    }

    private static ModelResourceLocation standalone(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.parse(Servantry.MODID + ":" + path));
    }

}
