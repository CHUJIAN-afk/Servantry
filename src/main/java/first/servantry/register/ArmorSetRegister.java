package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.armorSet.ArmorSet;
import first.servantry.api.register.ServantryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArmorSetRegister {

    private static final DeferredRegister<ArmorSet> Register = DeferredRegister.create(ServantryRegistries.ARMOR_SETS, Servantry.MODID);

    public static final Holder<ArmorSet> Hallowed = Register.register("hallowed", () -> ArmorSet.builder(Servantry.rl("hallowed"))
            .piece(ItemRegister.HallowedHelmet)
            .piece(ItemRegister.HallowedChestplate)
            .piece(ItemRegister.HallowedLeggings)
            .piece(ItemRegister.HallowedBoots)
            .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
            .modifier(AttributeRegister.ServantArmorPierce, 4, AttributeModifier.Operation.ADD_VALUE)
            .build()
    );

    public static final Holder<ArmorSet> Obsidian =
            Register.register("obsidian", () -> ArmorSet.builder(Servantry.rl("obsidian"))
                    .piece(ItemRegister.ObsidianHelmet)
                    .piece(ItemRegister.ObsidianChestplate)
                    .piece(ItemRegister.ObsidianLeggings)
                    .piece(ItemRegister.ObsidianBoots)
                    .modifier(Attributes.ARMOR, 8, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(AttributeRegister.ServantDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> ValhallaKnight =
            Register.register("valhalla_knight", () -> ArmorSet.builder(Servantry.rl("valhalla_knight"))
                    .piece(ItemRegister.ValhallaKnightHelmet)
                    .piece(ItemRegister.ValhallaKnightChestplate)
                    .piece(ItemRegister.ValhallaKnightLeggings)
                    .piece(ItemRegister.ValhallaKnightBoots)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.40, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(Attributes.KNOCKBACK_RESISTANCE, 1.0, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final Holder<ArmorSet> Chlorophyte =
            Register.register("chlorophyte", () -> ArmorSet.builder(Servantry.rl("chlorophyte"))
                    .piece(ItemRegister.ChlorophyteHelmet)
                    .piece(ItemRegister.ChlorophyteChestplate)
                    .piece(ItemRegister.ChlorophyteLeggings)
                    .piece(ItemRegister.ChlorophyteBoots)
                    .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final Holder<ArmorSet> Spooky =
            Register.register("spooky", () -> ArmorSet.builder(Servantry.rl("spooky"))
                    .piece(ItemRegister.SpookyHelmet)
                    .piece(ItemRegister.SpookyChestplate)
                    .piece(ItemRegister.SpookyLeggings)
                    .piece(ItemRegister.SpookyBoots)
                    .modifier(AttributeRegister.ServantDamage, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Tiki =
            Register.register("tiki", () -> ArmorSet.builder(Servantry.rl("tiki"))
                    .piece(ItemRegister.TikiHelmet)
                    .piece(ItemRegister.TikiChestplate)
                    .piece(ItemRegister.TikiLeggings)
                    .piece(ItemRegister.TikiBoots)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Stardust =
            Register.register("stardust", () -> ArmorSet.builder(Servantry.rl("stardust"))
                    .piece(ItemRegister.StardustHelmet)
                    .piece(ItemRegister.StardustChestplate)
                    .piece(ItemRegister.StardustLeggings)
                    .piece(ItemRegister.StardustBoots)
                    .build()
            );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
