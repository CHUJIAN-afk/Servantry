package first.servantry.register;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import first.servantry.Servantry;
import first.servantry.api.mithrilAnvil.MithrilAnvilCraftingRecipe;
import first.servantry.api.mithrilAnvil.MithrilAnvilRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MithrilAnvilRecipeRegister {

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Servantry.MODID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<MithrilAnvilCraftingRecipe>> MITHRIL_ANVIL_TYPE =
            RECIPE_TYPES.register("mithril_anvil", () -> RecipeType.simple(Servantry.rl("mithril_anvil")));
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Servantry.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, MithrilAnvilRecipeSerializer> MITHRIL_ANVIL_SERIALIZER =
            RECIPE_SERIALIZERS.register("mithril_anvil", MithrilAnvilRecipeSerializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }

    public static class MithrilAnvilRecipeSerializer implements RecipeSerializer<MithrilAnvilCraftingRecipe> {

        public static final StreamCodec<RegistryFriendlyByteBuf, MithrilAnvilCraftingRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeVarInt(recipe.inner().ingredients().size());
                    for (MithrilAnvilRecipe.IngredientWithCount iwc : recipe.inner().ingredients()) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, iwc.ingredient());
                        buf.writeVarInt(iwc.count());
                    }
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.inner().result());
                },
                buf -> {
                    int ingredientCount = buf.readVarInt();
                    List<MithrilAnvilRecipe.IngredientWithCount> ingredients = new ArrayList<>();
                    for (int i = 0; i < ingredientCount; i++) {
                        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                        int count = buf.readVarInt();
                        ingredients.add(new MithrilAnvilRecipe.IngredientWithCount(ingredient, count));
                    }
                    ItemStack result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                    return new MithrilAnvilCraftingRecipe(new MithrilAnvilRecipe(ingredients, result));
                }
        );
        private static final MapCodec<MithrilAnvilCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        IngredientWithCountCodec.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.inner().ingredients()),
                        ItemStack.SINGLE_ITEM_CODEC.fieldOf("result").forGetter(r -> r.inner().result())
                ).apply(instance, (ingredients, result) -> new MithrilAnvilCraftingRecipe(new MithrilAnvilRecipe(ingredients, result)))
        );

        @Override
        public @NotNull MapCodec<MithrilAnvilCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MithrilAnvilCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    /**
     * Codec for IngredientWithCount: { "ingredient": ..., "count": N }
     */
    private static class IngredientWithCountCodec {
        static final Codec<MithrilAnvilRecipe.IngredientWithCount> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MithrilAnvilRecipe.IngredientWithCount::ingredient),
                        Codec.INT.fieldOf("count").forGetter(MithrilAnvilRecipe.IngredientWithCount::count)
                ).apply(instance, MithrilAnvilRecipe.IngredientWithCount::new)
        );
    }
}