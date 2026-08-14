package first.summoner.register;

import first.lyra.api.LyraHelper;
import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.common.builder.ServantWeaponItemBuilder;
import first.lyra.common.creativeTab.Section;
import first.lyra.common.entity.PathNode;
import first.lyra.register.LyraItemRegisterBuilder;
import first.summoner.Summoner;
import first.summoner.common.dataComponent.ScabbardContainer;
import first.summoner.common.recipe.MithrilAnvilRecipe;
import first.summoner.common.sentryServant.*;
import first.summoner.common.servant.*;
import first.summoner.config.StartupConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

import static first.summoner.register.SummonerItemRegister.ITEMS;

public class SummonerServantWeaponRegister {

    public static final Section SERVANT_WEAPON = new Section(0, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionServantWeapon);

    /**
     * 黄蜂法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Hornet>.ServantWeaponItem> HornetStaff =
            LyraItemRegisterBuilder.build(ITEMS, "hornet_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.HORNET)
                            .damage(StartupConfig.HornetStaffDamage.get())
                            .knockback(StartupConfig.HornetStaffKnockback.get())
                            .armorPierce(StartupConfig.HornetStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Hornet Staff", "黄蜂法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.HORNET, "Hornet", "黄蜂")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 小鬼法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Imp>.ServantWeaponItem> ImpStaff =
            LyraItemRegisterBuilder.build(ITEMS, "imp_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.IMP)
                            .damage(StartupConfig.ImpStaffDamage.get())
                            .knockback(StartupConfig.ImpStaffKnockback.get())
                            .armorPierce(StartupConfig.ImpStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseImpStaff)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Imp Staff", "小鬼法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.IMP, "Imp", "小鬼")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 无人机
     */
    public static final DeferredItem<ServantWeaponItemBuilder<OreScout>.ServantWeaponItem> SurveyDroneRemote =
            LyraItemRegisterBuilder.build(ITEMS, "survey_drone_remote", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.ORE_SCOUT)
                            .damage(StartupConfig.SurveyDroneRemoteDamage.get())
                            .knockback(StartupConfig.SurveyDroneRemoteKnockback.get())
                            .armorPierce(StartupConfig.SurveyDroneRemoteArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                OreScout servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<OreScout> oreScouts = entityData.get(AttachmentEntityData.Type.Servant, weapon.getType());
                                    if (oreScouts.isEmpty()) {
                                        summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                        servant.init(servant.getInterpolatedIdleState(1.0f));
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(SummonerItemRegister.HallowedIngot, 8)
                            .ingredient(Items.DIAMOND, 8)
                            .ingredient(Items.NETHERITE_INGOT, 1)
                            .result(SummonerServantWeaponRegister.SurveyDroneRemote)
                            .save(output))
                    .itemLanguage("Survey Drone Remote", "矿勘无人机遥控器")
                    .servantLanguage(SummonerAttachmentEntityRegister.ORE_SCOUT, "Survey Drone", "矿勘无人机")
                    .itemLanguageTooltip(1, "Summons up to 1 Survey Drone", "最多召唤1架矿勘无人机")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 妖精铃铛
     */
    public static final DeferredItem<ServantWeaponItemBuilder<ScavengerFairy>.ServantWeaponItem> FairyBell =
            LyraItemRegisterBuilder.build(ITEMS, "fairy_bell", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.SCAVENGER_FAIRY)
                            .damage(StartupConfig.FairyBellDamage.get())
                            .knockback(StartupConfig.FairyBellKnockback.get())
                            .armorPierce(StartupConfig.FairyBellArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                ScavengerFairy servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<ScavengerFairy> fairies = entityData.get(AttachmentEntityData.Type.Servant, weapon.getType());
                                    if (fairies.isEmpty()) {
                                        servant.init(servant.getInterpolatedIdleState(1.0f));
                                        summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(SummonerItemRegister.HallowedIngot, 8)
                            .ingredient(Items.AMETHYST_SHARD, 8)
                            .result(SummonerServantWeaponRegister.FairyBell)
                            .save(output))
                    .itemLanguage("Fairy Bell", "妖精铃铛")
                    .servantLanguage(SummonerAttachmentEntityRegister.SCAVENGER_FAIRY, "Scavenger Fairy", "拾荒妖精")
                    .itemLanguageTooltip(1, "Summons up to 1 Scavenger Fairy", "最多召唤1只拾荒妖精")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 刃杖 - 召唤附魔飞刀群
     */
    public static final DeferredItem<ServantWeaponItemBuilder<EnchantedThrowingKnives>.ServantWeaponItem> BladeStaff =
            LyraItemRegisterBuilder.build(ITEMS, "blade_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES)
                            .damage(StartupConfig.BladeStaffDamage.get())
                            .knockback(StartupConfig.BladeStaffKnockback.get())
                            .armorPierce(StartupConfig.BladeStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EnchantedThrowingKnives servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    PathNode idle = servant.getInterpolatedIdleState(1.0f);
                                    Vec3 center = player.getBoundingBox()
                                            .getCenter();
                                    servant.init(new PathNode(new Vec3(center.x(), idle.pos()
                                            .y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                                    summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.AMETHYST_SHARD, 12)
                            .ingredient(Items.IRON_INGOT, 12)
                            .result(SummonerServantWeaponRegister.BladeStaff)
                            .save(output))
                    .itemLanguage("Blade Staff", "刃杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES, "Enchanted Throwing Knives", "附魔飞刀")
                    .itemLanguageTooltip(1, "Don't let their small size fool you", "别被它们小小的个头给骗了")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 雨云法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Cloud>.ServantWeaponItem> RainCloudStaff =
            LyraItemRegisterBuilder.build(ITEMS, "rain_cloud_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.CLOUD)
                            .damage(StartupConfig.RainCloudStaffDamage.get())
                            .knockback(StartupConfig.RainCloudStaffKnockback.get())
                            .armorPierce(StartupConfig.RainCloudStaffArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseCloudStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cloud cloud = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    cloud.init(new PathNode(player.getBoundingBox()
                                                                    .getCenter(), 0, 0, 0));
                                    cloud.setVelocity(player.getLookAngle());
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, cloud);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Rain Cloud Staff", "雨云法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.CLOUD, "Rain Cloud", "雨云")
                    .itemLanguageTooltip(1, "Summons a cloud to rain down on your foes", "召唤云朵来向敌人降下大雨")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 魔眼法杖 - 召唤双子魔眼
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Twins>.ServantWeaponItem> OpticStaff =
            LyraItemRegisterBuilder.build(ITEMS, "optic_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.TWINS)
                            .damage(StartupConfig.OpticStaffDamage.get())
                            .knockback(StartupConfig.OpticStaffKnockback.get())
                            .armorPierce(StartupConfig.OpticStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    RandomSource random = player.getRandom();
                                    PathNode pathNode = new PathNode(player.getBoundingBox()
                                                                             .getCenter()
                                                                             .offsetRandom(random, 2), 0, 0, 0);
                                    Twins laserEye = null;
                                    for (int i = 0; i < 2; i++) {
                                        Twins twins = weapon.createServant(player, itemStack);
                                        if (i == 0) {
                                            twins.setLaserEye(true);
                                            laserEye = twins;
                                        } else {
                                            twins.setFlameEye(true);
                                            twins.setOther(laserEye);
                                        }
                                        twins.setSlotCost(i);
                                        twins.init(pathNode);
                                        summonerHelper.add(AttachmentEntityData.Type.Servant, twins);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(SummonerItemRegister.BlackLens)
                            .ingredient(SummonerItemRegister.HallowedIngot, 12)
                            .result(SummonerServantWeaponRegister.OpticStaff)
                            .save(output))
                    .itemLanguage("Optic Staff", "魔眼法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.TWINS, "Twins", "双子魔眼")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 致命球法杖 - 召唤致命球仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<DeadlySphere>.ServantWeaponItem> DeadlySphereStaff =
            LyraItemRegisterBuilder.build(ITEMS, "deadly_sphere_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.DEADLY_SPHERE)
                            .damage(StartupConfig.DeadlySphereStaffDamage.get())
                            .knockback(StartupConfig.DeadlySphereStaffKnockback.get())
                            .armorPierce(StartupConfig.DeadlySphereStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Deadly Sphere Staff", "致命球法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.DEADLY_SPHERE, "Deadly Sphere", "致命球")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 外星法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<UFO>.ServantWeaponItem> XenoStaff =
            LyraItemRegisterBuilder.build(ITEMS, "xeno_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.UFO)
                            .damage(StartupConfig.XenoStaffDamage.get())
                            .knockback(StartupConfig.XenoStaffKnockback.get())
                            .armorPierce(StartupConfig.XenoStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Xeno Staff", "外星法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.UFO, "UFO", "UFO")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 脉冲炮塔遥控装置
     */
    public static final DeferredItem<ServantWeaponItemBuilder<PulseTurret>.ServantWeaponItem> PulseTurretRemote =
            LyraItemRegisterBuilder.build(ITEMS, "pulse_turret_remote", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.PULSE_TURRET)
                            .damage(StartupConfig.PulseTurretRemoteDamage.get())
                            .knockback(StartupConfig.PulseTurretRemoteKnockback.get())
                            .armorPierce(StartupConfig.PulseTurretRemoteArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                PulseTurret servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<PulseTurret> pulseTurrets = entityData.get(AttachmentEntityData.Type.SentryServant, SummonerAttachmentEntityRegister.PULSE_TURRET.get());
                                    if (pulseTurrets.isEmpty()) {
                                        servant.init(new PathNode(player.position()
                                                                          .add(0, 1, 0), 0, 0, 0));
                                        summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Pulse Turret Remote", "脉冲炮塔遥控装置")
                    .servantLanguage(SummonerAttachmentEntityRegister.PULSE_TURRET, "Pulse Turret", "脉冲炮塔")
                    .itemLanguageTooltip(1, "At most one pulse turret can exist at a time", "最多召唤一个脉冲炮塔")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 暴风雨法杖 - 召唤鲨鱼龙卷
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Sharknado>.ServantWeaponItem> TempestStaff =
            LyraItemRegisterBuilder.build(ITEMS, "tempest_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.SHARKNADO)
                            .damage(StartupConfig.TempestStaffDamage.get())
                            .knockback(StartupConfig.TempestStaffKnockback.get())
                            .armorPierce(StartupConfig.TempestStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Tempest Staff", "暴风雨法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.SHARKNADO, "Sharknado", "鲨鱼龙卷")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 泰拉棱镜 - 召唤泰拉棱镜仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Terraprism>.ServantWeaponItem> TerraPrism =
            LyraItemRegisterBuilder.build(ITEMS, "terraprism", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.TERRA_PRISM)
                            .damage(StartupConfig.TerraPrismDamage.get())
                            .knockback(StartupConfig.TerraPrismKnockback.get())
                            .armorPierce(StartupConfig.TerraPrismArmorPierce.get())
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                Terraprism servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1));
                                    summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Terraprism", "泰拉棱镜")
                    .servantLanguage(SummonerAttachmentEntityRegister.TERRA_PRISM, "Terraprism", "泰拉棱镜")
                    .itemLanguageTooltip(1, "A flawless blade once hailed as the Prism of the Earth", "曾被冠以大地棱彩美名的无暇之剑")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 弩车魔杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Ballista>.ServantWeaponItem> BallistaRod =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_rod", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(StartupConfig.BallistaRodDamage.get())
                            .knockback(StartupConfig.BallistaRodKnockback.get())
                            .armorPierce(StartupConfig.BallistaRodArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(1);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Ballista Rod", "弩车魔杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 弩车手杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Ballista>.ServantWeaponItem> BallistaCane =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_cane", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(StartupConfig.BallistaCaneDamage.get())
                            .knockback(StartupConfig.BallistaCaneKnockback.get())
                            .armorPierce(StartupConfig.BallistaCaneArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(2);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Ballista Cane", "弩车手杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 弩车法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Ballista>.ServantWeaponItem> BallistaStaff =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(StartupConfig.BallistaStaffDamage.get())
                            .knockback(StartupConfig.BallistaStaffKnockback.get())
                            .armorPierce(StartupConfig.BallistaStaffArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(3);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Ballista Staff", "弩车法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.BALLISTA, "Ballista", "弩车")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 缥缈星核法杖 - 召唤缥缈星核仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<EtherealStellarCore>.ServantWeaponItem> EtherealStellarCoreStaff =
            LyraItemRegisterBuilder.build(ITEMS, "ethereal_stellar_core_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE)
                            .damage(StartupConfig.EtherealStellarCoreStaffDamage.get())
                            .knockback(StartupConfig.EtherealStellarCoreStaffKnockback.get())
                            .armorPierce(StartupConfig.EtherealStellarCoreStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EtherealStellarCore servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    AttachmentEntityData entityData = summonerHelper.getEntityData();
                                    List<EtherealStellarCore> etherealStellarCores = entityData.get(AttachmentEntityData.Type.Servant, weapon.getType());
                                    if (etherealStellarCores.size() < 9) {
                                        servant.init(servant.getInterpolatedIdleState(1.0f));
                                        summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(SummonerItemRegister.Stardust, 12)
                            .result(SummonerServantWeaponRegister.EtherealStellarCoreStaff)
                            .save(output))
                    .itemLanguage("Ethereal Stellar Core Staff", "缥缈星核法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE, "Ethereal Stellar Core", "缥缈星核")
                    .itemLanguageTooltip(1, "Summons up to 9 Ethereal Stellar Cores", "最多召唤9个缥缈星核")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 星尘细胞杖 - 召唤星尘细胞仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<StardustCell>.ServantWeaponItem> StardustCellStaff =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_cell_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.STARDUST_CELL)
                            .damage(StartupConfig.StardustCellStaffDamage.get())
                            .knockback(StartupConfig.StardustCellStaffKnockback.get())
                            .armorPierce(StartupConfig.StardustCellStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(SummonerItemRegister.Stardust, 18)
                            .result(SummonerServantWeaponRegister.StardustCellStaff)
                            .save(output))
                    .itemLanguage("Stardust Cell Staff", "星尘细胞法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.STARDUST_CELL, "Stardust Cell", "星尘细胞")
                    .itemLanguageTooltip(1, "Cultivate the most beautiful cellular infection", "培养最美丽的细胞感染")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 星尘龙杖 - 召唤星尘龙（多体节仆从）
     */
    public static final DeferredItem<ServantWeaponItemBuilder<StardustDragon>.ServantWeaponItem> StardustDragonStaff =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_dragon_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.STARDUST_DRAGON)
                            .damage(StartupConfig.StardustDragonStaffDamage.get())
                            .knockback(StartupConfig.StardustDragonStaffKnockback.get())
                            .armorPierce(StartupConfig.StardustDragonStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                if (helper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    List<StardustDragon> existing = helper
                                            .getEntityData()
                                            .get(AttachmentEntityData.Type.Servant, SummonerAttachmentEntityRegister.STARDUST_DRAGON.get());
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            StardustDragon servant = weapon.createServant(player, itemStack);
                                            servant.setSegmentIndex(i);
                                            if (i < 2) {
                                                servant.setSlotCost(0);
                                            }
                                            servant.setTotalSegments(3);
                                            servant.init(new PathNode(player.position()
                                                                              .add(0, 3, -i * servant.getSegmentDistance()), 0, 0, 0));
                                            helper.add(AttachmentEntityData.Type.Servant, servant);
                                        }
                                    } else {
                                        StardustDragon servant = weapon.createServant(player, itemStack);
                                        servant.setSegmentIndex(existing.size());
                                        servant.setTotalSegments(existing.size() + 1);
                                        for (StardustDragon dragon : existing) {
                                            dragon.setTotalSegments(existing.size() + 1);
                                        }
                                        StardustDragon last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos()
                                                .add(last.getLookAngle()
                                                             .scale(-servant.getSegmentDistance()));
                                        servant.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(AttachmentEntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(SummonerItemRegister.Stardust, 18)
                            .result(SummonerServantWeaponRegister.StardustDragonStaff)
                            .save(output))
                    .itemLanguage("Stardust Dragon Staff", "星尘之龙法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.STARDUST_DRAGON, "Stardust Dragon", "星尘之龙")
                    .itemLanguageTooltip(1, "When you have a dragon, who needs a swarm?", "有了一条巨龙后，谁还需要一群仆从呢？")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 月亮传送门法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<MoonPortal>.ServantWeaponItem> MoonPortalStaff =
            LyraItemRegisterBuilder.build(ITEMS, "moon_portal_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.MOON_PORTAL)
                            .damage(StartupConfig.MoonPortalStaffDamage.get())
                            .knockback(StartupConfig.MoonPortalStaffKnockback.get())
                            .armorPierce(StartupConfig.MoonPortalStaffArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                MoonPortal servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 2, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Moon Portal Staff", "月亮传送门法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.MOON_PORTAL, "Moon Portal", "月亮传送门")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 七彩水晶法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<RainbowCrystal>.ServantWeaponItem> RainbowCrystalStaff =
            LyraItemRegisterBuilder.build(ITEMS, "rainbow_crystal_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL)
                            .damage(StartupConfig.RainbowCrystalStaffDamage.get())
                            .knockback(StartupConfig.RainbowCrystalStaffKnockback.get())
                            .armorPierce(StartupConfig.RainbowCrystalStaffArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                RainbowCrystal servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 2, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Rainbow Crystal Staff", "七彩水晶法杖")
                    .servantLanguage(SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL, "Rainbow Crystal", "七彩水晶")
                    .itemLanguageTooltip(1, "'The colors, Duke, the colors!'", "“公爵，多么缤纷的颜色！”")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 无限剑鞘
     */
    public static final DeferredItem<ServantWeaponItemBuilder<InfiniteShadow>.ServantWeaponItem> InfiniteScabbard =
            LyraItemRegisterBuilder.build(ITEMS, "infinite_scabbard", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.INFINITE_SHADOW)
                            .damage(StartupConfig.InfiniteScabbardDamage.get())
                            .knockback(StartupConfig.InfiniteScabbardKnockback.get())
                            .armorPierce(StartupConfig.InfiniteScabbardArmorPierce.get())
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                InfiniteShadow servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                ScabbardContainer container = player.getMainHandItem()
                                        .getComponents()
                                        .getOrDefault(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1) && !container.isEmpty()) {
                                    servant.setItemStack(container.itemStack());
                                    servant.init(servant.getInterpolatedIdleState(1));
                                    summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .summonTooltip((weapon, itemStack, type, loc, player) -> {
                                ScabbardContainer container = itemStack.getComponents()
                                        .getOrDefault(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                                if (!container.isEmpty()) {
                                    return container.itemStack().getDisplayName().copy().withStyle(ChatFormatting.GRAY);
                                }
                                return Component.translatable("servant." + loc.getNamespace() + "." + loc.getPath()).withStyle(ChatFormatting.BLUE);
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC)
                                    .component(SummonerDataComponentRegister.SCABBARD, ScabbardContainer.EMPTY))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Infinite Scabbard", "无限剑鞘")
                    .servantLanguage(SummonerAttachmentEntityRegister.INFINITE_SHADOW, "Infinite Shadow", "无限之影")
                    .itemLanguageTooltip(1, "A scabbard that stores a blade of infinite potential", "蕴含无限可能之'剑'的剑鞘")
                    .itemLanguageTooltip(2, "Right-click an item to store, right-click an empty slot to retrieve", "右键物品存入，右键空格子取出")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 虚空吞噬者傀具
     */
    public static final DeferredItem<ServantWeaponItemBuilder<VoidEater>.ServantWeaponItem> VoidEaterMarionette =
            LyraItemRegisterBuilder.build(ITEMS, "void_eater_marionette", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.VOID_EATER)
                            .damage(StartupConfig.VoidEaterMarionetteDamage.get())
                            .knockback(StartupConfig.VoidEaterMarionetteKnockback.get())
                            .armorPierce(StartupConfig.VoidEaterMarionetteArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                if (helper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    List<VoidEater> existing = helper.getEntityData()
                                            .get(AttachmentEntityData.Type.Servant, SummonerAttachmentEntityRegister.VOID_EATER.get());
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            VoidEater servant = weapon.createServant(player, itemStack);
                                            servant.setSegmentIndex(i);
                                            if (i < 2) {
                                                servant.setSlotCost(0);
                                            }
                                            servant.setTotalSegments(3);
                                            servant.init(new PathNode(player.position()
                                                                              .add(0, 3, -i * servant.getSegmentDistance()), 0, 0, 0));
                                            helper.add(AttachmentEntityData.Type.Servant, servant);
                                        }
                                    } else {
                                        VoidEater servant = weapon.createServant(player, itemStack);
                                        servant.setSegmentIndex(existing.size());
                                        servant.setTotalSegments(existing.size() + 1);
                                        for (VoidEater dragon : existing) {
                                            dragon.setTotalSegments(existing.size() + 1);
                                        }
                                        VoidEater last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos()
                                                .add(last.getLookAngle()
                                                             .scale(-servant.getSegmentDistance()));
                                        servant.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(AttachmentEntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Void Eater Marionette", "虚空吞噬者傀具")
                    .servantLanguage(SummonerAttachmentEntityRegister.VOID_EATER, "Void Eater", "虚空吞噬者")
                    .itemLanguageTooltip(1, "Contains the power to command miniature devourers", "蕴含掌控小型吞噬者的力量")
                    .itemLanguageTooltip(2, "The devourer uses the God Eater, Holy Incineration and Cosmic Maelstrom to attack", "吞噬者会使用噬神者，焚灭虔信之火和超宇宙狂涡攻击")
                    .itemLanguageTooltip(3, "While the devourer is present, all your attacks unleash God-Slaying Fury", "吞噬者在场时，你的所有攻击都会释放弑神怒焰")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 雷云盆栽
     */
    public static final DeferredItem<ServantWeaponItemBuilder<SuperPeashooter>.ServantWeaponItem> ThundercloudBonsai =
            LyraItemRegisterBuilder.build(ITEMS, "thundercloud_bonsai", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.SUPER_PEASHOOTER)
                            .damage(StartupConfig.ThundercloudBonsaiDamage.get())
                            .knockback(StartupConfig.ThundercloudBonsaiKnockback.get())
                            .armorPierce(StartupConfig.ThundercloudBonsaiArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                SuperPeashooter servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(3);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Thundercloud Bonsai", "雷云盆栽")
                    .servantLanguage(SummonerAttachmentEntityRegister.SUPER_PEASHOOTER, "Super Peashooter", "超级电能豌豆射手")
                    .itemLanguageTooltip(1, "Fires seven lightning balls at once, with a chance to fire a large number of scattered lightning balls at once", "一次发射七颗雷电球，有概率一次发射大量散射雷电球")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 玉米加农炮
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Cannon>.ServantWeaponItem> CornCannon =
            LyraItemRegisterBuilder.build(ITEMS, "corn_cannon", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.CANNON)
                            .damage(StartupConfig.CornCannonDamage.get())
                            .knockback(StartupConfig.CornCannonKnockback.get())
                            .armorPierce(StartupConfig.CornCannonArmorPierce.get())
                            .sentryServant()
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cannon servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (summonerHelper.canSummon(AttachmentEntityData.Type.SentryServant, 2)) {
                                    servant.setSlotCost(2);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Corn Cannon", "玉米加农炮")
                    .servantLanguage(SummonerAttachmentEntityRegister.CANNON, "Cannon", "玉米加农炮")
                    .itemLanguageTooltip(1, "Uses 2 Sentry Servant slots", "占用 2 哨戒仆从栏")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 死魂灵巫术单元
     */
    public static final DeferredItem<ServantWeaponItemBuilder<NecroSpirit>.ServantWeaponItem> NecroSpiritStaff =
            LyraItemRegisterBuilder.build(ITEMS, "necro_spirit_staff", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.NECRO_SPIRIT)
                            .damage(StartupConfig.NecroSpiritStaffDamage.get())
                            .knockback(StartupConfig.NecroSpiritStaffKnockback.get())
                            .armorPierce(StartupConfig.NecroSpiritStaffArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                NecroSpirit servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (servant.getSameSize() < 1 && summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1.0f));
                                    summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Necro Spirit Staff", "死魂灵巫术单元")
                    .servantLanguage(SummonerAttachmentEntityRegister.NECRO_SPIRIT, "Necro Spirit", "死魂灵")
                    .itemLanguageTooltip(1, "Summons up to 1 Necro Spirit", "最多召唤1个死魂灵")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 激光机枪 - 召唤激光机枪仆从（最多1个）
     */
    public static final DeferredItem<ServantWeaponItemBuilder<LaserMinigun>.ServantWeaponItem> LaserMinigunStaff =
            LyraItemRegisterBuilder.build(ITEMS, "laser_minigun", location -> new ServantWeaponItemBuilder<>(SummonerAttachmentEntityRegister.LASER_MINIGUN)
                            .damage(StartupConfig.LaserMinigunDamage.get())
                            .knockback(StartupConfig.LaserMinigunKnockback.get())
                            .armorPierce(StartupConfig.LaserMinigunArmorPierce.get())
                            .sound(SummonerSoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LaserMinigun servant = weapon.createServant(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                if (servant.getSameSize() < 1 && summonerHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1.0f));
                                    summonerHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Laser Minigun", "激光机枪")
                    .servantLanguage(SummonerAttachmentEntityRegister.LASER_MINIGUN, "Laser Minigun", "激光机枪")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(SummonerItemTagsRegister.ServantWeapon)
                    .build();

    public static void register() {
    }
}
