package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.register.AttributeRegister;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent
    public static void summon(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            Item item = itemStack.getItem();
            if (item instanceof IServantWeapon<?> iServantWeapon) {
                Player player = event.getEntity();
                if (!event.getLevel().isClientSide()) {
                    if (!player.isShiftKeyDown()) {
                        IServantWeapon.handleSummon(player, iServantWeapon);
                    } else {
                        iServantWeapon.remove(player);
                    }
                    player.swing(InteractionHand.MAIN_HAND, true);
                }
            }
        }
    }


    @SubscribeEvent
    public static void register(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AttributeRegister.ServantMaxCount);
        event.add(EntityType.PLAYER, AttributeRegister.ServantDamage);
        event.add(EntityType.PLAYER, AttributeRegister.ServantSpeed);
    }

}
