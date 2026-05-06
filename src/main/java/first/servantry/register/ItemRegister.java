package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.item.IServantWeapon;
import first.servantry.common.item.StardustDragonWeaponItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    // ===================== 仆从武器 =====================

    /** 泰拉棱镜 - 召唤泰拉棱镜仆从 */
    public static final DeferredItem<Item> TerraPrism = Register.register("terraprism", () ->
            new IServantWeapon.Builder<>(ServantRegister.TerraPrism)
                    .sound(SoundRegister.UseTerraprism)
                    .onSummon(servant -> servant.init(servant.getInterpolatedIdleState(1)))
                    .buildItem()
    );

    /** 刃杖 - 召唤附魔飞刀群 */
    public static final DeferredItem<Item> BladeStaff = Register.register("blade_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.EnchantedThrowingKnives)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> {
                        Player owner = servant.getOwner();
                        PathNode idle = servant.getInterpolatedIdleState(1.0f);
                        Vec3 center = owner.getBoundingBox().getCenter();
                        servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                    })
                    .buildItem()
    );

    /** 星尘细胞杖 - 召唤星尘细胞仆从 */
    public static final DeferredItem<Item> StardustCellStaff = Register.register("stardust_cell_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.StardustCell)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> {
                        Player owner = servant.getOwner();
                        RandomSource random = owner.getRandom();
                        servant.init(new PathNode(owner.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
                    })
                    .buildItem()
    );

    /**
     * 星尘龙杖 - 召唤星尘龙（多体节仆从）
     */
    public static final DeferredItem<Item> StardustDragonStaff = Register.register("stardust_dragon_staff", StardustDragonWeaponItem::new);

    // ===================== 套装物品 =====================

    public static final DeferredItem<Item> HallowedHelmet = Register.register("hallowed_helmet", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedChestplate = Register.register("hallowed_chestplate", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedLeggings = Register.register("hallowed_leggings", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedBoots = Register.register("hallowed_boots", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
