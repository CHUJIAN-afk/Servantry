package first.servantry.common.menu;

import first.servantry.api.mithrilAnvil.MithrilAnvilCraftingRecipe;
import first.servantry.client.screen.MithrilAnvilGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MithrilAnvilPlaceRecipeHandler {

    public static void handleRecipeClick(ServerPlayer player, AbstractContainerMenu menu,
                                         RecipeHolder<?> holder, boolean craftAll) {
        if (!(holder.value() instanceof MithrilAnvilCraftingRecipe craftingRecipe)) return;
        if (!(menu instanceof MithrilAnvilGui.MithrilAnvilMenu mithrilMenu)) return;
        if (!mithrilMenu.stillValid(player)) return;
        // Just set the selected recipe on the menu — materials are shown as ghost on client,
        // actual consumption happens when player takes the result item
        mithrilMenu.setSelectedRecipe((RecipeHolder<MithrilAnvilCraftingRecipe>) holder);
    }
}
