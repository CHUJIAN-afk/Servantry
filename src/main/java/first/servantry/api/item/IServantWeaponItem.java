package first.servantry.api.item;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.api.servant.Servant;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantryAttributeRegister;
import first.servantry.register.ServantryDataComponentRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 仆从武器接口，定义可召唤仆从的武器物品行为。
 */
public interface IServantWeaponItem<T extends Servant> {

    /**
     * 获取此武器对应的仆从类型。
     */
    @NotNull AttachmentEntityType<T> getType();

    /**
     * 是否是哨兵。
     */
    boolean isSentryServant();

    /**
     * 处理仆从召唤逻辑。
     */
    void summon(@NotNull Player player, @Nullable ItemStack itemStack);

    /**
     * 获取仆从伤害值。
     */
    float getServantDamage(@Nullable Player player, @Nullable ItemStack itemStack);

    /**
     * 获取仆从击退力度。
     */
    float getServantKnockback(@Nullable Player player, @Nullable ItemStack itemStack);

    /**
     * 获取仆从护甲穿透。
     */
    float getServantArmorPierce(@Nullable Player player, @Nullable ItemStack itemStack);

    default List<Component> getTooltips(ItemStack itemStack, Player player) {
        List<Component> toolTips = new ArrayList<>();
        AttachmentEntityType<?> type = getType();
        ResourceLocation location = ServantryRegistries.ATTACHMENT_ENTITY_TYPES.getKey(type);
        if (location != null) {
            String key = "servant." + location.getNamespace() + "." + location.getPath();
            float damage = getServantDamage(player, itemStack);
            if (damage > 0) {
                AttributeInstance attribute = player.getAttribute(ServantryAttributeRegister.ServantDamage);
                damage = attribute != null ? (float) (damage * attribute.getValue()) : damage;
                toolTips.add(Component.literal(String.format("%.1f ", damage)).withStyle(ChatFormatting.BLUE).append(Component.translatable("item.servantry.tooltip.damage").withStyle(ChatFormatting.GRAY)));
            }
            float knockback = getServantKnockback(player, itemStack);
            if (knockback > 0) {
                AttributeInstance attribute = player.getAttribute(ServantryAttributeRegister.ServantKnockback);
                knockback = attribute != null ? (float) (knockback * attribute.getValue()) : knockback;
                toolTips.add(Component.literal(String.format("%.1f ", knockback)).withStyle(ChatFormatting.BLUE).append(Component.translatable("item.servantry.tooltip.knockback").withStyle(ChatFormatting.GRAY)));
            }
            float armor_pierce = getServantArmorPierce(player, itemStack);
            if (armor_pierce > 0) {
                toolTips.add(Component.literal(String.format("%.1f ", armor_pierce)).withStyle(ChatFormatting.BLUE).append(Component.translatable("item.servantry.tooltip.armor_pierce").withStyle(ChatFormatting.GRAY)));
            }
            if (type == ServantryAttachmentEntityRegister.INFINITE_SHADOW.get()){
                ScabbardContainer container = itemStack.getComponents().getOrDefault(ServantryDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                if (!container.isEmpty()) {
                    toolTips.add(Component.translatable("item.servantry.tooltip.summon", container.itemStack().getDisplayName()).withStyle(ChatFormatting.GRAY));
                }
            } else {
                toolTips.add(Component.translatable("item.servantry.tooltip.summon", Component.translatable(key).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
            }
            ServantryHelper servantryHelper = ServantryHelper.get(player);
            if (!isSentryServant()){
                toolTips.add(Component.translatable("item.servantry.tooltip.servant_slots", Component.literal(String.valueOf(servantryHelper.getUsedSlots(EntityData.Type.Servant))).withStyle(ChatFormatting.BLUE), Component.literal(String.valueOf(servantryHelper.getMaxCount(EntityData.Type.Servant))).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
            } else {
                toolTips.add(Component.translatable("item.servantry.tooltip.sentry_servant_slots", Component.literal(String.valueOf(servantryHelper.getUsedSlots(EntityData.Type.SentryServant))).withStyle(ChatFormatting.BLUE), Component.literal(String.valueOf(servantryHelper.getMaxCount(EntityData.Type.SentryServant))).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
            }
            toolTips.add(Component.translatable("item.servantry.tooltip.remove_all").withStyle(ChatFormatting.GRAY));
        }
        return toolTips;
    }

    /** 获取召唤时播放的音效。 */
    default SoundEvent getSoundEvent() {
        return null;
    }

    /**
     * 构建一个已初始化属性的仆从实例。
     */
    default T createServant(@NotNull Player player, @Nullable ItemStack itemStack) {
        T servant = getType().factory().get();
        servant.setOwner(player);
        servant.setDamage(getServantDamage(player, itemStack));
        servant.setKnockback(getServantKnockback(player, itemStack));
        servant.setArmorPierce(getServantArmorPierce(player, itemStack));
        return servant;
    }

    /**
     * 移除玩家拥有的此类型仆从。
     */
    default void remove(@NotNull Player player) {
        EntityData entityData = ServantryHelper.get(player).getEntityData();
        entityData.remove(isSentryServant() ? EntityData.Type.SentryServant : EntityData.Type.Servant, getType());
    }
}
