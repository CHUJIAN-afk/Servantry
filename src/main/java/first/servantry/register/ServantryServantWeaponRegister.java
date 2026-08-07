package first.servantry.register;

import first.lyra.api.LyraHelper;
import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.common.builder.ServantWeaponItemBuilder;
import first.lyra.common.creativeTab.Section;
import first.lyra.common.entity.PathNode;
import first.lyra.register.LyraItemRegisterBuilder;
import first.servantry.Servantry;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.common.sentryServant.*;
import first.servantry.common.servant.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

import static first.servantry.register.ServantryItemRegister.ITEMS;

public class ServantryServantWeaponRegister {

    public static final Section SERVANT_WEAPON = new Section(0, Servantry.rl("textures/item/banner/banner.png"), new AnimBanner(18, 3, 8), ServantryItemTagsRegister.SectionServantWeapon);

    /**
     * 黄蜂法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Hornet>.ServantWeaponItem> HornetStaff =
            LyraItemRegisterBuilder.build(ITEMS, "hornet_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.HORNET)
                            .damage(0.7f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Hornet Staff", "黄蜂法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.HORNET, "Hornet", "黄蜂")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 小鬼法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Imp>.ServantWeaponItem> ImpStaff =
            LyraItemRegisterBuilder.build(ITEMS, "imp_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.IMP)
                            .damage(1.7f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseImpStaff)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Imp Staff", "小鬼法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.IMP, "Imp", "小鬼")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 无人机
     */
    public static final DeferredItem<ServantWeaponItemBuilder<OreScout>.ServantWeaponItem> SurveyDroneRemote =
            LyraItemRegisterBuilder.build(ITEMS, "survey_drone_remote", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.ORE_SCOUT)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                OreScout servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    AttachmentEntityData entityData = servantryHelper.getEntityData();
                                    List<OreScout> oreScouts = entityData.get(AttachmentEntityData.Type.Servant, weapon.getType());
                                    if (oreScouts.isEmpty()) {
                                        servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                        servant.init(servant.getInterpolatedIdleState(1.0f));
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 8)
                            .ingredient(Items.DIAMOND, 8)
                            .ingredient(Items.NETHERITE_INGOT, 1)
                            .result(ServantryServantWeaponRegister.SurveyDroneRemote)
                            .save(output))
                    .itemLanguage("Survey Drone Remote", "矿勘无人机遥控器")
                    .servantLanguage(ServantryAttachmentEntityRegister.ORE_SCOUT, "Survey Drone", "矿勘无人机")
                    .itemLanguageTooltip(1, "Summons up to 1 Survey Drone", "最多召唤1架矿勘无人机")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 妖精铃铛
     */
    public static final DeferredItem<ServantWeaponItemBuilder<ScavengerFairy>.ServantWeaponItem> FairyBell =
            LyraItemRegisterBuilder.build(ITEMS, "fairy_bell", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.SCAVENGER_FAIRY)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                ScavengerFairy servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    AttachmentEntityData entityData = servantryHelper.getEntityData();
                                    List<ScavengerFairy> fairies = entityData.get(AttachmentEntityData.Type.Servant, weapon.getType());
                                    if (fairies.isEmpty()) {
                                        servant.init(servant.getInterpolatedIdleState(1.0f));
                                        servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 8)
                            .ingredient(Items.AMETHYST_SHARD, 8)
                            .result(ServantryServantWeaponRegister.FairyBell)
                            .save(output))
                    .itemLanguage("Fairy Bell", "妖精铃铛")
                    .servantLanguage(ServantryAttachmentEntityRegister.SCAVENGER_FAIRY, "Scavenger Fairy", "拾荒妖精")
                    .itemLanguageTooltip(1, "Summons up to 1 Scavenger Fairy", "最多召唤1只拾荒妖精")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 刃杖 - 召唤附魔飞刀群
     */
    public static final DeferredItem<ServantWeaponItemBuilder<EnchantedThrowingKnives>.ServantWeaponItem> BladeStaff =
            LyraItemRegisterBuilder.build(ITEMS, "blade_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES)
                            .damage(0.6f)
                            .armorPierce(7.5f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EnchantedThrowingKnives servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    PathNode idle = servant.getInterpolatedIdleState(1.0f);
                                    Vec3 center = player.getBoundingBox()
                                            .getCenter();
                                    servant.init(new PathNode(new Vec3(center.x(), idle.pos()
                                            .y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                                    servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.AMETHYST_SHARD, 12)
                            .ingredient(Items.IRON_INGOT, 12)
                            .result(ServantryServantWeaponRegister.BladeStaff)
                            .save(output))
                    .itemLanguage("Blade Staff", "刃杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES, "Enchanted Throwing Knives", "附魔飞刀")
                    .itemLanguageTooltip(1, "Don't let their small size fool you", "别被它们小小的个头给骗了")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 雨云法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Cloud>.ServantWeaponItem> RainCloudStaff =
            LyraItemRegisterBuilder.build(ITEMS, "rain_cloud_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.CLOUD)
                            .damage(3f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseCloudStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cloud cloud = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    cloud.init(new PathNode(player.getBoundingBox()
                                                                    .getCenter(), 0, 0, 0));
                                    cloud.setVelocity(player.getLookAngle());
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, cloud);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Rain Cloud Staff", "雨云法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.CLOUD, "Rain Cloud", "雨云")
                    .itemLanguageTooltip(1, "Summons a cloud to rain down on your foes", "召唤云朵来向敌人降下大雨")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 魔眼法杖 - 召唤双子魔眼
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Twins>.ServantWeaponItem> OpticStaff =
            LyraItemRegisterBuilder.build(ITEMS, "optic_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.TWINS)
                            .damage(2.4f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
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
                                        servantryHelper.add(AttachmentEntityData.Type.Servant, twins);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BlackLens)
                            .ingredient(ServantryItemRegister.HallowedIngot, 12)
                            .result(ServantryServantWeaponRegister.OpticStaff)
                            .save(output))
                    .itemLanguage("Optic Staff", "魔眼法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.TWINS, "Twins", "双子魔眼")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 致命球法杖 - 召唤致命球仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<DeadlySphere>.ServantWeaponItem> DeadlySphereStaff =
            LyraItemRegisterBuilder.build(ITEMS, "deadly_sphere_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.DEADLY_SPHERE)
                            .damage(5.5f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Deadly Sphere Staff", "致命球法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.DEADLY_SPHERE, "Deadly Sphere", "致命球")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 外星法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<UFO>.ServantWeaponItem> XenoStaff =
            LyraItemRegisterBuilder.build(ITEMS, "xeno_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.UFO)
                            .damage(3.6f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Xeno Staff", "外星法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.UFO, "UFO", "UFO")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 脉冲炮塔遥控装置
     */
    public static final DeferredItem<ServantWeaponItemBuilder<PulseTurret>.ServantWeaponItem> PulseTurretRemote =
            LyraItemRegisterBuilder.build(ITEMS, "pulse_turret_remote", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.PULSE_TURRET)
                            .damage(15f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                PulseTurret servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    AttachmentEntityData entityData = servantryHelper.getEntityData();
                                    List<PulseTurret> pulseTurrets = entityData.get(AttachmentEntityData.Type.SentryServant, ServantryAttachmentEntityRegister.PULSE_TURRET.get());
                                    if (pulseTurrets.isEmpty()) {
                                        servant.init(new PathNode(player.position()
                                                                          .add(0, 1, 0), 0, 0, 0));
                                        servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Pulse Turret Remote", "脉冲炮塔遥控装置")
                    .servantLanguage(ServantryAttachmentEntityRegister.PULSE_TURRET, "Pulse Turret", "脉冲炮塔")
                    .itemLanguageTooltip(1, "At most one pulse turret can exist at a time", "最多召唤一个脉冲炮塔")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 暴风雨法杖 - 召唤鲨鱼龙卷
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Sharknado>.ServantWeaponItem> TempestStaff =
            LyraItemRegisterBuilder.build(ITEMS, "tempest_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.SHARKNADO)
                            .damage(5f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Tempest Staff", "暴风雨法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.SHARKNADO, "Sharknado", "鲨鱼龙卷")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 泰拉棱镜 - 召唤泰拉棱镜仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Terraprism>.ServantWeaponItem> TerraPrism =
            LyraItemRegisterBuilder.build(ITEMS, "terraprism", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.TERRA_PRISM)
                            .damage(9f)
                            .knockback(0.4f)
                            .sound(ServantrySoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                Terraprism servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1));
                                    servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Terraprism", "泰拉棱镜")
                    .servantLanguage(ServantryAttachmentEntityRegister.TERRA_PRISM, "Terraprism", "泰拉棱镜")
                    .itemLanguageTooltip(1, "A flawless blade once hailed as the Prism of the Earth", "曾被冠以大地棱彩美名的无暇之剑")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 弩车魔杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Ballista>.ServantWeaponItem> BallistaRod =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_rod", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.BALLISTA)
                            .damage(3f)
                            .knockback(0.47f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(1);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Ballista Rod", "弩车魔杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 弩车手杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Ballista>.ServantWeaponItem> BallistaCane =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_cane", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.BALLISTA)
                            .damage(7.4f)
                            .knockback(0.47f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(2);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 0, 0));
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Ballista Cane", "弩车手杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 弩车法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Ballista>.ServantWeaponItem> BallistaStaff =
            LyraItemRegisterBuilder.build(ITEMS, "ballista_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.BALLISTA)
                            .damage(15.6f)
                            .knockback(0.47f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(3);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Ballista Staff", "弩车法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.BALLISTA, "Ballista", "弩车")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 缥缈星核法杖 - 召唤缥缈星核仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<EtherealStellarCore>.ServantWeaponItem> EtherealStellarCoreStaff =
            LyraItemRegisterBuilder.build(ITEMS, "ethereal_stellar_core_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.ETHEREAL_STELLAR_CORE)
                            .damage(5.0f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EtherealStellarCore servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    AttachmentEntityData entityData = servantryHelper.getEntityData();
                                    List<EtherealStellarCore> etherealStellarCores = entityData.get(AttachmentEntityData.Type.Servant, weapon.getType());
                                    if (etherealStellarCores.size() < 9) {
                                        servant.init(servant.getInterpolatedIdleState(1.0f));
                                        servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Stardust, 12)
                            .result(ServantryServantWeaponRegister.EtherealStellarCoreStaff)
                            .save(output))
                    .itemLanguage("Ethereal Stellar Core Staff", "缥缈星核法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.ETHEREAL_STELLAR_CORE, "Ethereal Stellar Core", "缥缈星核")
                    .itemLanguageTooltip(1, "Summons up to 9 Ethereal Stellar Cores", "最多召唤9个缥缈星核")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 星尘细胞杖 - 召唤星尘细胞仆从
     */
    public static final DeferredItem<ServantWeaponItemBuilder<StardustCell>.ServantWeaponItem> StardustCellStaff =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_cell_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.STARDUST_CELL)
                            .damage(6f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Stardust, 18)
                            .result(ServantryServantWeaponRegister.StardustCellStaff)
                            .save(output))
                    .itemLanguage("Stardust Cell Staff", "星尘细胞法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.STARDUST_CELL, "Stardust Cell", "星尘细胞")
                    .itemLanguageTooltip(1, "Cultivate the most beautiful cellular infection", "培养最美丽的细胞感染")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 星尘龙杖 - 召唤星尘龙（多体节仆从）
     */
    public static final DeferredItem<ServantWeaponItemBuilder<StardustDragon>.ServantWeaponItem> StardustDragonStaff =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_dragon_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.STARDUST_DRAGON)
                            .damage(4f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                if (helper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    List<StardustDragon> existing = helper
                                            .getEntityData()
                                            .get(AttachmentEntityData.Type.Servant, ServantryAttachmentEntityRegister.STARDUST_DRAGON.get());
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
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Stardust, 18)
                            .result(ServantryServantWeaponRegister.StardustDragonStaff)
                            .save(output))
                    .itemLanguage("Stardust Dragon Staff", "星尘之龙法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.STARDUST_DRAGON, "Stardust Dragon", "星尘之龙")
                    .itemLanguageTooltip(1, "When you have a dragon, who needs a swarm?", "有了一条巨龙后，谁还需要一群仆从呢？")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 月亮传送门法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<MoonPortal>.ServantWeaponItem> MoonPortalStaff =
            LyraItemRegisterBuilder.build(ITEMS, "moon_portal_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.MOON_PORTAL)
                            .damage(10)
                            .knockback(0.75f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                MoonPortal servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 2, 0), 0, 0, 0));
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Moon Portal Staff", "月亮传送门法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.MOON_PORTAL, "Moon Portal", "月亮传送门")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 七彩水晶法杖
     */
    public static final DeferredItem<ServantWeaponItemBuilder<RainbowCrystal>.ServantWeaponItem> RainbowCrystalStaff =
            LyraItemRegisterBuilder.build(ITEMS, "rainbow_crystal_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.RAINBOW_CRYSTAL)
                            .damage(13)
                            .knockback(0.75f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                RainbowCrystal servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 2, 0), 0, 0, 0));
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Rainbow Crystal Staff", "七彩水晶法杖")
                    .servantLanguage(ServantryAttachmentEntityRegister.RAINBOW_CRYSTAL, "Rainbow Crystal", "七彩水晶")
                    .itemLanguageTooltip(1, "'The colors, Duke, the colors!'", "“公爵，多么缤纷的颜色！”")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 无限剑鞘
     */
    public static final DeferredItem<ServantWeaponItemBuilder<InfiniteShadow>.ServantWeaponItem> InfiniteScabbard =
            LyraItemRegisterBuilder.build(ITEMS, "infinite_scabbard", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.INFINITE_SHADOW)
                            .sound(ServantrySoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                InfiniteShadow servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                ScabbardContainer container = player.getMainHandItem()
                                        .getComponents()
                                        .getOrDefault(ServantryDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1) && !container.isEmpty()) {
                                    servant.setItemStack(container.itemStack());
                                    servant.init(servant.getInterpolatedIdleState(1));
                                    servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC)
                                    .component(ServantryDataComponentRegister.SCABBARD, ScabbardContainer.EMPTY))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Infinite Scabbard", "无限剑鞘")
                    .servantLanguage(ServantryAttachmentEntityRegister.INFINITE_SHADOW, "Infinite Shadow", "无限之影")
                    .itemLanguageTooltip(1, "A scabbard that stores a blade of infinite potential", "蕴含无限可能之'剑'的剑鞘")
                    .itemLanguageTooltip(2, "Right-click an item to store, right-click an empty slot to retrieve", "右键物品存入，右键空格子取出")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 虚空吞噬者傀具
     */
    public static final DeferredItem<ServantWeaponItemBuilder<VoidEater>.ServantWeaponItem> VoidEaterMarionette =
            LyraItemRegisterBuilder.build(ITEMS, "void_eater_marionette", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.VOID_EATER)
                            .damage(11f)
                            .knockback(0.5f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                if (helper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    List<VoidEater> existing = helper.getEntityData()
                                            .get(AttachmentEntityData.Type.Servant, ServantryAttachmentEntityRegister.VOID_EATER.get());
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
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Void Eater Marionette", "虚空吞噬者傀具")
                    .servantLanguage(ServantryAttachmentEntityRegister.VOID_EATER, "Void Eater", "虚空吞噬者")
                    .itemLanguageTooltip(1, "Contains the power to command miniature devourers", "蕴含掌控小型吞噬者的力量")
                    .itemLanguageTooltip(2, "The devourer uses the God Eater, Holy Incineration and Cosmic Maelstrom to attack", "吞噬者会使用噬神者，焚灭虔信之火和超宇宙狂涡攻击")
                    .itemLanguageTooltip(3, "While the devourer is present, all your attacks unleash God-Slaying Fury", "吞噬者在场时，你的所有攻击都会释放弑神怒焰")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 雷云盆栽
     */
    public static final DeferredItem<ServantWeaponItemBuilder<SuperPeashooter>.ServantWeaponItem> ThundercloudBonsai =
            LyraItemRegisterBuilder.build(ITEMS, "thundercloud_bonsai", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.SUPER_PEASHOOTER)
                            .damage(12f)
                            .knockback(0)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                SuperPeashooter servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(3);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 1, 0));
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Thundercloud Bonsai", "雷云盆栽")
                    .servantLanguage(ServantryAttachmentEntityRegister.SUPER_PEASHOOTER, "Super Peashooter", "超级电能豌豆射手")
                    .itemLanguageTooltip(1, "Fires seven lightning balls at once, with a chance to fire a large number of scattered lightning balls at once", "一次发射七颗雷电球，有概率一次发射大量散射雷电球")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 玉米加农炮
     */
    public static final DeferredItem<ServantWeaponItemBuilder<Cannon>.ServantWeaponItem> CornCannon =
            LyraItemRegisterBuilder.build(ITEMS, "corn_cannon", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.CANNON)
                            .damage(6400f)
                            .armorPierce(3600f)
                            .sentryServant()
                            .sound(ServantrySoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cannon servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servantryHelper.canSummon(AttachmentEntityData.Type.SentryServant, 2)) {
                                    servant.setSlotCost(2);
                                    servant.init(new PathNode(player.position()
                                                                      .add(0, 1, 0), 0, 0, 0));
                                    servantryHelper.add(AttachmentEntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Corn Cannon", "玉米加农炮")
                    .servantLanguage(ServantryAttachmentEntityRegister.CANNON, "Cannon", "玉米加农炮")
                    .itemLanguageTooltip(1, "Uses 2 Sentry Servant slots", "占用 2 哨戒仆从栏")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 死魂灵巫术单元
     */
    public static final DeferredItem<ServantWeaponItemBuilder<NecroSpirit>.ServantWeaponItem> NecroSpiritStaff =
            LyraItemRegisterBuilder.build(ITEMS, "necro_spirit_staff", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.NECRO_SPIRIT)
                            .damage(700f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                NecroSpirit servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servant.getSameSize() < 1 && servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1.0f));
                                    servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Necro Spirit Staff", "死魂灵巫术单元")
                    .servantLanguage(ServantryAttachmentEntityRegister.NECRO_SPIRIT, "Necro Spirit", "死魂灵")
                    .itemLanguageTooltip(1, "Summons up to 1 Necro Spirit", "最多召唤1个死魂灵")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();
    /**
     * 激光机枪 - 召唤激光机枪仆从（最多1个）
     */
    public static final DeferredItem<ServantWeaponItemBuilder<LaserMinigun>.ServantWeaponItem> LaserMinigunStaff =
            LyraItemRegisterBuilder.build(ITEMS, "laser_minigun", location -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.LASER_MINIGUN)
                            .damage(6f)
                            .knockback(0.2f)
                            .sound(ServantrySoundRegister.UseServantWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LaserMinigun servant = weapon.createServant(player, itemStack);
                                LyraHelper servantryHelper = LyraHelper.get(player);
                                if (servant.getSameSize() < 1 && servantryHelper.canSummon(AttachmentEntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1.0f));
                                    servantryHelper.add(AttachmentEntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .itemTag(ServantryItemTagsRegister.SectionServantWeapon)
                    .itemLanguage("Laser Minigun", "激光机枪")
                    .servantLanguage(ServantryAttachmentEntityRegister.LASER_MINIGUN, "Laser Minigun", "激光机枪")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .itemTag(ServantryItemTagsRegister.ServantWeapon)
                    .build();

    public static void register() {
    }
}
