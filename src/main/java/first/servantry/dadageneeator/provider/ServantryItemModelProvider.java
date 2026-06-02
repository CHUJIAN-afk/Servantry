package first.servantry.dadageneeator.provider;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class ServantryItemModelProvider extends ItemModelProvider {

    public ServantryItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Servantry.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Servantry.MODID))
                .forEach(item -> {
                    // 方块物品使用方块模型父级
                    if (item instanceof BlockItem) {
                        withExistingParent(BuiltInRegistries.ITEM.getKey(item).getPath(),
                                ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "block/" + BuiltInRegistries.ITEM.getKey(item).getPath()));
                        return;
                    }
                    switch (item) {
                        case IServantWeapon<?> ignored -> handheldItem(item);
                        default -> basicItem(item);
                    }
                });
    }

    @Override
    public @NotNull ItemModelBuilder handheldItem(ResourceLocation item) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath());
        boolean exists = this.existingFileHelper.exists(texture, ModelProvider.TEXTURE);
        if (!exists) {
            texture = ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/stick");
        }
        return this.getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", texture);
    }

    @Override
    public @NotNull ItemModelBuilder basicItem(ResourceLocation item) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath());
        boolean exists = this.existingFileHelper.exists(texture, ModelProvider.TEXTURE);
        if (!exists) {
            texture = ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/stick");
        }
        return this.getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", texture);
    }
}
