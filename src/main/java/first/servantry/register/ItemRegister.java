package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.ServantType;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    public static final DeferredItem<Item> TerraPrism = Register.register("terraprism", () ->
            new IServantWeapon.Builder<>(ServantRegister.TerraPrism)
                    .sound(SoundRegister.UseTerraprism)
                    .onSummon(servant -> servant.init(servant.getInterpolatedIdleState(1)))
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );

    public static final DeferredItem<Item> BladeStaff = Register.register("blade_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.EnchantedThrowingKnives)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> {
                        Player owner = servant.getOwner();
                        EntityData data = owner.getData(AttachmentRegister.EntityData);
                        PathNode idle = servant.getInterpolatedIdleState(owner, data.getOrder(servant), Math.max(1, data.getSameSize(servant)), 1.0f);
                        Vec3 center = owner.getBoundingBox().getCenter();
                        servant.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                    })
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );

    public static final DeferredItem<Item> StardustCellStaff = Register.register("stardust_cell_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.StardustCell)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> servant.init(new PathNode(servant.getOwner().position().add(0, 3, 0), 0, 0, 0)))
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );

    public static final DeferredItem<Item> StardustDragonStaff = Register.register("stardust_dragon_staff", () ->
            new StardustDragonWeaponItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );

    // 套装物品
    public static final DeferredItem<Item> HallowedHelmet = Register.register("hallowed_helmet", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedChestplate = Register.register("hallowed_chestplate", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedLeggings = Register.register("hallowed_leggings", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HallowedBoots = Register.register("hallowed_boots", () -> new ArmorItem(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(6)).rarity(Rarity.UNCOMMON)));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

    /** 星尘龙武器 - 处理多体节召唤 */
    private static class StardustDragonWeaponItem extends Item implements IServantWeapon<StardustDragon> {

        public StardustDragonWeaponItem(Properties props) {
            super(props);
        }

        @Override
        public ServantType<StardustDragon> getType() {
            return ServantRegister.StardustDragon.get();
        }

        @Override
        public StardustDragon getDummyServant() {
            return getType().factory().get();
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundRegister.UseServantWeapon.get();
        }

        @Override
        public void handleSummon(Player player) {
            EntityData data = player.getData(AttachmentRegister.EntityData);
            ServantType<StardustDragon> type = getType();

            // 查找现有体节
            List<StardustDragon> existing = data.getEntities().stream()
                    .filter(e -> e instanceof StardustDragon)
                    .map(e -> (StardustDragon) e)
                    .toList();

            if (existing.isEmpty()) {
                // 首次召唤：创建3个体节
                if (data.getUsedSlots() + 3 > data.getMaxServantSize(player)) return;

                Vec3 spawnPos = player.position().add(0, 3, 0);

                for (int i = 0; i < 3; i++) {
                    StardustDragon segment = type.factory().get();
                    segment.setOwner(player);
                    segment.setSegmentIndex(i);
                    segment.setTotalSegments(3);
                    if (data.summonServant(player, segment)) {
                        segment.init(new PathNode(spawnPos.subtract(0, 0, i * 0.4), 0, 0, 0));
                    }
                }
            } else {
                // 增加体节
                if (data.getUsedSlots() + 1 > data.getMaxServantSize(player)) return;

                int maxIndex = existing.stream()
                        .mapToInt(StardustDragon::getSegmentIndex)
                        .max()
                        .orElse(0);

                StardustDragon newSegment = type.factory().get();
                newSegment.setOwner(player);
                newSegment.setSegmentIndex(maxIndex + 1);

                if (data.summonServant(player, newSegment)) {
                    Vec3 lastPos = existing.stream()
                            .filter(e -> e.getSegmentIndex() == maxIndex)
                            .findFirst()
                            .map(StardustDragon::getPos)
                            .orElse(player.position().add(0, 3, 0));
                    newSegment.init(new PathNode(lastPos, 0, 0, 0));
                }
            }
        }

        @Override
        public void remove(Player player) {
            player.getData(AttachmentRegister.EntityData).removeServant(getType());
        }

    }

}
