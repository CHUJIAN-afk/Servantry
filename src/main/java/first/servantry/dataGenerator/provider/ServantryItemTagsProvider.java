package first.servantry.dataGenerator.provider;

import first.servantry.Servantry;
import first.servantry.api.item.CurioItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
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
                .filter(item -> BuiltInRegistries.ITEM.getKey(item)
                        .getNamespace()
                        .equals(Servantry.MODID))
                .forEach(item -> {
                    if (item instanceof CurioItem curioItem) {
                        tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "curio"))).add(curioItem);
                    }
                    if (item instanceof ArmorItem armorItem) {
                        tag(Tags.Items.ENCHANTABLES).add(armorItem);
                        tag(Tags.Items.ARMORS).add(armorItem);
                        tag(ItemTags.ARMOR_ENCHANTABLE).add(armorItem);
                        EquipmentSlot equipmentSlot = armorItem.getEquipmentSlot();
                        switch (equipmentSlot) {
                            case HEAD -> {
                                tag(ItemTags.HEAD_ARMOR).add(armorItem);
                                tag(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(armorItem);
                            }
                            case CHEST -> {
                                tag(ItemTags.CHEST_ARMOR).add(armorItem);
                                tag(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(armorItem);
                            }
                            case LEGS -> {
                                tag(ItemTags.LEG_ARMOR).add(armorItem);
                                tag(ItemTags.LEG_ARMOR_ENCHANTABLE).add(armorItem);
                            }
                            case FEET -> {
                                tag(ItemTags.FOOT_ARMOR).add(armorItem);
                                tag(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(armorItem);
                            }
                        }
                    }
                });
    }
}