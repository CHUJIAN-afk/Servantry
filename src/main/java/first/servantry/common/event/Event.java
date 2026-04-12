package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.Marker;
import first.servantry.api.ServantDamageSource;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.attachment.WhipData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent
    public static void attackEntity(AttackEntityEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IWhipWeapon) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void interactBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IWhipWeapon) {
            event.setCanceled(true);
        }
    }

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

        player.getData(AttachmentRegister.WhipData).tick(player);
        if (!player.level().isClientSide()) {
            player.syncData(AttachmentRegister.WhipData);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void ServantDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide() && source instanceof ServantDamageSource damageSource) {
            Servant servant = damageSource.getServant();
            Player owner = servant.getOwner();
            AttributeInstance instance = owner.getAttribute(AttributeRegister.ServantDamage);
            float scale = instance != null ? (float) instance.getValue() : 1;
            event.setAmount(event.getAmount() * scale);
            WhipData data = owner.getData(AttachmentRegister.WhipData);
            Marker activeMarker = data.getActiveMarker();
            if (activeMarker != null && data.getMarkedEntityId() == target.getId()) {
                event.setAmount(event.getAmount() + activeMarker.getExtraDamage());
                if (owner.getRandom().nextDouble() < activeMarker.getCritRate()) {
                    event.setAmount(event.getAmount() * 2);
                }
            }
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
                iServantWeapon.remove(player);
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
