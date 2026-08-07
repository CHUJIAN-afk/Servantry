package first.servantry.register;

import oshi.util.tuples.Pair;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SameParameterValue")
public class ServantryLanguageGenerateRegister {

    public static final Map<String, Pair<String, String>> LanguageGenerate = new HashMap<>();

    public static void init(){
        // ================= 模组基础 =================
        entry("modid.servantry", "Servantry", "仆从学");
        entry("death.attack.servantry.servant", "%1$s was torn apart by a servant", "%1$s 被仆从撕碎");
        entry("death.attack.servantry.servant.player", "%1$s was torn apart by a servant whilst fighting %2$s", "%1$s 在与 %2$s 战斗时被仆从撕碎");
        entry("container.servantry.mithril_anvil", "Forging", "超凡锻造");
        // ===================== 药水效果 =====================
        entry(ServantryMobEffectRegister.Obsession.get().getDescriptionId(), "Obsession", "着魔");
        entry(ServantryMobEffectRegister.CellParasitism.get().getDescriptionId(), "Cell Parasitism", "细胞寄生");
        entry(ServantryMobEffectRegister.CursedFlame.get().getDescriptionId(), "Cursed Flame", "诅咒焰");
        entry(ServantryMobEffectRegister.SoulMight.get().getDescriptionId(), "Soul Might", "灵魂力量");
        entry(ServantryMobEffectRegister.SoulDefense.get().getDescriptionId(), "Soul Defense", "灵魂防御");
        entry(ServantryMobEffectRegister.SoulRecovery.get().getDescriptionId(), "Soul Recovery", "灵魂恢复");
        entry(ServantryMobEffectRegister.HallowedMight.get().getDescriptionId(), "Hallowed Might", "神圣之力");
        entry(ServantryMobEffectRegister.HallowedGrace.get().getDescriptionId(), "Hallowed Grace", "神圣之佑");
        entry(ServantryMobEffectRegister.HallowedRadiance.get().getDescriptionId(), "Hallowed Radiance", "神圣之辉");
        entry(ServantryMobEffectRegister.PhantasmalMight.get().getDescriptionId(), "Phantasmal Might", "幻魂之力");
        entry(ServantryMobEffectRegister.PhantasmalBulwark.get().getDescriptionId(), "Phantasmal Bulwark", "幻魂坚盾");
        entry(ServantryMobEffectRegister.PhantasmalRebirth.get().getDescriptionId(), "Phantasmal Rebirth", "幻魂还生");
        entry(ServantryMobEffectRegister.Shadowflame.get().getDescriptionId(), "Shadowflame", "暗影焰");
        entry(ServantryMobEffectRegister.MoonBite.get().getDescriptionId(), "Moon Bite", "月噬");
        entry(ServantryMobEffectRegister.GodSlayerInferno.get().getDescriptionId(), "God Slayer Inferno", "噬神怒焰");
        entry(ServantryMobEffectRegister.ArmorCrunch.get().getDescriptionId(), "Armor Crunch", "碎甲");
        // ===================== 药水类物品 =====================
        potionEntry(ServantryPotionRegister.Obsession.getId().getPath(), "Obsession", "着魔");
        // ===================== JEI 兼容 =====================
        entry("jei.servantry.description.sold", "Occasionally sold by Clerics", "牧师偶尔出售");
        entry("jei.servantry.description.terraprism", "Occasionally drops from Allays", "悦灵偶尔掉落");
        entry("jei.servantry.description.drops_from_evokers", "Occasionally drops from Evokers", "唤魔者偶尔掉落");
        entry("jei.servantry.description.drops_from_zombie", "Occasionally drops from Zombie", "僵尸偶尔掉落");
        entry("jei.servantry.description.fishing", "Occasionally obtained from fishing in the ocean during rain", "在海洋中雨天钓鱼时偶尔获得");
        entry("jei.servantry.description.ancient_city", "Occasionally found in Ancient City chests", "偶尔在远古城市宝箱中发现");
    }

    public static void entry(String key, String enDesc, String zhDesc) {
        LanguageGenerate.put(key, new Pair<>(enDesc, zhDesc));
    }

    public static void potionEntry(String effectName, String enName, String zhName) {
        for (String variant : new String[]{"", "long_", "strong_"}) {
            String suffix = variant + effectName;
            entry("item.minecraft.potion.effect." + suffix, "Potion of " + enName, zhName + "药水");
            entry("item.minecraft.splash_potion.effect." + suffix, "Splash Potion of " + enName, "喷溅型" + zhName + "药水");
            entry("item.minecraft.lingering_potion.effect." + suffix, "Lingering Potion of " + enName, "滞留型" + zhName + "药水");
            entry("item.minecraft.tipped_arrow.effect." + suffix, "Arrow of " + enName, zhName + "之箭");
        }
    }
}
