package first.summoner.register;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.Summoner;
import first.summoner.common.projectile.Meteor;
import first.summoner.common.projectile.Petal;
import first.summoner.common.projectile.ShatteredStellarCore;
import first.summoner.common.summonMark.SummonMarkType;
import first.summoner.common.summonMark.SummonMarkTypeBuild;
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

public class SummonerSummonMarkRegister {

    private static final DeferredRegister<SummonMarkType> Register = DeferredRegister.create(SummonerRegistries.SUMMON_MARK_TYPE, Summoner.MODID);

    public static final DeferredHolder<SummonMarkType, SummonMarkType> Empty = Register.register("empty", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation).build());

    /** 食物链 - 命中加速+减速（可重复触发） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> FoodChain = Register.register("food_chain", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.2f)
            .onDamage((mark, target, source, damage) -> {
                LivingEntity owner = source.getAttachmentEntity() instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                if (owner != null) {
                    owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
                }
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            })
            .build());

    /** 丛林捕手 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> JungleHunter = Register.register("jungle_hunter", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.3f)
            .build());

    /** 死亡执政官 - 击杀溅射（3格内其他一个目标 150%） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DeathMagister = Register.register("death_magister", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.4f)
            .onKill((target, source) -> {
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 3, livingEntity -> target != livingEntity);
                entities.stream()
                        .findAny()
                        .ifPresent(living -> InvincibleData.attack(living)
                                .damageSource(source)
                                .damageAmount(minion.getDamage() * 1.5f)
                                .apply());
            })
            .build());

    /** 灵魂鞭挞 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> SoulFlail = Register.register("soul_flail", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.5f)
            .build());

    /** 星坠 - 命中生成流星（目标上方生成，直线下落，命中半径2溅射 133%） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Starfall = Register.register("starfall", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.2f)
            .onDamage((data, target, source, damage) -> {
                if (data.usePower(30)) {
                    AttachmentEntity minion = source.getAttachmentEntity();
                    Player owner = source.getAttachmentEntity() instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                    RandomSource random = minion.getRandom();
                    Vec3 pos = target.position().add(0, 8, 0).offsetRandom(random, 3);
                    Vec3 dir = target.position().add(0, 0.5f, 0).subtract(pos).normalize();
                    Meteor meteor = new Meteor();
                    meteor.setOwner(owner);
                    meteor.init(new PathNode(pos, dir));
                    meteor.copyAttributes(minion);
                    meteor.setVelocity(dir);
                    meteor.setDamage(damage * 1.33f);
                    LyraHelper.get(minion.getOwner()).add(meteor);
                }
            })
            .build());

    /** 荆棘 - 命中加速+中毒+凋零（可重复触发） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Bramble = Register.register("bramble", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.6f)
            .onDamage((mark, target, source, damage) -> {
                LivingEntity owner = source.getAttachmentEntity() instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                if (owner != null) {
                    owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40 + source.getAttachmentEntity().getRandom().nextInt(41), 0));
                }
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            })
            .build());

    /** 脉管 - 击杀回血（12% 已损生命，最多8点） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Vein = Register.register("vein", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.5f)
            .onKill((target, source) -> {
                LivingEntity owner = source.getAttachmentEntity() instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                if (owner != null) {
                    float missing = owner.getMaxHealth() - owner.getHealth();
                    owner.heal(Math.min(8, missing * 0.12f));
                }
            })
            .build());

    /** 脊柱 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Spine = Register.register("spine", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.7f)
            .armorPierce(4)
            .build());

    /**
     * 鞭炮 - 命中爆炸（275% 主目标 + 175% 半径3其他 + 点燃5秒）
     */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Firecracker = Register.register("firecracker", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .onDamage((mark, target, source, damage) -> {
                if (mark.usePower(30)) {
                    AttachmentEntity minion = source.getAttachmentEntity();
                    Vec3 pos = target.getBoundingBox().getCenter();
                    // 主目标：275% 结算伤害 + 点燃
                    InvincibleData.attack(target)
                            .damageSource(source)
                            .damageAmount(damage * 2.75f)
                            .apply();
                    target.igniteForSeconds(5);
                    // 半径 3 格内其他目标：175% + 点燃（TargetCache 目标选择）
                    List<LivingEntity> entities = minion.getTargetCache()
                            .getEntitiesInRadius(pos, 3, other -> other != target);
                    for (LivingEntity other : entities) {
                        InvincibleData.attack(other)
                                .damageSource(source)
                                .damageAmount(damage * 1.75f)
                                .apply();
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
                }
            })
            .build());

    /** 霜雪 - 命中减速2（可重复触发） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> FrostSnow = Register.register("frost_snow", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.6f)
            .armorPierce(8)
            .onDamage((mark, target, source, damage) -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1)))
            .build());

    /** 神圣裁决 - 击杀溅射（3格内其他一个目标 450%） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DivineJudgment = Register.register("divine_judgment", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.9f)
            .onKill((target, source) -> {
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 3, livingEntity -> target != livingEntity);
                entities.stream()
                        .findAny()
                        .ifPresent(living -> InvincibleData.attack(living)
                                .damageSource(source)
                                .damageAmount(minion.getDamage() * 4.5f)
                                .apply());
            })
            .build());

    /** 花儿的粗暴展示 - 花瓣射弹（3~6个，225%伤害/50穿透/不击退/延迟追踪） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> RoughDisplayOfFlowers = Register.register("rough_display_of_flowers", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(0.9f)
            .onDamage((mark, target, source, damage) -> {
                if (mark.usePower(30)) {
                    AttachmentEntity minion = source.getAttachmentEntity();
                    Player owner = minion instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                    Vec3 pos = target.getBoundingBox().getCenter();
                    RandomSource random = minion.getRandom();
                    int count = 3 + random.nextInt(4);
                    for (int i = 0; i < count; i++) {
                        Vec3 direction = pos.offsetRandom(random, 5).subtract(pos).normalize();
                        Petal petal = new Petal();
                        petal.setOwner(owner);
                        petal.init(new PathNode(pos, direction));
                        petal.copyAttributes(minion);
                        petal.setVelocity(direction);
                        petal.setDamage(damage * 2.25f / count);
                        petal.setArmorPierce(50);
                        petal.setKnockback(0);
                        petal.setChaseDelay(7 + random.nextInt(7));
                        LyraHelper.get(minion.getOwner()).add(petal);
                    }
                }
            })
            .build());

    /** 支配预兆 - 3颗星核射弹（20%结算伤害） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DominanceOmen = Register.register("dominance_omen", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(2.5f)
            .criticalRate(0.1f)
            .onDamage((mark, target, source, damage) -> {
                if (mark.usePower(10)) {
                    AttachmentEntity minion = source.getAttachmentEntity();
                    Player owner = minion instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                    RandomSource random = minion.getRandom();
                    for (int i = 0; i < 9; i++) {
                        Vec3 pos = target.getBoundingBox().getCenter().offsetRandom(random, 10);
                        Vec3 dir = pos.subtract(pos.offsetRandom(random, 10)).normalize();
                        ShatteredStellarCore core = new ShatteredStellarCore();
                        core.setOwner(owner);
                        core.init(new PathNode(pos, dir));
                        core.copyAttributes(minion);
                        core.setVelocity(dir);
                        core.setTickCount(15);
                        core.setDamage(damage * 0.2f);
                        core.setArmorPierce(50);
                        core.setChaseTarget(target);
                        LyraHelper.get(minion.getOwner()).add(core);
                    }
                }
            })
            .build());

    /** 暗黑收割 - 溅射（6格内其他最多3目标 25%，可重复触发，不标记已使用） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> DarkHarvest = Register.register("dark_harvest", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .onDamage((mark, target, source, damage) -> {
                AttachmentEntity minion = source.getAttachmentEntity();
                List<LivingEntity> entities = minion.getTargetCache()
                        .getEntitiesInRadius(target.position(), 6, other -> other != target);
                int count = 0;
                for (LivingEntity other : entities) {
                    if (count >= 3) {
                        break;
                    }
                    InvincibleData.attack(other)
                            .damageSource(source)
                            .damageAmount(damage * 0.25f)
                            .apply();
                    count++;
                }
            })
            .build());

    /** 星座 - 星核射弹（6颗 ShatteredStellarCore，30%伤害/100%穿透/100%击退） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Constellation = Register.register("constellation", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(1.5f)
            .criticalRate(0.15f)
            .onDamage((mark, target, source, damage) -> {
                if (mark.usePower(30)) {
                    AttachmentEntity minion = source.getAttachmentEntity();
                    Player owner = minion instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                    RandomSource random = minion.getRandom();
                    for (int i = 0; i < 6; i++) {
                        Vec3 pos = target.getBoundingBox().getCenter().offsetRandom(random, 5);
                        Vec3 dir = pos.subtract(pos.offsetRandom(random, 5)).normalize();
                        ShatteredStellarCore core = new ShatteredStellarCore();
                        core.setOwner(owner);
                        core.init(new PathNode(pos, dir));
                        core.copyAttributes(minion);
                        core.setVelocity(dir);
                        core.setDamage(damage * 0.3f);
                        core.setArmorPierce(minion.getArmorPierce());
                        core.setKnockback(minion.getKnockback());
                        core.setChaseTarget(target);
                        LyraHelper.get(minion.getOwner()).add(core);
                    }
                }
            })
            .build());

    /** 电流翻涌 - 连锁溅射（6格内其他目标 100%+每目标10%，最高200%） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> CurrentSurge = Register.register("current_surge", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(1.2f)
            .criticalRate(0.05f)
            .onDamage((mark, target, source, damage) -> {
                if (mark.usePower(30)) {
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
                        InvincibleData.attack(other)
                                .damageSource(source)
                                .damageAmount(damage * multiplier)
                                .apply();
                    }
                }
            })
            .build());

    /** 晨星 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> MorningStar = Register.register("morning_star", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(1f)
            .armorPierce(20)
            .criticalRate(0.1f)
            .build());

    /** 万花筒 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Kaleidoscope = Register.register("kaleidoscope", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(2f)
            .armorPierce(12)
            .criticalRate(0.1f)
            .build());

    /** 安魂曲 - 1标记伤害，15%暴击率，命中消耗40标记能量造成目标护甲值250%召唤伤害 */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Requiem = Register.register("requiem", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(1f)
            .criticalRate(0.15f)
            .onDamage((mark, target, source, damage) -> {
                if (mark.usePower(40)) {
                    InvincibleData.attack(target)
                            .damageSource(source)
                            .damageAmount(target.getArmorValue() * 2.5f)
                            .apply();
                }
            })
            .build());

    /** 银河 - 命中消耗40标记能量，对目标额外造成400%召唤伤害（能量积累+33%在网络包中实现） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Galaxy = Register.register("galaxy", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .onDamage((mark, target, source, damage) -> {
                if (mark.usePower(40)) {
                    InvincibleData.attack(target)
                            .damageSource(source)
                            .damageAmount(damage * 4f)
                            .apply();
                }
            })
            .build());

    /** 沉默的终时 - 3标记伤害，5%暴击率（持续时间+200%在网络包中实现） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> SilentEnd = Register.register("silent_end", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(3f)
            .criticalRate(0.05f)
            .build());

    /**
     * 月噬 - 1标记伤害，10%暴击率，能量>5时命中施加2秒月噬，击杀时目标附近所有其他目标施加10秒月噬
     */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> MoonBite = Register.register("moon_bite", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(1f)
            .criticalRate(0.1f)
            .onDamage((mark, target, source, damage) -> target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.MoonBite, 60, 0)))
            .onKill((target, source) -> {
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
            })
            .build());

    /** 天使 - 命中时生命不满则消耗20标记能量，恢复5%已损生命值+2点（最多10点） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> Angel = Register.register("angel", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .onDamage((mark, target, source, damage) -> {
                LivingEntity owner = source.getAttachmentEntity() instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                if (owner != null) {
                    float missing = owner.getMaxHealth() - owner.getHealth();
                    if (missing > 0 && mark.usePower(20)) {
                        owner.heal(Math.min(10, missing * 0.05f + 2));
                    }
                }
            })
            .build());

    /** 弑神者 - 50护甲穿透，命中施加10秒弑神怒焰，能量积累+33%（网络包） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> GodSlayer = Register.register("god_slayer", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .armorPierce(50f)
            .onDamage((mark, target, source, damage) -> target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.GodSlayerInferno, 200, 0)))
            .build());

    /** 水蓝色黎明 - 15%暴击率，暴击伤害+50%（MixinHandlerMixin） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> CeruleanDawn = Register.register("cerulean_dawn", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .criticalRate(0.15f)
            .build());

    /** 秘仪 - 1.5标记伤害，暴击伤害+15%，每2%标记暴击率额外提升1%暴击伤害（MixinHandlerMixin） */
    public static final DeferredHolder<SummonMarkType, SummonMarkType> ArcaneRite = Register.register("arcane_rite", ResourceLocation -> new SummonMarkTypeBuild(ResourceLocation)
            .damage(1.5f)
            .build());

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
