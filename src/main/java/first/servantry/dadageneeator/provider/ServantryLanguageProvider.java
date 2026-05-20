package first.servantry.dadageneeator.provider;

import first.servantry.register.AttributeRegister;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

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
                .en("Set Bonus:")
                .zh("套装效果:");
        entry("item.servantry.tooltip.damage")
                .en(" Summon Damage")
                .zh(" 召唤伤害");
        entry("item.servantry.tooltip.knockback")
                .en(" Knockback")
                .zh(" 击退强度");
        entry("item.servantry.tooltip.summon")
                .en("Summons %s to fight for you")
                .zh("召唤 %s 为你而战");
        entry("item.servantry.tooltip.slots")
                .en("Servant Slots: %s / %s")
                .zh("仆从栏位: %s / %s");
        entry("item.servantry.tooltip.remove_all")
                .en("Sneak + Use to dismiss all servants of this type")
                .zh("潜行右键以遣散该类型仆从");
        entry("death.attack.servantry.servant")
                .en("%1$s was torn apart by a servant")
                .zh("%1$s 被仆从撕碎");
        entry("death.attack.servantry.servant.player")
                .en("%1$s was torn apart by a servant whilst fighting %2$s")
                .zh("%1$s 在与 %2$s 战斗时被仆从撕碎");
        entry("item.servantry.terraprism")
                .en("Terraprism")
                .zh("泰拉棱镜");
        entry("servant.servantry.terraprism")
                .en("Terraprism")
                .zh("泰拉棱镜");
        entry("item.servantry.terraprism.tooltip.1")
                .en("'A flawless blade once hailed as the \"Prism of the Earth\"'")
                .zh("曾被冠以“大地棱彩”美名的无暇之剑");
        entry("item.servantry.infinite_scabbard")
                .en("Infinite Scabbard")
                .zh("无限剑鞘");
        entry("servant.servantry.infinite_shadow")
                .en("Infinite Shadow")
                .zh("无限之影");
        entry("item.servantry.infinite_scabbard.tooltip.1")
                .en("'A scabbard that stores a blade of infinite potential'")
                .zh("蕴含无限可能之“剑”的剑鞘");
        entry("item.servantry.infinite_scabbard.tooltip.2")
                .en("Right-click an item to store, right-click an empty slot to retrieve")
                .zh("右键物品存入，右键空格子取出");
        entry("item.servantry.blade_staff")
                .en("Blade Staff")
                .zh("刃杖");
        entry("servant.servantry.enchanted_throwing_knives")
                .en("Enchanted Throwing Knives")
                .zh("附魔飞刀");
        entry("item.servantry.blade_staff.tooltip.1")
                .en("Ignores 2.5 points of enemy Defense")
                .zh("忽略敌人 2.5 防御力");
        entry("item.servantry.blade_staff.tooltip.2")
                .en("'Don't let their small size fool you'")
                .zh("“别被它们小小的个头给骗了”");
        entry("item.servantry.stardust_cell_staff")
                .en("Stardust Cell Staff")
                .zh("星尘细胞法杖");
        entry("servant.servantry.stardust_cell")
                .en("Stardust Cell")
                .zh("星尘细胞");
        entry("item.servantry.stardust_cell_staff.tooltip.1")
                .en("'Cultivate the most beautiful cellular infection'")
                .zh("“培养最美丽的细胞感染”");
        entry("item.servantry.stardust_dragon_staff")
                .en("Stardust Dragon Staff")
                .zh("星尘之龙法杖");
        entry("servant.servantry.stardust_dragon")
                .en("Stardust Dragon")
                .zh("星尘之龙");
        entry("item.servantry.stardust_dragon_staff.tooltip.1")
                .en("'When you have a dragon, who needs a swarm?'")
                .zh("“有了一条巨龙后，谁还需要一群仆从呢？”");
        entry("item.servantry.optic_staff")
                .en("Optic Staff")
                .zh("魔眼法杖");
        entry("servant.servantry.twins")
                .en("Twins")
                .zh("双子魔眼");
        entry("item.servantry.tempest_staff")
                .en("Tempest Staff")
                .zh("暴风雨法杖");
        entry("servant.servantry.sharknado")
                .en("Sharknado")
                .zh("鲨鱼龙卷");
        entry(AttributeRegister.ServantMaxCount.value().getDescriptionId())
                .en("Max Servants")
                .zh("仆从栏");
        entry(AttributeRegister.ServantDamage.value().getDescriptionId())
                .en("Servant Damage")
                .zh("仆从伤害");
        entry(AttributeRegister.ServantKnockback.value().getDescriptionId())
                .en("Servant Knockback")
                .zh("仆从击退");
        // ===================== 饰品 =====================
        entry("curios.curios.accessories")
                .en("Accessories")
                .zh("配饰");
        entry("item.servantry.necromantic_scroll")
                .en("Necromantic Scroll")
                .zh("死灵卷轴");
        entry("item.servantry.papyrus_scarab")
                .en("Papyrus Scarab")
                .zh("甲虫莎草纸");
        entry("item.servantry.pygmy_necklace")
                .en("Pygmy Necklace")
                .zh("矮人项链");
        entry("item.servantry.hercules_beetle")
                .en("Hercules Beetle")
                .zh("大力士甲虫");
        entry("item.servantry.black_lens")
                .en("Black Lens")
                .zh("黑色晶状体");
        entry("item.servantry.summoner_emblem")
                .en("Summoner Emblem")
                .zh("召唤师徽章");
        entry("item.servantry.apprentices_scarf")
                .en("Apprentice's Scarf")
                .zh("学徒围巾");
        entry("item.servantry.huntresses_buckler")
                .en("Huntress's Buckler")
                .zh("女猎人圆盾");
        entry("item.servantry.monks_belt")
                .en("Monk's Belt")
                .zh("武僧腰带");
        entry("item.servantry.squires_shield")
                .en("Squire's Shield")
                .zh("侍卫护盾");
        // ===================== 护甲 =====================
        entry("item.servantry.hallowed_helmet")
                .en("Hallowed Helmet")
                .zh("神圣兜帽");
        entry("item.servantry.hallowed_chestplate")
                .en("Hallowed Chestplate")
                .zh("神圣板甲");
        entry("item.servantry.hallowed_leggings")
                .en("Hallowed Leggings")
                .zh("神圣护胫");
        entry("item.servantry.hallowed_boots")
                .en("Hallowed Boots")
                .zh("神圣战靴");
        entry("item.servantry.hallowed.set.1")
                .en("+2 Servant Slots")
                .zh("+2 仆从栏");
        entry("item.servantry.hallowed.set.2")
                .en("+15% Servant Damage")
                .zh("+15% 仆从伤害");
        entry("item.servantry.hallowed.set.3")
                .en("+4 Servant Armor Penetration")
                .zh("+4 仆从护甲穿透");
        entry("item.servantry.hallowed.set.4")
                .en("Servants grant I-frames on attack")
                .zh("仆从攻击使敌人高亮");
        entry("effect.servantry.obsession")
                .en("Obsession")
                .zh("着魔");
        entry("effect.servantry.cell_parasitism")
                .en("Cell Parasitism")
                .zh("细胞寄生");
        entry("effect.servantry.cursed_flame")
                .en("Cursed Flame")
                .zh("诅咒焰");
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
        return new LangEntry(key, this);
    }

    private record LangEntry(String key, ServantryLanguageProvider provider) {

        public LangEntry en(String enDesc) {
            if ("en_us".equals(provider.locale)) {
                provider.add(key, enDesc);
            }
            return this;
        }

        public void zh(String zhDesc) {
            if ("zh_cn".equals(provider.locale)) {
                provider.add(key, zhDesc);
            }
        }

    }

}
