package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.servant.PathNode;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedList;
import java.util.List;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    public static class RenderItem extends Item {
        public RenderItem(Properties properties) {
            super(properties);
        }
    }

    public static final DeferredItem<Item> TerraPrism = Register.register("terraprism", () ->
            new IServantWeapon.Builder<>(ServantRegister.TerraPrism)
                    .sound(SoundRegister.UseTerraprism)
                    .onSummon(servant -> servant.init(servant.getInterpolatedIdleState(servant.getOwner(), 1)))
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );
/*
    public static final DeferredItem<Item> EnchantedThrowingKnives = Register.registerItem("enchanted_throwing_knives_render_item", RenderItem::new);
    public static final DeferredItem<Item> BladeStaff = Register.register("blade_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.EnchantedThrowingKnives)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> {
                        Player owner = servant.getOwner();
                        ServantData data = owner.getData(AttachmentRegister.ServantData);
                        PathNode idle = servant.getInterpolatedIdleState(owner, data.getOrder(servant), data.getSameSize(servant), 1.0f);
                        Vec3 center = owner.getBoundingBox().getCenter();
                        servant.setPath(List.of(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll())));
                    })
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );

    public static final DeferredItem<Item> SanguineStaff = Register.register("sanguine_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.SanguineBat)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> {
                        Player owner = servant.getOwner();
                        servant.setPath(List.of(new PathNode("", owner.position(), owner.yBodyRot, 0, 0)));
                    })
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );
*/
    public static final DeferredItem<Item> StardustCell = Register.registerItem("stardust_cell_render_item", RenderItem::new);
    public static final DeferredItem<Item> StardustCellStaff = Register.register("stardust_cell_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.StardustCell)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> servant.init(new PathNode(servant.getOwner().position().add(0, 3, 0), 0, 0, 0)))
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );

    // 2. 注册套装物品
    public static final DeferredItem<Item> HallowedHelmet = Register.register("hallowed_helmet", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedChestplate = Register.register("hallowed_chestplate", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedLeggings = Register.register("hallowed_leggings", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedBoots = Register.register("hallowed_boots", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}