package first.servantry.dadageneeator.provider;

import first.servantry.register.ItemRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SoulRecipeProvider extends RecipeProvider {

    public SoulRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        /*
        // 1. 星尘细胞法杖: 2 下界之星, 1 木棍
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.StardustCellStaff.get())
                .pattern("  N")
                .pattern(" N ")
                .pattern("S  ")
                .define('N', Items.NETHER_STAR)
                .define('S', Items.STICK)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR)) // 获得下界之星时解锁配方
                .save(output);

        // 2. 刃杖: 1 铁剑, 1 木棍
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.BladeStaff.get())
                .pattern("  W")
                .pattern(" S ")
                .pattern("S  ")
                .define('W', Items.IRON_SWORD)
                .define('S', Items.STICK)
                .unlockedBy("has_iron_sword", has(Items.IRON_SWORD)) // 获得铁剑时解锁配方
                .save(output);
*/
        // ================= 神圣套装合成表 =================

        // 3. 神圣兜帽 (头盔: 5个材料)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.HallowedHelmet.get())
                .pattern("GRG")
                .pattern("I I")
                .define('R', Items.ROTTEN_FLESH)
                .define('G', Items.GOLD_INGOT)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        // 4. 神圣板甲 (胸甲: 8个材料)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.HallowedChestplate.get())
                .pattern("I I")
                .pattern("GRG")
                .pattern("IGI")
                .define('R', Items.ROTTEN_FLESH)
                .define('G', Items.GOLD_INGOT)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        // 5. 神圣护胫 (护腿: 7个材料)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.HallowedLeggings.get())
                .pattern("GRG")
                .pattern("I I")
                .pattern("I I")
                .define('R', Items.ROTTEN_FLESH)
                .define('G', Items.GOLD_INGOT)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        // 6. 神圣战靴 (鞋子: 4个材料)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.HallowedBoots.get())
                .pattern("R R")
                .pattern("I I")
                .define('R', Items.GOLD_INGOT)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);
    }
}