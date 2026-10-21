package first.summoner.register;

import first.lyra.client.render.model.LyraModelRenderer;
import first.summoner.Summoner;
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
 * 1.21.1: ModelEvent.RegisterAdditional + ModelResourceLocation.standalone。
 * Lyra 路径约定：assets/summoner/lyra_model/json/&lt;folder&gt;/&lt;id&gt;/&lt;id&gt;.json|png。
 * </p>
 */
@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class SummonerModelRegister {

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
    public static final ModelResourceLocation MOON_PORTAL = standalone("moon_portal");
    public static final ModelResourceLocation SCAVENGER_FAIRY = standalone("scavenger_fairy");
    public static final ModelResourceLocation SURVEY_DRONE = standalone("survey_drone");
    public static final ModelResourceLocation SUPER_PEASHOOTER = standalone("super_peashooter");
    public static final ModelResourceLocation CLOUD = standalone("cloud");
    public static final ModelResourceLocation PULSE_TURRET = standalone("pulse_turret");
    public static final ModelResourceLocation NECRO_SPIRIT = standalone("necro_spirit");
    public static final ModelResourceLocation BARNACLE = standalone("barnacle");
    public static final ModelResourceLocation HYDRA = standalone("hydra");
    public static final ModelResourceLocation INFERNO = standalone("inferno");
    public static final ModelResourceLocation VOID_EATER_HEAD = standalone("void_eater_head");
    public static final ModelResourceLocation VOID_EATER_BODY = standalone("void_eater_body");
    public static final ModelResourceLocation VOID_EATER_TAIL = standalone("void_eater_tail");

    public static final ModelResourceLocation CROSSBOW_BOLT = standaloneIn("projectile", "crossbow_bolt");
    public static final ModelResourceLocation MINI_NECRO_SPIRIT = standaloneIn("projectile", "mini_necro_spirit");
    public static final ModelResourceLocation FROST_BOLT = standaloneIn("projectile", "frost_bolt");
    public static final ModelResourceLocation EXPLOSION_FLAME_BALL = standaloneIn("projectile", "explosion_flame_ball");

    private static ModelResourceLocation standalone(String path) {
        return standaloneIn("minion", path);
    }

    private static ModelResourceLocation standaloneIn(String folder, String path) {
        ModelResourceLocation location = LyraModelRenderer.jsonLocation(Summoner.rl(folder + "/" + path));
        MODELS.add(location);
        return location;
    }

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        MODELS.forEach(event::register);
    }
}
