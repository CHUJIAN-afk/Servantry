package first.summoner.common.projectile;


import first.lyra.common.attachmentEntity.*;
import first.lyra.utils.EasingCurve;
import first.summoner.Summoner;
import first.summoner.common.particle.zenithParticle.ZenithParticleOptions;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.SummonerStreamCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class Zenith extends AttachmentEntity implements IEntityCollision<Zenith> {

    public Vec3 offest;
    public LivingEntity target;
    public RenderType renderType;
    public float alpha;

    public Zenith() {
        super(SummonerAttachmentEntityRegister.ZENITH);
        Random random = new Random();
        this.renderType = RenderType.values()[random.nextInt(RenderType.values().length)];
        this.alpha = random.nextFloat(0.105f, 1);
        if (alpha > 0.8) {
            alpha = 1;
        }
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(SummonerStreamCodecs.VEC_3, () -> offest, value -> offest = value);
        fields.field(SummonerStreamCodecs.ZENITH_RENDER_TYPE, () -> renderType, value -> renderType = value);
        fields.field(SummonerStreamCodecs.INT, () -> target != null ? target.getId() : -1, (level, value) -> {
            if (value != -1 && level.getEntity(value) instanceof LivingEntity living) {
                target = living;
            } else {
                target = null;
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        setCurrentPathNode(getRenderNode(1));
        RandomSource random = getRandom();
        if (tickCount > 2 && tickCount < 9 && random.nextFloat() < 0.5f) {
            PathNode last = historyNodes.getFirst();
            int count = random.nextIntBetweenInclusive(1, 5);
            for (int i = 0; i < count; i++) {
                Vec3 pos = getCurrentPathNode().pos();
                Vec3 direction = pos.subtract(last.pos());
                pos = pos.add(direction.scale(random.nextFloat() * 2)).offsetRandom(random, 0.5f);
                // 天顶剑粒子：颜色跟随剑型，大小/速度/阻力按范围随机，寿命固定 10 tick 自然消退
                float scale = Mth.nextFloat(random, 0.02F, 0.04F);
                float speed = Mth.nextFloat(random, 0.6F, 1.2F);
                float friction = Mth.nextFloat(random, 0.35F, 0.7F);
                int life = random.nextIntBetweenInclusive(5, 40);
                ParticleHelper.create(owner.level())
                        .type(new ZenithParticleOptions(renderType.getColor(), life, friction, scale))
                        .pos(pos)
                        .velocity(direction.normalize().scale(speed))
                        .count(0)
                        .emit();
            }
        }
    }

    @Override
    public boolean isAlive() {
        return tickCount <= 11;
    }

    public Ellipse getRenderEllipse(float partialTick) {
        Vec3 currentPos = owner.getPosition(partialTick).add(0, owner.getBbHeight() / 2, 0);
        Vec3 pointA = target != null ? target.getPosition(partialTick).add(0, target.getBbHeight() / 2, 0) : currentPos.add(offest);
        Vec3 pointB = currentPos.add(currentPos.subtract(pointA).normalize().scale(2));
        RandomSource random = getRandom();
        random.setSeed(getUuid().hashCode());
        Vec3 normal = Ellipse.randomPlaneNormal(random, pointA, pointB);
        float curvature = 0.2f + random.nextFloat() * 0.6f;
        return new Ellipse(pointA, pointB, normal, curvature);
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        Ellipse renderEllipse = getRenderEllipse(partialTick);
        float progress = (tickCount + partialTick) / 9;
        progress = EasingCurve.bezier()
                .control(0.1f)
                .control(0.2f)
                .control(0.3f)
                .control(0.4f)
                .control(0.45f)
                .control(0.475f)
                .control(0.5f)
                .control(0.5f)
                .control(0.525f)
                .control(0.575f)
                .control(0.6f)
                .control(0.7f)
                .control(0.8f)
                .control(0.9f)
                .control(1f)
                .build()
                .apply(progress);
        Vec3 point = renderEllipse.getPoint(progress);
        Vec3 endPos = renderEllipse.getPointA();
        Vec3 startPos = renderEllipse.getPointB();
        Vec3 normal = renderEllipse.getPlaneNormal();
        Vec3 tipPoint;
        if (progress < 0.5) {
            tipPoint = startPos.lerp(endPos, Mth.clamp(progress * 2, 0.05, 0.95));
        } else {
            tipPoint = endPos.lerp(startPos, Mth.clamp(progress * 2 - 1, 0.05, 0.95));
        }
        Vec3 tipDir = point.subtract(tipPoint).normalize();
        return getEulerNode(point, tipDir, normal);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.35, -0.1, -1.5, 0.35, 0.1, 1.5);
    }

    @Override
    public boolean isValidCollisionTarget(Zenith entity, LivingEntity target) {
        return owner != target;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            attack(hit.entity(), getDamage(), 1);
        }
    }

    @Override
    public void entityCollision() {
        IEntityCollision.super.entityCollision();
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
            this.texture = Summoner.rl("lyra_model/json/projectile/zenith/" + textureName + "/" + textureName);
            this.color = color;
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        public int getColor() {
            return color;
        }

        public int getColorARBG(float alpha) {
            return FastColor.ARGB32.color((int) (alpha * 255), color);
        }
    }
}
