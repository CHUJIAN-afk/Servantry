package first.servantry.dadageneeator.provider;

import first.servantry.Servantry;
import first.servantry.common.item.CurioItem;
import first.servantry.register.TagRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
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
        BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Servantry.MODID))
                .forEach(item -> {
                    if (item instanceof CurioItem curioItem) {
                        TagKey<Item> curio = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "curio"));
                        tag(curio).add(curioItem);
                    }
                    ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
                    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "item/" + location.getPath());
                    ExistingFileHelper fileHelper = this.existingFileHelper;
                    assert fileHelper != null;
                    boolean exists = fileHelper.exists(texture, ModelProvider.TEXTURE);
                    if (!exists) {
                        tag(TagRegister.ART_WIP).add(item);
                    }

                });
    }

}