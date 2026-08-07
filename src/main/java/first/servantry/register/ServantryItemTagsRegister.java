package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

public class ServantryItemTagsRegister {

    public static final TagKey<Item> Curio = register(ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "curio"));
    public static final TagKey<Item> ServantWeapon = register("servant_weapon");
    // 创造模式分类分段特征标签（物品带标签自动归入对应分段）
    public static final TagKey<Item> SectionServantWeapon = register("sections/servant_weapon");
    public static final TagKey<Item> SectionArmor = register("sections/armor");
    public static final TagKey<Item> SectionAccessory = register("sections/accessory");
    public static final TagKey<Item> SectionMaterial = register("sections/material");
    public static final TagKey<Item> SectionBlock = register("sections/block");

    private static TagKey<Item> register(ResourceLocation location) {
        return TagKey.create(Registries.ITEM, location);
    }

    private static TagKey<Item> register(String path) {
        return TagKey.create(Registries.ITEM, Servantry.rl(path));
    }
}
