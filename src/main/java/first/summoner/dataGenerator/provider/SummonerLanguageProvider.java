package first.summoner.dataGenerator.provider;

import first.lyra.dataGenerator.provider.LyraLanguageProvider;
import first.summoner.register.SummonerMobEffectRegister;
import first.summoner.register.SummonerPotionRegister;
import net.minecraft.data.PackOutput;

public class SummonerLanguageProvider extends LyraLanguageProvider {

    public SummonerLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    public void init() {
        // ================= 模组基础 =================
        entry("modid.summoner", "Summoner", "召唤师");
        // ===================== 药水效果 =====================
        entry(SummonerMobEffectRegister.Obsession.get().getDescriptionId(), "Obsession", "着魔");
        entry(SummonerMobEffectRegister.CellParasitism.get().getDescriptionId(), "Cell Parasitism", "细胞寄生");
        entry(SummonerMobEffectRegister.CursedFlame.get().getDescriptionId(), "Cursed Flame", "诅咒焰");
        entry(SummonerMobEffectRegister.SoulMight.get().getDescriptionId(), "Soul Might", "灵魂力量");
        entry(SummonerMobEffectRegister.SoulDefense.get().getDescriptionId(), "Soul Defense", "灵魂防御");
        entry(SummonerMobEffectRegister.SoulRecovery.get().getDescriptionId(), "Soul Recovery", "灵魂恢复");
        entry(SummonerMobEffectRegister.HallowedMight.get().getDescriptionId(), "Hallowed Might", "神圣之力");
        entry(SummonerMobEffectRegister.HallowedGrace.get().getDescriptionId(), "Hallowed Grace", "神圣之佑");
        entry(SummonerMobEffectRegister.HallowedRadiance.get().getDescriptionId(), "Hallowed Radiance", "神圣之辉");
        entry(SummonerMobEffectRegister.PhantasmalMight.get().getDescriptionId(), "Phantasmal Might", "幻魂之力");
        entry(SummonerMobEffectRegister.PhantasmalBulwark.get().getDescriptionId(), "Phantasmal Bulwark", "幻魂坚盾");
        entry(SummonerMobEffectRegister.PhantasmalRebirth.get().getDescriptionId(), "Phantasmal Rebirth", "幻魂还生");
        entry(SummonerMobEffectRegister.Shadowflame.get().getDescriptionId(), "Shadowflame", "暗影焰");
        entry(SummonerMobEffectRegister.MoonBite.get().getDescriptionId(), "Moon Bite", "月噬");
        entry(SummonerMobEffectRegister.GodSlayerInferno.get().getDescriptionId(), "God Slayer Inferno", "噬神怒焰");
        entry(SummonerMobEffectRegister.ArmorCrunch.get().getDescriptionId(), "Armor Crunch", "碎甲");
        // ===================== 药水类物品 =====================
        potionEntry(SummonerPotionRegister.Obsession.getId().getPath(), "Obsession", "着魔");
    }

    @SuppressWarnings("SameParameterValue")
    private static void potionEntry(String effectName, String enName, String zhName) {
        for (String variant : new String[]{"", "long_", "strong_"}) {
            String suffix = variant + effectName;
            entry("item.minecraft.potion.effect." + suffix, "Potion of " + enName, zhName + "药水");
            entry("item.minecraft.splash_potion.effect." + suffix, "Splash Potion of " + enName, "喷溅型" + zhName + "药水");
            entry("item.minecraft.lingering_potion.effect." + suffix, "Lingering Potion of " + enName, "滞留型" + zhName + "药水");
            entry("item.minecraft.tipped_arrow.effect." + suffix, "Arrow of " + enName, zhName + "之箭");
        }
    }
}
