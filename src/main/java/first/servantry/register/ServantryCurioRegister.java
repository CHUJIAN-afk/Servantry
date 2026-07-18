package first.servantry.register;

import com.google.common.collect.ImmutableMultimap;
import first.servantry.Servantry;
import first.servantry.api.damageInfo.IDamageSourceCritical;
import first.servantry.api.item.CurioItem;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.projectile.MiniStardustCell;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.utils.CuriosUtil;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public class ServantryCurioRegister {
    
    public static final TabGroup ACCESSORY = new TabGroup(2, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));

    /**
     * 蜂巢背包
     */
    public static final DeferredItem<CurioItem> HivePack = ServantryItemRegisterBuilder.build(ACCESSORY, "hive_pack", () -> CurioItem.builder()
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .itemLanguage("Hive Pack", "蜂巢背包")
            .itemLanguageTooltip(1, "Increased power of hornet", "增加黄蜂的力量")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 矮人项链 - 仆从栏+1
     */
    public static final DeferredItem<CurioItem> PygmyNecklace = ServantryItemRegisterBuilder.build(ACCESSORY, "pygmy_necklace", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Pygmy Necklace", "矮人项链")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 大力士甲虫 - 仆从栏+1，仆从击退+50%
     */
    public static final DeferredItem<CurioItem> HerculesBeetle = ServantryItemRegisterBuilder.build(ACCESSORY, "hercules_beetle", () -> CurioItem.builder()
                    
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.RARE))
                    .build())
            .itemLanguage("Hercules Beetle", "大力士甲虫")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 学徒围巾 - 仆从数量+1，召唤伤害+10%
     */
    public static final DeferredItem<CurioItem> ApprenticesScarf = ServantryItemRegisterBuilder.build(ACCESSORY, "apprentices_scarf", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.SentryServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Apprentice's Scarf", "学徒围巾")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 女猎人圆盾 - 护甲+2，仆从数量+1
     */
    public static final DeferredItem<CurioItem> HuntressesBuckler = ServantryItemRegisterBuilder.build(ACCESSORY, "huntresses_buckler", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.SentryServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Huntress's Buckler", "女猎人圆盾")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 武僧腰带 - 召唤伤害+10%，仆从击退+50%
     */
    public static final DeferredItem<CurioItem> MonksBelt = ServantryItemRegisterBuilder.build(ACCESSORY, "monks_belt", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(ServantryAttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .build())
            .itemLanguage("Monk's Belt", "武僧腰带")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 侍卫护盾 - 护甲+2，召唤伤害+10%
     */
    public static final DeferredItem<CurioItem> SquiresShield = ServantryItemRegisterBuilder.build(ACCESSORY, "squires_shield", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Squire's Shield", "侍卫护盾")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 召唤师徽章 - 召唤伤害+15%
     */
    public static final DeferredItem<CurioItem> SummonerEmblem = ServantryItemRegisterBuilder.build(ACCESSORY, "summoner_emblem", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Summoner Emblem", "召唤师徽章")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 始源暗影焰
     */
    public static final DeferredItem<CurioItem> PrimordialShadowflame = ServantryItemRegisterBuilder.build(ACCESSORY, "primordial_shadowflame", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .onPostDamage((servant, owner, target, damageSource) -> target.addEffect(new MobEffectInstance(ServantryMobEffectRegister.Shadowflame, 60, 0)))
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Primordial Shadowflame", "始源暗影焰")
            .itemLanguageTooltip(1, "Servants inflict shadowflame on attack", "仆从攻击施加暗影焰")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 死灵卷轴 - 仆从栏+1，仆从伤害+10%
     */
    public static final DeferredItem<CurioItem> NecromanticScroll = ServantryItemRegisterBuilder.build(ACCESSORY, "necromantic_scroll", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Necromantic Scroll", "死灵卷轴")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 威胁分析仪 - 加大仆从的索敌半径
     */
    public static final DeferredItem<CurioItem> ThreatAnalyzer = ServantryItemRegisterBuilder.build(ACCESSORY, "threat_analyzer", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantSearchRange, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .itemLanguage("Threat Analyzer", "威胁分析仪")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 甲虫莎草纸 - 仆从栏+2，仆从伤害+15%，仆从击退+50%
     */
    public static final DeferredItem<CurioItem> PapyrusScarab = ServantryItemRegisterBuilder.build(ACCESSORY, "papyrus_scarab", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(ServantryAttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .recipe(output -> MithrilAnvilRecipe.builder()
                    .ingredient(NecromanticScroll)
                    .ingredient(HerculesBeetle)
                    .result(ServantryCurioRegister.PapyrusScarab)
                    .save(output))
            .itemLanguage("Papyrus Scarab", "甲虫莎草纸")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 万花筒
     */
    public static final DeferredItem<CurioItem> Kaleidoscope = ServantryItemRegisterBuilder.build(ACCESSORY, "kaleidoscope", () -> CurioItem.builder()
                    .onPreDamage((servant, owner, target, damage, damageSource) -> {
                        RandomSource random = owner.getRandom();
                        if (random.nextFloat() < 0.1f) {
                            ((IDamageSourceCritical) damageSource).servantry$setCritical(true);
                            return damage * 2f;
                        }
                        return damage;
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .itemLanguage("Kaleidoscope", "万花筒")
            .itemLanguageTooltip(1, "Servant attacks have a 10% chance to critically strike", "仆从攻击时有 10% 概率造成暴击")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 灵魂浮雕 - 仆从攻击后随机获得灵魂增益（下位互斥）
     */
    public static final DeferredItem<CurioItem> SoulRelief = ServantryItemRegisterBuilder.build(ACCESSORY, "soul_relief", () -> CurioItem.builder()
                    .onPostDamage((servant, owner, target, damageSource) -> {
                        if (!CuriosUtil.isEquipped(owner, ServantryCurioRegister.HallowedRune.get()) && !CuriosUtil.isEquipped(owner, ServantryCurioRegister.PhantasmalRelic.get())) {
                            List<Holder<MobEffect>> effects = new ArrayList<>();
                            effects.add(ServantryMobEffectRegister.SoulMight);
                            effects.add(ServantryMobEffectRegister.SoulDefense);
                            effects.add(ServantryMobEffectRegister.SoulRecovery);
                            RandomSource random = owner.getRandom();
                            owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .itemLanguage("Soul Relief", "灵魂浮雕")
            .itemLanguageTooltip(1, "Servant attacks grant a random soul boon", "仆从攻击会使你获得一个随机灵魂增益")
            .itemLanguageTooltip(2, "Soul Might: +8% Servant Damage", "灵魂力量：+8% 仆从伤害")
            .itemLanguageTooltip(3, "Soul Defense: +2 Armor", "灵魂防御：+2 护甲")
            .itemLanguageTooltip(4, "Soul Recovery: +0.05 Regeneration", "灵魂恢复：+0.05 生命再生")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 神圣符文 - 仆从攻击后随机获得神圣增益（中位互斥）
     */
    public static final DeferredItem<CurioItem> HallowedRune = ServantryItemRegisterBuilder.build(ACCESSORY, "hallowed_rune", () -> CurioItem.builder()
                    .onPostDamage((servant, owner, target, damageSource) -> {
                        if (!CuriosUtil.isEquipped(owner, ServantryCurioRegister.PhantasmalRelic.get())) {
                            List<Holder<MobEffect>> effects = new ArrayList<>();
                            effects.add(ServantryMobEffectRegister.HallowedMight);
                            effects.add(ServantryMobEffectRegister.HallowedGrace);
                            effects.add(ServantryMobEffectRegister.HallowedRadiance);
                            RandomSource random = owner.getRandom();
                            owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.RARE))
                    .build())
            .itemLanguage("Hallowed Rune", "神圣符文")
            .itemLanguageTooltip(1, "Servant attacks grant a random hallowed boon", "仆从攻击会使你获得一个随机神圣增益")
            .itemLanguageTooltip(2, "Does not stack with lower-tier soul accessories", "该效果不会与其下位合成材料叠加")
            .itemLanguageTooltip(3, "Hallowed Might: +16% Servant Damage", "神圣之力：+16% 仆从伤害")
            .itemLanguageTooltip(4, "Hallowed Grace: +4 Armor", "神圣之佑：+4 护甲")
            .itemLanguageTooltip(5, "Hallowed Radiance: +0.1 Regeneration", "神圣之辉：+0.1 生命再生")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 幻魂神物 - 仆从攻击后随机获得幻魂增益（上位）
     */
    public static final DeferredItem<CurioItem> PhantasmalRelic = ServantryItemRegisterBuilder.build(ACCESSORY, "phantasmal_relic", () -> CurioItem.builder()
                    .onPostDamage((servant, owner, target, damageSource) -> {
                        List<Holder<MobEffect>> effects = new ArrayList<>();
                        effects.add(ServantryMobEffectRegister.PhantasmalMight);
                        effects.add(ServantryMobEffectRegister.PhantasmalBulwark);
                        effects.add(ServantryMobEffectRegister.PhantasmalRebirth);
                        RandomSource random = owner.getRandom();
                        owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .itemLanguage("Phantasmal Relic", "幻魂神物")
            .itemLanguageTooltip(1, "Servant attacks grant a random phantasmal boon", "仆从攻击会使你获得一个随机幻魂增益")
            .itemLanguageTooltip(2, "Does not stack with lower-tier soul accessories", "该效果不会与其下位合成材料叠加")
            .itemLanguageTooltip(3, "Phantasmal Might: +32% Servant Damage", "幻魂之力：+32% 仆从伤害")
            .itemLanguageTooltip(4, "Phantasmal Bulwark: +8 Armor", "幻魂坚盾：+8 护甲")
            .itemLanguageTooltip(5, "Phantasmal Rebirth: +0.2 Regeneration", "幻魂还生：+0.2 生命再生")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 猎魂徽记
     */
    public static final DeferredItem<CurioItem> HuntSoulEmblem = ServantryItemRegisterBuilder.build(ACCESSORY, "hunt_soul_emblem", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .itemLanguage("Hunt Soul Emblem", "猎魂徽记")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 蚀日尊戒
     */
    public static final DeferredItem<CurioItem> EclipseRing = ServantryItemRegisterBuilder.build(ACCESSORY, "eclipse_ring", () -> CurioItem.builder()
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ServantryAttributeRegister.ServantDamage, new AttributeModifier(id, 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(ServantryAttributeRegister.ServantMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.SentryServantMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(ServantryAttributeRegister.HealthRegen, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 3, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .itemLanguage("Eclipse Ring", "蚀日尊戒")
            .itemLanguageTooltip(1, "Contains the power of the solar eclipse", "蕴含着日蚀之阴的力量")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();
    /**
     * 星尘碎片 - 仆从攻击时5%概率产生星尘细胞射弹
     */
    public static final DeferredItem<CurioItem> StardustFragment = ServantryItemRegisterBuilder.build(ACCESSORY, "stardust_fragment", () -> CurioItem.builder()
                    .onPostDamage((servant, owner, target, damageSource) -> {
                        if (owner.getRandom().nextFloat() < 0.05f) {
                            Vec3 startPos = servant.getPos();
                            MiniStardustCell projectile = new MiniStardustCell(servant.getDamageSource(), startPos);
                            projectile.setVelocity(startPos.offsetRandom(owner.getRandom(), 1)
                                                           .subtract(startPos)
                                                           .normalize()
                                                           .scale(0.25f));
                            projectile.setChaseTarget(target);
                            projectile.join(owner);
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .itemLanguage("Stardust Fragment", "星尘碎片")
            .itemLanguageTooltip(1, "Servant hits have a 5% chance to release a Stardust Cell", "仆从攻击时有 5% 概率释放星尘细胞")
            .itemModel(ServantryItemRegisterBuilder::basicModel)
            .itemTag(ServantryItemTagsRegister.Curio)
            .build();

    public static void register() {
    }
}
