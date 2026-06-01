package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ArmorMaterialRegister {

    private static final DeferredRegister<ArmorMaterial> Register = DeferredRegister.create(Registries.ARMOR_MATERIAL, Servantry.MODID);

    public static final Holder<ArmorMaterial> Flinx = Register.register("flinx", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 1);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.BOOTS, 0);
            }),
            5,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.LEATHER),
            List.of(new ArmorMaterial.Layer(Servantry.rl("flinx"))),
            0.0F,
            0.0F
    ));

    public static final Holder<ArmorMaterial> ObsidianArmorMaterial = Register.register("obsidian", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.CHESTPLATE, 2);
                map.put(ArmorItem.Type.LEGGINGS, 2);
                map.put(ArmorItem.Type.BOOTS, 1);
            }),
            5,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.OBSIDIAN, Items.CRYING_OBSIDIAN),
            List.of(new ArmorMaterial.Layer(Servantry.rl("obsidian"))),
            0.0F,
            0.0F
    ));

    public static final Holder<ArmorMaterial> HallowedArmorMaterial = Register.register("hallowed", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.CHESTPLATE, 5);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.BOOTS, 3);
            }),
            35,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.GOLD_INGOT),
            List.of(new ArmorMaterial.Layer(Servantry.rl("hallowed"))),
            0.0F,
            0.0F
    ));

    public static final Holder<ArmorMaterial> ValhallaKnightArmorMaterial = Register.register("valhalla_knight", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 7);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.BOOTS, 4);
            }),
            20,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(Servantry.rl("valhalla_knight"))),
            0.0F,
            0.0F
    ));

    public static final Holder<ArmorMaterial> ChlorophyteArmorMaterial = Register.register("chlorophyte", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.BOOTS, 3);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(Servantry.rl("chlorophyte"))),
            0.0F,
            0.0F
    ));

    public static final Holder<ArmorMaterial> SpookyArmorMaterial = Register.register("spooky", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.CHESTPLATE, 4);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.BOOTS, 2);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(Servantry.rl("spooky"))),
            0.0F,
            0.0F
    ));

    public static final Holder<ArmorMaterial> TikiArmorMaterial = Register.register("tiki", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.BOOTS, 3);
            }),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(Servantry.rl("tiki"))),
            0.0F,
            0.0F
    ));

    public static final Holder<ArmorMaterial> StardustArmorMaterial = Register.register("stardust", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 4);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.BOOTS, 3);
            }),
            25,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(Servantry.rl("stardust"))),
            0.0F,
            0.0F
    ));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
