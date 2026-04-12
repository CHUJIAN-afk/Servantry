package first.servantry.dadageneeator;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.dadageneeator.provider.SoulLangProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.feature.BlueIceFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Servantry.MODID)
public class DataGeneratorEvent {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        //语言
        generator.addProvider(event.includeServer(), new SoulLangProvider(packOutput, Servantry.MODID, "en_us"));
        generator.addProvider(event.includeServer(), new SoulLangProvider(packOutput, Servantry.MODID, "zh_cn"));
        //物品模型模型
        generator.addProvider(event.includeClient(), new ItemModelProvider(packOutput, Servantry.MODID, existingFileHelper){

            @Override
            protected void registerModels() {
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> item instanceof IWhipWeapon || item instanceof IServantWeapon<?>)
                        .forEach(this::handheldItem);
            }

        });
    }

}
