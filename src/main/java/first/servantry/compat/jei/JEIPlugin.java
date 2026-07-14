package first.servantry.compat.jei;

import first.servantry.Servantry;
import first.servantry.api.armorSet.ArmorSet;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.compat.jei.category.MithrilAnvilRecipeCategory;
import first.servantry.register.ServantryCurioRegister;
import first.servantry.register.ServantryItemRegister;
import first.servantry.register.ServantryMithrilAnvilRecipeRegister;
import first.servantry.register.ServantryServantWeaponRegister;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
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
        registration.addRecipeCatalyst(new ItemStack(ServantryItemRegister.MithrilAnvil.asItem()), MithrilAnvilRecipeCategory.SoulRecipeType);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level instanceof Level level) {
            List<MithrilAnvilRecipe> soulRecipeList = level.getRecipeManager().getAllRecipesFor(ServantryMithrilAnvilRecipeRegister.MITHRIL_ANVIL_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();
            registration.addRecipes(MithrilAnvilRecipeCategory.SoulRecipeType, soulRecipeList);
        }
        List<ArmorSet> armorSets = ServantryRegistries.ARMOR_SETS.stream().toList();
        for (ArmorSet armorSet : armorSets) {
            List<ItemStack> itemStackList = armorSet.items().stream()
                    .map(ItemLike::asItem)
                    .map(Item::getDefaultInstance)
                    .toList();
            registration.addItemStackInfo(itemStackList, Component.empty());
        }
        // 牧师出售
        registration.addIngredientInfo(ServantryCurioRegister.PygmyNecklace.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ServantryCurioRegister.HerculesBeetle.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ServantryCurioRegister.ApprenticesScarf.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ServantryCurioRegister.HuntressesBuckler.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ServantryCurioRegister.MonksBelt.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ServantryCurioRegister.SquiresShield.get(), Component.translatable("jei.servantry.description.sold"));
        // 掉落
        registration.addIngredientInfo(ServantryServantWeaponRegister.TerraPrism.get(), Component.translatable("jei.servantry.description.terraprism"));
        registration.addIngredientInfo(ServantryCurioRegister.SummonerEmblem.get(), Component.translatable("jei.servantry.description.drops_from_evokers"));
        registration.addIngredientInfo(ServantryItemRegister.BlackLens.get(), Component.translatable("jei.servantry.description.drops_from_zombie"));
        registration.addIngredientInfo(ServantryServantWeaponRegister.TempestStaff.get(), Component.translatable("jei.servantry.description.fishing"));
    }
}
