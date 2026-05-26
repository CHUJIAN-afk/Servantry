package first.servantry.api.armorSet;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.*;

public record ArmorSet(ResourceLocation id, List<DeferredItem<Item>> items, Multimap<Holder<Attribute>, AttributeModifier> modifiers) {

    public static final Map<UUID, Map<ArmorSet, Boolean>> CACHE = new HashMap<>();

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public boolean full(Player player) {
        return CACHE.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).computeIfAbsent(this, k -> {
            Iterable<ItemStack> armorSlots = player.getArmorSlots();
            List<Item> target = new ArrayList<>();
            for (DeferredItem<Item> item : items) {
                target.add(item.get());
            }
            for (ItemStack armor : armorSlots) {
                target.remove(armor.getItem());
                if (target.isEmpty()) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ArmorSet armorSet) {
            return armorSet.id.equals(id);
        }
        return false;
    }

    public static final class Builder {

        private final ResourceLocation id;
        private final List<DeferredItem<Item>> items = new java.util.ArrayList<>();
        private final ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> modifiers = ImmutableMultimap.builder();

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder piece(DeferredItem<Item> item) {
            this.items.add(item);
            return this;
        }

        public Builder modifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
            this.modifiers.put(attribute, new AttributeModifier(id, amount, operation));
            return this;
        }

        public ArmorSet build() {
            return new ArmorSet(id, List.copyOf(items), modifiers.build());
        }
    }
}
