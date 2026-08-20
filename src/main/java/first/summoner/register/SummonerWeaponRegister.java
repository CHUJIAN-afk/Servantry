package first.summoner.register;

import first.lyra.api.LyraHelper;
import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.attachment.AttachmentEntityData;

import first.lyra.common.builder.MinionWeaponItemBuilder;
import first.lyra.common.creativeTab.Section;
import first.lyra.common.entity.PathNode;
import first.lyra.register.LyraItemRegisterBuilder;
import first.summoner.Summoner;
import first.summoner.common.dataComponent.ScabbardContainer;
import first.summoner.common.minion.*;
import first.summoner.common.sentry.*;
import first.summoner.config.StartupConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

import static first.summoner.register.SummonerItemRegister.ITEMS;

public class SummonerWeaponRegister {

    public static final Section MINION_WEAPON = new Section(0, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionMinionWeapon);

    /**
     * 召唤契约 - 无法召唤，潜行右键遣散所有仆从与哨兵
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Hornet>.MinionWeaponItem> SummonContract =
            LyraItemRegisterBuilder.build(ITEMS, "summon_contract", location -> new MinionWeaponItemBuilder<Hornet>(() -> null)
                            .summon((weapon, player, itemStack) -> {
                                // 无法召唤召唤物
                            })
                            .onRemove(player -> {
                                AttachmentEntityData entityData = LyraHelper.get(player).getEntityData();
                                entityData.getGroups().forEach((type, map) -> {
                                    if (type == AttachmentEntityData.Type.Minion || type == AttachmentEntityData.Type.Sentry) {
                                        map.keySet().forEach(entityType -> entityData.remove(type, entityType));
                                    }
                                });
                            })
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Summon Contract", "召唤契约")
                    .itemLanguageTooltip(1, "Sneak right-click to dismiss all minions and sentries", "潜行右键遣散所有仆从与哨兵")
                    // TODO: 恢复 itemModel 前需添加 textures/item/summon_contract.png（datagen 校验纹理必须存在）
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 黄蜂法杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Hornet>.MinionWeaponItem> HornetStaff =
            LyraItemRegisterBuilder.build(ITEMS, "hornet_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.HORNET)
                            .damage(StartupConfig.HornetStaffDamage.get().floatValue())
                            .knockback(StartupConfig.HornetStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.HornetStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Hornet Staff", "黄蜂法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.HORNET, "Hornet", "黄蜂")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 小鬼法杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Imp>.MinionWeaponItem> ImpStaff =
            LyraItemRegisterBuilder.build(ITEMS, "imp_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.IMP)
                            .damage(StartupConfig.ImpStaffDamage.get().floatValue())
                            .knockback(StartupConfig.ImpStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.ImpStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseImpStaff)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Imp Staff", "小鬼法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.IMP, "Imp", "小鬼")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 无人机
     */
    public static final DeferredItem<MinionWeaponItemBuilder<OreScout>.MinionWeaponItem> SurveyDroneRemote =
            LyraItemRegisterBuilder.build(ITEMS, "survey_drone_remote", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.ORE_SCOUT)
                            .damage(StartupConfig.SurveyDroneRemoteDamage.get().floatValue())
                            .knockback(StartupConfig.SurveyDroneRemoteKnockback.get().floatValue())
                            .armorPierce(StartupConfig.SurveyDroneRemoteArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                OreScout minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<OreScout> oreScouts = entityData.get(AttachmentEntityData.Type.Minion, weapon.getType());
                                    if (oreScouts.isEmpty()) {
                                        summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                        minion.init(minion.getRenderNode(1.0f));
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Survey Drone Remote", "矿勘无人机遥控器")
                    .summonLanguage(SummonerAttachmentEntityRegister.ORE_SCOUT, "Survey Drone", "矿勘无人机")
                    .itemLanguageTooltip(1, "Summons up to 1 Survey Drone", "最多召唤1架矿勘无人机")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 妖精铃铛
     */
    public static final DeferredItem<MinionWeaponItemBuilder<ScavengerFairy>.MinionWeaponItem> FairyBell =
            LyraItemRegisterBuilder.build(ITEMS, "fairy_bell", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.SCAVENGER_FAIRY)
                            .damage(StartupConfig.FairyBellDamage.get().floatValue())
                            .knockback(StartupConfig.FairyBellKnockback.get().floatValue())
                            .armorPierce(StartupConfig.FairyBellArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                ScavengerFairy minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<ScavengerFairy> fairies = entityData.get(AttachmentEntityData.Type.Minion, weapon.getType());
                                    if (fairies.isEmpty()) {
                                        minion.init(minion.getInterpolatedIdleState(1.0f));
                                        summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Fairy Bell", "妖精铃铛")
                    .summonLanguage(SummonerAttachmentEntityRegister.SCAVENGER_FAIRY, "Scavenger Fairy", "拾荒妖精")
                    .itemLanguageTooltip(1, "Summons up to 1 Scavenger Fairy", "最多召唤1只拾荒妖精")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 刃杖 - 召唤附魔飞刀群
     */
    public static final DeferredItem<MinionWeaponItemBuilder<EnchantedThrowingKnives>.MinionWeaponItem> BladeStaff =
            LyraItemRegisterBuilder.build(ITEMS, "blade_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES)
                            .damage(StartupConfig.BladeStaffDamage.get().floatValue())
                            .knockback(StartupConfig.BladeStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.BladeStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EnchantedThrowingKnives minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    PathNode idle = minion.getInterpolatedIdleState(1.0f);
                                    Vec3 center = player.getBoundingBox()
                                            .getCenter();
                                    minion.init(new PathNode(new Vec3(center.x(), idle.pos()
                                            .y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                                    summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Blade Staff", "刃杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES, "Enchanted Throwing Knives", "附魔飞刀")
                    .itemLanguageTooltip(1, "Don't let their small size fool you", "别被它们小小的个头给骗了")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 雨云法杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Cloud>.MinionWeaponItem> RainCloudStaff =
            LyraItemRegisterBuilder.build(ITEMS, "rain_cloud_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.CLOUD)
                            .damage(StartupConfig.RainCloudStaffDamage.get().floatValue())
                            .knockback(StartupConfig.RainCloudStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.RainCloudStaffArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseCloudStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cloud cloud = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    cloud.init(new PathNode(player.getBoundingBox()
                                                                    .getCenter(), 0, 0, 0));
                                    cloud.setVelocity(player.getLookAngle());
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, cloud);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Rain Cloud Staff", "雨云法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.CLOUD, "Rain Cloud", "雨云")
                    .itemLanguageTooltip(1, "Summons a cloud to rain down on your foes", "召唤云朵来向敌人降下大雨")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 魔眼法杖 - 召唤双子魔眼
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Twins>.MinionWeaponItem> OpticStaff =
            LyraItemRegisterBuilder.build(ITEMS, "optic_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.TWINS)
                            .damage(StartupConfig.OpticStaffDamage.get().floatValue())
                            .knockback(StartupConfig.OpticStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.OpticStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    RandomSource random = player.getRandom();
                                    PathNode pathNode = new PathNode(player.getBoundingBox()
                                                                             .getCenter()
                                                                             .offsetRandom(random, 2), 0, 0, 0);
                                    Twins laserEye = null;
                                    for (int i = 0; i < 2; i++) {
                                        Twins twins = weapon.createMinion(player, itemStack);
                                        if (i == 0) {
                                            twins.setLaserEye(true);
                                            laserEye = twins;
                                        } else {
                                            twins.setFlameEye(true);
                                            twins.setOther(laserEye);
                                        }
                                        twins.setSlotCost(i);
                                        twins.init(pathNode);
                                        summonerHelper.add(AttachmentEntityData.Type.Minion, twins);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Optic Staff", "魔眼法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.TWINS, "Twins", "双子魔眼")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 致命球法杖 - 召唤致命球仆从
     */
    public static final DeferredItem<MinionWeaponItemBuilder<DeadlySphere>.MinionWeaponItem> DeadlySphereStaff =
            LyraItemRegisterBuilder.build(ITEMS, "deadly_sphere_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.DEADLY_SPHERE)
                            .damage(StartupConfig.DeadlySphereStaffDamage.get().floatValue())
                            .knockback(StartupConfig.DeadlySphereStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.DeadlySphereStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Deadly Sphere Staff", "致命球法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.DEADLY_SPHERE, "Deadly Sphere", "致命球")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 外星法杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<UFO>.MinionWeaponItem> XenoStaff =
            LyraItemRegisterBuilder.build(ITEMS, "xeno_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.UFO)
                            .damage(StartupConfig.XenoStaffDamage.get().floatValue())
                            .knockback(StartupConfig.XenoStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.XenoStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Xeno Staff", "外星法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.UFO, "UFO", "UFO")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 脉冲炮塔遥控装置
     */
    public static final DeferredItem<MinionWeaponItemBuilder<PulseTurret>.MinionWeaponItem> PulseTurretRemote =
            LyraItemRegisterBuilder.build(ITEMS, "pulse_turret_remote", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.PULSE_TURRET)
                            .damage(StartupConfig.PulseTurretRemoteDamage.get().floatValue())
                            .knockback(StartupConfig.PulseTurretRemoteKnockback.get().floatValue())
                            .armorPierce(StartupConfig.PulseTurretRemoteArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                PulseTurret minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<PulseTurret> pulseTurrets = entityData.get(AttachmentEntityData.Type.Sentry, SummonerAttachmentEntityRegister.PULSE_TURRET.get());
                                    if (pulseTurrets.isEmpty()) {
                                        minion.init(new PathNode(player.position()
                                                                          .add(0, 1, 0), 0, 0, 0));
                                        summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Pulse Turret Remote", "脉冲炮塔遥控装置")
                    .summonLanguage(SummonerAttachmentEntityRegister.PULSE_TURRET, "Pulse Turret", "脉冲炮塔")
                    .itemLanguageTooltip(1, "At most one pulse turret can exist at a time", "最多召唤一个脉冲炮塔")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 暴风雨法杖 - 召唤鲨鱼龙卷
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Sharknado>.MinionWeaponItem> TempestStaff =
            LyraItemRegisterBuilder.build(ITEMS, "tempest_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.SHARKNADO)
                            .damage(StartupConfig.TempestStaffDamage.get().floatValue())
                            .knockback(StartupConfig.TempestStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.TempestStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Tempest Staff", "暴风雨法杖")
                    .jeiInfo(1, "Occasionally obtained from fishing in the ocean during rain", "在海洋中雨天钓鱼时偶尔获得")
                    .summonLanguage(SummonerAttachmentEntityRegister.SHARKNADO, "Sharknado", "鲨鱼龙卷")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 泰拉棱镜 - 召唤泰拉棱镜仆从
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Terraprism>.MinionWeaponItem> TerraPrism =
            LyraItemRegisterBuilder.build(ITEMS, "terraprism", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.TERRA_PRISM)
                            .damage(StartupConfig.TerraPrismDamage.get().floatValue())
                            .knockback(StartupConfig.TerraPrismKnockback.get().floatValue())
                            .armorPierce(StartupConfig.TerraPrismArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                Terraprism minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    minion.init(minion.getInterpolatedIdleState(1));
                                    summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Terraprism", "泰拉棱镜")
                    .itemLanguageTooltip(1, "A flawless blade once hailed as the Prism of the Earth", "曾被冠以大地棱彩美名的无暇之剑")
                    .jeiInfo(1, "Occasionally drops from Allays", "悦灵偶尔掉落")
                    .summonLanguage(SummonerAttachmentEntityRegister.TERRA_PRISM, "Terraprism", "泰拉棱镜")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 弩车魔杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Ballista>.MinionWeaponItem> BallistaRod =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_rod", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(StartupConfig.BallistaRodDamage.get().floatValue())
                            .knockback(StartupConfig.BallistaRodKnockback.get().floatValue())
                            .armorPierce(StartupConfig.BallistaRodArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    minion.setLevel(1);
                                    minion.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ballista Rod", "弩车魔杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 弩车手杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Ballista>.MinionWeaponItem> BallistaCane =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_cane", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(StartupConfig.BallistaCaneDamage.get().floatValue())
                            .knockback(StartupConfig.BallistaCaneKnockback.get().floatValue())
                            .armorPierce(StartupConfig.BallistaCaneArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    minion.setLevel(2);
                                    minion.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ballista Cane", "弩车手杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 弩车法杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Ballista>.MinionWeaponItem> BallistaStaff =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(StartupConfig.BallistaStaffDamage.get().floatValue())
                            .knockback(StartupConfig.BallistaStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.BallistaStaffArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    minion.setLevel(3);
                                    minion.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ballista Staff", "弩车法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.BALLISTA, "Ballista", "弩车")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 缥缈星核法杖 - 召唤缥缈星核仆从
     */
    public static final DeferredItem<MinionWeaponItemBuilder<EtherealStellarCore>.MinionWeaponItem> EtherealStellarCoreStaff =
            LyraItemRegisterBuilder.build(ITEMS, "ethereal_stellar_core_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE)
                            .damage(StartupConfig.EtherealStellarCoreStaffDamage.get().floatValue())
                            .knockback(StartupConfig.EtherealStellarCoreStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.EtherealStellarCoreStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EtherealStellarCore minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<EtherealStellarCore> etherealStellarCores = entityData.get(AttachmentEntityData.Type.Minion, weapon.getType());
                                    if (etherealStellarCores.size() < 9) {
                                        minion.init(minion.getInterpolatedIdleState(1.0f));
                                        summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ethereal Stellar Core Staff", "缥缈星核法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE, "Ethereal Stellar Core", "缥缈星核")
                    .itemLanguageTooltip(1, "Summons up to 9 Ethereal Stellar Cores", "最多召唤9个缥缈星核")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 星尘细胞杖 - 召唤星尘细胞仆从
     */
    public static final DeferredItem<MinionWeaponItemBuilder<StardustCell>.MinionWeaponItem> StardustCellStaff =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_cell_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.STARDUST_CELL)
                            .damage(StartupConfig.StardustCellStaffDamage.get().floatValue())
                            .knockback(StartupConfig.StardustCellStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.StardustCellStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Stardust Cell Staff", "星尘细胞法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.STARDUST_CELL, "Stardust Cell", "星尘细胞")
                    .itemLanguageTooltip(1, "Cultivate the most beautiful cellular infection", "培养最美丽的细胞感染")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 星尘龙杖 - 召唤星尘龙（多体节仆从）
     */
    public static final DeferredItem<MinionWeaponItemBuilder<StardustDragon>.MinionWeaponItem> StardustDragonStaff =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_dragon_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.STARDUST_DRAGON)
                            .damage(StartupConfig.StardustDragonStaffDamage.get().floatValue())
                            .knockback(StartupConfig.StardustDragonStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.StardustDragonStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                if (helper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    List<StardustDragon> existing = helper
                                            .getEntityData()
                                            .get(AttachmentEntityData.Type.Minion, SummonerAttachmentEntityRegister.STARDUST_DRAGON.get());
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            StardustDragon minion = weapon.createMinion(player, itemStack);
                                            minion.setSegmentIndex(i);
                                            if (i < 2) {
                                                minion.setSlotCost(0);
                                            }
                                            minion.setTotalSegments(3);
                                            minion.init(new PathNode(player.position()
                                                                              .add(0, 3, -i * minion.getSegmentDistance()), 0, 0, 0));
                                            helper.add(AttachmentEntityData.Type.Minion, minion);
                                        }
                                    } else {
                                        StardustDragon minion = weapon.createMinion(player, itemStack);
                                        minion.setSegmentIndex(existing.size());
                                        minion.setTotalSegments(existing.size() + 1);
                                        for (StardustDragon dragon : existing) {
                                            dragon.setTotalSegments(existing.size() + 1);
                                        }
                                        StardustDragon last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos()
                                                .add(last.getLookAngle()
                                                             .scale(-minion.getSegmentDistance()));
                                        minion.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(AttachmentEntityData.Type.Minion, minion);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Stardust Dragon Staff", "星尘之龙法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.STARDUST_DRAGON, "Stardust Dragon", "星尘之龙")
                    .itemLanguageTooltip(1, "When you have a dragon, who needs a swarm?", "有了一条巨龙后，谁还需要一群仆从呢？")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 月亮传送门法杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<MoonPortal>.MinionWeaponItem> MoonPortalStaff =
            LyraItemRegisterBuilder.build(ITEMS, "moon_portal_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.MOON_PORTAL)
                            .damage(StartupConfig.MoonPortalStaffDamage.get().floatValue())
                            .knockback(StartupConfig.MoonPortalStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.MoonPortalStaffArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                MoonPortal minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    minion.init(new PathNode(player.position()
                                                                      .add(0, 2, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Moon Portal Staff", "月亮传送门法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.MOON_PORTAL, "Moon Portal", "月亮传送门")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 七彩水晶法杖
     */
    public static final DeferredItem<MinionWeaponItemBuilder<RainbowCrystal>.MinionWeaponItem> RainbowCrystalStaff =
            LyraItemRegisterBuilder.build(ITEMS, "rainbow_crystal_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL)
                            .damage(StartupConfig.RainbowCrystalStaffDamage.get().floatValue())
                            .knockback(StartupConfig.RainbowCrystalStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.RainbowCrystalStaffArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                RainbowCrystal minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    minion.init(new PathNode(player.position()
                                                                      .add(0, 2, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Rainbow Crystal Staff", "七彩水晶法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL, "Rainbow Crystal", "七彩水晶")
                    .itemLanguageTooltip(1, "'The colors, Duke, the colors!'", "“公爵，多么缤纷的颜色！”")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 无限剑鞘
     */
    public static final DeferredItem<MinionWeaponItemBuilder<InfiniteShadow>.MinionWeaponItem> InfiniteScabbard =
            LyraItemRegisterBuilder.build(ITEMS, "infinite_scabbard", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.INFINITE_SHADOW)
                            .damage(StartupConfig.InfiniteScabbardDamage.get().floatValue())
                            .knockback(StartupConfig.InfiniteScabbardKnockback.get().floatValue())
                            .armorPierce(StartupConfig.InfiniteScabbardArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                InfiniteShadow minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                ScabbardContainer container = player.getMainHandItem()
                                        .getComponents()
                                        .getOrDefault(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1) && !container.isEmpty()) {
                                    minion.setItemStack(container.itemStack());
                                    minion.init(minion.getInterpolatedIdleState(1));
                                    summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                }
                            })
                            .summonTooltip((weapon, itemStack, type, loc, player) -> {
                                ScabbardContainer container = itemStack.getComponents()
                                        .getOrDefault(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                                if (!container.isEmpty()) {
                                    return container.itemStack().getDisplayName().copy().withStyle(ChatFormatting.GRAY);
                                }
                                return Component.translatable("summon." + loc.getNamespace() + "." + loc.getPath()).withStyle(ChatFormatting.BLUE);
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC)
                                    .component(SummonerDataComponentRegister.SCABBARD, ScabbardContainer.EMPTY))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Infinite Scabbard", "无限剑鞘")
                    .summonLanguage(SummonerAttachmentEntityRegister.INFINITE_SHADOW, "Infinite Shadow", "无限之影")
                    .itemLanguageTooltip(1, "A scabbard that stores a blade of infinite potential", "蕴含无限可能之'剑'的剑鞘")
                    .itemLanguageTooltip(2, "Right-click an item to store, right-click an empty slot to retrieve", "右键物品存入，右键空格子取出")
                    .jeiInfo(1, "Occasionally found in Ancient City chests", "偶尔在远古城市宝箱中发现")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 虚空吞噬者傀具
     */
    public static final DeferredItem<MinionWeaponItemBuilder<VoidEater>.MinionWeaponItem> VoidEaterMarionette =
            LyraItemRegisterBuilder.build(ITEMS, "void_eater_marionette", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.VOID_EATER)
                            .damage(StartupConfig.VoidEaterMarionetteDamage.get().floatValue())
                            .knockback(StartupConfig.VoidEaterMarionetteKnockback.get().floatValue())
                            .armorPierce(StartupConfig.VoidEaterMarionetteArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                if (helper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    List<VoidEater> existing = helper.getEntityData()
                                            .get(AttachmentEntityData.Type.Minion, SummonerAttachmentEntityRegister.VOID_EATER.get());
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            VoidEater minion = weapon.createMinion(player, itemStack);
                                            minion.setSegmentIndex(i);
                                            if (i < 2) {
                                                minion.setSlotCost(0);
                                            }
                                            minion.setTotalSegments(3);
                                            minion.init(new PathNode(player.position()
                                                                              .add(0, 3, -i * minion.getSegmentDistance()), 0, 0, 0));
                                            helper.add(AttachmentEntityData.Type.Minion, minion);
                                        }
                                    } else {
                                        VoidEater minion = weapon.createMinion(player, itemStack);
                                        minion.setSegmentIndex(existing.size());
                                        minion.setTotalSegments(existing.size() + 1);
                                        for (VoidEater dragon : existing) {
                                            dragon.setTotalSegments(existing.size() + 1);
                                        }
                                        VoidEater last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos()
                                                .add(last.getLookAngle()
                                                             .scale(-minion.getSegmentDistance()));
                                        minion.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(AttachmentEntityData.Type.Minion, minion);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Void Eater Marionette", "虚空吞噬者傀具")
                    .summonLanguage(SummonerAttachmentEntityRegister.VOID_EATER, "Void Eater", "虚空吞噬者")
                    .itemLanguageTooltip(1, "Contains the power to command miniature devourers", "蕴含掌控小型吞噬者的力量")
                    .itemLanguageTooltip(2, "The devourer uses the God Eater, Holy Incineration and Cosmic Maelstrom to attack", "吞噬者会使用噬神者，焚灭虔信之火和超宇宙狂涡攻击")
                    .itemLanguageTooltip(3, "While the devourer is present, all your attacks unleash God-Slaying Fury", "吞噬者在场时，你的所有攻击都会释放弑神怒焰")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 雷云盆栽
     */
    public static final DeferredItem<MinionWeaponItemBuilder<SuperPeashooter>.MinionWeaponItem> ThundercloudBonsai =
            LyraItemRegisterBuilder.build(ITEMS, "thundercloud_bonsai", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.SUPER_PEASHOOTER)
                            .damage(StartupConfig.ThundercloudBonsaiDamage.get().floatValue())
                            .knockback(StartupConfig.ThundercloudBonsaiKnockback.get().floatValue())
                            .armorPierce(StartupConfig.ThundercloudBonsaiArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                SuperPeashooter minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 1)) {
                                    minion.setLevel(3);
                                    minion.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Thundercloud Bonsai", "雷云盆栽")
                    .summonLanguage(SummonerAttachmentEntityRegister.SUPER_PEASHOOTER, "Super Peashooter", "超级电能豌豆射手")
                    .itemLanguageTooltip(1, "Fires seven lightning balls at once, with a chance to fire a large number of scattered lightning balls at once", "一次发射七颗雷电球，有概率一次发射大量散射雷电球")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 玉米加农炮
     */
    public static final DeferredItem<MinionWeaponItemBuilder<Cannon>.MinionWeaponItem> CornCannon =
            LyraItemRegisterBuilder.build(ITEMS, "corn_cannon", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.CANNON)
                            .damage(StartupConfig.CornCannonDamage.get().floatValue())
                            .knockback(StartupConfig.CornCannonKnockback.get().floatValue())
                            .armorPierce(StartupConfig.CornCannonArmorPierce.get().floatValue())
                            .sentry()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cannon minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Sentry, 2)) {
                                    minion.setSlotCost(2);
                                    minion.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.Sentry, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Corn Cannon", "玉米加农炮")
                    .summonLanguage(SummonerAttachmentEntityRegister.CANNON, "Cannon", "玉米加农炮")
                    .itemLanguageTooltip(1, "Uses 2 Sentry slots", "占用 2 哨兵栏")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 死魂灵巫术单元
     */
    public static final DeferredItem<MinionWeaponItemBuilder<NecroSpirit>.MinionWeaponItem> NecroSpiritStaff =
            LyraItemRegisterBuilder.build(ITEMS, "necro_spirit_staff", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.NECRO_SPIRIT)
                            .damage(StartupConfig.NecroSpiritStaffDamage.get().floatValue())
                            .knockback(StartupConfig.NecroSpiritStaffKnockback.get().floatValue())
                            .armorPierce(StartupConfig.NecroSpiritStaffArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                NecroSpirit minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (minion.getSameSize() < 1 && summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    minion.init(minion.getInterpolatedIdleState(1.0f));
                                    summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Necro Spirit Staff", "死魂灵巫术单元")
                    .summonLanguage(SummonerAttachmentEntityRegister.NECRO_SPIRIT, "Necro Spirit", "死魂灵")
                    .itemLanguageTooltip(1, "Summons up to 1 Necro Spirit", "最多召唤1个死魂灵")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 激光机枪 - 召唤激光机枪仆从（最多1个）
     */
    public static final DeferredItem<MinionWeaponItemBuilder<LaserMinigun>.MinionWeaponItem> LaserMinigunStaff =
            LyraItemRegisterBuilder.build(ITEMS, "laser_minigun", location -> new MinionWeaponItemBuilder<>(SummonerAttachmentEntityRegister.LASER_MINIGUN)
                            .damage(StartupConfig.LaserMinigunDamage.get().floatValue())
                            .knockback(StartupConfig.LaserMinigunKnockback.get().floatValue())
                            .armorPierce(StartupConfig.LaserMinigunArmorPierce.get().floatValue())
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LaserMinigun minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (minion.getSameSize() < 1 && summonerHelper.canSummon(AttachmentEntityData.Type.Minion, 1)) {
                                    minion.init(minion.getInterpolatedIdleState(1.0f));
                                    summonerHelper.add(AttachmentEntityData.Type.Minion, minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Laser Minigun", "激光机枪")
                    .summonLanguage(SummonerAttachmentEntityRegister.LASER_MINIGUN, "Laser Minigun", "激光机枪")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();

    public static void register() {
    }
}
