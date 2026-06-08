package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class ArmorMaterialRegister {

    private static final DeferredRegister<ArmorMaterial> Register = DeferredRegister.create(Registries.ARMOR_MATERIAL, Servantry.MODID);

    public static final Holder<ArmorMaterial> Flinx = builder("flinx")
            .enchantmentValue(5)
            .sound(SoundEvents.ARMOR_EQUIP_LEATHER)
            .build();

    public static final Holder<ArmorMaterial> BeeArmorMaterial = builder("bee")
            .enchantmentValue(5)
            .sound(SoundEvents.ARMOR_EQUIP_LEATHER)
            .build();

    public static final Holder<ArmorMaterial> ObsidianArmorMaterial = builder("obsidian")
            .enchantmentValue(5)
            .sound(SoundEvents.ARMOR_EQUIP_IRON)
            .build();

    public static final Holder<ArmorMaterial> SpiderArmorMaterial = builder("spider")
            .enchantmentValue(10)
            .sound(SoundEvents.ARMOR_EQUIP_LEATHER)
            .build();

    public static final Holder<ArmorMaterial> ForbiddenArmorMaterial = builder("forbidden")
            .enchantmentValue(15)
            .sound(SoundEvents.ARMOR_EQUIP_IRON)
            .build();

    public static final Holder<ArmorMaterial> HallowedArmorMaterial = builder("hallowed")
            .enchantmentValue(35)
            .sound(SoundEvents.ARMOR_EQUIP_LEATHER)
            .build();

    public static final Holder<ArmorMaterial> ChlorophyteArmorMaterial = builder("chlorophyte")
            .enchantmentValue(10)
            .sound(SoundEvents.ARMOR_EQUIP_IRON)
            .build();

    public static final Holder<ArmorMaterial> ValhallaKnightArmorMaterial = builder("valhalla_knight")
            .enchantmentValue(20)
            .sound(SoundEvents.ARMOR_EQUIP_IRON)
            .build();

    public static final Holder<ArmorMaterial> TikiArmorMaterial = builder("tiki")
            .enchantmentValue(15)
            .sound(SoundEvents.ARMOR_EQUIP_LEATHER)
            .build();

    public static final Holder<ArmorMaterial> SpookyArmorMaterial = builder("spooky")
            .enchantmentValue(10)
            .sound(SoundEvents.ARMOR_EQUIP_LEATHER)
            .build();

    public static final Holder<ArmorMaterial> StardustArmorMaterial = builder("stardust")
            .enchantmentValue(25)
            .sound(SoundEvents.ARMOR_EQUIP_IRON)
            .build();

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

    private static Builder builder(String name) {
        return new Builder(name);
    }

    private static class Builder {
        private final String name;
        private int enchantmentValue;
        private Holder<SoundEvent> sound = SoundEvents.ARMOR_EQUIP_LEATHER;
        private Supplier<Ingredient> ingredient = () -> Ingredient.EMPTY;
        private float toughness;
        private float knockbackResistance;

        Builder(String name) {
            this.name = name;
        }

        Builder enchantmentValue(int value) {
            this.enchantmentValue = value;
            return this;
        }

        Builder sound(Holder<SoundEvent> sound) {
            this.sound = sound;
            return this;
        }

        Builder ingredient(ItemLike... items) {
            this.ingredient = () -> Ingredient.of(items);
            return this;
        }

        Builder ingredient(Supplier<Ingredient> ingredient) {
            this.ingredient = ingredient;
            return this;
        }

        Builder toughness(float toughness) {
            this.toughness = toughness;
            return this;
        }

        Builder knockbackResistance(float knockbackResistance) {
            this.knockbackResistance = knockbackResistance;
            return this;
        }

        Holder<ArmorMaterial> build() {
            return Register.register(name, () -> new ArmorMaterial(
                    HashMap.newHashMap(4),
                    enchantmentValue,
                    sound,
                    ingredient,
                    List.of(new ArmorMaterial.Layer(Servantry.rl(name))),
                    toughness,
                    knockbackResistance));
        }
    }
}
