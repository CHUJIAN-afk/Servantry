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

public class ServantryArmorSetRegister {

    private static final DeferredRegister<ArmorSet> Register = DeferredRegister.create(ServantryRegistries.ARMOR_SETS, Servantry.MODID);

    public static final Holder<ArmorSet> Bee =
            Register.register("bee", () -> ArmorSet.builder(Servantry.rl("bee"))
                    .piece(ServantryArmorRegister.BeeHeadgear)
                    .piece(ServantryArmorRegister.BeeChestplate)
                    .piece(ServantryArmorRegister.BeeLeggings)
                    .piece(ServantryArmorRegister.BeeBoots)
                    .modifier(ServantryAttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Spider =
            Register.register("spider", () -> ArmorSet.builder(Servantry.rl("spider"))
                    .piece(ServantryArmorRegister.SpiderMask)
                    .piece(ServantryArmorRegister.SpiderChestplate)
                    .piece(ServantryArmorRegister.SpiderLeggings)
                    .piece(ServantryArmorRegister.SpiderBoots)
                    .modifier(ServantryAttributeRegister.ServantDamage, 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Forbidden =
            Register.register("forbidden", () -> ArmorSet.builder(Servantry.rl("forbidden"))
                    .piece(ServantryArmorRegister.ForbiddenMask)
                    .piece(ServantryArmorRegister.ForbiddenRobe)
                    .piece(ServantryArmorRegister.ForbiddenLeggings)
                    .piece(ServantryArmorRegister.ForbiddenBoots)
                    .tooltip(1, "Allows you to summon an ancient storm to attract nearby enemies", "允许你召唤远古风暴吸引附近的敌人")
                    .build()
            );

    public static final Holder<ArmorSet> Obsidian =
            Register.register("obsidian", () -> ArmorSet.builder(Servantry.rl("obsidian"))
                    .piece(ServantryArmorRegister.ObsidianHelmet)
                    .piece(ServantryArmorRegister.ObsidianChestplate)
                    .piece(ServantryArmorRegister.ObsidianLeggings)
                    .piece(ServantryArmorRegister.ObsidianBoots)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(ServantryAttributeRegister.ServantDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(ServantryAttributeRegister.ServantSearchRange, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Hallowed = Register.register("hallowed", () -> ArmorSet.builder(Servantry.rl("hallowed"))
            .piece(ServantryArmorRegister.HallowedHelmet)
            .piece(ServantryArmorRegister.HallowedChestplate)
            .piece(ServantryArmorRegister.HallowedLeggings)
            .piece(ServantryArmorRegister.HallowedBoots)
            .modifier(ServantryAttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
            .modifier(ServantryAttributeRegister.ServantArmorPierce, 4, AttributeModifier.Operation.ADD_VALUE)
            .tooltip(1, "Servants grant I-frames on attack", "仆从攻击使敌人高亮")
            .build()
    );

    public static final Holder<ArmorSet> ValhallaKnight =
            Register.register("valhalla_knight", () -> ArmorSet.builder(Servantry.rl("valhalla_knight"))
                    .piece(ServantryArmorRegister.ValhallaKnightHelmet)
                    .piece(ServantryArmorRegister.ValhallaKnightChestplate)
                    .piece(ServantryArmorRegister.ValhallaKnightLeggings)
                    .piece(ServantryArmorRegister.ValhallaKnightBoots)
                    .modifier(ServantryAttributeRegister.SentryServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(ServantryAttributeRegister.ServantDamage, 0.40, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(Attributes.KNOCKBACK_RESISTANCE, 1.0, AttributeModifier.Operation.ADD_VALUE)
                    .tooltip(1, "Summons a powerful leaf crystal to shoot at nearby enemies", "大幅提升弩车开火速度，弩箭速度，弩箭最大穿透数，弩箭伤害")
                    .tooltip(2, "Summons a powerful leaf crystal to shoot at nearby enemies", "受到伤害时，提供弩车恐慌增益，极大幅提升弩车开火速度")
                    .build()
            );

    public static final Holder<ArmorSet> Chlorophyte =
            Register.register("chlorophyte", () -> ArmorSet.builder(Servantry.rl("chlorophyte"))
                    .piece(ServantryArmorRegister.ChlorophyteHelmet)
                    .piece(ServantryArmorRegister.ChlorophyteChestplate)
                    .piece(ServantryArmorRegister.ChlorophyteLeggings)
                    .piece(ServantryArmorRegister.ChlorophyteBoots)
                    .modifier(ServantryAttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .onStart(player -> {
                        ChlorophyteCrystal crystal = new ChlorophyteCrystal();
                        crystal.setDamage(10);
                        crystal.setKnockback(1);
                        ServantryHelper.get(player).add(EntityData.Type.ExtraServant, crystal);
                    })
                    .onRemove(player -> ServantryHelper.get(player).getEntityData().remove(EntityData.Type.ExtraServant, ServantryAttachmentEntityRegister.ChlorophyteCrystal.get()))
                    .tooltip(1, "Summons a powerful leaf crystal to shoot at nearby enemies", "召唤强大的叶状水晶来射击附近的敌人")
                    .build()
            );

    public static final Holder<ArmorSet> Spooky =
            Register.register("spooky", () -> ArmorSet.builder(Servantry.rl("spooky"))
                    .piece(ServantryArmorRegister.SpookyHelmet)
                    .piece(ServantryArmorRegister.SpookyChestplate)
                    .piece(ServantryArmorRegister.SpookyLeggings)
                    .piece(ServantryArmorRegister.SpookyBoots)
                    .modifier(ServantryAttributeRegister.ServantDamage, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(ServantryAttributeRegister.ServantArmorPierce, 12, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final Holder<ArmorSet> Tiki =
            Register.register("tiki", () -> ArmorSet.builder(Servantry.rl("tiki"))
                    .piece(ServantryArmorRegister.TikiHelmet)
                    .piece(ServantryArmorRegister.TikiChestplate)
                    .piece(ServantryArmorRegister.TikiLeggings)
                    .piece(ServantryArmorRegister.TikiBoots)
                    .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(ServantryAttributeRegister.ServantSearchRange, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final Holder<ArmorSet> Stardust =
            Register.register("stardust", () -> ArmorSet.builder(Servantry.rl("stardust"))
                    .piece(ServantryArmorRegister.StardustHelmet)
                    .piece(ServantryArmorRegister.StardustChestplate)
                    .piece(ServantryArmorRegister.StardustLeggings)
                    .piece(ServantryArmorRegister.StardustBoots)
                    .tooltip(1, "A stardust guardian will protect you from nearby enemies", "星尘守卫将保护你不受附近敌人的伤害")
                    .build()
            );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
