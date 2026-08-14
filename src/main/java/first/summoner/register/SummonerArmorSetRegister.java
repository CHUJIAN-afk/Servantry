package first.summoner.register;

import first.lyra.api.LyraHelper;
import first.lyra.common.armorSet.ArmorSet;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.register.LyraAttributeRegister;
import first.lyra.register.LyraRegistries;
import first.summoner.Summoner;
import first.summoner.common.servant.ChlorophyteCrystal;
import first.summoner.config.StartupConfig;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SummonerArmorSetRegister {

    private static final DeferredRegister<ArmorSet> Register = DeferredRegister.create(LyraRegistries.ARMOR_SETS, Summoner.MODID);

    public static final DeferredHolder<ArmorSet, ArmorSet> Bee =
            Register.register("bee", () -> ArmorSet.builder(Summoner.rl("bee"))
                    .piece(SummonerArmorRegister.BeeHeadgear)
                    .piece(SummonerArmorRegister.BeeChestplate)
                    .piece(SummonerArmorRegister.BeeLeggings)
                    .piece(SummonerArmorRegister.BeeBoots)
                    .modifier(LyraAttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Spider =
            Register.register("spider", () -> ArmorSet.builder(Summoner.rl("spider"))
                    .piece(SummonerArmorRegister.SpiderMask)
                    .piece(SummonerArmorRegister.SpiderChestplate)
                    .piece(SummonerArmorRegister.SpiderLeggings)
                    .piece(SummonerArmorRegister.SpiderBoots)
                    .modifier(LyraAttributeRegister.ServantDamage, 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Forbidden =
            Register.register("forbidden", () -> ArmorSet.builder(Summoner.rl("forbidden"))
                    .piece(SummonerArmorRegister.ForbiddenMask)
                    .piece(SummonerArmorRegister.ForbiddenRobe)
                    .piece(SummonerArmorRegister.ForbiddenLeggings)
                    .piece(SummonerArmorRegister.ForbiddenBoots)
                    .tooltip(1, "Allows you to summon an ancient storm to attract nearby enemies", "允许你召唤远古风暴吸引附近的敌人")
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Obsidian =
            Register.register("obsidian", () -> ArmorSet.builder(Summoner.rl("obsidian"))
                    .piece(SummonerArmorRegister.ObsidianHelmet)
                    .piece(SummonerArmorRegister.ObsidianChestplate)
                    .piece(SummonerArmorRegister.ObsidianLeggings)
                    .piece(SummonerArmorRegister.ObsidianBoots)
                    .modifier(Attributes.MOVEMENT_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(LyraAttributeRegister.ServantDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(LyraAttributeRegister.ServantSearchRange, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Hallowed = Register.register("hallowed", () -> ArmorSet.builder(Summoner.rl("hallowed"))
            .piece(SummonerArmorRegister.HallowedHelmet)
            .piece(SummonerArmorRegister.HallowedChestplate)
            .piece(SummonerArmorRegister.HallowedLeggings)
            .piece(SummonerArmorRegister.HallowedBoots)
            .modifier(LyraAttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
            .modifier(LyraAttributeRegister.ServantArmorPierce, 4, AttributeModifier.Operation.ADD_VALUE)
            .tooltip(1, "Servants grant I-frames on attack", "仆从攻击使敌人高亮")
            .build()
    );

    public static final DeferredHolder<ArmorSet, ArmorSet> ValhallaKnight =
            Register.register("valhalla_knight", () -> ArmorSet.builder(Summoner.rl("valhalla_knight"))
                    .piece(SummonerArmorRegister.ValhallaKnightHelmet)
                    .piece(SummonerArmorRegister.ValhallaKnightChestplate)
                    .piece(SummonerArmorRegister.ValhallaKnightLeggings)
                    .piece(SummonerArmorRegister.ValhallaKnightBoots)
                    .modifier(LyraAttributeRegister.SentryServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(LyraAttributeRegister.ServantDamage, 0.40, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(Attributes.KNOCKBACK_RESISTANCE, 1.0, AttributeModifier.Operation.ADD_VALUE)
                    .tooltip(1, "Summons a powerful leaf crystal to shoot at nearby enemies", "大幅提升弩车开火速度，弩箭速度，弩箭最大穿透数，弩箭伤害")
                    .tooltip(2, "Summons a powerful leaf crystal to shoot at nearby enemies", "受到伤害时，提供弩车恐慌增益，极大幅提升弩车开火速度")
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Chlorophyte =
            Register.register("chlorophyte", () -> ArmorSet.builder(Summoner.rl("chlorophyte"))
                    .piece(SummonerArmorRegister.ChlorophyteHelmet)
                    .piece(SummonerArmorRegister.ChlorophyteChestplate)
                    .piece(SummonerArmorRegister.ChlorophyteLeggings)
                    .piece(SummonerArmorRegister.ChlorophyteBoots)
                    .modifier(LyraAttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                    .onStart(player -> {
                        ChlorophyteCrystal crystal = new ChlorophyteCrystal();
                        crystal.setDamage(StartupConfig.ChlorophyteCrystalDamage.get());
                        crystal.setKnockback(StartupConfig.ChlorophyteCrystalDamage.get());
                        crystal.setArmorPierce(StartupConfig.ChlorophyteCrystalArmorPierce.get());
                        LyraHelper.get(player).add(AttachmentEntityData.Type.ExtraServant, crystal);
                    })
                    .onRemove(player -> LyraHelper.get(player).getEntityData().remove(AttachmentEntityData.Type.ExtraServant, SummonerAttachmentEntityRegister.CHLOROPHYTE_CRYSTAL.get()))
                    .tooltip(1, "Summons a powerful leaf crystal to shoot at nearby enemies", "召唤强大的叶状水晶来射击附近的敌人")
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Spooky =
            Register.register("spooky", () -> ArmorSet.builder(Summoner.rl("spooky"))
                    .piece(SummonerArmorRegister.SpookyHelmet)
                    .piece(SummonerArmorRegister.SpookyChestplate)
                    .piece(SummonerArmorRegister.SpookyLeggings)
                    .piece(SummonerArmorRegister.SpookyBoots)
                    .modifier(LyraAttributeRegister.ServantDamage, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .modifier(LyraAttributeRegister.ServantArmorPierce, 12, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Tiki =
            Register.register("tiki", () -> ArmorSet.builder(Summoner.rl("tiki"))
                    .piece(SummonerArmorRegister.TikiHelmet)
                    .piece(SummonerArmorRegister.TikiChestplate)
                    .piece(SummonerArmorRegister.TikiLeggings)
                    .piece(SummonerArmorRegister.TikiBoots)
                    .modifier(LyraAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                    .modifier(LyraAttributeRegister.ServantSearchRange, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final DeferredHolder<ArmorSet, ArmorSet> Stardust =
            Register.register("stardust", () -> ArmorSet.builder(Summoner.rl("stardust"))
                    .piece(SummonerArmorRegister.StardustHelmet)
                    .piece(SummonerArmorRegister.StardustChestplate)
                    .piece(SummonerArmorRegister.StardustLeggings)
                    .piece(SummonerArmorRegister.StardustBoots)
                    .tooltip(1, "A stardust guardian will protect you from nearby enemies", "星尘守卫将保护你不受附近敌人的伤害")
                    .build()
            );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
