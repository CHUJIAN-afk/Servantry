package first.servantry.compat;

import first.servantry.Servantry;
import first.servantry.register.ItemRegister;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return Servantry.rl("jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(ItemRegister.TerraPrism.get(), Component.translatable("jei.servantry.description.terraprism"));
        // 牧师出售
        registration.addIngredientInfo(ItemRegister.PygmyNecklace.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ItemRegister.HerculesBeetle.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ItemRegister.ApprenticesScarf.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ItemRegister.HuntressesBuckler.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ItemRegister.MonksBelt.get(), Component.translatable("jei.servantry.description.sold"));
        registration.addIngredientInfo(ItemRegister.SquiresShield.get(), Component.translatable("jei.servantry.description.sold"));
        // 掉落
        registration.addIngredientInfo(ItemRegister.SummonerEmblem.get(), Component.translatable("jei.servantry.description.drops_from_evokers"));
        registration.addIngredientInfo(ItemRegister.BlackLens.get(), Component.translatable("jei.servantry.description.drops_from_zombie"));
        registration.addIngredientInfo(ItemRegister.TempestStaff.get(), Component.translatable("jei.servantry.description.fishing"));
    }

}
