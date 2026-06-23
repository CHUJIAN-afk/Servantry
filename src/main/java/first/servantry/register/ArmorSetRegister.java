package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.ServantryHelper;
import first.servantry.api.armorSet.ArmorSet;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.common.servant.ChlorophyteCrystal;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArmorSetRegister {

    private static final DeferredRegister<ArmorSet> Register = DeferredRegister.create(ServantryRegistries.ARMOR_SETS, Servantry.MODID);

    public static final Holder<ArmorSet> Bee =
            Register.register("bee", () -> ArmorSet.builder(Servantry.rl("bee"))
                    .piece(ArmorRegister.BeeHeadgear)
                    .piece(ArmorRegister.BeeChestplate)
                    .piece(ArmorRegister.BeeLeggings)
                    .piece(ArmorRegister.BeeBoots)
                    .modifier(AttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Spider =
            Register.register("spider", () -> ArmorSet.builder(Servantry.rl("spider"))
                    .piece(ArmorRegister.SpiderMask)
                    .piece(ArmorRegister.SpiderChestplate)
                    .piece(ArmorRegister.SpiderLeggings)
                    .piece(ArmorRegister.SpiderBoots)
                    .modifier(AttributeRegister.ServantDamage, 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Forbidden =
            Register.register("forbidden", () -> ArmorSet.builder(Servantry.rl("forbidden"))
                    .piece(ArmorRegister.ForbiddenMask)
                    .piece(ArmorRegister.ForbiddenRobe)
                    .piece(ArmorRegister.ForbiddenLeggings)
                    .piece(ArmorRegister.ForbiddenBoots)
                    .tooltip(1, "Allows you to summon an ancient storm to attract nearby enemies", "允许你召唤远古风暴吸引附近的敌人")
                    .build()
            );

    public static final Holder<ArmorSet> Obsidian =
            Register.register("obsidian", () -> ArmorSet.builder(Servantry.rl("obsidian"))
                    .piece(ArmorRegister.ObsidianHelmet)
                    .piece(ArmorRegister.ObsidianChestplate)
                    .piece(ArmorRegister.ObsidianLeggings)
                    .piece(ArmorRegister.ObsidianBoots)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(AttributeRegister.ServantDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(AttributeRegister.ServantSearchRange, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Hallowed = Register.register("hallowed", () -> ArmorSet.builder(Servantry.rl("hallowed"))
            .piece(ArmorRegister.HallowedHelmet)
            .piece(ArmorRegister.HallowedChestplate)
            .piece(ArmorRegister.HallowedLeggings)
            .piece(ArmorRegister.HallowedBoots)
            .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
            .modifier(AttributeRegister.ServantArmorPierce, 4, AttributeModifier.Operation.ADD_VALUE)
            .tooltip(1, "Servants grant I-frames on attack", "仆从攻击使敌人高亮")
            .build()
    );

    public static final Holder<ArmorSet> ValhallaKnight =
            Register.register("valhalla_knight", () -> ArmorSet.builder(Servantry.rl("valhalla_knight"))
                    .piece(ArmorRegister.ValhallaKnightHelmet)
                    .piece(ArmorRegister.ValhallaKnightChestplate)
                    .piece(ArmorRegister.ValhallaKnightLeggings)
                    .piece(ArmorRegister.ValhallaKnightBoots)
                    .modifier(AttributeRegister.SentryServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantDamage, 0.40, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(Attributes.KNOCKBACK_RESISTANCE, 1.0, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final Holder<ArmorSet> Chlorophyte =
            Register.register("chlorophyte", () -> ArmorSet.builder(Servantry.rl("chlorophyte"))
                    .piece(ArmorRegister.ChlorophyteHelmet)
                    .piece(ArmorRegister.ChlorophyteChestplate)
                    .piece(ArmorRegister.ChlorophyteLeggings)
                    .piece(ArmorRegister.ChlorophyteBoots)
                    .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .onStart(player -> {
                        ChlorophyteCrystal crystal = new ChlorophyteCrystal();
                        crystal.setDamage(10);
                        crystal.setKnockback(1);
                        ServantryHelper.get(player).add(EntityData.Type.ExtraServant, crystal);
                    })
                    .onRemove(player -> ServantryHelper.get(player).getEntityData().remove(EntityData.Type.ExtraServant, AttachmentEntityRegister.ChlorophyteCrystal.get()))
                    .tooltip(1, "Summons a powerful leaf crystal to shoot at nearby enemies", "召唤强大的叶状水晶来射击附近的敌人")
                    .build()
            );

    public static final Holder<ArmorSet> Spooky =
            Register.register("spooky", () -> ArmorSet.builder(Servantry.rl("spooky"))
                    .piece(ArmorRegister.SpookyHelmet)
                    .piece(ArmorRegister.SpookyChestplate)
                    .piece(ArmorRegister.SpookyLeggings)
                    .piece(ArmorRegister.SpookyBoots)
                    .modifier(AttributeRegister.ServantDamage, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(AttributeRegister.ServantArmorPierce, 12, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final Holder<ArmorSet> Tiki =
            Register.register("tiki", () -> ArmorSet.builder(Servantry.rl("tiki"))
                    .piece(ArmorRegister.TikiHelmet)
                    .piece(ArmorRegister.TikiChestplate)
                    .piece(ArmorRegister.TikiLeggings)
                    .piece(ArmorRegister.TikiBoots)
                    .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(AttributeRegister.ServantSearchRange, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Stardust =
            Register.register("stardust", () -> ArmorSet.builder(Servantry.rl("stardust"))
                    .piece(ArmorRegister.StardustHelmet)
                    .piece(ArmorRegister.StardustChestplate)
                    .piece(ArmorRegister.StardustLeggings)
                    .piece(ArmorRegister.StardustBoots)
                    .tooltip(1, "A stardust guardian will protect you from nearby enemies", "星尘守卫将保护你不受附近敌人的伤害")
                    .build()
            );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
