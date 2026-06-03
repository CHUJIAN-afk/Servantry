package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.item.IServantWeapon;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.common.servant.StardustDragon;
import first.servantry.common.servant.Twins;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class ServantWeaponRegister {

    private static final Registers Register = Registers.getInstance();
    public static final TabGroup SERVANT_WEAPON = new TabGroup(0, Servantry.rl("textures/item/banner/banner.png"), new AnimInfo(18, 3, 8));
    /**
     * 致命球法杖 - 召唤致命球仆从
     */
    public static final DeferredItem<Item> DeadlySphereStaff =
            Register.register(SERVANT_WEAPON, "deadly_sphere_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.DeadlySphere)
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPost(servant -> {
                                Player owner = servant.getOwner();
                                RandomSource random = owner.getRandom();
                                servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .language("Deadly Sphere Staff", "致命球法杖")
                    .servant(AttachmentEntityRegister.DeadlySphere, "Deadly Sphere", "致命球")
                    .build();    /**
     * 无人机
     */
    public static final DeferredItem<Item> SurveyDroneRemote =
            Register.register(SERVANT_WEAPON, "survey_drone_remote", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.OreScout)
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPre((player, servant) -> servant.getSameSize() < 1)
                            .summonPost(servant -> servant.init(servant.getInterpolatedIdleState(1.0f)))
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
     * 暴风雨法杖 - 召唤鲨鱼龙卷
     */
    public static final DeferredItem<Item> TempestStaff =
            Register.register(SERVANT_WEAPON, "tempest_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Sharknado)
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPost(servant -> {
                                Player owner = servant.getOwner();
                                RandomSource random = owner.getRandom();
                                servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE))
                            .build())
                    .language("Tempest Staff", "暴风雨法杖")
                    .servant(AttachmentEntityRegister.Sharknado, "Sharknado", "鲨鱼龙卷")
                    .build();    /**
     * 刃杖 - 召唤附魔飞刀群
     */
    public static final DeferredItem<Item> BladeStaff =
            Register.register(SERVANT_WEAPON, "blade_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.EnchantedThrowingKnives)
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPost(servant -> {
                                Player owner = servant.getOwner();
                                PathNode idle = servant.getInterpolatedIdleState(1.0f);
                                Vec3 center = owner.getBoundingBox().getCenter();
                                servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
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
                    .tooltip(1, "Ignores 2.5 points of enemy Defense", "忽略敌人 2.5 防御力")
                    .tooltip(2, "Don't let their small size fool you", "别被它们小小的个头给骗了")
                    .build();
    /**
     * 泰拉棱镜 - 召唤泰拉棱镜仆从
     */
    public static final DeferredItem<Item> TerraPrism =
            Register.register(SERVANT_WEAPON, "terraprism", () ->
                            new IServantWeapon.Builder<>(AttachmentEntityRegister.TerraPrism)
                                    .sound(SoundRegister.UseTerraprism)
                                    .summonPost(servant -> servant.init(servant.getInterpolatedIdleState(1)))
                                    .properties(properties -> properties.rarity(Rarity.EPIC))
                                    .build())
                    .language("Terraprism", "泰拉棱镜")
                    .servant(AttachmentEntityRegister.TerraPrism, "Terraprism", "泰拉棱镜")
                    .tooltip(1, "A flawless blade once hailed as the Prism of the Earth", "曾被冠以大地棱彩美名的无暇之剑")
                    .build();    /**
     * 魔眼法杖 - 召唤双子魔眼
     */
    public static final DeferredItem<Item> OpticStaff =
            Register.register(SERVANT_WEAPON, "optic_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.Twins)
                            .sound(SoundRegister.UseTerraprism)
                            .summonPost(servant -> {
                                Player owner = servant.getOwner();
                                RandomSource random = owner.getRandom();
                                random.setSeed(owner.level().getGameTime());
                                PathNode pathNode = new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0);
                                servant.init(pathNode);
                                EntityData data = owner.getData(AttachmentRegister.EntityData);
                                Twins twins = AttachmentEntityRegister.Twins.get().factory().get();
                                twins.setOwner(owner);
                                twins.setLaserEye(false);
                                if (data.summonServant(owner, twins)) {
                                    twins.init(pathNode);
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
     * 无限剑鞘
     */
    public static final DeferredItem<Item> InfiniteScabbard =
            Register.register(SERVANT_WEAPON, "infinite_scabbard", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.InfiniteShadow)
                            .sound(SoundRegister.UseTerraprism)
                            .summonPre((player, infiniteShadow) -> {
                                ItemStack mainHandItem = player.getMainHandItem();
                                ScabbardContainer container = mainHandItem.getComponents().getOrDefault(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
                                if (!container.isEmpty()) {
                                    infiniteShadow.setItemStack(container.itemStack());
                                    return true;
                                }
                                return false;
                            })
                            .summonPost(infiniteShadow -> infiniteShadow.init(infiniteShadow.getInterpolatedIdleState(1)))
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(DataComponentRegister.Scabbard, ScabbardContainer.EMPTY))
                            .build())
                    .language("Infinite Scabbard", "无限剑鞘")
                    .servant(AttachmentEntityRegister.InfiniteShadow, "Infinite Shadow", "无限之影")
                    .tooltip(1, "A scabbard that stores a blade of infinite potential", "蕴含无限可能之'剑'的剑鞘")
                    .tooltip(2, "Right-click an item to store, right-click an empty slot to retrieve", "右键物品存入，右键空格子取出")
                    .build();    /**
     * 妖精铃铛
     */
    public static final DeferredItem<Item> FairyBell =
            Register.register(SERVANT_WEAPON, "fairy_bell", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.ScavengerFairy)
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPre((player, servant) -> servant.getSameSize() < 1)
                            .summonPost(servant -> servant.init(servant.getInterpolatedIdleState(1.0f)))
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

    public static void register() {
    }


    /**
     * 缥缈星核法杖 - 召唤缥缈星核仆从
     */
    public static final DeferredItem<Item> EtherealStellarCoreStaff =
            Register.register(SERVANT_WEAPON, "ethereal_stellar_core_staff", () -> new IServantWeapon.Builder<>(AttachmentEntityRegister.EtherealStellarCore)
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPre((player, etherealStellarCore) -> etherealStellarCore.getSameSize() < 9)
                            .summonPost(servant -> {
                                Player owner = servant.getOwner();
                                PathNode idle = servant.getInterpolatedIdleState(1.0f);
                                Vec3 center = owner.getBoundingBox().getCenter();
                                servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
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
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPost(servant -> {
                                Player owner = servant.getOwner();
                                RandomSource random = owner.getRandom();
                                servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
                            })
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
                            .sound(SoundRegister.UseServantWeapon)
                            .summonPre((player, servant) -> player.getData(AttachmentRegister.EntityData).canSummon(player, 1))
                            .summonPost(stardustDragon -> {
                                Player owner = stardustDragon.getOwner();
                                EntityData data = owner.getData(AttachmentRegister.EntityData);
                                AttachmentEntityType<StardustDragon> type = AttachmentEntityRegister.StardustDragon.get();
                                List<StardustDragon> existing = data.getEntities().stream()
                                        .filter(e -> e instanceof StardustDragon)
                                        .map(e -> (StardustDragon) e)
                                        .toList();
                                if (existing.isEmpty()) {
                                    // 首次召唤：设置索引0，额外召唤2个体节
                                    stardustDragon.setSegmentIndex(0);
                                    stardustDragon.setTotalSegments(3);
                                    stardustDragon.init(new PathNode(owner.position().add(0, 3, 0), 0, 0, 0));

                                    for (int i = 1; i < 3; i++) {
                                        StardustDragon segment = type.factory().get();
                                        segment.setOwner(owner);
                                        segment.setSegmentIndex(i);
                                        segment.setTotalSegments(3);
                                        if (data.summonServant(owner, segment)) {
                                            segment.init(new PathNode(owner.position().add(0, 3, -i * segment.getSegmentDistance()), 0, 0, 0));
                                        }
                                    }
                                } else {
                                    // 增加体节：找到最大索引，设置新索引
                                    int maxIndex = existing.stream()
                                            .mapToInt(StardustDragon::getSegmentIndex)
                                            .max()
                                            .orElse(0);

                                    stardustDragon.setSegmentIndex(maxIndex + 1);

                                    // 更新所有体节的总数
                                    int newTotal = maxIndex + 2;
                                    existing.forEach(s -> s.setTotalSegments(newTotal));
                                    stardustDragon.setTotalSegments(newTotal);

                                    // 在最后一个体节位置初始化
                                    Vec3 lastPos = existing.stream()
                                            .filter(e -> e.getSegmentIndex() == maxIndex)
                                            .findFirst()
                                            .map(StardustDragon::getPos)
                                            .orElse(owner.position().add(0, 3, 0));
                                    stardustDragon.init(new PathNode(lastPos, 0, 0, 0));
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



}
