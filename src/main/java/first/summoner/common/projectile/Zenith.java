package first.summoner.common.projectile;


import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.projectile.Projectile;
import first.summoner.Summoner;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.RenderUtil;
import first.summoner.utils.SummonerStreamCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Zenith extends Projectile implements IEntityCollision<Zenith> {

    public Vec3 initialPosition = Vec3.ZERO;
    public RenderType renderType;
    public float alpha;

    public Zenith() {
        super(SummonerAttachmentEntityRegister.ZENITH);
        setMaxTickCount(30);
        setPhysics(false);
        Random random = new Random();
        this.renderType = RenderType.values()[random.nextInt(RenderType.values().length)];
        this.alpha = random.nextFloat(2, 10) * 0.1f;
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(SummonerStreamCodecs.VEC_3, () -> initialPosition, value -> initialPosition = value);
        fields.field(SummonerStreamCodecs.ZENITH_RENDER_TYPE, () -> renderType, value -> renderType = value);
    }

    @Override
    public void tick() {
        super.tick();
        ArrayList<PathNode> pathNodes = getHistoryNodes();
        historyNodes.clear();
        historyNodes.addAll(pathNodes);
    }

    @Override
    public boolean isAlive() {
        return isExecutingPath();
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        PathNode renderNode = super.getRenderNode(partialTick);
        Vec3 currentPos = owner.getPosition(partialTick);
        return renderNode.modifyPos(renderNode.pos().add(currentPos.subtract(initialPosition)));
    }

    @Override
    public ArrayList<PathNode> getHistoryNodes() {
        ArrayList<PathNode> pathNodes = super.getHistoryNodes();
        if (tickCount > 0) {
            ArrayList<PathNode> list = new ArrayList<>();
            float partialTick = RenderUtil.getPartialTick();
            Vec3 currentPos = owner.getPosition(partialTick);
            for (PathNode pathNode : pathNodes) {
                list.add(pathNode.modifyPos(pathNode.pos().add(currentPos.subtract(initialPosition))));
            }
            return list;
        }
        return pathNodes;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-1, -0.5, -1.5, 1, 0.5, 2);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            attack(hit.entity(), getDamage(), 1);
        }
    }

    /**
     * 天顶剑轨迹所使用的剑类型，颜色为各自轨迹的渲染颜色。
     */
    public enum RenderType {
        // 铜短剑 - 浅橙色 (#EBA687)
        COPPER_SHORT_SWORD("copper_short_sword", 0xEBA687),
        // 魔光剑 - 紫色 (#7A42BF)
        LIGHTS_BANE("lights_bane", 0x7A42BF),
        // 村正 - 海军蓝 (#384ED2)
        MURAMASA("muramasa", 0x384ED2),
        // 泰拉魔刃 - 亮薄荷绿 (#B2FFB4)
        TERRA_BLADE("terra_blade", 0xB2FFB4),
        // 血腥屠刀 - 红色 (#ED1C24)
        BLOOD_BUTCHERER("blood_butcherer", 0xED1C24),
        // 星怒 - 粉色 (#EC3EC0)
        STARFURY("starfury", 0xEC3EC0),
        // 附魔剑 - 浅蓝色 (#5B9EE8)
        ENCHANTED_SWORD("enchanted_sword", 0x5B9EE8),
        // 养蜂人 - 黄色 (#FFE745)
        BEE_KEEPER("bee_keeper", 0xFFE745),
        // 草剑 - 绿色 (#6BCB00)
        BLADE_OF_GRASS("blade_of_grass", 0x6BCB00),
        // 火山 - 橙色 (#FE9E23)
        FIERY_GREATSWORD("fiery_greatsword", 0xFE9E23),
        // 永夜刃 - 紫色 (#B336C9)
        NIGHTS_EDGE("nights_edge", 0xB336C9),
        // 真永夜刃 - 紫色 (#B336C9)
        TRUE_NIGHTS_EDGE("true_nights_edge", 0xB336C9),
        // 断钢剑 - 黄色 (#ECC813)
        EXCALIBUR("excalibur", 0xECC813),
        // 真断钢剑 - 黄色 (#ECC813)
        TRUE_EXCALIBUR("true_excalibur", 0xECC813),
        // 无头骑士剑 - 橙色 (#FC5F04)
        THE_HORSEMANS_BLADE("the_horsemans_blade", 0xFC5F04),
        // 种子弯刀 - 绿色 (#8FD71D)
        SEEDLER("seedler", 0x8FD71D),
        // 泰拉刃 - 浅绿色 (#50DE7A)
        TRUE_TERRA_BLADE("true_terra_blade", 0x50DE7A),
        // 波涌之刃 - 青色 (#54EAF5)
        INFLUX_WAVER("influx_waver", 0x54EAF5),
        // 狂星之怒 - 粉色 (#ED3F85)
        STAR_WRATH("star_wrath", 0xED3F85),
        // 彩虹猫之刃 - 浅粉色 (#FEC2FA)
        MEOWMERE("meowmere", 0xFEC2FA),
        // 天顶剑 - 亮薄荷绿 (#B2FFB4)
        ZENITH("zenith", 0xB2FFB4);

        private final ResourceLocation texture;
        private final int color;

        RenderType(String textureName, int color) {
            // 与 assets/summoner/lyra_model/json/projectile/zenith/<name>/<name>.png 一一对应
            this.texture = Summoner.rl("lyra_model/json/projectile/zenith/" + textureName + "/" + textureName);
            this.color = color;
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        public int getColor() {
            return color;
        }

        public int getColorRBG(float alpha) {
            return ((int) (alpha * 255) << 24) | (this.color & 0xFFFFFF);
        }
    }
}
