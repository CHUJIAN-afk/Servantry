package first.servantry.register;

import com.google.common.collect.ImmutableMultimap;
import first.servantry.Servantry;
import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.item.IServantWeapon;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.item.CurioItem;
import first.servantry.common.servant.StardustDragon;
import first.servantry.common.servant.Twins;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    // ===================== 仆从武器 =====================

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

    /** 泰拉棱镜 - 召唤泰拉棱镜仆从 */
    public static final DeferredItem<Item> TerraPrism = Register.register("terraprism", () ->
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

                        if (existing.size() == 1) {
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
                    .summonCount(2)
                    .summonPre((player, twins) -> {
                        long count = player.getData(AttachmentRegister.EntityData).getEntities().stream()
                                .filter(attachmentEntity -> attachmentEntity instanceof Twins)
                                .count();
                        twins.setLaserEye(count % 2 == 0);
                        return true;
                    })
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        RandomSource random = owner.getRandom();
                        random.setSeed(owner.level().getGameTime());
                        servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
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

    /**
     * 永夜法杖 - 召唤永夜之眼仆从
     */
    public static final DeferredItem<Item> EyeOfEternalNightStaff = Register.register("eye_of_eternal_night_staff", () ->
            new IServantWeapon.Builder<>(AttachmentEntityRegister.EyeOfEternalNight)
                    .sound(SoundRegister.UseServantWeapon)
                    .summonPost(servant -> {
                        Player owner = servant.getOwner();
                        PathNode idle = servant.getInterpolatedIdleState(1.0f);
                        Vec3 center = owner.getBoundingBox().getCenter();
                        servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                    })
                    .buildItem()
    );

    // ===================== 材料 =====================

    public static final DeferredItem<Item> BlackLens = Register.register("black_lens", () ->
            new Item(new Item.Properties())
    );

    // ===================== 套装物品 =====================

    public static final DeferredItem<Item> HallowedHelmet =
            Register.register("hallowed_helmet", () -> new ArmorItem(
                    ArmorMaterialRegister.HallowedArmorMaterial,
                    ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(6))
                            .rarity(Rarity.UNCOMMON)
            ));

    public static final DeferredItem<Item> HallowedChestplate =
            Register.register("hallowed_chestplate", () -> new ArmorItem(
                    ArmorMaterialRegister.HallowedArmorMaterial,
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(6))
                            .rarity(Rarity.UNCOMMON)
            ));

    public static final DeferredItem<Item> HallowedLeggings =
            Register.register("hallowed_leggings", () -> new ArmorItem(
                    ArmorMaterialRegister.HallowedArmorMaterial,
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(6))
                            .rarity(Rarity.UNCOMMON)
            ));

    public static final DeferredItem<Item> HallowedBoots =
            Register.register("hallowed_boots", () -> new ArmorItem(
                    ArmorMaterialRegister.HallowedArmorMaterial,
                    ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.BOOTS.getDurability(6))
                            .rarity(Rarity.UNCOMMON)
            ));

    // ===================== 饰品 =====================

    /** 死灵卷轴 - 仆从栏+1，仆从伤害+10% */
    public static final DeferredItem<Item> NecromanticScroll = Register.register("necromantic_scroll", () -> CurioItem.builder()
            .canEquipFromUse(true)
            .attributeModifiers((slotContext, id, stack) -> {
                ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                return builder.build();
            })
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
            .build()
    );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
