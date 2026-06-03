package first.servantry.compat;

import first.servantry.Servantry;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.compat.jei.MithrilAnvilRecipeCategory;
import first.servantry.register.CurioRegister;
import first.servantry.register.ItemRegister;
import first.servantry.register.MithrilAnvilRecipeRegister;
import first.servantry.register.ServantWeaponRegister;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return Servantry.rl("jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MithrilAnvilRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ItemRegister.MithrilAnvil.asItem()), MithrilAnvilRecipeCategory.SoulRecipeType);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level instanceof Level level) {
            List<MithrilAnvilRecipe> soulRecipeList = level.getRecipeManager().getAllRecipesFor(MithrilAnvilRecipeRegister.MITHRIL_ANVIL_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();
            registration.addRecipes(MithrilAnvilRecipeCategory.SoulRecipeType, soulRecipeList);
        }
        registration.addIngredientInfo(ServantWeaponRegister.TerraPrism.get(), Component.translatable("jei.servantry.description.terraprism"));
        // 牧师出售
        registration.addIngredientInfo(CurioRegister.PygmyNecklace.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(CurioRegister.HerculesBeetle.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(CurioRegister.ApprenticesScarf.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(CurioRegister.HuntressesBuckler.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(CurioRegister.MonksBelt.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(CurioRegister.SquiresShield.get(), Component.translatable("jei.servantry.description.sold"));
        // 掉落
        registration.addIngredientInfo(CurioRegister.SummonerEmblem.get(), Component.translatable("jei.servantry.description.drops_from_evokers"));
        registration.addIngredientInfo(ItemRegister.BlackLens.get(), Component.translatable("jei.servantry.description.drops_from_zombie"));
        registration.addIngredientInfo(ServantWeaponRegister.TempestStaff.get(), Component.translatable("jei.servantry.description.fishing"));
    }
}
