package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.ServantDamageSource;
import first.servantry.api.event.ServantIncomingDamageEvent;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ServantData data = player.getData(AttachmentRegister.ServantData);
        List<Servant> servants = data.getServants();
        while (!servants.isEmpty() && data.getMaxSize(player) < servants.size()) {
            servants.removeFirst();
        }
        if (!servants.isEmpty()) {
            for (Servant servant : servants) {
                servant.setOwner(player);
                servant.tick();
            }
        }
        if (!player.level().isClientSide() && !servants.isEmpty() || data.isChange()) {
            data.setChange(false);
            player.syncData(AttachmentRegister.ServantData);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void summon(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();
        Level level = player.level();
        if (!level.isClientSide() && event.getHand() == InteractionHand.MAIN_HAND && itemStack.getItem() instanceof IServantWeapon<?> iServantWeapon) {
            if (!player.isShiftKeyDown()) {
                IServantWeapon.handleSummon(player, iServantWeapon);
            } else {
                ServantData data = player.getData(AttachmentRegister.ServantData);
                data.getServants().removeIf(servant -> servant.getType() == iServantWeapon.getType());
                data.setChange(true);
            }
            player.swing(InteractionHand.MAIN_HAND, true);
            SoundEvent soundEvent = iServantWeapon.getSoundEvent();
            if (soundEvent != null) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, player.getSoundSource());
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
