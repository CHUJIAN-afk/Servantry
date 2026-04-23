package first.servantry.dadageneeator.provider;

import first.servantry.register.AttributeRegister;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class SoulLangProvider extends LanguageProvider {

    private final String locale;

    public SoulLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        // ================= 模组基础 =================
        entry("modid.servantry").en("Servantry").zh("仆从学");

        entry("item.servantry.tooltip.set_bonus_title").en("Set Bonus:").zh("套装效果:");
        entry("item.servantry.tooltip.damage").en(" Summon Damage").zh(" 召唤伤害");
        entry("item.servantry.tooltip.knockback").en(" Knockback").zh(" 击退强度");
        entry("item.servantry.tooltip.summon").en("Summons %s to fight for you").zh("召唤 %s 为你而战");
        entry("item.servantry.tooltip.slots").en("Servant Slots: %s / %s").zh("仆从栏位: %s / %s");
        entry("item.servantry.tooltip.remove_all").en("Sneak + Use to dismiss all servants of this type").zh("潜行右键以遣散该类型仆从");

        entry("death.attack.servantry.servant").en("%1$s was torn apart by a servant").zh("%1$s 被仆从撕碎");
        entry("death.attack.servantry.servant.player").en("%1$s was torn apart by a servant whilst fighting %2$s").zh("%1$s 在与 %2$s 战斗时被仆从撕碎");

        entry("item.servantry.terraprism").en("Terraprism").zh("泰拉棱镜");
        entry("servant.servantry.terraprism").en("Terraprism").zh("泰拉棱镜");
        entry("item.servantry.terraprism.tooltip.1").en("'Summons a light prism blade to perform near-flawless attacks'").zh("召唤一柄光棱剑，施展近乎完美的攻击");
        entry("item.servantry.terraprism.tooltip.2").en("'A flawless blade once hailed as the \"Prism of the Earth\"'").zh("——曾被冠以“大地棱彩”美名的无暇之剑");

        entry("item.servantry.blade_staff").en("Blade Staff").zh("刃杖");
        entry("servant.servantry.enchanted_throwing_knives").en("Enchanted Throwing Knives").zh("附魔飞刀");
        entry("item.servantry.blade_staff.tooltip.1").en("Ignores 2.5 points of enemy Defense").zh("忽略敌人 2.5 防御力");
        entry("item.servantry.blade_staff.tooltip.2").en("'Don't let their small size fool you'").zh("“别被它们小小的个头给骗了”");

        entry("item.servantry.stardust_cell_staff").en("Stardust Cell Staff").zh("星尘细胞法杖");
        entry("servant.servantry.stardust_cell").en("Stardust Cell").zh("星尘细胞");
        entry("item.servantry.stardust_cell_staff.tooltip.1").en("'Cultivate the most beautiful cellular infection'").zh("“培养最美丽的细胞感染”");

        entry(AttributeRegister.ServantMaxCount.value().getDescriptionId()).en("Max Servants").zh("仆从栏");
        entry(AttributeRegister.ServantDamage.value().getDescriptionId()).en("Servant Damage").zh("仆从伤害");

        entry("item.servantry.hallowed_helmet").en("Hallowed Helmet").zh("神圣兜帽");
        entry("item.servantry.hallowed_chestplate").en("Hallowed Chestplate").zh("神圣板甲");
        entry("item.servantry.hallowed_leggings").en("Hallowed Leggings").zh("神圣护胫");
        entry("item.servantry.hallowed_boots").en("Hallowed Boots").zh("神圣战靴");
        entry("item.servantry.hallowed.set.1").en("+2 Servant Slots").zh("+2 仆从栏");
        entry("item.servantry.hallowed.set.2").en("+15% Servant Damage").zh("+15% 仆从伤害");
        entry("item.servantry.hallowed.set.3").en("+4 Servant Armor Penetration").zh("+4 仆从护甲穿透");
        entry("item.servantry.hallowed.set.4").en("Servants grant I-frames on attack").zh("仆从攻击使敌人高亮");

        entry("effect.servantry.obsession").en("Obsession").zh("着魔");
        entry("effect.servantry.cell_parasitism").en("Cell Parasitism").zh("细胞寄生");

        entry("item.minecraft.potion.effect.obsession").en("Potion of Obsession").zh("着魔药水");
        entry("item.minecraft.splash_potion.effect.obsession").en("Splash Potion of Obsession").zh("喷溅型着魔药水");
        entry("item.minecraft.lingering_potion.effect.obsession").en("Lingering Potion of Obsession").zh("滞留型着魔药水");
        entry("item.minecraft.tipped_arrow.effect.obsession").en("Arrow of Obsession").zh("着魔之箭");
    }

    private LangEntry entry(String key) {
        return new LangEntry(key);
    }

    private class LangEntry {

        private final String key;

        public LangEntry(String key) {
            this.key = key;
        }

        public LangEntry en(String enDesc) {
            if ("en_us".equals(locale)) {
                SoulLangProvider.this.add(key, enDesc);
            }
            return this;
        }

        public LangEntry zh(String zhDesc) {
            if ("zh_cn".equals(locale)) {
                SoulLangProvider.this.add(key, zhDesc);
            }
            return this;
        }

    }

}