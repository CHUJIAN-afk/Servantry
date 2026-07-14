package first.servantry.dataGenerator.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ServantryRecipeProvider extends RecipeProvider {

    public static final List<Consumer<RecipeOutput>> RecipeGenerate = new ArrayList<>();

    public ServantryRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        RecipeGenerate.removeIf(generate -> {
            generate.accept(output);
            return true;
        });
    }
}
