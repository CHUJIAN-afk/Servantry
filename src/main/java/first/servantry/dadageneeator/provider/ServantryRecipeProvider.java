package first.servantry.dadageneeator.provider;

import first.servantry.Servantry;
import first.servantry.api.mithrilAnvil.MithrilAnvilCraftingRecipe;
import first.servantry.api.mithrilAnvil.MithrilAnvilRecipe;
import first.servantry.register.ItemRegister;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServantryRecipeProvider extends RecipeProvider {

    public ServantryRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    private static MithrilAnvilRecipe.IngredientWithCount iwc(ItemLike item, int count) {
        return new MithrilAnvilRecipe.IngredientWithCount(Ingredient.of(item), count);
    }

    @SafeVarargs
    private static List<MithrilAnvilRecipe.IngredientWithCount> mithrilIngredients(MithrilAnvilRecipe.IngredientWithCount... iwcs) {
        return List.of(iwcs);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {

        // 1. 星尘细胞法杖
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.StardustCellStaff.get())
                .pattern("  N")
                .pattern(" N ")
                .pattern("S  ")
                .define('N', Items.NETHER_STAR)
                .define('S', Items.STICK)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR)) // 获得下界之星时解锁配方
                .save(output);

        // 1. 星尘之龙法杖:
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.StardustDragonStaff.get())
                .pattern("  D")
                .pattern(" N ")
                .pattern("S  ")
                .define('D', Items.DRAGON_HEAD)
                .define('N', Items.NETHER_STAR)
                .define('S', Items.STICK)
                .unlockedBy("has_nether_star", has(Items.DRAGON_HEAD)) // 获得下界之星时解锁配方
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

        // ================= 饰品合成表 =================
        // 死灵卷轴 - 枯萎的玫瑰 + 纸
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.NecromanticScroll.get())
                .pattern("PPP")
                .pattern("PSP")
                .pattern("PPP")
                .define('S', Items.WITHER_SKELETON_SKULL)
                .define('P', Items.PAPER)
                .unlockedBy("has_wither_skeleton_skull", has(Items.WITHER_SKELETON_SKULL))
                .save(output);

        // 甲虫莎草纸 - 死灵卷轴 + 大力士甲虫
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ItemRegister.PapyrusScarab.get())
                .requires(ItemRegister.NecromanticScroll.get())
                .requires(ItemRegister.HerculesBeetle.get())
                .unlockedBy("has_necromantic_scroll", has(ItemRegister.NecromanticScroll.get()))
                .unlockedBy("has_hercules_beetle", has(ItemRegister.HerculesBeetle.get()))
                .save(output);

        // 魔眼法杖 - 2黑色晶状体 + 2木棍，倾斜摆放
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemRegister.OpticStaff.get())
                .pattern(" L ")
                .pattern(" SL")
                .pattern("S  ")
                .define('L', ItemRegister.BlackLens.get())
                .define('S', Items.STICK)
                .unlockedBy("has_black_lens", has(ItemRegister.BlackLens.get()))
                .save(output);
        // ==================== 秘银砧配方 ====================
        buildMithrilAnvilRecipes(output);
    }

    private void buildMithrilAnvilRecipes(RecipeOutput output) {
        // 1 铁剑 ← 铁锭×2 + 木棍×1
        mithril(output, "iron_sword",
                mithrilIngredients(iwc(Items.IRON_INGOT, 2), iwc(Items.STICK, 1)),
                Items.IRON_SWORD);

        // 2 铁镐 ← 铁锭×3 + 木棍×2
        mithril(output, "iron_pickaxe",
                mithrilIngredients(iwc(Items.IRON_INGOT, 3), iwc(Items.STICK, 2)),
                Items.IRON_PICKAXE);

        // 3 铁斧 ← 铁锭×3 + 木棍×2
        mithril(output, "iron_axe",
                mithrilIngredients(iwc(Items.IRON_INGOT, 3), iwc(Items.STICK, 2)),
                Items.IRON_AXE);

        // 4 铁锹 ← 铁锭×1 + 木棍×2
        mithril(output, "iron_shovel",
                mithrilIngredients(iwc(Items.IRON_INGOT, 1), iwc(Items.STICK, 2)),
                Items.IRON_SHOVEL);

        // 5 铁胸甲 ← 铁锭×8
        mithril(output, "iron_chestplate",
                mithrilIngredients(iwc(Items.IRON_INGOT, 8)),
                Items.IRON_CHESTPLATE);

        // 6 铁护腿 ← 铁锭×7
        mithril(output, "iron_leggings",
                mithrilIngredients(iwc(Items.IRON_INGOT, 7)),
                Items.IRON_LEGGINGS);

        // 7 铁头盔 ← 铁锭×5
        mithril(output, "iron_helmet",
                mithrilIngredients(iwc(Items.IRON_INGOT, 5)),
                Items.IRON_HELMET);

        // 8 铁靴子 ← 铁锭×4
        mithril(output, "iron_boots",
                mithrilIngredients(iwc(Items.IRON_INGOT, 4)),
                Items.IRON_BOOTS);

        // 9 钻石剑 ← 钻石×2 + 木棍×1
        mithril(output, "diamond_sword",
                mithrilIngredients(iwc(Items.DIAMOND, 2), iwc(Items.STICK, 1)),
                Items.DIAMOND_SWORD);

        // 10 钻石镐 ← 钻石×3 + 木棍×2
        mithril(output, "diamond_pickaxe",
                mithrilIngredients(iwc(Items.DIAMOND, 3), iwc(Items.STICK, 2)),
                Items.DIAMOND_PICKAXE);

        // 11 钻石胸甲 ← 钻石×8
        mithril(output, "diamond_chestplate",
                mithrilIngredients(iwc(Items.DIAMOND, 8)),
                Items.DIAMOND_CHESTPLATE);

        // 12 钻石护腿 ← 钻石×7
        mithril(output, "diamond_leggings",
                mithrilIngredients(iwc(Items.DIAMOND, 7)),
                Items.DIAMOND_LEGGINGS);

        // 13 盾牌 ← 铁锭×1 + 木板×6
        mithril(output, "shield",
                mithrilIngredients(iwc(Items.IRON_INGOT, 1), iwc(Items.OAK_PLANKS, 6)),
                Items.SHIELD);

        // 14 弓 ← 木棍×3 + 线×3
        mithril(output, "bow",
                mithrilIngredients(iwc(Items.STICK, 3), iwc(Items.STRING, 3)),
                Items.BOW);

        // 15 铁马铠 ← 铁锭×8 + 皮革×1
        mithril(output, "iron_horse_armor",
                mithrilIngredients(iwc(Items.IRON_INGOT, 8), iwc(Items.LEATHER, 1)),
                Items.IRON_HORSE_ARMOR);

        // 16 钻石马铠 ← 钻石×8 + 金锭×1
        mithril(output, "diamond_horse_armor",
                mithrilIngredients(iwc(Items.DIAMOND, 8), iwc(Items.GOLD_INGOT, 1)),
                Items.DIAMOND_HORSE_ARMOR);

        // 17 金苹果 ← 苹果×1 + 金锭×8
        mithril(output, "golden_apple",
                mithrilIngredients(iwc(Items.APPLE, 1), iwc(Items.GOLD_INGOT, 8)),
                Items.GOLDEN_APPLE);

        // 18 附魔金苹果 ← 苹果×1 + 金块×8
        mithril(output, "enchanted_golden_apple",
                mithrilIngredients(iwc(Items.APPLE, 1), iwc(Items.GOLD_BLOCK, 8)),
                Items.ENCHANTED_GOLDEN_APPLE);

        // 19 望远镜 ← 紫水晶碎片×1 + 铜锭×2
        mithril(output, "spyglass",
                mithrilIngredients(iwc(Items.AMETHYST_SHARD, 1), iwc(Items.COPPER_INGOT, 2)),
                Items.SPYGLASS);

        // 20 末影之眼 ← 末影珍珠×1 + 烈焰粉×1
        mithril(output, "ender_eye",
                mithrilIngredients(iwc(Items.ENDER_PEARL, 1), iwc(Items.BLAZE_POWDER, 1)),
                Items.ENDER_EYE);
    }

    private void mithril(RecipeOutput output, String name, List<MithrilAnvilRecipe.IngredientWithCount> ingredients, ItemLike result) {
        ResourceLocation id = Servantry.rl("mithril_anvil/" + name);
        MithrilAnvilRecipe recipe = new MithrilAnvilRecipe(ingredients, result.asItem().getDefaultInstance());
        MithrilAnvilCraftingRecipe craftingRecipe = new MithrilAnvilCraftingRecipe(recipe);
        AdvancementHolder advancement = output.advancement()
                .addCriterion("has_result", has(result))
                .build(id.withPrefix("recipes/"));
        output.accept(id, craftingRecipe, advancement);
    }
}