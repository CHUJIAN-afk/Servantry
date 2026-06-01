package first.servantry.dadageneeator.provider;

import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.register.AttributeRegister;
import first.servantry.register.ItemRegister;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nullable;

public class ServantryLanguageProvider extends LanguageProvider {

    private final String locale;

    public ServantryLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        // ================= 模组基础 =================
        entry("modid.servantry")
                .en("Servantry")
                .zh("仆从学");
        entry("item.servantry.tooltip.set_bonus_title")
                .en("Set Rewards:")
                .zh("套装奖励:");
        entry("item.servantry.tooltip.damage")
                .en("Summon Damage")
                .zh("召唤伤害");
        entry("item.servantry.tooltip.knockback")
                .en("Knockback")
                .zh("击退强度");
        entry("item.servantry.tooltip.summon")
                .en("Summons %s to fight for you")
                .zh("召唤 %s 为你而战");
        entry("item.servantry.tooltip.slots")
                .en("Servant Slots: %s / %s")
                .zh("仆从栏位: %s / %s");
        entry("item.servantry.tooltip.remove_all")
                .en("Sneak + Use to dismiss all servants of this type")
                .zh("潜行右键以遣散该类型仆从");
        entry("item.servantry.tooltip.code_wip")
                .en("[WIP - Code not yet implemented]")
                .zh("[未完成 - 代码尚未实现]");
        entry("item.servantry.tooltip.art_wip")
                .en("[WIP - Art not yet implemented]")
                .zh("[未完成 - 美术尚未实现]");
        entry("death.attack.servantry.servant")
                .en("%1$s was torn apart by a servant")
                .zh("%1$s 被仆从撕碎");
        entry("death.attack.servantry.servant.player")
                .en("%1$s was torn apart by a servant whilst fighting %2$s")
                .zh("%1$s 在与 %2$s 战斗时被仆从撕碎");
        // ================= 仆从武器 =================
        entry(ItemRegister.TerraPrism)
                .en("Terraprism").zh("泰拉棱镜")
                .servant().en("Terraprism").zh("泰拉棱镜")
                .tooltip().en("'A flawless blade once hailed as the 'Prism of the Earth'").zh("曾被冠以'大地棱彩'美名的无暇之剑");
        entry(ItemRegister.InfiniteScabbard)
                .en("Infinite Scabbard").zh("无限剑鞘")
                .servant().en("Infinite Shadow").zh("无限之影")
                .tooltip().en("'A scabbard that stores a blade of infinite potential'").zh("蕴含无限可能之'剑'的剑鞘")
                .tooltip().en("Right-click an item to store, right-click an empty slot to retrieve").zh("右键物品存入，右键空格子取出");
        entry(ItemRegister.BladeStaff)
                .en("Blade Staff").zh("刃杖")
                .servant().en("Enchanted Throwing Knives").zh("附魔飞刀")
                .tooltip().en("Ignores 2.5 points of enemy Defense").zh("忽略敌人 2.5 防御力")
                .tooltip().en("'Don't let their small size fool you'").zh("'别被它们小小的个头给骗了'");
        entry(ItemRegister.StardustCellStaff)
                .en("Stardust Cell Staff").zh("星尘细胞法杖")
                .servant().en("Stardust Cell").zh("星尘细胞")
                .tooltip().en("'Cultivate the most beautiful cellular infection'").zh("'培养最美丽的细胞感染'");
        entry(ItemRegister.StardustDragonStaff)
                .en("Stardust Dragon Staff").zh("星尘之龙法杖")
                .servant().en("Stardust Dragon").zh("星尘之龙")
                .tooltip().en("'When you have a dragon, who needs a swarm?'").zh("'有了一条巨龙后，谁还需要一群仆从呢？'");
        entry(ItemRegister.BlackLens)
                .en("Black Lens")
                .zh("黑色晶状体");
        entry(ItemRegister.OpticStaff)
                .en("Optic Staff").zh("魔眼法杖")
                .servant().en("Twins").zh("双子魔眼");
        entry(ItemRegister.TempestStaff)
                .en("Tempest Staff").zh("暴风雨法杖")
                .servant().en("Sharknado").zh("鲨鱼龙卷");
        entry(ItemRegister.DeadlySphereStaff)
                .en("Deadly Sphere Staff").zh("致命球法杖")
                .servant().en("Deadly Sphere").zh("致命球");
        entry(ItemRegister.EtherealStellarCoreStaff)
                .en("Ethereal Stellar Core Staff").zh("缥缈星核法杖")
                .servant().en("Ethereal Stellar Core").zh("缥缈星核")
                .tooltip().en("Summons up to 9 Ethereal Stellar Cores").zh("最多召唤9个缥缈星核");
        entry(ItemRegister.SurveyDroneRemote)
                .en("Survey Drone Remote").zh("矿勘无人机遥控器")
                .servant().en("Survey Drone").zh("矿勘无人机")
                .tooltip().en("Summons up to 1 Survey Drone").zh("最多召唤1架矿勘无人机");
        entry(ItemRegister.FairyBell)
                .en("Fairy Bell").zh("妖精铃铛")
                .servant().en("Scavenger Fairy").zh("拾荒妖精")
                .tooltip().en("Summons up to 1 Scavenger Fairy").zh("最多召唤1只拾荒妖精");
        // ================= 属性 =================
        entry(AttributeRegister.ServantMaxCount.value().getDescriptionId())
                .en("Max Servants")
                .zh("仆从栏");
        entry(AttributeRegister.ServantDamage.value().getDescriptionId())
                .en("Servant Damage")
                .zh("仆从伤害");
        entry(AttributeRegister.ServantKnockback.value().getDescriptionId())
                .en("Servant Knockback")
                .zh("仆从击退");
        entry(AttributeRegister.ServantArmorPierce.value().getDescriptionId())
                .en("Servant Armor Pierce")
                .zh("仆从护甲穿透");
        entry(AttributeRegister.ServantSearchRange.value().getDescriptionId())
                .en("Servant Search Range")
                .zh("仆从索敌范围");
        entry(AttributeRegister.HealthRegen.value().getDescriptionId())
                .en("Health Regen")
                .zh("生命再生");
        // ===================== 饰品 =====================
        entry("curios.curios.accessories")
                .en("Accessories")
                .zh("配饰");
        entry(ItemRegister.NecromanticScroll)
                .en("Necromantic Scroll")
                .zh("死灵卷轴");
        entry(ItemRegister.PapyrusScarab)
                .en("Papyrus Scarab")
                .zh("甲虫莎草纸");
        entry(ItemRegister.PygmyNecklace)
                .en("Pygmy Necklace")
                .zh("矮人项链");
        entry(ItemRegister.HerculesBeetle)
                .en("Hercules Beetle")
                .zh("大力士甲虫");
        entry(ItemRegister.SummonerEmblem)
                .en("Summoner Emblem")
                .zh("召唤师徽章");
        entry(ItemRegister.ApprenticesScarf)
                .en("Apprentice's Scarf")
                .zh("学徒围巾");
        entry(ItemRegister.HuntressesBuckler)
                .en("Huntress's Buckler")
                .zh("女猎人圆盾");
        entry(ItemRegister.MonksBelt)
                .en("Monk's Belt")
                .zh("武僧腰带");
        entry(ItemRegister.SquiresShield)
                .en("Squire's Shield")
                .zh("侍卫护盾");
        entry(ItemRegister.SoulRelief)
                .en("Soul Relief")
                .zh("灵魂浮雕")
                .tooltip().en("Servant attacks grant a random soul boon").zh("仆从攻击会使你获得一个随机灵魂增益")
                .tooltip().en("Soul Might: +8% Servant Damage").zh("灵魂力量：+8% 仆从伤害")
                .tooltip().en("Soul Defense: +2 Armor").zh("灵魂防御：+2 护甲")
                .tooltip().en("Soul Recovery: +0.05 Regeneration").zh("灵魂恢复：+0.05 生命再生");
        entry(ItemRegister.HallowedRune)
                .en("Hallowed Rune")
                .zh("神圣符文")
                .tooltip().en("Servant attacks grant a random hallowed boon").zh("仆从攻击会使你获得一个随机神圣增益")
                .tooltip().en("Does not stack with lower-tier soul accessories").zh("该效果不会与其下位合成材料叠加")
                .tooltip().en("Hallowed Might: +16% Servant Damage").zh("神圣之力：+16% 仆从伤害")
                .tooltip().en("Hallowed Grace: +4 Armor").zh("神圣之佑：+4 护甲")
                .tooltip().en("Hallowed Radiance: +0.1 Regeneration").zh("神圣之辉：+0.1 生命再生");
        entry(ItemRegister.PhantasmalRelic)
                .en("Phantasmal Relic")
                .zh("幻魂神物")
                .tooltip().en("Servant attacks grant a random phantasmal boon").zh("仆从攻击会使你获得一个随机幻魂增益")
                .tooltip().en("Does not stack with lower-tier soul accessories").zh("该效果不会与其下位合成材料叠加")
                .tooltip().en("Phantasmal Might: +32% Servant Damage").zh("幻魂之力：+32% 仆从伤害")
                .tooltip().en("Phantasmal Bulwark: +8 Armor").zh("幻魂坚盾：+8 护甲")
                .tooltip().en("Phantasmal Rebirth: +0.2 Regeneration").zh("幻魂还生：+0.2 生命再生");
        entry(ItemRegister.EclipseRing)
                .en("Eclipse Ring")
                .zh("蚀日尊戒")
                .tooltip().en("Contains the power of the solar eclipse").zh("'蕴含着日蚀之阴的力量'");
        entry(ItemRegister.PrimordialShadowflame)
                .en("Primordial Shadowflame")
                .zh("始源暗影焰")
                .tooltip().en("Servants inflict shadowflame on attack").zh("仆从攻击施加暗影焰");
        entry(ItemRegister.Kaleidoscope)
                .en("Kaleidoscope")
                .zh("万花筒")
                .tooltip().en("Servant attacks have a 10% chance to critically strike").zh("仆从攻击时有 10% 概率造成暴击");
        entry(ItemRegister.HuntSoulEmblem)
                .en("Hunt Soul Emblem")
                .zh("猎魂徽记");
        entry(ItemRegister.StardustFragment)
                .en("Stardust Fragment")
                .zh("星尘碎片")
                .tooltip().en("Servant hits have a 5% chance to release a Stardust Cell").zh("仆从攻击时有 5% 概率释放星尘细胞");
        entry(ItemRegister.ThreatAnalyzer)
                .en("Threat Analyzer")
                .zh("威胁分析仪");
        // ===================== 护甲 =====================
        entry(ItemRegister.HallowedIngot)
                .en("Hallowed Ingot")
                .zh("神圣锭");
        entry(ItemRegister.HallowedHelmet)
                .en("Hallowed Helmet")
                .zh("神圣兜帽");
        entry(ItemRegister.HallowedChestplate)
                .en("Hallowed Chestplate")
                .zh("神圣板甲");
        entry(ItemRegister.HallowedLeggings)
                .en("Hallowed Leggings")
                .zh("神圣护胫");
        entry(ItemRegister.HallowedBoots)
                .en("Hallowed Boots")
                .zh("神圣战靴");
        entry("servantry.servantry.hallowed.set.1")
                .en("Servants grant I-frames on attack")
                .zh("仆从攻击使敌人高亮");
        // ===================== 黑曜石护甲 =====================
        entry(ItemRegister.FlinxFurCoat)
                .en("Flinx Fur Coat")
                .zh("小雪怪皮毛外套");
        entry(ItemRegister.ObsidianHelmet)
                .en("Obsidian Helmet")
                .zh("黑曜石逃犯帽");
        entry(ItemRegister.ObsidianChestplate)
                .en("Obsidian Chestplate")
                .zh("黑曜石风衣");
        entry(ItemRegister.ObsidianLeggings)
                .en("Obsidian Leggings")
                .zh("黑曜石裤");
        entry(ItemRegister.ObsidianBoots)
                .en("Obsidian Boots")
                .zh("黑曜石靴");
        // ===================== 英灵殿骑士护甲 =====================
        entry(ItemRegister.ValhallaKnightHelmet)
                .en("Valhalla Knight Helmet")
                .zh("英灵殿骑士头盔");
        entry(ItemRegister.ValhallaKnightChestplate)
                .en("Valhalla Knight Chestplate")
                .zh("英灵殿骑士胸甲");
        entry(ItemRegister.ValhallaKnightLeggings)
                .en("Valhalla Knight Leggings")
                .zh("英灵殿骑士护腿");
        entry(ItemRegister.ValhallaKnightBoots)
                .en("Valhalla Knight Boots")
                .zh("英灵殿骑士战靴");
        // ===================== 叶绿护甲 =====================
        entry(ItemRegister.ChlorophyteHelmet)
                .en("Chlorophyte Mask")
                .zh("叶绿面具");
        entry(ItemRegister.ChlorophyteChestplate)
                .en("Chlorophyte Breastplate")
                .zh("叶绿板甲");
        entry(ItemRegister.ChlorophyteLeggings)
                .en("Chlorophyte Leggings")
                .zh("叶绿护胫");
        entry(ItemRegister.ChlorophyteBoots)
                .en("Chlorophyte Boots")
                .zh("叶绿战靴");
        entry("servantry.servantry.chlorophyte.set.1")
                .en("Summons a powerful leaf crystal to shoot at nearby enemies")
                .zh("召唤强大的叶状水晶来射击附近的敌人");
        // ===================== 阴森护甲 =====================
        entry(ItemRegister.SpookyHelmet)
                .en("Spooky Helmet")
                .zh("阴森头盔");
        entry(ItemRegister.SpookyChestplate)
                .en("Spooky Chestplate")
                .zh("阴森胸甲");
        entry(ItemRegister.SpookyLeggings)
                .en("Spooky Leggings")
                .zh("阴森护腿");
        entry(ItemRegister.SpookyBoots)
                .en("Spooky Boots")
                .zh("阴森战靴");
        // ===================== 提基护甲 =====================
        entry(ItemRegister.TikiHelmet)
                .en("Tiki Mask")
                .zh("提基面具");
        entry(ItemRegister.TikiChestplate)
                .en("Tiki Chestplate")
                .zh("提基胸甲");
        entry(ItemRegister.TikiLeggings)
                .en("Tiki Leggings")
                .zh("提基护腿");
        entry(ItemRegister.TikiBoots)
                .en("Tiki Boots")
                .zh("提基战靴");
        // ===================== 星尘护甲 =====================
        entry(ItemRegister.StardustHelmet)
                .en("Stardust Helmet")
                .zh("星尘头盔");
        entry(ItemRegister.StardustChestplate)
                .en("Stardust Chestplate")
                .zh("星尘板甲");
        entry(ItemRegister.StardustLeggings)
                .en("Stardust Leggings")
                .zh("星尘护腿");
        entry(ItemRegister.StardustBoots)
                .en("Stardust Boots")
                .zh("星尘战靴");
        entry("servantry.servantry.stardust.set.1")
                .en("A stardust guardian will protect you from nearby enemies")
                .zh("星尘守卫将保护你不受附近敌人的伤害");
        entry(ItemRegister.Zenith)
                .en("Zenith")
                .zh("天顶剑");
        // ===================== 药水效果 =====================
        entry("effect.servantry.obsession")
                .en("Obsession")
                .zh("着魔");
        entry("effect.servantry.cell_parasitism")
                .en("Cell Parasitism")
                .zh("细胞寄生");
        entry("effect.servantry.cursed_flame")
                .en("Cursed Flame")
                .zh("诅咒焰");
        entry("effect.servantry.soul_might")
                .en("Soul Might")
                .zh("灵魂力量");
        entry("effect.servantry.soul_defense")
                .en("Soul Defense")
                .zh("灵魂防御");
        entry("effect.servantry.soul_recovery")
                .en("Soul Recovery")
                .zh("灵魂恢复");
        entry("effect.servantry.hallowed_might")
                .en("Hallowed Might")
                .zh("神圣之力");
        entry("effect.servantry.hallowed_grace")
                .en("Hallowed Grace")
                .zh("神圣之佑");
        entry("effect.servantry.hallowed_radiance")
                .en("Hallowed Radiance")
                .zh("神圣之辉");
        entry("effect.servantry.phantasmal_might")
                .en("Phantasmal Might")
                .zh("幻魂之力");
        entry("effect.servantry.phantasmal_bulwark")
                .en("Phantasmal Bulwark")
                .zh("幻魂坚盾");
        entry("effect.servantry.phantasmal_rebirth")
                .en("Phantasmal Rebirth")
                .zh("幻魂还生");
        entry("effect.servantry.shadowflame")
                .en("Shadowflame")
                .zh("暗影焰");

