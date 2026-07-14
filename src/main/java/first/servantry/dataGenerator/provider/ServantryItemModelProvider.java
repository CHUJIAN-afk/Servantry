package first.servantry.dataGenerator.provider;

import first.servantry.Servantry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServantryItemModelProvider extends ItemModelProvider {

    public static final Map<ResourceLocation, BiConsumer<ResourceLocation, ServantryItemModelProvider>> ItemModelGenerate = new HashMap<>();

    public ServantryItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Servantry.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ItemModelGenerate.entrySet()
                .removeIf(entry -> {
                    ResourceLocation key = entry.getKey();
                    BiConsumer<ResourceLocation, ServantryItemModelProvider> consumer = entry.getValue();
                    consumer.accept(key, this);
                    return true;
                });
    }
}
