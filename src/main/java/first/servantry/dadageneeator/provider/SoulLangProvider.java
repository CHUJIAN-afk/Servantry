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
        add("item.servantry.tooltip.1", "Servant type:", "仆从类型:");
        add("item.servantry.tooltip.2", "Servant Bar Usage:", "仆从槽位消耗:");
        add("item.servantry.tooltip.3", "Use to summon a Servant", "使用以召唤仆从");
        add("item.servantry.tooltip.4", "Sneak Use to remove oldest servant of this type", "潜行使用以移除最早召唤的此类型仆从");
        add("item.servantry.tooltip.5", "Summon Damage: ", "召唤伤害: ");
        add("death.attack.servantry.servant", "%1$s was torn apart by a servant", "%1$s 被仆从撕碎");
        add("death.attack.servantry.servant.player", "%1$s was torn apart by a servant whilst fighting %2$s", "%1$s 在与 %2$s 战斗时被仆从撕碎");
        add("item.servantry.terraprism", "Terraprism", "泰拉棱镜");
        add("servant.servantry.terraprism", "Terraprism", "泰拉棱镜");
        add("item.servantry.blade_staff", "Blade Staff", "刃杖");
        add("servant.servantry.enchanted_throwing_knives", "Enchanted Throwing Knives", "附魔飞刀");
        add("item.servantry.blade_staff.tooltip.1", "Ignores 2.5 points of enemy Defense", "忽略敌人2.5防御力");
        add("item.servantry.blade_staff.tooltip.2", "'Don't let their small size fool you'", "“别被它们小小的个头给骗了”");
        add("item.servantry.slime_staff", "Slime Staff", "史莱姆法杖");
        add("servant.servantry.slime_baby", "Slime Baby", "史莱姆宝宝");
        add("item.servantry.sanguine_staff", "Sanguine Staff", "血红法杖");
        add("servant.servantry.sanguine_bat", "sanguine_bat", "血蝙蝠");
    }

    private void add(String key, String enDesc, String zhDesc) {
        if (locale.equals("en_us")) {
            add(key, enDesc);
        } else if (locale.equals("zh_cn")) {
            add(key, zhDesc);
        }
    }

}
