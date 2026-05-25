package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagRegister {

    public static final TagKey<Item> ART_WIP = TagKey.create(Registries.ITEM, Servantry.rl("art_wip"));
    public static final TagKey<Item> CODE_WIP = TagKey.create(Registries.ITEM, Servantry.rl("code_wip"));
}
