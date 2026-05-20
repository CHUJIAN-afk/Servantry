package first.servantry.dadageneeator.provider;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ServantryItemModelProvider extends ItemModelProvider {

    public ServantryItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Servantry.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Servantry.MODID))
                .forEach(item -> {
                    switch (item) {
                        case IServantWeapon<?> ignored -> handheldItem(item);
                        default -> basicItem(item);
                    }
                });
    }
}
