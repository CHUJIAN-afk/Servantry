package first.servantry.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import first.servantry.Servantry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

@EventBusSubscriber(modid = Servantry.MODID)
public class AttributeArmorItem extends ArmorItem {

    private final Multimap<Holder<Attribute>, AttributeModifier> modifiers;

    public AttributeArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, Multimap<Holder<Attribute>, AttributeModifier> modifiers) {
        super(material, type, properties);
        this.modifiers = modifiers;
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        Item item = event.getItemStack().getItem();
        if (item instanceof AttributeArmorItem armorItem) {
            Collection<Map.Entry<Holder<Attribute>, AttributeModifier>> entries = armorItem.modifiers.entries();
            for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : entries) {
                Holder<Attribute> attributeHolder = entry.getKey();
                AttributeModifier attributeModifier = entry.getValue();
                event.addModifier(attributeHolder, attributeModifier, EquipmentSlotGroup.bySlot(armorItem.getEquipmentSlot()));
            }
        }
    }

    public static Builder builder(Holder<ArmorMaterial> material, Type type) {
        return new Builder(material, type);
    }

    public static class Builder {
        private final Holder<ArmorMaterial> material;
        private final Type type;
        private final Properties properties;
        private final ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> modifierBuilder = ImmutableMultimap.builder();

        private Builder(Holder<ArmorMaterial> material, Type type) {
            this.material = material;
            this.type = type;
            this.properties = new Properties().durability(type.getDurability(material.value().getDefense(type)));
        }

        public Builder modifier(Holder<Attribute> attribute, AttributeModifier modifier) {
            modifierBuilder.put(attribute, modifier);
            return this;
        }

        public Builder modifier(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
            return modifier(attribute, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "armor_" + type.getName()), value, operation));
        }

        public Builder properties(Consumer<Properties> customizer) {
            customizer.accept(properties);
            return this;
        }

        public AttributeArmorItem build() {
            return new AttributeArmorItem(material, type, properties, modifierBuilder.build());
        }
    }
}
