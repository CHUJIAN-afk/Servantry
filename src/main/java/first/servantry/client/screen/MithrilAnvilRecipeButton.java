package first.servantry.client.screen;

import first.servantry.api.mithrilAnvil.MithrilAnvilCraftingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MithrilAnvilRecipeButton extends AbstractWidget {

    private static final ResourceLocation SLOT_CRAFTABLE = ResourceLocation.withDefaultNamespace("recipe_book/slot_craftable");
    private static final ResourceLocation SLOT_UNCRAFTABLE = ResourceLocation.withDefaultNamespace("recipe_book/slot_uncraftable");

    private RecipeHolder<MithrilAnvilCraftingRecipe> recipe;
    private boolean craftable;

    public MithrilAnvilRecipeButton() {
        super(0, 0, 25, 25, CommonComponents.EMPTY);
    }

    public void init(RecipeHolder<MithrilAnvilCraftingRecipe> recipe, boolean craftable) {
        this.recipe = recipe;
        this.craftable = craftable;
    }

    public RecipeHolder<MithrilAnvilCraftingRecipe> getRecipe() {
        return recipe;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (recipe == null) return;
        graphics.blitSprite(craftable ? SLOT_CRAFTABLE : SLOT_UNCRAFTABLE, getX(), getY(), width, height);
        ItemStack result = recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess());
        graphics.renderFakeItem(result, getX() + 4, getY() + 4);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (recipe != null) {
            ItemStack result = recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess());
            output.add(NarratedElementType.TITLE, Component.translatable("narration.recipe", result.getHoverName()));
        }
    }
}
