package first.servantry.dadageneeator.provider;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

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
                        case ICurioItem ignored -> basicItem(item);
                        case IServantWeapon<?> ignored -> handheldItem(item);
                        case ArmorItem ignored -> basicItem(item);
                        default -> {
                        }
                    }
                });
    }
}
