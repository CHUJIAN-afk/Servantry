package first.summoner.register;

import first.lyra.register.LyraItemRegistries;

public class SummonerLanguageRegister implements LyraItemRegistries.LanguageInit {

    public void init(LyraItemRegistries registries) {
        // ================= 模组基础 =================
        registries.language("modid.summoner", "Summoner", "召唤师");
        registries.language("entity.summoner.doomsday_star", "Doomsday Star", "末世星");
        // ===================== 药水效果 =====================
        registries.language(SummonerMobEffectRegister.Obsession.get().getDescriptionId(), "Obsession", "着魔");
        registries.language(SummonerMobEffectRegister.CellParasitism.get().getDescriptionId(), "Cell Parasitism", "细胞寄生");
        registries.language(SummonerMobEffectRegister.CursedFlame.get().getDescriptionId(), "Cursed Flame", "诅咒焰");
        registries.language(SummonerMobEffectRegister.SoulMight.get().getDescriptionId(), "Soul Might", "灵魂力量");
        registries.language(SummonerMobEffectRegister.SoulDefense.get().getDescriptionId(), "Soul Defense", "灵魂防御");
        registries.language(SummonerMobEffectRegister.SoulRecovery.get().getDescriptionId(), "Soul Recovery", "灵魂恢复");
        registries.language(SummonerMobEffectRegister.HallowedMight.get().getDescriptionId(), "Hallowed Might", "神圣之力");
        registries.language(SummonerMobEffectRegister.HallowedGrace.get().getDescriptionId(), "Hallowed Grace", "神圣之佑");
        registries.language(SummonerMobEffectRegister.HallowedRadiance.get().getDescriptionId(), "Hallowed Radiance", "神圣之辉");
        registries.language(SummonerMobEffectRegister.PhantasmalMight.get().getDescriptionId(), "Phantasmal Might", "幻魂之力");
        registries.language(SummonerMobEffectRegister.PhantasmalBulwark.get().getDescriptionId(), "Phantasmal Bulwark", "幻魂坚盾");
        registries.language(SummonerMobEffectRegister.PhantasmalRebirth.get().getDescriptionId(), "Phantasmal Rebirth", "幻魂还生");
        registries.language(SummonerMobEffectRegister.Shadowflame.get().getDescriptionId(), "Shadowflame", "暗影焰");
        registries.language(SummonerMobEffectRegister.MoonBite.get().getDescriptionId(), "Moon Bite", "月噬");
        registries.language(SummonerMobEffectRegister.GodSlayerInferno.get().getDescriptionId(), "God Slayer Inferno", "噬神怒焰");
        registries.language(SummonerMobEffectRegister.ArmorCrunch.get().getDescriptionId(), "Armor Crunch", "碎甲");
        // ===================== 药水类物品 =====================
        potionLanguage(registries, SummonerPotionRegister.Obsession.getId().getPath(), "Obsession", "着魔");
        // 附魔名字键
        registries.language("enchantment.summoner.food_chain", "Food Chain", "食物链");
        registries.language("enchantment.summoner.jungle_hunter", "Jungle Hunter", "丛林捕手");
        registries.language("enchantment.summoner.death_magister", "Death Magister", "死亡执政官");
        registries.language("enchantment.summoner.soul_flail", "Soul Flail", "灵魂鞭挞");
        registries.language("enchantment.summoner.starfall", "Starfall", "星坠");
        registries.language("enchantment.summoner.bramble", "Bramble", "荆棘");
        registries.language("enchantment.summoner.vein", "Vein", "脉管");
        registries.language("enchantment.summoner.spine", "Spine", "脊柱");
        registries.language("enchantment.summoner.firecracker", "Firecracker", "鞭炮");
        registries.language("enchantment.summoner.frost_snow", "Frost Snow", "霜雪");
        registries.language("enchantment.summoner.divine_judgment", "Divine Judgment", "神圣裁决");
        registries.language("enchantment.summoner.rough_display_of_flowers", "Rough Display of Flowers", "花儿的粗暴展示");
        registries.language("enchantment.summoner.dominance_omen", "Dominance Omen", "支配预兆");
        registries.language("enchantment.summoner.dark_harvest", "Dark Harvest", "暗黑收割");
        registries.language("enchantment.summoner.constellation", "Constellation", "星座");
        registries.language("enchantment.summoner.current_surge", "Current Surge", "电弧涌动");
        registries.language("enchantment.summoner.morning_star", "Morning Star", "晨星");
        registries.language("enchantment.summoner.kaleidoscope", "Kaleidoscope", "万花筒");
        registries.language("enchantment.summoner.requiem", "Requiem", "安魂曲");
        registries.language("enchantment.summoner.galaxy", "Galaxy", "银河");
        registries.language("enchantment.summoner.silent_end", "Silent End", "沉默的终时");
        registries.language("enchantment.summoner.moon_bite", "Moon Bite", "月噬");
        registries.language("enchantment.summoner.angel", "Angel", "天使");
        registries.language("enchantment.summoner.god_slayer", "God Slayer", "弑神者");
        registries.language("enchantment.summoner.cerulean_dawn", "Cerulean Dawn", "水蓝色黎明");
        registries.language("enchantment.summoner.arcane_rite", "Arcane Rite", "秘仪");
        registries.language("enchantment.summoner.arcane_rite.desc.1", "Critical damage +15%, and every 2% mark critical rate grants an additional 1% critical damage", "暴击伤害提升15%，每2%标记暴击率额外提升1%暴击伤害");
        registries.language("enchantment.summoner.food_chain.desc.1", "On minion hit: you gain Speed for 3s, and the target gains Slowness for 3s", "召唤物命中时，为你施加3秒速度效果，为目标施加3秒减速效果");
        registries.language("enchantment.summoner.death_magister.desc.1", "On minion kill: deal 150% summon damage to one other target within 3 blocks", "召唤物击杀时，对半径3格内另一个目标造成150%召唤伤害");
        registries.language("enchantment.summoner.starfall.desc.1", "On minion hit: consume 30 mark energy to summon a meteor dealing 133% summon damage to targets within 2 blocks", "召唤物命中时，消耗30标记能量，召唤流星，流星对半径2格内目标造成133%召唤伤害");
        registries.language("enchantment.summoner.bramble.desc.1", "On minion hit: you gain Speed for 2-4s, and the target gains Poison and Wither for 3s", "召唤物命中时，为你施加2~4秒速度效果，为目标施加3秒中毒和凋零效果");
        registries.language("enchantment.summoner.vein.desc.1", "On minion kill: restore 12% of missing health (up to 8 HP)", "召唤物击杀时，为你恢复12%已损生命值，最多8点");
        registries.language("enchantment.summoner.firecracker.desc.1", "On minion hit: consume 30 mark energy to deal 275% summon damage to the target and 175% to other enemies within 3 blocks, igniting them for 5s", "召唤物命中时，消耗30标记能量，对目标造成275%召唤伤害，对半径3格内其他目标造成175%召唤伤害，并点燃5秒");
        registries.language("enchantment.summoner.frost_snow.desc.1", "On minion hit: apply Slowness II for 6s", "召唤物命中时，为目标施加6秒减速2效果");
        registries.language("enchantment.summoner.divine_judgment.desc.1", "On minion kill: deal 450% summon damage to one other target within 3 blocks", "召唤物击杀时，对半径3格内另一个目标造成450%召唤伤害");
        registries.language("enchantment.summoner.rough_display_of_flowers.desc.1", "On minion hit: consume 30 mark energy to summon 3-6 homing petals dealing 225% summon damage, 50 armor pierce, no knockback", "召唤物命中时，消耗30标记能量，召唤3~6枚追踪花瓣，花瓣造成225%召唤伤害，50护甲穿透，不造成击退");
        registries.language("enchantment.summoner.dominance_omen.desc.1", "On minion hit: consume 10 mark energy to summon 3 homing stellar cores dealing 20% summon damage, 50 armor pierce;", "召唤物命中时，消耗10标记能量，召唤3颗追踪星核，星核造成20%召唤伤害，50护甲穿透");
        registries.language("enchantment.summoner.dark_harvest.desc.1", "On minion hit: deal 25% summon damage to up to 3 other targets within 6 blocks", "召唤物命中时，对半径6格内最多3个其他目标造成25%召唤伤害");
        registries.language("enchantment.summoner.constellation.desc.1", "On minion hit: consume 30 mark energy to summon 6 homing stellar cores dealing 30% summon damage", "召唤物命中时，消耗30标记能量，召唤6颗追踪星核，星核造成30%召唤伤害");
        registries.language("enchantment.summoner.current_surge.desc.1", "On minion hit: consume 30 mark energy to deal 100% summon damage to other targets within 6 blocks, +10% per extra target, up to 200%", "召唤物命中时，消耗30标记能量，对半径6格内其他目标造成100%召唤伤害，每多一个目标提升10%，最高200%");
        registries.language("enchantment.summoner.requiem.desc.1", "On minion hit: consume 40 mark energy to deal summon damage equal to 250% of the target's armor value", "召唤物命中时，消耗40标记能量，造成目标护甲值250%的召唤伤害");
        registries.language("enchantment.summoner.galaxy.desc.1", "Mark energy accumulation speed +33%", "标记能量积累速度+33%");
        registries.language("enchantment.summoner.galaxy.desc.2", "On minion hit: consume 40 mark energy to deal an additional 400% summon damage", "召唤物命中时，消耗40标记能量，对目标额外造成400%召唤伤害");
        registries.language("enchantment.summoner.silent_end.desc.1", "Mark duration +200%", "标记持续时间+200%");
        registries.language("enchantment.summoner.moon_bite.desc.1", "On minion hit: apply Moon Bite to the target for 3s", "召唤物命中时，为目标施加3秒月噬效果");
        registries.language("enchantment.summoner.moon_bite.desc.2", "On minion kill: apply Moon Bite for 10s to all other targets within 3 blocks", "召唤物击杀时，对半径3格内的所有其他目标施加10秒月噬效果");
        registries.language("enchantment.summoner.angel.desc.1", "On minion hit: if your health is not full, consume 20 mark energy to restore 5% of missing health +2 (up to 10 HP)", "召唤物命中时，生命值不满时，消耗20标记能量，为你恢复5%已损生命值+2点生命值，最多10点");
        registries.language("enchantment.summoner.god_slayer.desc.1", "On minion hit: apply God Slayer Inferno to the target for 10s", "召唤物命中时，为目标施加10秒弑神怒焰效果");
        registries.language("enchantment.summoner.god_slayer.desc.2", "Mark energy accumulation speed +33%", "标记能量积累速度+33%");
        registries.language("enchantment.summoner.cerulean_dawn.desc.1", "Critical damage +50%", "暴击伤害提升50%");
        registries.language("enchantment.summoner.shift_detail", "Hold Shift for Details", "按下Shift键显示附魔详情");
        // ===================== 标记通用数值 tooltip（SummonMarkType.getTooltips 统一使用） =====================
        registries.language("mark.additional_damage", "Mark Damage", "标记伤害");
        registries.language("mark.additional_armor_pierce", "Armor Pierce", "护甲穿透");
        registries.language("mark.critical_hit_rate", "Crit Rate", "暴击率");
    }

    @SuppressWarnings("SameParameterValue")
    private static void potionLanguage(LyraItemRegistries registries, String effectName, String enName, String zhName) {
        for (String variant : new String[]{"", "long_", "strong_"}) {
            String suffix = variant + effectName;
            registries.language("item.minecraft.potion.effect." + suffix, "Potion of " + enName, zhName + "药水");
            registries.language("item.minecraft.splash_potion.effect." + suffix, "Splash Potion of " + enName, "喷溅型" + zhName + "药水");
            registries.language("item.minecraft.lingering_potion.effect." + suffix, "Lingering Potion of " + enName, "滞留型" + zhName + "药水");
            registries.language("item.minecraft.tipped_arrow.effect." + suffix, "Arrow of " + enName, zhName + "之箭");
        }
    }
}
