package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.event.ServantIncomingDamageEvent;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servant.StardustCell;
import first.servantry.common.servant.goal.StardustCellAttackGoal;
import first.servantry.mixin.MobEffectInstanceAccessor;
import first.servantry.register.ArmorMaterialRegister;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import first.servantry.register.ItemRegister;
import first.servantry.utils.ArmorSetUtil;
import first.servantry.utils.AttributeUtils;
import first.servantry.utils.CuriosUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import first.servantry.register.PotionRegister;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Map;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        // 着魔药水：粗制的药水 + 凋零玫瑰
        event.getBuilder().addMix(Potions.AWKWARD, Items.WITHER_ROSE, PotionRegister.Obsession);
        // 长效着魔药水：着魔药水 + 红石
        event.getBuilder().addMix(PotionRegister.Obsession, Items.REDSTONE, PotionRegister.LongObsession);
        // 强效着魔药水：着魔药水 + 荧石
        event.getBuilder().addMix(PotionRegister.Obsession, Items.GLOWSTONE_DUST, PotionRegister.StrongObsession);
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CLERIC) {
            // 等级1（新手）添加四件饰品交易
            event.getTrades().get(1).add(new BasicItemListing(
                    20,
                    ItemRegister.ApprenticesScarf.get().getDefaultInstance(),
                    8,
                    10
            ));
            event.getTrades().get(1).add(new BasicItemListing(
                    20,
                    ItemRegister.HuntressesBuckler.get().getDefaultInstance(),
                    8,
                    10
            ));
            event.getTrades().get(1).add(new BasicItemListing(
                    20,
                    ItemRegister.MonksBelt.get().getDefaultInstance(),
                    8,
                    10
            ));
            event.getTrades().get(1).add(new BasicItemListing(
                    20,
                    ItemRegister.SquiresShield.get().getDefaultInstance(),
                    8,
                    10
            ));
            // 等级3（老手）添加矮人项链交易：25绿宝石 + 1骷髅头
            event.getTrades().get(3).add(new BasicItemListing(
                    new ItemStack(Items.EMERALD, 25),
                    new ItemStack(Items.SKELETON_SKULL),
                    ItemRegister.PygmyNecklace.get().getDefaultInstance(),
                    1,
                    30,
                    1.0f
            ));
            // 等级4（专家）添加大力士甲虫交易：30绿宝石
            event.getTrades().get(4).add(new BasicItemListing(
                    30,
                    ItemRegister.HerculesBeetle.get().getDefaultInstance(),
                    1,
                    40
            ));
        }
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        Map<Item, Boolean> cache = CuriosUtil.CACHE.get(event.getEntity().getUUID());
        if (cache != null && !cache.isEmpty()) {
            cache.clear();
        }
    }

    @SubscribeEvent
    public static void onServantIncomingDamage(ServantIncomingDamageEvent event) {
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
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity living = event.getEntity();
        if (living instanceof Player player && !player.level().isClientSide()) {
            AttributeUtils.condition(player, AttributeRegister.ServantMaxCount, Servantry.rl("hallowed_set_servant_max_count"), 2, AttributeModifier.Operation.ADD_VALUE, ArmorSetUtil.hasFullSet(player, ArmorMaterialRegister.HallowedArmorMaterial));
            AttributeUtils.condition(player, AttributeRegister.ServantDamage, Servantry.rl("hallowed_set_servant_damage"), 0.15, AttributeModifier.Operation.ADD_VALUE, ArmorSetUtil.hasFullSet(player, ArmorMaterialRegister.HallowedArmorMaterial));
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
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
    public static void onLivingDamageEventPost(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        if (damageSource instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof StardustCell) {
            return;
        }
        if (damageSource.getEntity() instanceof Player player && !player.level().isClientSide()) {
            LivingEntity target = event.getEntity();
            if (target.isAlive()) {
                EntityData entityData = player.getData(AttachmentRegister.EntityData);
                for (Servant servant : entityData.getServants()) {
                    if (servant instanceof StardustCell cell && cell.getExtraShootCooldown() <= 0 && player.getRandom().nextFloat() < 0.33f) {
                        Vec3 startPos = servant.getPos();
                        StardustProjectile newProjectile = new StardustProjectile(player.getUUID(), servant.getUuid(), startPos, target);
                        newProjectile.life = 10;
                        entityData.addProjectile(newProjectile);
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
