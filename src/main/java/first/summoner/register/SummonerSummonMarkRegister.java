package first.summoner.register;


import first.lyra.api.LyraHelper;

import first.lyra.common.attachment.SummonMarkTracker;
import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.attachmentEntity.AttachmentEntityDamageSource;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.summonMark.SummonMarkInstance;
import first.lyra.common.summonMark.SummonMarkType;
import first.summoner.Summoner;
import first.summoner.common.projectile.Meteor;
import first.summoner.common.projectile.Petal;
import first.summoner.common.projectile.ShatteredStellarCore;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * 召唤标记类型注册。
 * <p>
 * 全部标记直接使用上游 {@link SummonMarkType} 的完整构造函数注册，构造参数依次为：
 * <pre>
 * location, additionalDamage, additionalArmorPierce, criticalHitRate,
 * tickConsumer, damagePre, damagePost, killConsumer
 * </pre>
 * 其中 damagePost 即原 onDamage（命中结算后触发），killConsumer 即原 onKill（击杀时触发）。
 * <p>
 * 标记能量（power）已被上游移除，原“消耗能量”的效果一律改为
 * {@link SummonMarkInstance#isUsed()} / {@link SummonMarkInstance#setUsed(boolean)} 的一次性标记判断；
 * 标记持续时间、暴击伤害等上游已无对应能力的效果一并移除。
 * <p>
 * {@link AttachmentEntityDamageSource#getOwner()} 必定非空，因此召唤物持有者直接取用，无需判空。
 */
public class SummonerSummonMarkRegister {

    private static final DeferredRegister<SummonMarkType> Register = DeferredRegister.create(SummonerRegistries.SUMMON_MARK_TYPE, Summoner.MODID);

    public static final DeferredHolder<SummonMarkType, SummonMarkType> Empty = Register.register("empty", location -> new SummonMarkType(
            location,
            0f,
            0f,
            0f,
            null,
            null,
            null,
            null));

    /** 食物链 - 命中加速+减速（可重复触发） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> FoodChain = Register.register("food_chain", location -> new SummonMarkType(
            location,
            0.2f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                source.getOwner().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            },
            null));

    /** 丛林捕手 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> JungleHunter = Register.register("jungle_hunter", location -> new SummonMarkType(
            location,
            0.3f,
            0f,
            0f,
            null,
            null,
            null,
            null));

    /** 死亡执政官 - 击杀溅射（3格内其他一个目标 150%） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DeathMagister = Register.register("death_magister", location -> new SummonMarkType(
            location,
            0.4f,
            0f,
            0f,
            null,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source) -> {
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 3, livingEntity -> target != livingEntity);
                entities.stream()
                        .findAny()
                        .ifPresent(living -> source.getAttachmentEntity().attack(living, minion.getDamage() * 1.5f, 0));
            }));

    /** 灵魂鞭挞 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> SoulFlail = Register.register("soul_flail", location -> new SummonMarkType(
            location,
            0.5f,
            0f,
            0f,
            null,
            null,
            null,
            null));

    /** 星坠 - 命中生成流星（目标上方生成，直线下落，命中半径2溅射 133%），每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Starfall = Register.register("starfall", location -> new SummonMarkType(
            location,
            0.2f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                AttachmentEntity minion = source.getAttachmentEntity();
                RandomSource random = minion.getRandom();
                Vec3 pos = target.position().add(0, 8, 0).offsetRandom(random, 3);
                Vec3 dir = target.position().add(0, 0.5f, 0).subtract(pos).normalize();
                Meteor meteor = new Meteor();
                meteor.setOwner(source.getOwner());
                meteor.init(new PathNode(pos, dir));
                meteor.copyAttributes(minion);
                meteor.setVelocity(dir);
                meteor.setDamage(damage * 1.33f);
                LyraHelper.get(source.getOwner()).add(meteor);
            },
            null));

    /** 荆棘 - 命中加速+中毒+凋零（可重复触发） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Bramble = Register.register("bramble", location -> new SummonMarkType(
            location,
            0.6f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                source.getOwner().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40 + source.getAttachmentEntity().getRandom().nextInt(41), 0));
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            },
            null));

    /** 脉管 - 击杀回血（12% 已损生命，最多8点） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Vein = Register.register("vein", location -> new SummonMarkType(
            location,
            0.5f,
            0f,
            0f,
            null,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source) -> {
                Player owner = source.getOwner();
                float missing = owner.getMaxHealth() - owner.getHealth();
                owner.heal(Math.min(8, missing * 0.12f));
            }));

    /** 脊柱 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Spine = Register.register("spine", location -> new SummonMarkType(
            location,
            0.7f,
            4f,
            0f,
            null,
            null,
            null,
            null));

    /** 鞭炮 - 命中爆炸（275% 主目标 + 175% 半径3其他 + 点燃5秒），每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Firecracker = Register.register("firecracker", location -> new SummonMarkType(
            location,
            0f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                AttachmentEntity minion = source.getAttachmentEntity();
                Vec3 pos = target.getBoundingBox().getCenter();
                // 主目标：275% 结算伤害 + 点燃
                source.getAttachmentEntity().attack(target, damage * 2.75f, 0);
                target.igniteForSeconds(5);
                // 半径 3 格内其他目标：175% + 点燃（TargetCache 目标选择）
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(pos, 3, other -> other != target);
                for (LivingEntity other : entities) {
                    source.getAttachmentEntity().attack(other, damage * 1.75f, 0);
                    other.igniteForSeconds(5);
                }
                ParticleHelper.create(target.level())
                        .generic(GenericParticleBuilder.create()
                                         .centerColor(0xea9900)
                                         .edgeColor(0xea5700)
                                         .lifetime(5)
                                         .lifetimeRandom(20)
                                         .spin(0.3f)
                                         .spinRandom(0.05F)
                                         .friction(0.75F)
                                         .scale(0.025f)
                                         .scaleRandom(0.005f)
                        )
                        .pos(pos)
                        .offset(0.25)
                        .velocity(pos.offsetRandom(minion.getRandom(), 5).subtract(pos).normalize())
                        .count(200)
                        .speed(0.75)
                        .spread(Math.TAU)
                        .emit();
            },
            null));

    /** 霜雪 - 命中减速2（可重复触发） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> FrostSnow = Register.register("frost_snow", location -> new SummonMarkType(
            location,
            0.6f,
            8f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) ->
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1)),
            null));

    /** 神圣裁决 - 击杀溅射（3格内其他一个目标 450%） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DivineJudgment = Register.register("divine_judgment", location -> new SummonMarkType(
            location,
            0.9f,
            0f,
            0f,
            null,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source) -> {
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 3, livingEntity -> target != livingEntity);
                entities.stream()
                        .findAny()
                        .ifPresent(living -> source.getAttachmentEntity().attack(living, minion.getDamage() * 4.5f, 0));
            }));

    /** 花儿的粗暴展示 - 花瓣射弹（3~6个，225%伤害/50穿透/不击退/延迟追踪），每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> RoughDisplayOfFlowers = Register.register("rough_display_of_flowers", location -> new SummonMarkType(
            location,
            0.9f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                AttachmentEntity minion = source.getAttachmentEntity();
                Vec3 pos = target.getBoundingBox().getCenter();
                RandomSource random = minion.getRandom();
                int count = 3 + random.nextInt(4);
                for (int i = 0; i < count; i++) {
                    Vec3 direction = pos.offsetRandom(random, 5).subtract(pos).normalize();
                    Petal petal = new Petal();
                    petal.setOwner(source.getOwner());
                    petal.init(new PathNode(pos, direction));
                    petal.copyAttributes(minion);
                    petal.setVelocity(direction);
                    petal.setDamage(damage * 2.25f / count);
                    petal.setArmorPierce(50);
                    petal.setKnockback(0);
                    petal.setChaseDelay(7 + random.nextInt(7));
                    LyraHelper.get(source.getOwner()).add(petal);
                }
            },
            null));

    /** 支配预兆 - 9颗星核射弹（20%结算伤害），每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DominanceOmen = Register.register("dominance_omen", location -> new SummonMarkType(
            location,
            2.5f,
            0f,
            0.1f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                AttachmentEntity minion = source.getAttachmentEntity();
                RandomSource random = minion.getRandom();
                for (int i = 0; i < 9; i++) {
                    Vec3 pos = target.getBoundingBox().getCenter().offsetRandom(random, 10);
                    Vec3 dir = pos.subtract(pos.offsetRandom(random, 10)).normalize();
                    ShatteredStellarCore core = new ShatteredStellarCore();
                    core.setOwner(source.getOwner());
                    core.init(new PathNode(pos, dir));
                    core.copyAttributes(minion);
                    core.setVelocity(dir);
                    core.setTickCount(15);
                    core.setDamage(damage * 0.2f);
                    core.setArmorPierce(50);
                    core.setChaseTarget(target);
                    LyraHelper.get(source.getOwner()).add(core);
                }
            },
            null));

    /** 暗黑收割 - 溅射（6格内其他最多3目标 25%，可重复触发） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DarkHarvest = Register.register("dark_harvest", location -> new SummonMarkType(
            location,
            0f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 6, other -> other != target);
                int count = 0;
                for (LivingEntity other : entities) {
                    if (count >= 3) {
                        break;
                    }
                    source.getAttachmentEntity().attack(other, damage * 0.25f, 0);
                    count++;
                }
            },
            null));

    /** 星座 - 星核射弹（6颗 ShatteredStellarCore，30%伤害/召唤物穿透/召唤物击退），每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Constellation = Register.register("constellation", location -> new SummonMarkType(
            location,
            1.5f,
            0f,
            0.15f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                AttachmentEntity minion = source.getAttachmentEntity();
                RandomSource random = minion.getRandom();
                for (int i = 0; i < 6; i++) {
                    Vec3 pos = target.getBoundingBox().getCenter().offsetRandom(random, 5);
                    Vec3 dir = pos.subtract(pos.offsetRandom(random, 5)).normalize();
                    ShatteredStellarCore core = new ShatteredStellarCore();
                    core.setOwner(source.getOwner());
                    core.init(new PathNode(pos, dir));
                    core.copyAttributes(minion);
                    core.setVelocity(dir);
                    core.setDamage(damage * 0.3f);
                    core.setArmorPierce(minion.getArmorPierce());
                    core.setKnockback(minion.getKnockback());
                    core.setChaseTarget(target);
                    LyraHelper.get(source.getOwner()).add(core);
                }
            },
            null));

    /** 电流翻涌 - 连锁溅射（6格内其他目标 100%+每目标10%，最高200%），每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> CurrentSurge = Register.register("current_surge", location -> new SummonMarkType(
            location,
            1.2f,
            0f,
            0.05f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                // 6 格内其他目标（TargetCache 目标选择），伤害系数 100%+每目标10%，最高 200%
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> others = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 6, other -> other != target);
                float multiplier = Math.min(1f + 0.1f * others.size(), 2f);
                Vec3 center = target.getBoundingBox().getCenter();
                ParticleHelper.create(target.level())
                        .generic(GenericParticleBuilder.create()
                                         .centerColor(0x38ffec)
                                         .edgeColor(0x2fc1ae)
                                         .lifetime(10)
                                         .lifetimeRandom(20)
                                         .spin(0.3f)
                                         .spinRandom(0.05F)
                                         .friction(0.82F)
                                         .scale(0.025f)
                                         .scaleRandom(0.005f)
                        )
                        .pos(center)
                        .offset(0.15)
                        .velocity(center.offsetRandom(minion.getRandom(), 5).subtract(center).normalize())
                        .count(160)
                        .speed(0.55)
                        .spread(Math.TAU)
                        .emit();
                for (LivingEntity other : others) {
                    Vec3 pos = other.getBoundingBox().getCenter();
                    ParticleHelper.create(target.level())
                            .generic(GenericParticleBuilder.create()
                                             .centerColor(0x38ffec)
                                             .edgeColor(0x2fc1ae)
                                             .lifetime(10)
                                             .lifetimeRandom(20)
                                             .spin(0.3f)
                                             .spinRandom(0.05F)
                                             .friction(0.82F)
                                             .scale(0.025f)
                                             .scaleRandom(0.005f)
                            )
                            .pos(pos)
                            .offset(0.15)
                            .velocity(pos.offsetRandom(other.getRandom(), 5).subtract(pos).normalize())
                            .count(80)
                            .speed(0.25)
                            .spread(Math.TAU)
                            .emit();
                    source.getAttachmentEntity().attack(other, damage * multiplier, 0);
                }
            },
            null));

    /** 晨星 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> MorningStar = Register.register("morning_star", location -> new SummonMarkType(
            location,
            1f,
            20f,
            0.1f,
            null,
            null,
            null,
            null));

    /** 万花筒 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Kaleidoscope = Register.register("kaleidoscope", location -> new SummonMarkType(
            location,
            2f,
            12f,
            0.1f,
            null,
            null,
            null,
            null));

    /** 安魂曲 - 1标记伤害，15%暴击率，命中额外造成目标护甲值250%伤害，每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Requiem = Register.register("requiem", location -> new SummonMarkType(
            location,
            1f,
            0f,
            0.15f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                source.getAttachmentEntity().attack(target, target.getArmorValue() * 2.5f, 0);
            },
            null));

    /** 银河 - 命中对目标额外造成400%召唤伤害，每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Galaxy = Register.register("galaxy", location -> new SummonMarkType(
            location,
            0f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                if (instance.isUsed()) {
                    return;
                }
                instance.setUsed(true);
                source.getAttachmentEntity().attack(target, damage * 4f, 0);
            },
            null));

    /** 沉默的终时 - 3标记伤害，5%暴击率 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> SilentEnd = Register.register("silent_end", location -> new SummonMarkType(
            location,
            3f,
            0f,
            0.05f,
            null,
            null,
            null,
            null));

    /** 月噬 - 1标记伤害，10%暴击率，命中施加2秒月噬，击杀时目标附近其他目标施加10秒月噬 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> MoonBite = Register.register("moon_bite", location -> new SummonMarkType(
            location,
            1f,
            0f,
            0.1f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) ->
                    target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.MoonBite, 60, 0)),
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source) -> {
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 3, other -> other != target);
                for (LivingEntity other : entities) {
                    other.addEffect(new MobEffectInstance(SummonerMobEffectRegister.MoonBite, 200, 0));
                }
                Vec3 center = target.getBoundingBox().getCenter();
                ParticleHelper.create(target.level())
                        .generic(GenericParticleBuilder.create()
                                         .centerColor(0x00fdd6)
                                         .edgeColor(0x00d4b0)
                                         .lifetime(10)
                                         .lifetimeRandom(20)
                                         .spin(0.3f)
                                         .spinRandom(0.05F)
                                         .friction(0.82F)
                                         .scale(0.025f)
                                         .scaleRandom(0.005f)
                        )
                        .pos(center)
                        .offset(0.15)
                        .velocity(center.offsetRandom(minion.getRandom(), 5).subtract(center).normalize())
                        .count(160)
                        .speed(0.55)
                        .spread(Math.TAU)
                        .emit();
            }));

    /** 天使 - 命中时生命不满则恢复5%已损生命值+2点（最多10点），每次标记仅触发一次 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Angel = Register.register("angel", location -> new SummonMarkType(
            location,
            0f,
            0f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) -> {
                Player owner = source.getOwner();
                float missing = owner.getMaxHealth() - owner.getHealth();
                if (missing > 0 && !instance.isUsed()) {
                    instance.setUsed(true);
                    owner.heal(Math.min(10, missing * 0.05f + 2));
                }
            },
            null));

    /** 弑神者 - 50护甲穿透，命中施加10秒弑神怒焰 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> GodSlayer = Register.register("god_slayer", location -> new SummonMarkType(
            location,
            0f,
            50f,
            0f,
            null,
            null,
            (SummonMarkTracker tracker, SummonMarkInstance instance, LivingEntity target, AttachmentEntityDamageSource source, Float damage) ->
                    target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.GodSlayerInferno, 200, 0)),
            null));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
