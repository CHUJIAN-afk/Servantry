package first.servantry.api.core;

import first.servantry.Servantry;
import first.servantry.api.armorSet.ArmorSet;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import first.servantry.utils.AttributeUtils;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void projectileClear(EntityTravelToDimensionEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            EntityData data = player.getData(AttachmentRegister.EntityData);
            for (Projectile projectile : data.get(EntityData.Type.Projectile, Projectile.class)) {
                projectile.setRemove();
            }
            for (Servant projectile : data.get(EntityData.Type.SentryServant, Servant.class)) {
                projectile.setRemove();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void InvincibleDataTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living) {
            living.getData(AttachmentRegister.InvincibleData).tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void armorSetAttribute(LivingEquipmentChangeEvent event) {
        LivingEntity living = event.getEntity();
        if (living instanceof Player player) {
            Map<ArmorSet, Boolean> cache = ArmorSet.CACHE.get(event.getEntity().getUUID());
            Map<ArmorSet, Boolean> lookup = new HashMap<>();
            if (cache != null && !cache.isEmpty()) {
                lookup.putAll(cache);
                cache.clear();
            }
            List<ArmorSet> list = ServantryRegistries.ARMOR_SETS.stream().toList();
            for (ArmorSet armorSet : list) {
                boolean full = armorSet.full(player);
                if (full) {
                    if ((!lookup.containsKey(armorSet) || !lookup.get(armorSet))) {
                        //首次生效时调用
                        armorSet.onStart().accept(player);
                    }
                } else {
                    if (lookup.containsKey(armorSet) && lookup.get(armorSet)) {
                        //失效时调用
                        armorSet.onRemove().accept(player);
                    }
                }
                if (!player.level().isClientSide()) {
                    Collection<Map.Entry<Holder<Attribute>, AttributeModifier>> entries = armorSet.modifiers().entries();
                    for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : entries) {
                        Holder<Attribute> key = entry.getKey();
                        AttributeModifier value = entry.getValue();
                        AttributeUtils.condition(player, key, value.id(), value.amount(), value.operation(), full);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityDataTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            player.getData(AttachmentRegister.TargetCache).update(player);
        }
        player.getData(AttachmentRegister.EntityData).tick(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void summonServant(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();
        Level level = player.level();
        ItemCooldowns cooldowns = player.getCooldowns();
        if (event.getHand() == InteractionHand.MAIN_HAND && !cooldowns.isOnCooldown(itemStack.getItem()) && itemStack.getItem() instanceof IServantWeapon<?> iServantWeapon) {
            cooldowns.addCooldown(itemStack.getItem(), 4);
            player.swing(InteractionHand.MAIN_HAND, true);
            if (!level.isClientSide()) {
                if (!player.isShiftKeyDown()) {
                    iServantWeapon.summon(player);
                } else {
                    iServantWeapon.remove(player);
                }
                SoundEvent soundEvent = iServantWeapon.getSoundEvent();
                if (soundEvent != null) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, player.getSoundSource());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addAttribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            event.add(type, AttributeRegister.HealthRegen);
        }
        event.add(EntityType.PLAYER, AttributeRegister.ServantMaxCount);
        event.add(EntityType.PLAYER, AttributeRegister.SentryServantMaxCount);
        event.add(EntityType.PLAYER, AttributeRegister.ServantDamage);
        event.add(EntityType.PLAYER, AttributeRegister.ServantKnockback);
        event.add(EntityType.PLAYER, AttributeRegister.ServantArmorPierce);
        event.add(EntityType.PLAYER, AttributeRegister.ServantSearchRange);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hurtHistory(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        if (damageSource.getEntity() instanceof Player attacker) {
            event.getEntity().getData(AttachmentRegister.InvincibleData).getHurtHistory().put(attacker.getUUID(), new AtomicInteger(100));
        }
    }
}
