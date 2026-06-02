package first.servantry.common.recipe;

import first.servantry.Servantry;
import first.servantry.register.MithrilAnvilRecipeRegister;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MithrilAnvilRecipe(List<Ingredient> ingredients, List<Integer> counts, ItemStack result) implements Recipe<CraftingInput> {

    public static final int MAX_INGREDIENTS = 5;

    private static int countInInventory(Player player, Ingredient ingredient) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    // ==================== Recipe 接口 ====================

    public static Builder builder() {
        return new Builder();
    }

    public int count(int index) {
        return counts.get(index);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        List<ItemStack> nonEmpty = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) nonEmpty.add(stack.copy());
        }
        return matchesItems(nonEmpty);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return MithrilAnvilRecipeRegister.MITHRIL_ANVIL_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<MithrilAnvilRecipe> getType() {
        return MithrilAnvilRecipeRegister.MITHRIL_ANVIL_TYPE.get();
    }

    // ==================== 业务逻辑 ====================

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    public boolean canCraft(Player player) {
        for (int i = 0; i < ingredients.size(); i++) {
            if (countInInventory(player, ingredients.get(i)) < counts.get(i)) {
                return false;
            }
        }
        return true;
    }

    public void consumeIngredients(Player player) {
        for (int i = 0; i < ingredients.size(); i++) {
            int remaining = counts.get(i);
            Ingredient ingredient = ingredients.get(i);
            for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    int toTake = Math.min(stack.getCount(), remaining);
                    stack.shrink(toTake);
                    remaining -= toTake;
                }
            }
        }
    }

    public boolean hasEnough(Player player, int index) {
        return countInInventory(player, ingredients.get(index)) >= counts.get(index);
    }

    // ==================== Builder ====================

    private boolean matchesItems(List<ItemStack> available) {
        for (int i = 0; i < ingredients.size(); i++) {
            int remaining = counts.get(i);
            Ingredient ingredient = ingredients.get(i);
            for (ItemStack stack : available) {
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    int toMatch = Math.min(stack.getCount(), remaining);
                    stack.shrink(toMatch);
                    remaining -= toMatch;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    public static final class Builder {
        private final List<Ingredient> ingredients = new ArrayList<>();
        private final List<Integer> counts = new ArrayList<>();
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
        private ItemStack result = ItemStack.EMPTY;

        private Builder() {
        }

        public Builder ingredient(ItemLike item) {
            return ingredient(Ingredient.of(item), 1);
        }

        public Builder ingredient(ItemLike item, int count) {
            return ingredient(Ingredient.of(item), count);
        }

        public Builder ingredient(Ingredient ingredient, int count) {
            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i).equals(ingredient)) {
                    counts.set(i, counts.get(i) + count);
                    return this;
                }
            }
            if (ingredients.size() >= MAX_INGREDIENTS) {
                throw new IllegalStateException("Max " + MAX_INGREDIENTS + " ingredients");
            }
            this.ingredients.add(ingredient);
            this.counts.add(count);
            return this;
        }

        public Builder result(ItemLike item) {
            this.result = new ItemStack(item.asItem());
            return this;
        }

        public Builder result(ItemLike item, int count) {
            this.result = new ItemStack(item.asItem(), count);
            return this;
        }

        public Builder result(ItemStack result) {
            this.result = result;
            return this;
        }

        public void save(RecipeOutput output) {
            save(output, Servantry.rl("mithril_anvil/" + BuiltInRegistries.ITEM.getKey(result.getItem()).getPath()));
        }

        public void save(RecipeOutput output, ResourceLocation id) {
            MithrilAnvilRecipe recipe = new MithrilAnvilRecipe(List.copyOf(ingredients), List.copyOf(counts), result);
            // 自动生成解锁条件：获得任意材料即可解锁
            if (criteria.isEmpty()) {
                for (int i = 0; i < ingredients.size(); i++) {
                    ItemStack[] items = ingredients.get(i).getItems();
                    if (items.length > 0) {
                        criteria.put("has_ingredient_" + i, InventoryChangeTrigger.TriggerInstance.hasItems(items[0].getItem()));
                    }
                }
            }
            Advancement.Builder advancement = output.advancement();
            this.criteria.forEach(advancement::addCriterion);
            AdvancementHolder advancementHolder = advancement.build(id.withPrefix("recipes/"));
            output.accept(id, recipe, advancementHolder);
        }
    }
}
