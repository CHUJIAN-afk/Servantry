package first.servantry.api.core;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.ProjectileData;
import first.servantry.api.common.attachment.ServantData;
import first.servantry.api.event.ServantIncomingDamageEvent;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servent.StardustCell;
import first.servantry.common.servent.goal.StardustCellAttackGoal;
import first.servantry.mixin.MobEffectInstanceAccessor;
import first.servantry.register.ArmorMaterialRegister;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import first.servantry.register.ItemRegister;
import first.servantry.utils.ArmorSetUtil;
import first.servantry.utils.AttributeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
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

        // 射弹tick（使用副本遍历避免并发修改异常）
        ProjectileData projectileData = player.getData(AttachmentRegister.ProjectileData);
        List<Projectile> projectiles = new ArrayList<>(projectileData.getProjectiles());
        if (!projectiles.isEmpty()) {
            for (Projectile projectile : projectiles) {
                projectile.tick(player);
            }
        }

        // 清理标记为移除的射弹
        projectileData.cleanupMarkedProjectiles();

        // 同步数据
        if (!player.level().isClientSide() && (!servants.isEmpty() || data.isChange())) {
            data.setChange(false);
            player.syncData(AttachmentRegister.ServantData);
        }
        if (!player.level().isClientSide() && (!projectileData.getProjectiles().isEmpty() || projectileData.isChanged())) {
            projectileData.setChanged(false);
            player.syncData(AttachmentRegister.ProjectileData);
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
