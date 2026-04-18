package first.servantry.dadageneeator.provider;

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
        add("modid.servantry", "Servantry", "仆从学");

        // --- 重构后的 UI 提示词条 ---
        add("item.servantry.tooltip.damage", " Summon Damage", " 召唤伤害");
        add("item.servantry.tooltip.knockback", " Knockback", " 击退强度");
        add("item.servantry.tooltip.summon", "Summons %s to fight for you", "召唤%s为你而战");
        add("item.servantry.tooltip.slots", "Servant Slots: %s / %s", "仆从栏位: %s / %s");
        add("item.servantry.tooltip.remove_all", "Sneak + Use to dismiss all servants of this type", "潜行右键以遣散此类型的所有仆从");

        // --- 死亡提示 ---
        add("death.attack.servantry.servant", "%1$s was torn apart by a servant", "%1$s 被仆从撕碎");
        add("death.attack.servantry.servant.player", "%1$s was torn apart by a servant whilst fighting %2$s", "%1$s 在与 %2$s 战斗时被仆从撕碎");

        // --- 物品与实体名 ---
        add("item.servantry.terraprism", "Terraprism", "泰拉棱镜");
        add("servant.servantry.terraprism", "Terraprism", "泰拉棱镜");
        add("item.servantry.terraprism.tooltip.1", "'Summons a light prism blade to perform near-flawless attacks'", "召唤一柄光棱剑，施展近乎完美的攻击");
        add("item.servantry.terraprism.tooltip.2", "'A flawless blade once hailed as the \"Prism of the Earth\"'", "——曾被冠以“大地棱彩”美名的无暇之剑");

        add("item.servantry.blade_staff", "Blade Staff", "刃杖");
        add("servant.servantry.enchanted_throwing_knives", "Enchanted Throwing Knives", "附魔飞刀");
        add("item.servantry.blade_staff.tooltip.1", "Ignores 2.5 points of enemy Defense", "忽略敌人2.5防御力");
        add("item.servantry.blade_staff.tooltip.2", "'Don't let their small size fool you'", "“别被它们小小的个头给骗了”");

        add("item.servantry.sanguine_staff", "Sanguine Staff", "血红法杖");
        add("servant.servantry.sanguine_bat", "Sanguine Bat", "血蝙蝠");

        add("item.servantry.stardust_cell_staff", "Stardust Cell Staff", "星尘细胞法杖");
        add("servant.servantry.stardust_cell", "Stardust Cell", "星尘细胞");
        add("item.servantry.stardust_cell_staff.tooltip.1", "'Cultivate the most beautiful cellular infection'", "“培养最美丽的细胞感染”");
    }

    private void add(String key, String enDesc, String zhDesc) {
        if (locale.equals("en_us")) {
            add(key, enDesc);
        } else if (locale.equals("zh_cn")) {
            add(key, zhDesc);
        }
    }

}