        // 长效和强效药水
        entry("item.minecraft.potion.effect.obsession")
                .en("Potion of Obsession")
                .zh("着魔药水");
        entry("item.minecraft.splash_potion.effect.obsession")
                .en("Splash Potion of Obsession")
                .zh("喷溅型着魔药水");
        entry("item.minecraft.lingering_potion.effect.obsession")
                .en("Lingering Potion of Obsession")
                .zh("滞留型着魔药水");
        entry("item.minecraft.tipped_arrow.effect.obsession")
                .en("Arrow of Obsession")
                .zh("着魔之箭");
        entry("item.minecraft.potion.effect.long_obsession")
                .en("Potion of Obsession")
                .zh("着魔药水");
        entry("item.minecraft.splash_potion.effect.long_obsession")
                .en("Splash Potion of Obsession")
                .zh("喷溅型着魔药水");
        entry("item.minecraft.lingering_potion.effect.long_obsession")
                .en("Lingering Potion of Obsession")
                .zh("滞留型着魔药水");
        entry("item.minecraft.tipped_arrow.effect.long_obsession")
                .en("Arrow of Obsession")
                .zh("着魔之箭");
        entry("item.minecraft.potion.effect.strong_obsession")
                .en("Potion of Obsession")
                .zh("着魔药水");
        entry("item.minecraft.splash_potion.effect.strong_obsession")
                .en("Splash Potion of Obsession")
                .zh("喷溅型着魔药水");
        entry("item.minecraft.lingering_potion.effect.strong_obsession")
                .en("Lingering Potion of Obsession")
                .zh("滞留型着魔药水");
        entry("item.minecraft.tipped_arrow.effect.strong_obsession")
                .en("Arrow of Obsession")
                .zh("着魔之箭");
        // JEI 兼容
        entry("jei.servantry.description.sold")
                .en("Occasionally sold by Clerics")
                .zh("牧师偶尔出售");
        entry("jei.servantry.description.terraprism")
                .en("Occasionally drops from Allays")
                .zh("悦灵偶尔掉落");
        entry("jei.servantry.description.drops_from_evokers")
                .en("Occasionally drops from Evokers")
                .zh("唤魔者偶尔掉落");
        entry("jei.servantry.description.drops_from_zombie")
                .en("Occasionally drops from Zombie")
                .zh("僵尸偶尔掉落");
        entry("jei.servantry.description.fishing")
                .en("Occasionally obtained from fishing in the ocean during rain")
                .zh("在海洋中雨天钓鱼时偶尔获得");
        entry("jei.servantry.description.ancient_city")
                .en("Occasionally found in Ancient City chests")
                .zh("偶尔在远古城市宝箱中发现");
    }

    private LangEntry entry(String key) {
        return new LangEntry(key, null, this);
    }

    private LangEntry entry(DeferredItem<Item> deferredItem) {
        ResourceLocation id = deferredItem.getId();
        return new LangEntry("item." + id.getNamespace() + "." + id.getPath(), deferredItem, this);
    }

    private LangEntry entry(DeferredHolder<AttachmentEntityType<?>, ?> deferredHolder) {
        ResourceLocation id = deferredHolder.getId();
        return new LangEntry("servant." + id.getNamespace() + "." + id.getPath(), null, this);
    }

    private static class LangEntry {
        @Nullable
        private final DeferredItem<Item> originItem;
        private final ServantryLanguageProvider provider;
        private String key;
        private int index = 0;

        LangEntry(String key, @Nullable DeferredItem<Item> originItem, ServantryLanguageProvider provider) {
            this.key = key;
            this.originItem = originItem;
            this.provider = provider;
        }

        public LangEntry en(String enDesc) {
            if ("en_us".equals(provider.locale)) {
                provider.add(key, enDesc);
            }
            return this;
        }

        public LangEntry zh(String zhDesc) {
            if ("zh_cn".equals(provider.locale)) {
                provider.add(key, zhDesc);
            }
            return this;
        }

        public LangEntry servant() {
            if (originItem == null) {
                throw new IllegalStateException("servant() can only be called on an entry created from a DeferredItem");
            }
            Item item = originItem.get();
            if (!(item instanceof IServantWeapon<?> weapon)) {
                throw new IllegalStateException("servant() requires the item to implement IServantWeapon, got: " + item.getClass().getSimpleName());
            }
            ResourceLocation servantId = ServantryRegistries.ATTACHMENT_ENTITY_TYPES.getKey(weapon.getType());
            assert servantId != null;
            this.key = "servant." + servantId.getNamespace() + "." + servantId.getPath();
            return this;
        }

        public LangEntry tooltip() {
            if (originItem == null) {
                throw new IllegalStateException("tooltip() can only be called on an entry created from a DeferredItem");
            }
            ResourceLocation id = originItem.getId();
            this.key = "item." + id.getNamespace() + "." + id.getPath() + ".tooltip." + ++index;
            return this;
        }
    }
}
