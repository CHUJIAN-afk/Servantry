package first.summoner.dataGenerator.provider;

import first.summoner.Summoner;
import first.summoner.register.SummonerEnchantmentRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SummonerEnchantmentTagsProvider extends EnchantmentTagsProvider {

    public SummonerEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Summoner.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        TagsProvider.TagAppender<Enchantment> appender = tag(EnchantmentTags.TREASURE);
        SummonerEnchantmentRegister.RegisterCache.keySet()
                .stream()
                .map(DeferredHolder::getKey)
                .forEach(appender::add);
    }
}
