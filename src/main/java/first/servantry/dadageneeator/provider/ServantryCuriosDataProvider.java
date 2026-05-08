package first.servantry.dadageneeator.provider;

import first.servantry.Servantry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class ServantryCuriosDataProvider extends CuriosDataProvider {

    public ServantryCuriosDataProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(Servantry.MODID, packOutput, existingFileHelper, lookupProvider);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        this.createEntities("curio")
                .addPlayer()
                .addSlots("curio");
    }
}
