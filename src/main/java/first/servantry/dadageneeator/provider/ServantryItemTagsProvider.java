package first.servantry.dadageneeator.provider;

import first.servantry.Servantry;
import first.servantry.common.item.CurioItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.CompletableFuture;

public class ServantryItemTagsProvider extends ItemTagsProvider {

    public ServantryItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTags, Servantry.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "curio")))
                .add(BuiltInRegistries.ITEM.stream()
                        .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Servantry.MODID))
                        .filter(item -> item instanceof CurioItem)
                        .toArray(Item[]::new)
                );
    }

}
