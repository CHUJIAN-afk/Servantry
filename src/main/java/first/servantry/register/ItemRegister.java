package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.PathNode;
import first.servantry.api.item.IServantWeapon;
import first.servantry.common.attachment.ServantData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    public static final DeferredItem<Item> TerraPrismRenderItem = Register.registerItem("terra_prism_render_item", Item::new);
    public static final DeferredItem<Item> TerraPrism = Register.register("terraprism", () ->
            new IServantWeapon.Builder<>(ServantRegister.TerraPrism)
                    .damage(9f)
                    .sound(SoundRegister.UseTerraprism::get)
                    .onSummon(servant -> {
                        Player owner = servant.getOwner();
                        ServantData data = owner.getData(AttachmentRegister.ServantData);
                        PathNode node = servant.getIdleState(owner, data.getOrder(servant), data.getSameSize(servant));
                        servant.setPath(List.of(node));
                    })
                    .buildItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
    );

    public static final DeferredItem<Item> EnchantedThrowingKnives = Register.registerItem("enchanted_throwing_knives_render_item", Item::new);
    public static final DeferredItem<Item> BladeStaff = Register.register("blade_staff", () ->
            new IServantWeapon.Builder<>(ServantRegister.EnchantedThrowingKnives)
                    .damage(0.6f)
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
                    .damage(3.5f)
                    .sound(SoundRegister.UseServantWeapon)
                    .onSummon(servant -> {
                        Player owner = servant.getOwner();
                        servant.setPath(List.of(new PathNode("", owner.position(), owner.yBodyRot, 0, 0)));
                    })
                    .buildItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}