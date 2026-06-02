package first.servantry.dadageneeator.provider;

import first.servantry.register.ItemRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ServantryRecipeProvider extends RecipeProvider {

    public ServantryRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        ItemRegister.Register.getRecipeGenerate().forEach(generate -> generate.recipe(output));
    }

    @FunctionalInterface
    public interface Generate {
        void recipe(RecipeOutput output);
    }
}
