package first.servantry.api.mithrilAnvil;

import first.servantry.register.MithrilAnvilRecipeRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * 适配 MithrilAnvilRecipe 到原版 Recipe 接口，使配方书可识别
 */
public class MithrilAnvilCraftingRecipe implements Recipe<CraftingInput> {

    private final MithrilAnvilRecipe inner;
    private final NonNullList<Ingredient> adaptedIngredients;

    public MithrilAnvilCraftingRecipe(MithrilAnvilRecipe inner) {
        this.inner = inner;
        this.adaptedIngredients = NonNullList.create();
        for (MithrilAnvilRecipe.IngredientWithCount iwc : inner.ingredients()) {
            adaptedIngredients.add(iwc.ingredient());
        }
    }

    public MithrilAnvilRecipe inner() {
        return inner;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        SimpleContainerView view = new SimpleContainerView(input);
        return inner.matches(view);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return inner.result().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 5 && height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return inner.result();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return MithrilAnvilRecipeRegister.MITHRIL_ANVIL_SERIALIZER.get();
    }

    @Override
    public RecipeType<MithrilAnvilCraftingRecipe> getType() {
        return MithrilAnvilRecipeRegister.MITHRIL_ANVIL_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return adaptedIngredients;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    /**
     * 简易 Container 适配器，将 CraftingInput 当作 Container 使用
     */
    private record SimpleContainerView(CraftingInput input) implements Container {

        @Override
        public int getContainerSize() {
            return input.size();
        }

        @Override
        public boolean isEmpty() {
            return input.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return input.getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}