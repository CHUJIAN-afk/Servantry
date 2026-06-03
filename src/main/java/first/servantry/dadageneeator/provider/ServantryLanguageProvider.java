package first.servantry.dadageneeator.provider;

import first.servantry.register.AttributeRegister;
import first.servantry.register.Registers;
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
        Registers.getInstance().getLanguageGenerate().forEach(langEntry -> langEntry.build(this));
        // ================= 模组基础 =================
        entry("modid.servantry", "Servantry", "仆从学");
        entry("item.servantry.tooltip.set_bonus_title", "Set Rewards:", "套装奖励:");
        entry("item.servantry.tooltip.damage", "Summon Damage", "召唤伤害");
        entry("item.servantry.tooltip.knockback", "Knockback", "击退强度");
        entry("item.servantry.tooltip.summon", "Summons %s to fight for you", "召唤 %s 为你而战");
        entry("item.servantry.tooltip.slots", "Servant Slots: %s / %s", "仆从栏位: %s / %s");
        entry("item.servantry.tooltip.remove_all", "Sneak + Use to dismiss all servants of this type", "潜行右键以遣散该类型仆从");
        entry("death.attack.servantry.servant", "%1$s was torn apart by a servant", "%1$s 被仆从撕碎");
        entry("death.attack.servantry.servant.player", "%1$s was torn apart by a servant whilst fighting %2$s", "%1$s 在与 %2$s 战斗时被仆从撕碎");
        entry("container.servantry.mithril_anvil", "Forging", "超凡锻造");
        // ================= 属性 =================
        entry(AttributeRegister.ServantMaxCount.value().getDescriptionId(), "Max Servants", "仆从栏");
        entry(AttributeRegister.ServantDamage.value().getDescriptionId(), "Servant Damage", "仆从伤害");
        entry(AttributeRegister.ServantKnockback.value().getDescriptionId(), "Servant Knockback", "仆从击退");
        entry(AttributeRegister.ServantArmorPierce.value().getDescriptionId(), "Servant Armor Pierce", "仆从护甲穿透");
        entry(AttributeRegister.ServantSearchRange.value().getDescriptionId(), "Servant Search Range", "仆从索敌范围");
        entry(AttributeRegister.HealthRegen.value().getDescriptionId(), "Health Regen", "生命再生");
        // ===================== 套装效果  =====================
        entry("servantry.servantry.hallowed.set.1", "Servants grant I-frames on attack", "仆从攻击使敌人高亮");
        entry("servantry.servantry.chlorophyte.set.1", "Summons a powerful leaf crystal to shoot at nearby enemies", "召唤强大的叶状水晶来射击附近的敌人");
        entry("servantry.servantry.stardust.set.1", "A stardust guardian will protect you from nearby enemies", "星尘守卫将保护你不受附近敌人的伤害");
        entry("servantry.servantry.forbidden.set.1", "Allows you to summon an ancient storm to attract nearby enemies", "允许你召唤远古风暴吸引附近的敌人");
        // ===================== 药水效果 =====================
        entry("effect.servantry.obsession", "Obsession", "着魔");
        entry("effect.servantry.cell_parasitism", "Cell Parasitism", "细胞寄生");
        entry("effect.servantry.cursed_flame", "Cursed Flame", "诅咒焰");
        entry("effect.servantry.soul_might", "Soul Might", "灵魂力量");
        entry("effect.servantry.soul_defense", "Soul Defense", "灵魂防御");
        entry("effect.servantry.soul_recovery", "Soul Recovery", "灵魂恢复");
        entry("effect.servantry.hallowed_might", "Hallowed Might", "神圣之力");
        entry("effect.servantry.hallowed_grace", "Hallowed Grace", "神圣之佑");
        entry("effect.servantry.hallowed_radiance", "Hallowed Radiance", "神圣之辉");
        entry("effect.servantry.phantasmal_might", "Phantasmal Might", "幻魂之力");
        entry("effect.servantry.phantasmal_bulwark", "Phantasmal Bulwark", "幻魂坚盾");
        entry("effect.servantry.phantasmal_rebirth", "Phantasmal Rebirth", "幻魂还生");
        entry("effect.servantry.shadowflame", "Shadowflame", "暗影焰");
        // 长效和强效药水
        entry("item.minecraft.potion.effect.obsession", "Potion of Obsession", "着魔药水");
        entry("item.minecraft.splash_potion.effect.obsession", "Splash Potion of Obsession", "喷溅型着魔药水");
        entry("item.minecraft.lingering_potion.effect.obsession", "Lingering Potion of Obsession", "滞留型着魔药水");
        entry("item.minecraft.tipped_arrow.effect.obsession", "Arrow of Obsession", "着魔之箭");
        entry("item.minecraft.potion.effect.long_obsession", "Potion of Obsession", "着魔药水");
        entry("item.minecraft.splash_potion.effect.long_obsession", "Splash Potion of Obsession", "喷溅型着魔药水");
        entry("item.minecraft.lingering_potion.effect.long_obsession", "Lingering Potion of Obsession", "滞留型着魔药水");
        entry("item.minecraft.tipped_arrow.effect.long_obsession", "Arrow of Obsession", "着魔之箭");
        entry("item.minecraft.potion.effect.strong_obsession", "Potion of Obsession", "着魔药水");
        entry("item.minecraft.splash_potion.effect.strong_obsession", "Splash Potion of Obsession", "喷溅型着魔药水");
        entry("item.minecraft.lingering_potion.effect.strong_obsession", "Lingering Potion of Obsession", "滞留型着魔药水");
        entry("item.minecraft.tipped_arrow.effect.strong_obsession", "Arrow of Obsession", "着魔之箭");
        // JEI 兼容
        entry("jei.servantry.description.sold", "Occasionally sold by Clerics", "牧师偶尔出售");
        entry("jei.servantry.description.terraprism", "Occasionally drops from Allays", "悦灵偶尔掉落");
        entry("jei.servantry.description.drops_from_evokers", "Occasionally drops from Evokers", "唤魔者偶尔掉落");
        entry("jei.servantry.description.drops_from_zombie", "Occasionally drops from Zombie", "僵尸偶尔掉落");
        entry("jei.servantry.description.fishing", "Occasionally obtained from fishing in the ocean during rain", "在海洋中雨天钓鱼时偶尔获得");
        entry("jei.servantry.description.ancient_city", "Occasionally found in Ancient City chests", "偶尔在远古城市宝箱中发现");
    }

    private void entry(String key, String enDesc, String zhDesc) {
        LangEntry langEntry = new LangEntry(key, enDesc, zhDesc);
        langEntry.build(this);
    }

    public record LangEntry(String key, String enDesc, String zhDesc) {
        public void build(ServantryLanguageProvider provider) {
            if (key != null) {
                if (enDesc != null && "en_us".equals(provider.locale)) {
                    provider.add(key, enDesc);
                }
                if (zhDesc != null && "zh_cn".equals(provider.locale)) {
                    provider.add(key, zhDesc);
                }
            }
        }
    }
}
