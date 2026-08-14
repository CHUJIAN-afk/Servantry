package first.summoner.dataGenerator.provider;

import first.summoner.Summoner;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class SummonerCuriosDataProvider extends CuriosDataProvider {

    public SummonerCuriosDataProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(Summoner.MODID, packOutput, existingFileHelper, lookupProvider);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        this.createEntities("curio")
                .addPlayer()
                .addSlots("curio");
    }
}
