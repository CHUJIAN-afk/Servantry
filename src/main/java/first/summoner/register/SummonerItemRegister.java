package first.summoner.register;

import first.lyra.register.LyraItemRegistries;
import first.summoner.Summoner;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredItem;

public class SummonerItemRegister {

    public static final LyraItemRegistries REGISTRIES = Summoner.REGISTRIES;

    public static final DeferredItem<SwordItem> Zenith = REGISTRIES.build("zenith", location -> new SwordItem(Tiers.NETHERITE, new Item.Properties()
            .attributes(ItemAttributeModifiers.builder()
                                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(location, 190, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                                .build())
            .rarity(Rarity.EPIC)
            .stacksTo(1)))
            .itemLanguage("Zenith", "天顶剑")
            .itemTag(ItemTags.DURABILITY_ENCHANTABLE)
            .itemTag(ItemTags.WEAPON_ENCHANTABLE)
            .itemTag(ItemTags.SWORD_ENCHANTABLE)
            .itemModel(ItemModelProvider::handheldItem)
            .build();

    public static void register() {
    }
}
