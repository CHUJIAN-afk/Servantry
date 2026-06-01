package first.servantry.api.mithrilAnvil;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public record MithrilAnvilRecipe(List<IngredientWithCount> ingredients, ItemStack result) {

    public static final int MAX_INGREDIENTS = 5;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 无序匹配：检查容器中非空物品是否满足所有材料需求
     */
    public boolean matches(Container container) {
        List<ItemStack> nonEmpty = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                nonEmpty.add(stack.copy());
            }
        }
        for (IngredientWithCount iwc : ingredients) {
            int remaining = iwc.count();
            for (ItemStack stack : nonEmpty) {
                if (!stack.isEmpty() && iwc.ingredient().test(stack)) {
                    int toMatch = Math.min(stack.getCount(), remaining);
                    stack.shrink(toMatch);
                    remaining -= toMatch;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    /**
     * 从容器中消耗材料（匹配成功后调用）
     */
    public void consumeIngredients(Container container) {
        for (IngredientWithCount iwc : ingredients) {
            int remaining = iwc.count();
            for (int i = 0; i < container.getContainerSize() && remaining > 0; i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty() && iwc.ingredient().test(stack)) {
                    int toTake = Math.min(stack.getCount(), remaining);
                    stack.shrink(toTake);
                    remaining -= toTake;
                }
            }
        }
    }

    public boolean canCraft(Player player) {
        for (IngredientWithCount ingredient : ingredients) {
            if (!ingredient.hasEnough(player)) {
                return false;
            }
        }
        return true;
    }

    public void consumeIngredients(Player player) {
        for (IngredientWithCount ingredient : ingredients) {
            ingredient.consume(player);
        }
    }

    public record IngredientWithCount(Ingredient ingredient, int count) {

        public boolean hasEnough(Player player) {
            return countInInventory(player) >= count;
        }

        public int countInInventory(Player player) {
            int total = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    total += stack.getCount();
                }
            }
            return total;
        }

        public void consume(Player player) {
            int remaining = count;
            for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    int toTake = Math.min(stack.getCount(), remaining);
                    stack.shrink(toTake);
                    remaining -= toTake;
                }
            }
        }
    }

    public static final class Builder {

        private final List<IngredientWithCount> ingredients = new ArrayList<>();
        private ItemStack result = ItemStack.EMPTY;

        private Builder() {
        }

        public Builder ingredient(Item item, int count) {
            if (ingredients.size() >= MAX_INGREDIENTS)
                throw new IllegalStateException("Max " + MAX_INGREDIENTS + " ingredients");
            this.ingredients.add(new IngredientWithCount(Ingredient.of(item), count));
            return this;
        }

        public Builder ingredient(Ingredient ingredient, int count) {
            if (ingredients.size() >= MAX_INGREDIENTS)
                throw new IllegalStateException("Max " + MAX_INGREDIENTS + " ingredients");
            this.ingredients.add(new IngredientWithCount(ingredient, count));
            return this;
        }

        public Builder result(Item item, int count) {
            this.result = new ItemStack(item, count);
            return this;
        }

        public Builder result(ItemStack result) {
            this.result = result;
            return this;
        }

        public MithrilAnvilRecipe build() {
            return new MithrilAnvilRecipe(List.copyOf(ingredients), result);
        }
    }
}
