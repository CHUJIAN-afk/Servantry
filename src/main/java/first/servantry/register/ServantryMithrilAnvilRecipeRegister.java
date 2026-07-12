package first.servantry.register;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import first.servantry.Servantry;
import first.servantry.common.recipe.MithrilAnvilRecipe;
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

public class ServantryMithrilAnvilRecipeRegister {

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Servantry.MODID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<MithrilAnvilRecipe>> MITHRIL_ANVIL_TYPE =
            RECIPE_TYPES.register("mithril_anvil", () -> RecipeType.simple(Servantry.rl("mithril_anvil")));

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Servantry.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, MithrilAnvilRecipeSerializer> MITHRIL_ANVIL_SERIALIZER =
            RECIPE_SERIALIZERS.register("mithril_anvil", MithrilAnvilRecipeSerializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }

    public static class MithrilAnvilRecipeSerializer implements RecipeSerializer<MithrilAnvilRecipe> {

        public static final StreamCodec<RegistryFriendlyByteBuf, MithrilAnvilRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeVarInt(recipe.ingredients().size());
                    for (int i = 0; i < recipe.ingredients().size(); i++) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredients().get(i));
                        buf.writeVarInt(recipe.counts().get(i));
                    }
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.result());
                },
                buf -> {
                    int size = buf.readVarInt();
                    List<Ingredient> ingredients = new ArrayList<>();
                    List<Integer> counts = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                        counts.add(buf.readVarInt());
                    }
                    ItemStack result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                    return new MithrilAnvilRecipe(List.copyOf(ingredients), List.copyOf(counts), result);
                }
        );

        private static final MapCodec<MithrilAnvilRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        IngredientWithCount.CODEC.listOf().fieldOf("ingredients").forGetter(r -> {
                            List<IngredientWithCount> list = new ArrayList<>();
                            for (int i = 0; i < r.ingredients().size(); i++) {
                                list.add(new IngredientWithCount(r.ingredients().get(i), r.counts().get(i)));
                            }
                            return list;
                        }),
                        ItemStack.SINGLE_ITEM_CODEC.fieldOf("result").forGetter(MithrilAnvilRecipe::result)
                ).apply(instance, (iwcs, result) -> {
                    List<Ingredient> ingredients = new ArrayList<>();
                    List<Integer> counts = new ArrayList<>();
                    for (IngredientWithCount iwc : iwcs) {
                        ingredients.add(iwc.ingredient);
                        counts.add(iwc.count);
                    }
                    return new MithrilAnvilRecipe(ingredients, counts, result);
                })
        );

        @Override
        public @NotNull MapCodec<MithrilAnvilRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MithrilAnvilRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    private record IngredientWithCount(Ingredient ingredient, int count) {
        private static final Codec<IngredientWithCount> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(IngredientWithCount::ingredient),
                        Codec.INT.fieldOf("count").forGetter(IngredientWithCount::count)
                ).apply(instance, IngredientWithCount::new)
        );
    }
}
