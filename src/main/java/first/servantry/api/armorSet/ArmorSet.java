package first.servantry.api.armorSet;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import first.servantry.dadageneeator.provider.ServantryLanguageProvider;
import first.servantry.register.Registers;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.*;
import java.util.function.Consumer;

public record ArmorSet(
        ResourceLocation id,
        List<DeferredItem<Item>> items,
        Multimap<Holder<Attribute>, AttributeModifier> modifiers,
        Consumer<Player> onStart,
        Consumer<Player> onRemove
) {

    public static final Map<UUID, Map<ArmorSet, Boolean>> CACHE = new WeakHashMap<>();

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public boolean full(Player player) {
        return CACHE.computeIfAbsent(player.getUUID(), k -> new WeakHashMap<>())
                    .computeIfAbsent(this, k -> {
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
        private Consumer<Player> onStart = player -> {
        };
        private Consumer<Player> onRemove = player -> {
        };

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

        public Builder onStart(Consumer<Player> onStart) {
            this.onStart = onStart;
            return this;
        }

        public Builder onRemove(Consumer<Player> oRemove) {
            this.onRemove = oRemove;
            return this;
        }

        public Builder tooltip(int index, String en, String zh) {
            String key = "servantry." + id.getNamespace() + "." + id.getPath() + ".set." + index;
            ServantryLanguageProvider.LangEntry langEntry = new ServantryLanguageProvider.LangEntry(key, en, zh);
            Registers.getInstance().getLanguageGenerate().add(langEntry);
            return this;
        }

        public ArmorSet build() {
            return new ArmorSet(id, List.copyOf(items), modifiers.build(), onStart, onRemove);
        }
    }
}
