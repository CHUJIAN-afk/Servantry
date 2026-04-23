package first.servantry.api.core;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living) {
            living.getData(AttachmentRegister.InvincibleData).tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        // 更新目标缓存（仅服务端）
        if (!player.level().isClientSide()) {
            player.getData(AttachmentRegister.TargetCache).update(player);
        }
        EntityData data = player.getData(AttachmentRegister.EntityData);
        List<Servant> servants = data.getServants();
        while (!servants.isEmpty() && data.getMaxServantSize(player) < servants.size()) {
            Servant toRemove = servants.getFirst();
            data.remove(toRemove.getUuid());
            servants = data.getServants();
        }
        // 统一tick所有实体
        data.tickAll(player);
        // 同步数据
        if (!player.level().isClientSide() && (!servants.isEmpty() || !data.getProjectiles().isEmpty() || data.isChanged())) {
            data.setChanged(false);
            player.syncData(AttachmentRegister.EntityData);
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
                EntityData data = player.getData(AttachmentRegister.EntityData);
                data.removeServant(iServantWeapon.getType());
                data.setChanged(true);
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
