package first.summoner.compat.jei;

import first.lyra.common.armorSet.ArmorSet;
import first.lyra.register.LyraRegistries;
import first.summoner.Summoner;
import first.summoner.register.SummonerCurioRegister;
import first.summoner.register.SummonerItemRegister;
import first.summoner.register.SummonerWeaponRegister;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return Summoner.rl("jei_plugin");
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
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
        registration.addIngredientInfo(SummonerWeaponRegister.TerraPrism.get(), Component.translatable("jei.summoner.description.terraprism"));
        registration.addIngredientInfo(SummonerCurioRegister.SummonerEmblem.get(), Component.translatable("jei.summoner.description.drops_from_evokers"));
        registration.addIngredientInfo(SummonerItemRegister.BlackLens.get(), Component.translatable("jei.summoner.description.drops_from_zombie"));
        registration.addIngredientInfo(SummonerWeaponRegister.TempestStaff.get(), Component.translatable("jei.summoner.description.fishing"));
    }
}
