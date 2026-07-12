package first.servantry.api.common;

import first.servantry.Servantry;
import first.servantry.api.armorSet.ArmorSet;
import first.servantry.api.builder.ServantWeaponItemBuilder;
import first.servantry.api.common.attachment.BatchedParticlesData;
import first.servantry.api.common.attachment.DamageInfoData;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.register.ServantryAttributeRegister;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(LevelTickEvent.Post event) {
        BatchedParticlesData.handler(event);
        DamageInfoData.handler(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handler(LivingEquipmentChangeEvent event) {
        ArmorSet.handler(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handler(PlayerInteractEvent.RightClickItem event) {
        ServantWeaponItemBuilder.handler(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handler(EntityAttributeModificationEvent event) {
        ServantryAttributeRegister.handler(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handler(LivingDamageEvent.Post event) {
        InvincibleData.handler(event);
        DamageInfoData.handler(event);
    }
}
