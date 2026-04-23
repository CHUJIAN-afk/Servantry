package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.event.ServantIncomingDamageEvent;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.api.common.attachment.ProjectileData;
import first.servantry.api.common.attachment.ServantData;
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
    public static void servantDamage(ServantIncomingDamageEvent event) {
        Player owner = event.getSource().getServant().getOwner();
        if (!owner.level().isClientSide() && ArmorSetUtil.hasFullSet(owner, ArmorMaterialRegister.HallowedArmorMaterial)) {
            LivingEntity target = event.getEntity();
            MobEffectInstance instance = target.getEffect(MobEffects.GLOWING);
            if (instance == null) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 59));
            } else {
                MobEffectInstanceAccessor accessor = (MobEffectInstanceAccessor) instance;
                accessor.setDuration(59);
            }
        }
    }

    @SubscribeEvent
    public static void equipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity living = event.getEntity();
        if (living instanceof Player player && !player.level().isClientSide()) {
            AttributeUtils.condition(player, AttributeRegister.ServantMaxCount, Servantry.rl("hallowed_set_servant_max_count"), 2, AttributeModifier.Operation.ADD_VALUE, ArmorSetUtil.hasFullSet(player, ArmorMaterialRegister.HallowedArmorMaterial));
            AttributeUtils.condition(player, AttributeRegister.ServantDamage, Servantry.rl("hallowed_set_servant_damage"), 0.15, AttributeModifier.Operation.ADD_VALUE, ArmorSetUtil.hasFullSet(player, ArmorMaterialRegister.HallowedArmorMaterial));
        }
    }

    @SubscribeEvent
    public static void itemModify(ItemAttributeModifierEvent event) {
        Item item = event.getItemStack().getItem();
        // 头部：+2 仆从栏，+25% 仆从伤害
        if (item == ItemRegister.HallowedHelmet.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_helmet_count"), 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_helmet_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HEAD);
        }
        // 胸甲：+3 仆从栏，+25% 仆从伤害
        if (item == ItemRegister.HallowedChestplate.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_chestplate_count"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_chestplate_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.CHEST);
        }
        // 护腿：+1 仆从栏，+25% 仆从伤害
        if (item == ItemRegister.HallowedLeggings.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_leggings_count"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.LEGS);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_leggings_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.LEGS);
        }
        // 鞋子：+1 仆从栏，+25% 仆从伤害，+15% 移动速度
        if (item == ItemRegister.HallowedBoots.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_boots_count"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_boots_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.FEET);
            event.addModifier(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_boots_speed"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.FEET);
        }
    }
    
    /**
     * 玩家攻击联动事件 - 每个星细胞仆从有33%概率向被攻击目标发射新射弹。
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        if (damageSource instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof StardustCell) {
            return;
        }
        if (damageSource.getEntity() instanceof Player player && !player.level().isClientSide()) {
            LivingEntity target = event.getEntity();
            if (target.isAlive()) {
                ServantData servantData = player.getData(AttachmentRegister.ServantData);
                ProjectileData projectileData = player.getData(AttachmentRegister.ProjectileData);
                for (Servant servant : servantData.getServants()) {
                    if (servant instanceof StardustCell cell && cell.getExtraShootCooldown() <= 0 && player.getRandom().nextFloat() < 0.33f) {
                        Vec3 startPos = servant.getPos();
                        StardustProjectile newProjectile = new StardustProjectile(player.getUUID(), servant.getUuid(), startPos, target);
                        newProjectile.life = 10;
                        projectileData.add(newProjectile);
                        cell.setExtraShootCooldown(14);
                        // 后坐力
                        Vec3 direction = target.getBoundingBox().getCenter().subtract(startPos).normalize();
                        StardustCellAttackGoal.spawnShootParticles((ServerLevel) player.level(), startPos, direction);
                        cell.applyForce(direction.scale(-0.5));
                    }
                }
            }
        }
    }

}
