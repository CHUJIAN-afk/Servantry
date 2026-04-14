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
        add("item.servantry.tooltip.6", "Summon Mark Damage: ", "召唤标记伤害: ");
        add("item.servantry.tooltip.7", "Summon Crit Rate: ", "召唤暴击率: ");
        add("item.servantry.tooltip.8", "Extension Length: ", "延展长度: ");
        add("item.servantry.tooltip.9", "Swing Duration: ", "挥动时间: ");
        add("item.servantry.tooltip.10", "Damage Decay: ", "伤害衰减: ");
        add("item.servantry.tooltip.11", "Can penetrate blocks", "能穿透方块");
        add("item.servantry.tooltip.12", "Cannot penetrate blocks", "不能穿透方块");
        add("item.servantry.tooltip.13", "Your servants will focus on the target you hit", "你的仆从将集中攻击被打中的目标");
        add("death.attack.servantry.servant", "%1$s was torn apart by a servant", "%1$s 被仆从撕碎");
        add("death.attack.servantry.servant.player", "%1$s was torn apart by a servant whilst fighting %2$s", "%1$s 在与 %2$s 战斗时被仆从撕碎");
        add("item.servantry.terraprism", "Terraprism", "泰拉棱镜");
        add("servant.servantry.terraprism", "Terraprism", "泰拉棱镜");
        add("item.servantry.cobweb_whip", "Cobweb Whip", "蛛网鞭");
        add("item.servantry.cobweb_whip.tooltip.1", "Slows hit targets", "减速被打击的目标");
        add("item.servantry.slime_whip", "Slime Whip", "史莱姆鞭");
        add("item.servantry.slime_whip.tooltip.1", "Slows hit targets", "鞭梢打击可点燃目标");
        add("item.servantry.leather_whip", "Leather Whip", "皮鞭");
        add("item.servantry.leather_whip.tooltip.1", "\"Die monster!\"", "“该死的怪物！”");
        add("item.servantry.soulscourge", "Soulscourge", "魂笞");
        add("item.servantry.soulscourge.tooltip.1", "\"Whip your enemies with the remnants of the incarnation of evil\"", "“用邪恶化身的残余鞭打你的敌人”");
        add("item.servantry.starcrash", "Starcrash", "星坠");
        add("item.servantry.starcrash.tooltip.1", "Strike enemies with Excited Energy", "用激发能打击敌人");
        add("item.servantry.starcrash.tooltip.2", "When a servant hits an excited enemy, it triggers a small explosion", "仆从击中被激发的敌人时，将引发流星雨");
        add("item.servantry.vasculash", "Vasculash", "脉管");
        add("item.servantry.vasculash.tooltip.1", "\"Arteries, veins, and tendons!\"", "“动脉、静脉和肌腱！”");
    }

    private void add(String key, String enDesc, String zhDesc) {
        if (locale.equals("en_us")) {
            add(key, enDesc);
        } else if (locale.equals("zh_cn")) {
            add(key, zhDesc);
        }
    }

}
