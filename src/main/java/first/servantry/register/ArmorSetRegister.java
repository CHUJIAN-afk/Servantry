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

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
