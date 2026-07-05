package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.PathNode;
import first.servantry.api.item.IServantWeapon;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.common.sentryServant.*;
import first.servantry.common.servant.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class ServantWeaponRegister {

    private static final Registers Register = Registers.getInstance();
    public static final TabGroup SERVANT_WEAPON = new TabGroup(0, Servantry.rl("textures/item/banner/banner.png"), new AnimInfo(18, 3, 8));

    /**
     * 无人机
     */
    public static final DeferredItem<Item> SurveyDroneRemote =
            Register.register(SERVANT_WEAPON, "survey_drone_remote", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.OreScout)
                            .sound(SoundRegister.UseServantWeapon)
                            .summon((weapon, player) -> {
                                OreScout servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servant.getSameSize() < 1 && servantryHelper.canSummon(EntityData.Type.Servant, 1)) {
                                    servantryHelper.add(EntityData.Type.Servant, servant);
                                    servant.init(servant.getInterpolatedIdleState(1.0f));
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.HallowedIngot, 8)
                            .ingredient(Items.DIAMOND, 8)
                            .ingredient(Items.NETHERITE_INGOT, 1)
                            .result(ServantWeaponRegister.SurveyDroneRemote)
                            .save(output))
                    .language("Survey Drone Remote", "矿勘无人机遥控器")
                    .servant(AttachmentEntityRegister.OreScout, "Survey Drone", "矿勘无人机")
                    .tooltip(1, "Summons up to 1 Survey Drone", "最多召唤1架矿勘无人机")
                    .build();
    /**
     * 妖精铃铛
     */
    public static final DeferredItem<Item> FairyBell =
            Register.register(SERVANT_WEAPON, "fairy_bell", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.ScavengerFairy)
                            .sound(SoundRegister.UseServantWeapon)
                            .summon((weapon, player) -> {
                                ScavengerFairy servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servant.getSameSize() < 1 && servantryHelper.canSummon(EntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1.0f));
                                    servantryHelper.add(EntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.HallowedIngot, 8)
                            .ingredient(Items.AMETHYST_SHARD, 8)
                            .result(ServantWeaponRegister.FairyBell)
                            .save(output))
                    .language("Fairy Bell", "妖精铃铛")
                    .servant(AttachmentEntityRegister.ScavengerFairy, "Scavenger Fairy", "拾荒妖精")
                    .tooltip(1, "Summons up to 1 Scavenger Fairy", "最多召唤1只拾荒妖精")
                    .build();
    /**
     * 刃杖 - 召唤附魔飞刀群
     */
    public static final DeferredItem<Item> BladeStaff =
            Register.register(SERVANT_WEAPON, "blade_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.EnchantedThrowingKnives)
                            .damage(0.6f)
                            .armorPierce(7.5f)
                            .sound(SoundRegister.UseServantWeapon)
                            .summon((weapon, player) -> {
                                EnchantedThrowingKnives servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.Servant, 1)) {
                                    PathNode idle = servant.getInterpolatedIdleState(1.0f);
                                    Vec3 center = player.getBoundingBox().getCenter();
                                    servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                                    servantryHelper.add(EntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.AMETHYST_SHARD, 12)
                            .ingredient(Items.IRON_INGOT, 12)
                            .result(ServantWeaponRegister.BladeStaff)
                            .save(output))
                    .language("Blade Staff", "刃杖")
                    .servant(AttachmentEntityRegister.EnchantedThrowingKnives, "Enchanted Throwing Knives", "附魔飞刀")
                    .tooltip(1, "Don't let their small size fool you", "别被它们小小的个头给骗了")
                    .build();
    /**
     * 雨云法杖
     */
    public static final DeferredItem<Item> RainCloudStaff =
            Register.register(SERVANT_WEAPON, "rain_cloud_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Cloud)
                            .damage(3f)
                            .knockback(0)
                            .sentryServant()
                            .sound(SoundRegister.UseCloudStaff)
                            .summon((weapon, player) -> {
                                Cloud cloud = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    cloud.init(new PathNode(player.getBoundingBox().getCenter(), 0, 0, 0));
                                    cloud.setVelocity(player.getLookAngle());
                                    servantryHelper.add(EntityData.Type.SentryServant, cloud);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Rain Cloud Staff", "雨云法杖")
                    .servant(AttachmentEntityRegister.Cloud, "Rain Cloud", "雨云")
                    .tooltip(1, "Summons a cloud to rain down on your foes", "召唤云朵来向敌人降下大雨")
                    .build();
    /**
     * 魔眼法杖 - 召唤双子魔眼
     */
    public static final DeferredItem<Item> OpticStaff =
            Register.register(SERVANT_WEAPON, "optic_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Twins)
                            .damage(2.4f)
                            .knockback(0.1f)
                            .sound(SoundRegister.UseTerraprism)
                            .summon((weapon, player) -> {
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.Servant, 1)) {
                                    RandomSource random = player.getRandom();
                                    PathNode pathNode = new PathNode(player.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0);
                                    Twins laserEye = null;
                                    for (int i = 0; i < 2; i++) {
                                        Twins twins = weapon.createServant(player);
                                        if (i == 0) {
                                            twins.setLaserEye(true);
                                            laserEye = twins;
                                        } else {
                                            twins.setFlameEye(true);
                                            twins.setOther(laserEye);
                                        }
                                        twins.setSlotCost(i);
                                        twins.init(pathNode);
                                        servantryHelper.add(EntityData.Type.Servant, twins);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.BlackLens)
                            .ingredient(ItemRegister.HallowedIngot, 12)
                            .result(ServantWeaponRegister.OpticStaff)
                            .save(output))
                    .language("Optic Staff", "魔眼法杖")
                    .servant(AttachmentEntityRegister.Twins, "Twins", "双子魔眼")
                    .build();
    /**
     * 致命球法杖 - 召唤致命球仆从
     */
    public static final DeferredItem<Item> DeadlySphereStaff =
            Register.register(SERVANT_WEAPON, "deadly_sphere_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.DeadlySphere)
                            .damage(5.5f)
                            .knockback(0.1f)
                            .sound(SoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .language("Deadly Sphere Staff", "致命球法杖")
                    .servant(AttachmentEntityRegister.DeadlySphere, "Deadly Sphere", "致命球")
                    .build();
    /**
     * 脉冲炮塔遥控装置
     */
    public static final DeferredItem<Item> PulseTurretRemote =
            Register.register(SERVANT_WEAPON, "pulse_turret_remote", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.PulseTurret)
                            .damage(7.5f)
                            .knockback(0.1f)
                            .sentryServant()
                            .sound(SoundRegister.UseBallistaStaff)
                            .summon((weapon, player) -> {
                                PulseTurret servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    if (servantryHelper.getEntityData().get(EntityData.Type.SentryServant, PulseTurret.class).isEmpty()) {
                                        servant.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                        servantryHelper.add(EntityData.Type.SentryServant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Pulse Turret Remote", "脉冲炮塔遥控装置")
                    .servant(AttachmentEntityRegister.PulseTurret, "Pulse Turret", "脉冲炮塔")
                    .tooltip(1, "At most one pulse turret can exist at a time", "最多召唤一个脉冲炮塔")
                    .build();
    /**
     * 暴风雨法杖 - 召唤鲨鱼龙卷
     */
    public static final DeferredItem<Item> TempestStaff =
            Register.register(SERVANT_WEAPON, "tempest_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Sharknado)
                            .damage(5f)
                            .knockback(0.1f)
                            .sound(SoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .language("Tempest Staff", "暴风雨法杖")
                    .servant(AttachmentEntityRegister.Sharknado, "Sharknado", "鲨鱼龙卷")
                    .build();
    /**
     * 泰拉棱镜 - 召唤泰拉棱镜仆从
     */
    public static final DeferredItem<Item> TerraPrism =
            Register.register(SERVANT_WEAPON, "terraprism", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.TerraPrism)
                            .damage(9f).knockback(0.1f)
                            .sound(SoundRegister.UseTerraprism)
                            .summon((weapon, player) -> {
                                Terraprism servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1));
                                    servantryHelper.add(EntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Terraprism", "泰拉棱镜")
                    .servant(AttachmentEntityRegister.TerraPrism, "Terraprism", "泰拉棱镜")
                    .tooltip(1, "A flawless blade once hailed as the Prism of the Earth", "曾被冠以大地棱彩美名的无暇之剑")
                    .build();
    /**
     * 弩车魔杖
     */
    public static final DeferredItem<Item> BallistaRod =
            Register.register(SERVANT_WEAPON, "ballista_rod", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Ballista)
                            .damage(3f)
                            .knockback(0.1f)
                            .sentryServant()
                            .sound(SoundRegister.UseBallistaStaff)
                            .summon((weapon, player) -> {
                                Ballista servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(1);
                                    servant.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    servantryHelper.add(EntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Ballista Rod", "弩车魔杖")
                    .tooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .build();
    /**
     * 弩车手杖
     */
    public static final DeferredItem<Item> BallistaCane =
            Register.register(SERVANT_WEAPON, "ballista_cane", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Ballista)
                            .damage(7.4f)
                            .knockback(0.2f)
                            .sentryServant()
                            .sound(SoundRegister.UseBallistaStaff)
                            .summon((weapon, player) -> {
                                Ballista servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(2);
                                    servant.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    servantryHelper.add(EntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Ballista Cane", "弩车手杖")
                    .tooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .build();
    /**
     * 弩车法杖
     */
    public static final DeferredItem<Item> BallistaStaff =
            Register.register(SERVANT_WEAPON, "ballista_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Ballista)
                            .damage(15.6f)
                            .knockback(0.4f)
                            .sentryServant()
                            .sound(SoundRegister.UseBallistaStaff)
                            .summon((weapon, player) -> {
                                Ballista servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(3);
                                    servant.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    servantryHelper.add(EntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Ballista Staff", "弩车法杖")
                    .servant(AttachmentEntityRegister.Ballista, "Ballista", "弩车")
                    .tooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .build();
    /**
     * 缥缈星核法杖 - 召唤缥缈星核仆从
     */
    public static final DeferredItem<Item> EtherealStellarCoreStaff =
            Register.register(SERVANT_WEAPON, "ethereal_stellar_core_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.EtherealStellarCore)
                            .damage(10.0f)
                            .knockback(0.5f)
                            .sound(SoundRegister.UseServantWeapon)
                            .summon((weapon, player) -> {
                                EtherealStellarCore servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servant.getSameSize() < 9 && servantryHelper.canSummon(EntityData.Type.Servant, 1)) {
                                    servant.init(servant.getInterpolatedIdleState(1.0f));
                                    servantryHelper.add(EntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Stardust, 12)
                            .result(ServantWeaponRegister.EtherealStellarCoreStaff)
                            .save(output))
                    .language("Ethereal Stellar Core Staff", "缥缈星核法杖")
                    .servant(AttachmentEntityRegister.EtherealStellarCore, "Ethereal Stellar Core", "缥缈星核")
                    .tooltip(1, "Summons up to 9 Ethereal Stellar Cores", "最多召唤9个缥缈星核")
                    .build();
    /**
     * 星尘细胞杖 - 召唤星尘细胞仆从
     */
    public static final DeferredItem<Item> StardustCellStaff =
            Register.register(SERVANT_WEAPON, "stardust_cell_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.StardustCell)
                            .damage(6f)
                            .knockback(0.2f)
                            .sound(SoundRegister.UseServantWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Stardust, 18)
                            .result(ServantWeaponRegister.StardustCellStaff)
                            .save(output))
                    .language("Stardust Cell Staff", "星尘细胞法杖")
                    .servant(AttachmentEntityRegister.StardustCell, "Stardust Cell", "星尘细胞")
                    .tooltip(1, "Cultivate the most beautiful cellular infection", "培养最美丽的细胞感染")
                    .build();
    /**
     * 星尘龙杖 - 召唤星尘龙（多体节仆从）
     */
    public static final DeferredItem<Item> StardustDragonStaff =
            Register.register(SERVANT_WEAPON, "stardust_dragon_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.StardustDragon)
                            .damage(8f).knockback(0.5f)
                            .sound(SoundRegister.UseServantWeapon)
                            .summon((weapon, player) -> {
                                ServantryHelper helper = ServantryHelper.get(player);
                                if (helper.canSummon(EntityData.Type.Servant, 1)) {
                                    List<StardustDragon> existing = helper.getEntityData().get(EntityData.Type.Servant, StardustDragon.class, true);
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            StardustDragon servant = weapon.createServant(player);
                                            servant.setSegmentIndex(i);
                                            if (i < 2) {
                                                servant.setSlotCost(0);
                                            }
                                            servant.setTotalSegments(3);
                                            servant.init(new PathNode(player.position().add(0, 3, -i * servant.getSegmentDistance()), 0, 0, 0));
                                            helper.add(EntityData.Type.Servant, servant);
                                        }
                                    } else {
                                        StardustDragon servant = weapon.createServant(player);
                                        servant.setSegmentIndex(existing.size());
                                        servant.setTotalSegments(existing.size() + 1);
                                        for (StardustDragon dragon : existing) {
                                            dragon.setTotalSegments(existing.size() + 1);
                                        }
                                        StardustDragon last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos().add(last.getLookAngle().scale(-servant.getSegmentDistance()));
                                        servant.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(EntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Stardust, 18)
                            .result(ServantWeaponRegister.StardustDragonStaff)
                            .save(output))
                    .language("Stardust Dragon Staff", "星尘之龙法杖")
                    .servant(AttachmentEntityRegister.StardustDragon, "Stardust Dragon", "星尘之龙")
                    .tooltip(1, "When you have a dragon, who needs a swarm?", "有了一条巨龙后，谁还需要一群仆从呢？")
                    .build();
    /**
     * 月亮传送门法杖
     */
    public static final DeferredItem<Item> MoonPortalStaff =
            Register.register(SERVANT_WEAPON, "moon_portal_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.MoonPortal)
                            .damage(10)
                            .knockback(0.7f)
                            .sentryServant()
                            .sound(SoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player) -> {
                                MoonPortal servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    servant.init(new PathNode(player.position().add(0, 2, 0), 0, 0, 0));
                                    servantryHelper.add(EntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Moon Portal Staff", "月亮传送门法杖")
                    .servant(AttachmentEntityRegister.MoonPortal, "Moon Portal", "月亮传送门")
                    .build();
    /**
     * 七彩水晶法杖
     */
    public static final DeferredItem<Item> RainbowCrystalStaff =
            Register.register(SERVANT_WEAPON, "rainbow_crystal_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.RainbowCrystal)
                            .damage(13)
                            .knockback(0.7f)
                            .sentryServant()
                            .sound(SoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player) -> {
                                RainbowCrystal servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    servant.init(new PathNode(player.position().add(0, 2, 0), 0, 0, 0));
                                    servantryHelper.add(EntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Rainbow Crystal Staff", "七彩水晶法杖")
                    .servant(AttachmentEntityRegister.RainbowCrystal, "Rainbow Crystal", "七彩水晶")
                    .tooltip(1, "'The colors, Duke, the colors!'", "“公爵，多么缤纷的颜色！”")
                    .build();
    /**
     * 无限剑鞘
     */
    public static final DeferredItem<Item> InfiniteScabbard =
            Register.register(SERVANT_WEAPON, "infinite_scabbard", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.InfiniteShadow)
                            .sound(SoundRegister.UseTerraprism)
                            .summon((weapon, player) -> {
                                InfiniteShadow servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                ScabbardContainer container = player.getMainHandItem().getComponents().getOrDefault(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
                                if (servantryHelper.canSummon(EntityData.Type.Servant, 1) && !container.isEmpty()) {
                                    servant.setItemStack(container.itemStack());
                                    servant.init(servant.getInterpolatedIdleState(1));
                                    servantryHelper.add(EntityData.Type.Servant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC)
                                    .component(DataComponentRegister.Scabbard, ScabbardContainer.EMPTY))
                            .build())
                    .language("Infinite Scabbard", "无限剑鞘")
                    .servant(AttachmentEntityRegister.InfiniteShadow, "Infinite Shadow", "无限之影")
                    .tooltip(1, "A scabbard that stores a blade of infinite potential", "蕴含无限可能之'剑'的剑鞘")
                    .tooltip(2, "Right-click an item to store, right-click an empty slot to retrieve", "右键物品存入，右键空格子取出")
                    .build();
    /**
     * 虚空吞噬者傀具
     */
    public static final DeferredItem<Item> VoidEaterMarionette =
            Register.register(SERVANT_WEAPON, "void_eater_marionette", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.VoidEater)
                            .damage(11f)
                            .knockback(0.5f)
                            .sound(SoundRegister.UseServantWeapon)
                            .summon((weapon, player) -> {
                                ServantryHelper helper = ServantryHelper.get(player);
                                if (helper.canSummon(EntityData.Type.Servant, 1)) {
                                    List<VoidEater> existing = helper.getEntityData().get(EntityData.Type.Servant, VoidEater.class, true);
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            VoidEater servant = weapon.createServant(player);
                                            servant.setSegmentIndex(i);
                                            if (i < 2) {
                                                servant.setSlotCost(0);
                                            }
                                            servant.setTotalSegments(3);
                                            servant.init(new PathNode(player.position().add(0, 3, -i * servant.getSegmentDistance()), 0, 0, 0));
                                            helper.add(EntityData.Type.Servant, servant);
                                        }
                                    } else {
                                        VoidEater servant = weapon.createServant(player);
                                        servant.setSegmentIndex(existing.size());
                                        servant.setTotalSegments(existing.size() + 1);
                                        for (VoidEater dragon : existing) {
                                            dragon.setTotalSegments(existing.size() + 1);
                                        }
                                        VoidEater last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos().add(last.getLookAngle().scale(-servant.getSegmentDistance()));
                                        servant.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(EntityData.Type.Servant, servant);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Void Eater Marionette", "虚空吞噬者傀具")
                    .servant(AttachmentEntityRegister.VoidEater, "Void Eater", "虚空吞噬者")
                    .tooltip(1, "Contains the power to command miniature devourers", "蕴含掌控小型吞噬者的力量")
                    .tooltip(2, "The devourer uses the God Eater, Holy Incineration and Cosmic Maelstrom to attack", "吞噬者会使用噬神者，焚灭虔信之火和超宇宙狂涡攻击")
                    .tooltip(3, "While the devourer is present, all your attacks unleash God-Slaying Fury", "吞噬者在场时，你的所有攻击都会释放弑神怒焰")
                    .build();
    /**
     * 雷云盆栽
     */
    public static final DeferredItem<Item> ThundercloudBonsai =
            Register.register(SERVANT_WEAPON, "thundercloud_bonsai", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.SuperPeashooter)
                            .damage(12f)
                            .knockback(0)
                            .sentryServant()
                            .sound(SoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player) -> {
                                SuperPeashooter servant = weapon.createServant(player);
                                ServantryHelper servantryHelper = ServantryHelper.get(player);
                                if (servantryHelper.canSummon(EntityData.Type.SentryServant, 1)) {
                                    servant.setLevel(3);
                                    servant.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    servantryHelper.add(EntityData.Type.SentryServant, servant);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC))
                            .build())
                    .language("Thundercloud Bonsai", "雷云盆栽")
                    .servant(AttachmentEntityRegister.SuperPeashooter, "Super Peashooter", "超级电能豌豆射手")
                    .tooltip(1, "Fires seven lightning balls at once, with a chance to fire a large number of scattered lightning balls at once", "一次发射七颗雷电球，有概率一次发射大量散射雷电球")
                    .build();

    public static void register() {
    }
}
