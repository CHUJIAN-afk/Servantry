package first.servantry.common.item;

import first.servantry.Servantry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class AttributeArmorItem extends ArmorItem {

    public AttributeArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    public static Builder builder(Holder<ArmorMaterial> material, Type type) {
        return new Builder(material, type);
    }

    public static class Builder {
        private final Holder<ArmorMaterial> material;
        private final Type type;
        private final Properties properties;
        ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.builder();

        private Builder(Holder<ArmorMaterial> material, Type type) {
            this.material = material;
            this.type = type;
            this.properties = new Properties().durability(type.getDurability(material.value().getDefense(type)))
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                    .component(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }

        public Builder modifier(Holder<Attribute> attribute, AttributeModifier modifier) {
            modifiers.add(attribute, modifier, EquipmentSlotGroup.bySlot(type.getSlot()));
            return this;
        }

        public Builder modifier(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
            return modifier(attribute, new AttributeModifier(Servantry.rl("armor_" + type.getName()), value, operation));
        }

        public Builder properties(Consumer<Properties> customizer) {
            customizer.accept(properties);
            return this;
        }

        public AttributeArmorItem build() {
            return new AttributeArmorItem(material, type, properties.attributes(modifiers.build()));
        }
    }
}
