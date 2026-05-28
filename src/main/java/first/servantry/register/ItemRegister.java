package first.servantry.register;

import com.google.common.collect.ImmutableMultimap;
import first.servantry.Servantry;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.item.IServantWeapon;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.item.AttributeArmorItem;
import first.servantry.common.item.CurioItem;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servant.StardustDragon;
import first.servantry.common.servant.Twins;
import first.servantry.utils.CuriosUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class ItemRegister {

    public static final Registers Register = Registers.create();

    // ===================== 仆从武器 =====================

    /**
     * 泰拉棱镜 - 召唤泰拉棱镜仆从
     */
    public static final DeferredItem<Item> TerraPrism =
            Register.push(new TabSection(0, Servantry.rl("textures/item/banner/banner.png"), new AnimInfo(18, 3, 8)))
                    .register("terraprism", () ->
                            new IServantWeapon.Builder<>(AttachmentEntityRegister.TerraPrism)
                                    .sound(SoundRegister.UseTerraprism)
                                    .summonPost(servant -> servant.init(servant.getInterpolatedIdleState(1)))
                                    .buildItem()
                    );

    /** 刃杖 - 召唤附魔飞刀群 */
    public static final DeferredItem<Item> BladeStaff = Register.register("blade_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.EnchantedThrowingKnives)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        PathNode idle = servant.getInterpolatedIdleState(1.0f);
                        Vec3 center = owner.getBoundingBox().getCenter();
                        servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                    })
                    .buildItem()
    );

    /** 星尘细胞杖 - 召唤星尘细胞仆从 */
    public static final DeferredItem<Item> StardustCellStaff = Register.register("stardust_cell_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.StardustCell)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        RandomSource random = owner.getRandom();
                        servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
                    })
                    .buildItem()
    );

    /**
     * 星尘龙杖 - 召唤星尘龙（多体节仆从）
     */
    public static final DeferredItem<Item> StardustDragonStaff = Register.register("stardust_dragon_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.StardustDragon)
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
                    .buildItem()
    );

    /**
     * 魔眼法杖 - 召唤双子魔眼
     */
    public static final DeferredItem<Item> OpticStaff = Register.register("optic_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.Twins)
                    .sound(SoundRegister.UseTerraprism)
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        RandomSource random = owner.getRandom();
                        random.setSeed(owner.level().getGameTime());
                        PathNode pathNode = new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0);
                        servant.init(pathNode);
                        EntityData data = owner.getData(AttachmentRegister.EntityData);
                        long count = data.getEntities().stream()
                                .filter(attachmentEntity -> attachmentEntity instanceof Twins)
                                .count();
                        if (true) {
                            Twins twins = AttachmentEntityRegister.Twins.get().factory().get();
                            twins.setOwner(owner);
                            twins.setLaserEye(false);
                            if (data.summonServant(owner, twins)) {
                                twins.init(pathNode);
                            }
                        }
                    })
                    .buildItem()
    );

    /**
     * 暴风雨法杖 - 召唤鲨鱼龙卷
     */
    public static final DeferredItem<Item> TempestStaff = Register.register("tempest_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.Sharknado)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        RandomSource random = owner.getRandom();
                        servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
                    })
                    .buildItem()
    );

    /**
     * 致命球法杖 - 召唤致命球仆从
     */
    public static final DeferredItem<Item> DeadlySphereStaff = Register.register("deadly_sphere_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.DeadlySphere)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        RandomSource random = owner.getRandom();
                        servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
                    })
                    .buildItem()
    );

    public static final DeferredItem<Item> InfiniteScabbard = Register.register("infinite_scabbard", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.InfiniteShadow)
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
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1).component(DataComponentRegister.Scabbard, ScabbardContainer.EMPTY))
    );

    /**
     * 缥缈星核法杖 - 召唤缥缈星核仆从
     */
    public static final DeferredItem<Item> EtherealStellarCoreStaff = Register.register("ethereal_stellar_core_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.EtherealStellarCore)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPre((player, etherealStellarCore) -> player.getData(AttachmentRegister.EntityData).getSameSize(etherealStellarCore) < 9)
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        PathNode idle = servant.getInterpolatedIdleState(1.0f);
                        Vec3 center = owner.getBoundingBox().getCenter();
                        servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                    })
                    .buildItem()
    );

    public static final DeferredItem<Item> SurveyDroneRemote = Register.register("survey_drone_remote", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.OreScout)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPre((player, servant) -> player.getData(AttachmentRegister.EntityData).getSameSize(servant) < 1)
                    .summonPost(servant -> servant.init(servant.getInterpolatedIdleState(1.0f)))
                    .buildItem()
    );

    public static final DeferredItem<Item> FairyBell = Register.register("fairy_bell", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.ScavengerFairy)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPre((player, servant) -> player.getData(AttachmentRegister.EntityData).getSameSize(servant) < 1)
                    .summonPost(servant -> servant.init(servant.getInterpolatedIdleState(1.0f)))
                    .buildItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1))
    );

    // ===================== 套装物品 =====================

    /**
     * 黑曜石头盔 - +8% 仆从伤害
     */
    public static final DeferredItem<Item> ObsidianHelmet =
            Register.push(1).register("obsidian_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.HELMET)
                    .modifier(AttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    /**
     * 黑曜石胸甲 - +1 仆从栏
     */
    public static final DeferredItem<Item> ObsidianChestplate =
            Register.register("obsidian_chestplate", () -> AttributeArmorItem.builder(
                            ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.CHESTPLATE)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 黑曜石护腿 - +8% 仆从伤害
     */
    public static final DeferredItem<Item> ObsidianLeggings =
            Register.register("obsidian_leggings", () -> AttributeArmorItem.builder(
                            ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.LEGGINGS)
                    .modifier(AttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    /**
     * 黑曜石靴子 - +8% 移动速度
     */
    public static final DeferredItem<Item> ObsidianBoots =
            Register.register("obsidian_boots", () -> AttributeArmorItem.builder(
                            ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.BOOTS)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    /**
     * 神圣头盔 - +1 仆从栏，+7% 仆从伤害
     */
    public static final DeferredItem<Item> HallowedHelmet =
            Register.register("hallowed_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    /**
     * 神圣胸甲 - +1 仆从栏，+7% 仆从伤害
     */
    public static final DeferredItem<Item> HallowedChestplate =
            Register.register("hallowed_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    /** 神圣护腿 - +1 仆从栏，+7% 仆从伤害 */
    public static final DeferredItem<Item> HallowedLeggings =
            Register.register("hallowed_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    /** 神圣靴子 - +7% 移动速度，+7% 仆从伤害 */
    public static final DeferredItem<Item> HallowedBoots =
            Register.register("hallowed_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS)
                    .modifier(AttributeRegister.ServantDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    // ===================== 叶绿套装 =====================

    /**
     * 叶绿面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteHelmet =
            Register.register("chlorophyte_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.HELMET)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    /**
     * 叶绿板甲 - +6% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteChestplate =
            Register.register("chlorophyte_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.CHESTPLATE)
                    .modifier(AttributeRegister.ServantDamage, 0.19, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    /**
     * 叶绿护胫 - +4% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteLeggings =
            Register.register("chlorophyte_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.LEGGINGS)
                    .modifier(AttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    /**
     * 叶绿战靴 - +2% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteBoots =
            Register.register("chlorophyte_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.BOOTS)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .build()
            );

    // ===================== 阴森套装 =====================

    /**
     * 阴森头盔 - +11% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyHelmet =
            Register.register("spooky_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.HELMET)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 阴森胸甲 - +11% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyChestplate =
            Register.register("spooky_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.CHESTPLATE)
                    .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 阴森护腿 - +8% 仆从伤害，+1 召唤栏
     */
    public static final DeferredItem<Item> SpookyLeggings =
            Register.register("spooky_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.LEGGINGS)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 阴森战靴 - +4% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyBoots =
            Register.register("spooky_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.BOOTS)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    // ===================== 提基套装 =====================

    /**
     * 提基面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<Item> TikiHelmet =
            Register.register("tiki_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.HELMET)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 提基胸甲 - +10% 仆从伤害
     */
    public static final DeferredItem<Item> TikiChestplate =
            Register.register("tiki_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.CHESTPLATE)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 提基护腿 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> TikiLeggings =
            Register.register("tiki_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.LEGGINGS)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 提基战靴 - +3% 仆从伤害
     */
    public static final DeferredItem<Item> TikiBoots =
            Register.register("tiki_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.BOOTS)
                    .modifier(AttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    // ===================== 英灵殿骑士套装 =====================

    /**
     * 英灵殿骑士头盔 - +1 召唤栏，+10% 仆从伤害，+10% 原版伤害
     */
    public static final DeferredItem<Item> ValhallaKnightHelmet =
            Register.register("valhalla_knight_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.HELMET)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(Attributes.ATTACK_DAMAGE, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 英灵殿骑士胸甲 - +30% 仆从伤害，+0.4 生命再生
     */
    public static final DeferredItem<Item> ValhallaKnightChestplate =
            Register.register("valhalla_knight_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.CHESTPLATE)
                    .modifier(AttributeRegister.ServantDamage, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(AttributeRegister.HealthRegen, 0.4, AttributeModifier.Operation.ADD_VALUE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 英灵殿骑士护腿 - +20% 仆从伤害，+20% 原版伤害
     */
    public static final DeferredItem<Item> ValhallaKnightLeggings =
            Register.register("valhalla_knight_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.LEGGINGS)
                    .modifier(AttributeRegister.ServantDamage, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(Attributes.ATTACK_DAMAGE, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    /**
     * 英灵殿骑士战靴 - +20% 移动速度
     */
    public static final DeferredItem<Item> ValhallaKnightBoots =
            Register.register("valhalla_knight_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.BOOTS)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.RARE))
                    .build()
            );

    // ===================== 星尘套装 =====================

    /**
     * 星尘头盔 - +1 召唤栏，+16% 仆从伤害
     */
    public static final DeferredItem<Item> StardustHelmet =
            Register.register("stardust_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.HELMET)
                    .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .build()
            );

    /**
     * 星尘板甲 - +22% 仆从伤害
     */
    public static final DeferredItem<Item> StardustChestplate =
            Register.register("stardust_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.CHESTPLATE)
                    .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.37, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .build()
            );

    /**
     * 星尘护腿 - +15% 仆从伤害
     */
    public static final DeferredItem<Item> StardustLeggings =
            Register.register("stardust_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.LEGGINGS)
                    .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.37, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .build()
            );

    /**
     * 星尘战靴 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> StardustBoots =
            Register.register("stardust_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.BOOTS)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .build()
            );

    // ===================== 饰品 =====================

    /**
     * 死灵卷轴 - 仆从栏+1，仆从伤害+10%
     */
    public static final DeferredItem<Item> NecromanticScroll = Register.push(2).register("necromantic_scroll", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /** 甲虫莎草纸 - 仆从栏+2，仆从伤害+15%，仆从击退+50% */
    public static final DeferredItem<Item> PapyrusScarab = Register.register("papyrus_scarab", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
    );

    /** 矮人项链 - 仆从栏+1 */
    public static final DeferredItem<Item> PygmyNecklace = Register.register("pygmy_necklace", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /** 大力士甲虫 - 仆从栏+1，仆从击退+50% */
    public static final DeferredItem<Item> HerculesBeetle = Register.register("hercules_beetle", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.RARE))
            .build()
    );

    /** 召唤师徽章 - 召唤伤害+15% */
    public static final DeferredItem<Item> SummonerEmblem = Register.register("summoner_emblem", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /** 学徒围巾 - 仆从数量+1，召唤伤害+10% */
    public static final DeferredItem<Item> ApprenticesScarf = Register.register("apprentices_scarf", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /** 女猎人圆盾 - 护甲+2，仆从数量+1*/
    public static final DeferredItem<Item> HuntressesBuckler = Register.register("huntresses_buckler", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /** 武僧腰带 - 召唤伤害+10%，仆从击退+50%*/
    public static final DeferredItem<Item> MonksBelt = Register.register("monks_belt", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
            .build()
    );

    /** 侍卫护盾 - 护甲+2，召唤伤害+10% */
    public static final DeferredItem<Item> SquiresShield = Register.register("squires_shield", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /**
     * 威胁分析仪 - 加大仆从的索敌半径
     */
    public static final DeferredItem<Item> ThreatAnalyzer = Register.register("threat_analyzer", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
    );

    /**
     * 幻魂神物 - 仆从攻击后随机获得幻魂增益（上位）
     */
    public static final DeferredItem<Item> PhantasmalRelic = Register.register("phantasmal_relic", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .onPostDamage((servant, owner, target) -> {
                List<Holder<MobEffect>> effects = new ArrayList<>();
                effects.add(MobEffectRegister.PhantasmalMight);
                effects.add(MobEffectRegister.PhantasmalBulwark);
                effects.add(MobEffectRegister.PhantasmalRebirth);
                owner.addEffect(new MobEffectInstance(effects.get(owner.getRandom().nextInt(effects.size())), 60));
            })
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
    );

    /**
     * 神圣符文 - 仆从攻击后随机获得神圣增益（中位互斥）
     */
    public static final DeferredItem<Item> HallowedRune = Register.register("hallowed_rune", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .onPostDamage((servant, owner, target) -> {
                if (!CuriosUtil.isEquipped(owner, ItemRegister.PhantasmalRelic.get())) {
                    List<Holder<MobEffect>> effects = new ArrayList<>();
                    effects.add(MobEffectRegister.HallowedMight);
                    effects.add(MobEffectRegister.HallowedGrace);
                    effects.add(MobEffectRegister.HallowedRadiance);
                    owner.addEffect(new MobEffectInstance(effects.get(owner.getRandom().nextInt(effects.size())), 60));
                }
            })
            .properties(properties -> properties.rarity(Rarity.RARE))
            .build()
    );

    /**
     * 灵魂浮雕 - 仆从攻击后随机获得灵魂增益（下位互斥）
     */
    public static final DeferredItem<Item> SoulRelief = Register.register("soul_relief", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .onPostDamage((servant, owner, target) -> {
                if (!CuriosUtil.isEquipped(owner, ItemRegister.HallowedRune.get()) && !CuriosUtil.isEquipped(owner, ItemRegister.PhantasmalRelic.get())) {
                    List<Holder<MobEffect>> effects = new ArrayList<>();
                    effects.add(MobEffectRegister.SoulMight);
                    effects.add(MobEffectRegister.SoulDefense);
                    effects.add(MobEffectRegister.SoulRecovery);
                    owner.addEffect(new MobEffectInstance(effects.get(owner.getRandom().nextInt(effects.size())), 60));
                }
            })
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /**
     * 蚀日尊戒
     */
    public static final DeferredItem<Item> EclipseRing = Register.register("eclipse_ring", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.HealthRegen, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_VALUE));
                builder.put(Attributes.ARMOR, new AttributeModifier(id, 3, AttributeModifier.Operation.ADD_VALUE));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
    );

    /**
     * 始源暗影焰
     */
    public static final DeferredItem<Item> PrimordialShadowflame = Register.register("primordial_shadowflame", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                return builder.build();
            })
            .onPostDamage((servant, owner, target) -> target.addEffect(new MobEffectInstance(MobEffectRegister.Shadowflame, 60, 0)))
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .build()
    );

    /**
     * 万花筒
     */
    public static final DeferredItem<Item> Kaleidoscope = Register.register("kaleidoscope", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .onServantDamage((servant, owner, target, damage) -> {
                if (owner.getRandom().nextFloat() < 0.1f) {
                    return damage * 1.5f;
                }
                return damage;
            })
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
    );

    /**
     * 猎魂徽记
     */
    public static final DeferredItem<Item> HuntSoulEmblem = Register.register("hunt_soul_emblem", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                return builder.build();
            })
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
    );

    /**
     * 星尘碎片 - 仆从攻击时5%概率产生星尘细胞射弹
     */
    public static final DeferredItem<Item> StardustFragment = Register.register("stardust_fragment", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .onPostDamage((servant, owner, target) -> {
                if (owner.getRandom().nextFloat() < 0.05f) {
                    Level level = owner.level();
                    DamageSource damageSource = DamageRegister.getDamageSource(DamageRegister.Servant, level);
                    Vec3 startPos = servant.getPos();
                    StardustProjectile projectile = new StardustProjectile(damageSource, startPos);
                    projectile.setVelocity(startPos.offsetRandom(owner.getRandom(), 1).subtract(startPos).normalize().scale(0.25f));
                    projectile.setChaseTarget(target);
                    projectile.join(owner);
                }
            })
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
    );

    // ===================== 材料 =====================

    public static final DeferredItem<Item> BlackLens = Register.push(3).register("black_lens", () ->
            new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> HallowedIngot = Register.register("hallowed_ingot", () ->
            new Item(new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        Register.Register.register(eventBus);
    }

    public static class Registers {

        private final DeferredRegister.Items Register;
        private final LinkedHashMap<TabSection, List<DeferredItem<Item>>> map = new LinkedHashMap<>();
        private TabSection tabSection = null;

        private Registers(String modid) {
            this.Register = DeferredRegister.createItems(modid);
        }

        private static Registers create() {
            return new Registers(Servantry.MODID);
        }

        public Registers push(TabSection tabSection) {
            this.tabSection = tabSection;
            return this;
        }

        public Registers push(int order) {
            this.tabSection = new TabSection(order, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
            return this;
        }

        @SuppressWarnings("unchecked")
        private <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> sup) {
            DeferredItem<I> register = Register.register(name, sup);
            map.computeIfAbsent(tabSection, k -> new ArrayList<>()).add((DeferredItem<Item>) register);
            return register;
        }

        public List<ItemRegister.TabSection> sortedEntries() {
            List<ItemRegister.TabSection> sortedKeys = new ArrayList<>(map.keySet());
            sortedKeys.sort(Comparator.comparingInt(ItemRegister.TabSection::integer));
            return sortedKeys;
        }

        public LinkedHashMap<TabSection, List<DeferredItem<Item>>> getMap() {
            return map;
        }
    }

    public record TabSection(Integer integer, ResourceLocation texture, AnimInfo animInfo) {
    }

}
