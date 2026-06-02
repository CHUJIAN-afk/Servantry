package first.servantry.client.screen;

import first.servantry.common.recipe.MithrilAnvilRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class MithrilAnvilRecipeButton extends AbstractWidget {

    private RecipeHolder<MithrilAnvilRecipe> recipe;
    private boolean craftable;

    public MithrilAnvilRecipeButton() {
        super(0, 0, 25, 25, CommonComponents.EMPTY);
    }

    public void init(RecipeHolder<MithrilAnvilRecipe> recipe, boolean craftable) {
        this.recipe = recipe;
        this.craftable = craftable;
    }

    public RecipeHolder<MithrilAnvilRecipe> getRecipe() {
        return recipe;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (recipe != null && level != null) {
            ResourceLocation sprite = craftable ? ResourceLocation.withDefaultNamespace("recipe_book/slot_craftable") : ResourceLocation.withDefaultNamespace("recipe_book/slot_uncraftable");
            graphics.blitSprite(sprite, getX(), getY(), width, height);
            ItemStack result = recipe.value().getResultItem(level.registryAccess());
            graphics.renderFakeItem(result, getX() + 4, getY() + 4);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        ClientLevel level = Minecraft.getInstance().level;
        if (recipe != null && level != null) {
            ItemStack result = recipe.value().getResultItem(level.registryAccess());
            output.add(NarratedElementType.TITLE, Component.translatable("narration.recipe", result.getHoverName()));
        }
    }
}
