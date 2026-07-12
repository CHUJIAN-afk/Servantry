package first.servantry.compat.jei.category;

import first.servantry.Servantry;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.register.ServantryItemRegister;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MithrilAnvilRecipeCategory implements IRecipeCategory<MithrilAnvilRecipe> {

    public static final RecipeType<MithrilAnvilRecipe> SoulRecipeType = new RecipeType<>(Servantry.rl("mithril_anvil"), MithrilAnvilRecipe.class);

    private final IGuiHelper helper;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public MithrilAnvilRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createBlankDrawable(140, 20);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ServantryItemRegister.MithrilAnvil.asItem()));
        this.localizedName = Component.translatable("container.servantry.mithril_anvil");
    }

    @Override
    public @NotNull RecipeType<MithrilAnvilRecipe> getRecipeType() {
        return SoulRecipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return localizedName;
    }

    @Override
    public void draw(@NotNull MithrilAnvilRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        for (int i = 0; i < 7; i++) {
            int xOffset = 1 + i * 18;
            int yOffset = 1;
            if (i < recipe.ingredients().size()) {
                helper.getSlotDrawable().draw(guiGraphics, xOffset, yOffset);
            }
            if (i == 5) {
                xOffset += 4;
                helper.getRecipeArrow().draw(guiGraphics, xOffset, yOffset);
            }
            if (i == 6) {
                xOffset += 12;
                helper.getSlotDrawable().draw(guiGraphics, xOffset, yOffset);
            }
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull MithrilAnvilRecipe recipe, @NotNull IFocusGroup focuses) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < 5; i++) {
            int xOffset = 2 + i * 18;
            int yOffset = 2;
            if (i < ingredients.size()) {
                Ingredient ingredient = ingredients.get(i);
                int count = recipe.count(i);
                ItemStack[] items = ingredient.getItems();
                for (ItemStack item : items) {
                    item.setCount(count);
                    builder.addSlot(RecipeIngredientRole.INPUT, xOffset, yOffset).addItemStack(item);
                }
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 2).addItemStack(recipe.result());
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }
}