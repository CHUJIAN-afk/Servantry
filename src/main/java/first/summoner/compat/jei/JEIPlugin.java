package first.summoner.compat.jei;

import first.lyra.common.armorSet.ArmorSet;
import first.lyra.register.LyraRegistries;
import first.summoner.Summoner;
import first.summoner.common.recipe.MithrilAnvilRecipe;
import first.summoner.compat.jei.category.MithrilAnvilRecipeCategory;
import first.summoner.register.SummonerCurioRegister;
import first.summoner.register.SummonerItemRegister;
import first.summoner.register.SummonerMithrilAnvilRecipeRegister;
import first.summoner.register.SummonerServantWeaponRegister;
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
        return Summoner.rl("jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MithrilAnvilRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(SummonerItemRegister.MithrilAnvil.asItem()), MithrilAnvilRecipeCategory.SoulRecipeType);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level instanceof Level level) {
            List<MithrilAnvilRecipe> soulRecipeList = level.getRecipeManager().getAllRecipesFor(SummonerMithrilAnvilRecipeRegister.MITHRIL_ANVIL_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();
            registration.addRecipes(MithrilAnvilRecipeCategory.SoulRecipeType, soulRecipeList);
        }
        List<ArmorSet> armorSets = LyraRegistries.ARMOR_SETS.stream().toList();
        for (ArmorSet armorSet : armorSets) {
            List<ItemStack> itemStackList = armorSet.items().stream()
                    .map(ItemLike::asItem)
                    .map(Item::getDefaultInstance)
                    .toList();
            registration.addItemStackInfo(itemStackList, Component.empty());
        }
        // 牧师出售
        registration.addIngredientInfo(SummonerCurioRegister.PygmyNecklace.get(), Component.translatable("jei.summoner.description.sold"));
        registration.addIngredientInfo(SummonerCurioRegister.HerculesBeetle.get(), Component.translatable("jei.summoner.description.sold"));
        registration.addIngredientInfo(SummonerCurioRegister.ApprenticesScarf.get(), Component.translatable("jei.summoner.description.sold"));
        registration.addIngredientInfo(SummonerCurioRegister.HuntressesBuckler.get(), Component.translatable("jei.summoner.description.sold"));
        registration.addIngredientInfo(SummonerCurioRegister.MonksBelt.get(), Component.translatable("jei.summoner.description.sold"));
        registration.addIngredientInfo(SummonerCurioRegister.SquiresShield.get(), Component.translatable("jei.summoner.description.sold"));
        // 掉落
        registration.addIngredientInfo(SummonerServantWeaponRegister.TerraPrism.get(), Component.translatable("jei.summoner.description.terraprism"));
        registration.addIngredientInfo(SummonerCurioRegister.SummonerEmblem.get(), Component.translatable("jei.summoner.description.drops_from_evokers"));
        registration.addIngredientInfo(SummonerItemRegister.BlackLens.get(), Component.translatable("jei.summoner.description.drops_from_zombie"));
        registration.addIngredientInfo(SummonerServantWeaponRegister.TempestStaff.get(), Component.translatable("jei.summoner.description.fishing"));
    }
}
