package first.servantry.register;

import com.google.common.collect.ImmutableMultimap;
import first.servantry.Servantry;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.item.CurioItem;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.utils.CuriosUtil;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public class CurioRegister {

    private static final Registers Register = Registers.getInstance();

    public static final TabGroup ACCESSORY = new TabGroup(2, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));

    /**
     * 矮人项链 - 仆从栏+1
     */
    public static final DeferredItem<Item> PygmyNecklace = Register.register(ACCESSORY, "pygmy_necklace", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .language("Pygmy Necklace", "矮人项链")
            .build();
    /**
     * 大力士甲虫 - 仆从栏+1，仆从击退+50%
     */
    public static final DeferredItem<Item> HerculesBeetle = Register.register(ACCESSORY, "hercules_beetle", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.RARE))
                    .build())
            .language("Hercules Beetle", "大力士甲虫")
            .build();
    /**
     * 学徒围巾 - 仆从数量+1，召唤伤害+10%
     */
    public static final DeferredItem<Item> ApprenticesScarf = Register.register(ACCESSORY, "apprentices_scarf", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .language("Apprentice's Scarf", "学徒围巾")
            .build();
    /**
     * 女猎人圆盾 - 护甲+2，仆从数量+1
     */
    public static final DeferredItem<Item> HuntressesBuckler = Register.register(ACCESSORY, "huntresses_buckler", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .language("Huntress's Buckler", "女猎人圆盾")
            .build();
    /**
     * 武僧腰带 - 召唤伤害+10%，仆从击退+50%
     */
    public static final DeferredItem<Item> MonksBelt = Register.register(ACCESSORY, "monks_belt", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .build())
            .language("Monk's Belt", "武僧腰带")
            .build();
    /**
     * 侍卫护盾 - 护甲+2，召唤伤害+10%
     */
    public static final DeferredItem<Item> SquiresShield = Register.register(ACCESSORY, "squires_shield", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .language("Squire's Shield", "侍卫护盾")
            .build();
    /**
     * 召唤师徽章 - 召唤伤害+15%
     */
    public static final DeferredItem<Item> SummonerEmblem = Register.register(ACCESSORY, "summoner_emblem", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .language("Summoner Emblem", "召唤师徽章")
            .build();
    /**
     * 始源暗影焰
     */
    public static final DeferredItem<Item> PrimordialShadowflame = Register.register(ACCESSORY, "primordial_shadowflame", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .onPostDamage((servant, owner, target) -> target.addEffect(new MobEffectInstance(MobEffectRegister.Shadowflame, 60, 0)))
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .language("Primordial Shadowflame", "始源暗影焰")
            .tooltip(1, "Servants inflict shadowflame on attack", "仆从攻击施加暗影焰")
            .build();
    /**
     * 死灵卷轴 - 仆从栏+1，仆从伤害+10%
     */
    public static final DeferredItem<Item> NecromanticScroll =
            Register.register(ACCESSORY, "necromantic_scroll",
                              () -> CurioItem.builder()
                                      .canEquipFromUse(true)
                                      .attributeModifiers((slotContext, id, stack) -> {
                                          ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                                          builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
                                          builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                                          return builder.build();
                                      })
                                      .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                                      .build())
                    .language("Necromantic Scroll", "死灵卷轴")
                    .build();
    /**
     * 威胁分析仪 - 加大仆从的索敌半径
     */
    public static final DeferredItem<Item> ThreatAnalyzer = Register.register(ACCESSORY, "threat_analyzer", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantSearchRange, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .language("Threat Analyzer", "威胁分析仪")
            .build();
    /**
     * 甲虫莎草纸 - 仆从栏+2，仆从伤害+15%，仆从击退+50%
     */
    public static final DeferredItem<Item> PapyrusScarab = Register.register(ACCESSORY, "papyrus_scarab", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .recipe(output -> MithrilAnvilRecipe.builder()
                    .ingredient(NecromanticScroll)
                    .ingredient(HerculesBeetle)
                    .result(CurioRegister.PapyrusScarab)
                    .save(output))
            .language("Papyrus Scarab", "甲虫莎草纸")
            .build();
    /**
     * 万花筒
     */
    public static final DeferredItem<Item> Kaleidoscope = Register.register(ACCESSORY, "kaleidoscope", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .onServantDamage((servant, owner, target, damage) -> {
                        if (owner.getRandom()
                                .nextFloat() < 0.1f) {
                            return damage * 1.5f;
                        }
                        return damage;
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .language("Kaleidoscope", "万花筒")
            .tooltip(1, "Servant attacks have a 10% chance to critically strike", "仆从攻击时有 10% 概率造成暴击")
            .build();
    /**
     * 灵魂浮雕 - 仆从攻击后随机获得灵魂增益（下位互斥）
     */
    public static final DeferredItem<Item> SoulRelief = Register.register(ACCESSORY, "soul_relief", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .onPostDamage((servant, owner, target) -> {
                        if (!CuriosUtil.isEquipped(owner, CurioRegister.HallowedRune.get()) && !CuriosUtil.isEquipped(owner, CurioRegister.PhantasmalRelic.get())) {
                            List<Holder<MobEffect>> effects = new ArrayList<>();
                            effects.add(MobEffectRegister.SoulMight);
                            effects.add(MobEffectRegister.SoulDefense);
                            effects.add(MobEffectRegister.SoulRecovery);
                            RandomSource random = owner.getRandom();
                            owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.UNCOMMON))
                    .build())
            .language("Soul Relief", "灵魂浮雕")
            .tooltip(1, "Servant attacks grant a random soul boon", "仆从攻击会使你获得一个随机灵魂增益")
            .tooltip(2, "Soul Might: +8% Servant Damage", "灵魂力量：+8% 仆从伤害")
            .tooltip(3, "Soul Defense: +2 Armor", "灵魂防御：+2 护甲")
            .tooltip(4, "Soul Recovery: +0.05 Regeneration", "灵魂恢复：+0.05 生命再生")
            .build();
    /**
     * 神圣符文 - 仆从攻击后随机获得神圣增益（中位互斥）
     */
    public static final DeferredItem<Item> HallowedRune = Register.register(ACCESSORY, "hallowed_rune", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .onPostDamage((servant, owner, target) -> {
                        if (!CuriosUtil.isEquipped(owner, CurioRegister.PhantasmalRelic.get())) {
                            List<Holder<MobEffect>> effects = new ArrayList<>();
                            effects.add(MobEffectRegister.HallowedMight);
                            effects.add(MobEffectRegister.HallowedGrace);
                            effects.add(MobEffectRegister.HallowedRadiance);
                            RandomSource random = owner.getRandom();
                            owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                        }
                    })
                    .properties(properties -> properties.rarity(Rarity.RARE))
                    .build())
            .language("Hallowed Rune", "神圣符文")
            .tooltip(1, "Servant attacks grant a random hallowed boon", "仆从攻击会使你获得一个随机神圣增益")
            .tooltip(2, "Does not stack with lower-tier soul accessories", "该效果不会与其下位合成材料叠加")
            .tooltip(3, "Hallowed Might: +16% Servant Damage", "神圣之力：+16% 仆从伤害")
            .tooltip(4, "Hallowed Grace: +4 Armor", "神圣之佑：+4 护甲")
            .tooltip(5, "Hallowed Radiance: +0.1 Regeneration", "神圣之辉：+0.1 生命再生")
            .build();
    /**
     * 幻魂神物 - 仆从攻击后随机获得幻魂增益（上位）
     */
    public static final DeferredItem<Item> PhantasmalRelic = Register.register(ACCESSORY, "phantasmal_relic", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .onPostDamage((servant, owner, target) -> {
                        List<Holder<MobEffect>> effects = new ArrayList<>();
                        effects.add(MobEffectRegister.PhantasmalMight);
                        effects.add(MobEffectRegister.PhantasmalBulwark);
                        effects.add(MobEffectRegister.PhantasmalRebirth);
                        RandomSource random = owner.getRandom();
                        owner.addEffect(new MobEffectInstance(effects.get(random.nextInt(effects.size())), 60));
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .language("Phantasmal Relic", "幻魂神物")
            .tooltip(1, "Servant attacks grant a random phantasmal boon", "仆从攻击会使你获得一个随机幻魂增益")
            .tooltip(2, "Does not stack with lower-tier soul accessories", "该效果不会与其下位合成材料叠加")
            .tooltip(3, "Phantasmal Might: +32% Servant Damage", "幻魂之力：+32% 仆从伤害")
            .tooltip(4, "Phantasmal Bulwark: +8 Armor", "幻魂坚盾：+8 护甲")
            .tooltip(5, "Phantasmal Rebirth: +0.2 Regeneration", "幻魂还生：+0.2 生命再生")
            .build();
    /**
     * 猎魂徽记
     */
    public static final DeferredItem<Item> HuntSoulEmblem = Register.register(ACCESSORY, "hunt_soul_emblem", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .language("Hunt Soul Emblem", "猎魂徽记")
            .build();
    /**
     * 蚀日尊戒
     */
    public static final DeferredItem<Item> EclipseRing = Register.register(ACCESSORY, "eclipse_ring", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .attributeModifiers((slotContext, id, stack) -> {
                        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(AttributeRegister.ServantDamage, new AttributeModifier(id, 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(AttributeRegister.ServantMaxCount, new AttributeModifier(id, 2, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(AttributeRegister.ServantKnockback, new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(AttributeRegister.HealthRegen, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_VALUE));
                        builder.put(Attributes.ARMOR, new AttributeModifier(id, 3, AttributeModifier.Operation.ADD_VALUE));
                        return builder.build();
                    })
                    .properties(properties -> properties.rarity(Rarity.EPIC))
                    .build())
            .language("Eclipse Ring", "蚀日尊戒")
            .tooltip(1, "Contains the power of the solar eclipse", "蕴含着日蚀之阴的力量")
            .build();
    /**
     * 星尘碎片 - 仆从攻击时5%概率产生星尘细胞射弹
     */
    public static final DeferredItem<Item> StardustFragment = Register.register(ACCESSORY, "stardust_fragment", () -> CurioItem.builder()
                    .canEquipFromUse(true)
                    .onPostDamage((servant, owner, target) -> {
                        if (owner.getRandom().nextFloat() < 0.05f) {
                            Vec3 startPos = servant.getPos();
                            StardustProjectile projectile = new StardustProjectile(servant.getDamageSource(), startPos);
                            projectile.setDamage(servant.getDamage());
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
            .language("Stardust Fragment", "星尘碎片")
            .tooltip(1, "Servant hits have a 5% chance to release a Stardust Cell", "仆从攻击时有 5% 概率释放星尘细胞")
            .build();
    public static void register() {
    }
}
