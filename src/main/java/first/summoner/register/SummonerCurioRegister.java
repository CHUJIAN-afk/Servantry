package first.summoner.register;


import com.google.common.collect.ImmutableMultimap;
import first.lyra.api.LyraHelper;
import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.creativeTab.Section;
import first.lyra.register.LyraAttributeRegister;
import first.lyra.register.LyraItemRegistries;
import first.summoner.Summoner;
import first.summoner.common.item.CurioItem;
import first.summoner.common.projectile.MiniStardustCell;
import first.summoner.utils.CuriosUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public class SummonerCurioRegister {

    public static final LyraItemRegistries REGISTRIES = Summoner.REGISTRIES;

    public static final Section ACCESSORY = new Section(2, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionAccessory);

    /**
     * 蜂巢背包
     */
    public static final DeferredItem<CurioItem> HivePack = REGISTRIES.build("hive_pack", (location) -> CurioItem.builder()
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Hive Pack", "蜂巢背包")
            .itemLanguageTooltip(1, "Increased power of hornet", "增加黄蜂的力量")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 矮人项链 - 仆从栏+1
     */
    public static final DeferredItem<CurioItem> PygmyNecklace = REGISTRIES.build("pygmy_necklace", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.MinionMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Pygmy Necklace", "矮人项链")
            .jeiInfo(1, "Occasionally sold by Clerics", "牧师偶尔出售")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 大力士甲虫 - 仆从栏+1，召唤击退+50%
     */
    public static final DeferredItem<CurioItem> HerculesBeetle = REGISTRIES.build("hercules_beetle", (location) -> CurioItem.builder()
                    
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.MinionMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SummonKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.RARE))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Hercules Beetle", "大力士甲虫")
            .jeiInfo(1, "Occasionally sold by Clerics", "牧师偶尔出售")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 学徒围巾 - 仆从数量+1，召唤伤害+10%
     */
    public static final DeferredItem<CurioItem> ApprenticesScarf = REGISTRIES.build("apprentices_scarf", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.SentryMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Apprentice's Scarf", "学徒围巾")
            .jeiInfo(1, "Occasionally sold by Clerics", "牧师偶尔出售")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 女猎人圆盾 - 护甲+2，仆从数量+1
     */
    public static final DeferredItem<CurioItem> HuntressesBuckler = REGISTRIES.build("huntresses_buckler", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SentryMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Huntress's Buckler", "女猎人圆盾")
            .jeiInfo(1, "Occasionally sold by Clerics", "牧师偶尔出售")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 武僧腰带 - 召唤伤害+10%，召唤击退+50%
     */
    public static final DeferredItem<CurioItem> MonksBelt = REGISTRIES.build("monks_belt", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(LyraAttributeRegister.SummonKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Monk's Belt", "武僧腰带")
            .jeiInfo(1, "Occasionally sold by Clerics", "牧师偶尔出售")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 侍卫护盾 - 护甲+2，召唤伤害+10%
     */
    public static final DeferredItem<CurioItem> SquiresShield = REGISTRIES.build("squires_shield", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Squire's Shield", "侍卫护盾")
            .jeiInfo(1, "Occasionally sold by Clerics", "牧师偶尔出售")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 召唤师徽章 - 召唤伤害+15%
     */
    public static final DeferredItem<CurioItem> SummonerEmblem = REGISTRIES.build("summoner_emblem", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Summoner Emblem", "召唤师徽章")
            .jeiInfo(1, "Occasionally drops from Evokers", "唤魔者偶尔掉落")
            .lootTable(ResourceLocation.withDefaultNamespace("entities/evoker"), LootTable -> LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(SummonerCurioRegister.SummonerEmblem.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.1f)))
                    .build())
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 始源暗影焰
     */
    public static final DeferredItem<CurioItem> PrimordialShadowflame = REGISTRIES.build("primordial_shadowflame", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.MinionMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .onPostDamage((minion, owner, target, damageSource) -> target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.Shadowflame, 60, 0)))
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Primordial Shadowflame", "始源暗影焰")
            .itemLanguageTooltip(1, "Minions inflict shadowflame on attack", "仆从攻击施加暗影焰")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 死灵卷轴 - 仆从栏+1，召唤伤害+10%
     */
    public static final DeferredItem<CurioItem> NecromanticScroll = REGISTRIES.build("necromantic_scroll", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.MinionMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Necromantic Scroll", "死灵卷轴")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 威胁分析仪 - 加大仆从的索敌半径
     */
    public static final DeferredItem<CurioItem> ThreatAnalyzer = REGISTRIES.build("threat_analyzer", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.SummonSearchRange, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Threat Analyzer", "威胁分析仪")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 甲虫莎草纸 - 仆从栏+2，召唤伤害+15%，召唤击退+50%
     */
    public static final DeferredItem<CurioItem> PapyrusScarab = REGISTRIES.build("papyrus_scarab", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.MinionMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(LyraAttributeRegister.SummonKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Papyrus Scarab", "甲虫莎草纸")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 灵魂浮雕 - 仆从攻击后随机获得灵魂增益（下位互斥）
     */
    public static final DeferredItem<CurioItem> SoulRelief = REGISTRIES.build("soul_relief", (location) -> CurioItem.builder()
                    .onPostDamage((minion, owner, target, damageSource) -> {
                        if (!CuriosUtil.isEquipped(owner, SummonerCurioRegister.HallowedRune.get()) && !CuriosUtil.isEquipped(owner, SummonerCurioRegister.PhantasmalRelic.get())) {
                            List<Holder<MobEffect>> effects = new ArrayList<>();
                            effects.add(SummonerMobEffectRegister.SoulMight);
                            effects.add(SummonerMobEffectRegister.SoulDefense);
                            effects.add(SummonerMobEffectRegister.SoulRecovery);
                            RandomSource random = minion.getRandom();
                            owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Soul Relief", "灵魂浮雕")
            .itemLanguageTooltip(1, "Minion attacks grant a random soul boon", "仆从攻击会使你获得一个随机灵魂增益")
            .itemLanguageTooltip(2, "Soul Might: +8% Minion Damage", "灵魂力量：+8% 召唤伤害")
            .itemLanguageTooltip(3, "Soul Defense: +2 Armor", "灵魂防御：+2 护甲")
            .itemLanguageTooltip(4, "Soul Recovery: +0.05 Regeneration", "灵魂恢复：+0.05 生命再生")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 神圣符文 - 仆从攻击后随机获得神圣增益（中位互斥）
     */
    public static final DeferredItem<CurioItem> HallowedRune = REGISTRIES.build("hallowed_rune", (location) -> CurioItem.builder()
                    .onPostDamage((minion, owner, target, damageSource) -> {
                        if (!CuriosUtil.isEquipped(owner, SummonerCurioRegister.PhantasmalRelic.get())) {
                            List<Holder<MobEffect>> effects = new ArrayList<>();
                            effects.add(SummonerMobEffectRegister.HallowedMight);
                            effects.add(SummonerMobEffectRegister.HallowedGrace);
                            effects.add(SummonerMobEffectRegister.HallowedRadiance);
                            RandomSource random = minion.getRandom();
                            owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.RARE))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Hallowed Rune", "神圣符文")
            .itemLanguageTooltip(1, "Minion attacks grant a random hallowed boon", "仆从攻击会使你获得一个随机神圣增益")
            .itemLanguageTooltip(2, "Does not stack with lower-tier soul accessories", "该效果不会与其下位合成材料叠加")
            .itemLanguageTooltip(3, "Hallowed Might: +16% Minion Damage", "神圣之力：+16% 召唤伤害")
            .itemLanguageTooltip(4, "Hallowed Grace: +4 Armor", "神圣之佑：+4 护甲")
            .itemLanguageTooltip(5, "Hallowed Radiance: +0.1 Regeneration", "神圣之辉：+0.1 生命再生")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 幻魂神物 - 仆从攻击后随机获得幻魂增益（上位）
     */
    public static final DeferredItem<CurioItem> PhantasmalRelic = REGISTRIES.build("phantasmal_relic", (location) -> CurioItem.builder()
                    .onPostDamage((minion, owner, target, damageSource) -> {
                        List<Holder<MobEffect>> effects = new ArrayList<>();
                        effects.add(SummonerMobEffectRegister.PhantasmalMight);
                        effects.add(SummonerMobEffectRegister.PhantasmalBulwark);
                        effects.add(SummonerMobEffectRegister.PhantasmalRebirth);
                        RandomSource random = minion.getRandom();
                        owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Phantasmal Relic", "幻魂神物")
            .itemLanguageTooltip(1, "Minion attacks grant a random phantasmal boon", "仆从攻击会使你获得一个随机幻魂增益")
            .itemLanguageTooltip(2, "Does not stack with lower-tier soul accessories", "该效果不会与其下位合成材料叠加")
            .itemLanguageTooltip(3, "Phantasmal Might: +32% Minion Damage", "幻魂之力：+32% 召唤伤害")
            .itemLanguageTooltip(4, "Phantasmal Bulwark: +8 Armor", "幻魂坚盾：+8 护甲")
            .itemLanguageTooltip(5, "Phantasmal Rebirth: +0.2 Regeneration", "幻魂还生：+0.2 生命再生")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 猎魂徽记
     */
    public static final DeferredItem<CurioItem> HuntSoulEmblem = REGISTRIES.build("hunt_soul_emblem", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Hunt Soul Emblem", "猎魂徽记")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 蚀日尊戒
     */
    public static final DeferredItem<CurioItem> EclipseRing = REGISTRIES.build("eclipse_ring", (location) -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(LyraAttributeRegister.SummonDamage, new AttributeModifier(id, 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(LyraAttributeRegister.MinionMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SentryMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.SummonKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(LyraAttributeRegister.HealthRegen, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 3, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Eclipse Ring", "蚀日尊戒")
            .itemLanguageTooltip(1, "Contains the power of the solar eclipse", "蕴含着日蚀之阴的力量")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();
    /**
     * 星尘碎片 - 仆从攻击时5%概率产生星尘细胞射弹
     */
    public static final DeferredItem<CurioItem> StardustFragment = REGISTRIES.build("stardust_fragment", (location) -> CurioItem.builder()
                    .onPostDamage((minion, owner, target, damageSource) -> {
                        if (minion.getRandom().nextFloat() < 0.05f) {
                            Vec3 startPos = minion.getPos();
                            Vec3 direction = startPos.offsetRandom(minion.getRandom(), 1).subtract(startPos).normalize();
                            MiniStardustCell projectile = new MiniStardustCell();
                            projectile.setOwner(owner);
                            projectile.init(new PathNode(startPos, direction));
                            projectile.copyAttributes(minion);
                            projectile.setVelocity(direction.scale(0.25f));
                            projectile.setChaseTarget(target);
                            LyraHelper.get(owner).add(projectile);
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build(location))
            .itemTag(SummonerItemTagsRegister.SectionAccessory)
            .itemLanguage("Stardust Fragment", "星尘碎片")
            .itemLanguageTooltip(1, "Minion hits have a 5% chance to release a Stardust Cell", "仆从攻击时有 5% 概率释放星尘细胞")
            .itemModel(ItemModelProvider::basicItem)
            .itemTag(SummonerItemTagsRegister.Curio)
            .build();

    public static void register() {
    }
}
