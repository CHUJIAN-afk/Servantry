package first.summoner.register;

import first.summoner.Summoner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public class SummonerEnchantmentRegister {

    public static final DeferredRegister<Enchantment> Register = DeferredRegister.create(Registries.ENCHANTMENT, Summoner.MODID);
    public static final Map<DeferredHolder<Enchantment, Enchantment>, Enchantment.EnchantmentDefinition> RegisterCache = new HashMap<>();

    /**
     * 食物链 - 命中加速+减速（可重复触发）
     */
    public static final DeferredHolder<Enchantment, Enchantment> FoodChain = register("food_chain", 32, 2);
    /**
     * 丛林捕手
     */
    public static final DeferredHolder<Enchantment, Enchantment> JungleHunter = register("jungle_hunter", 32, 2);
    /**
     * 死亡执政官 - 击杀溅射
     */
    public static final DeferredHolder<Enchantment, Enchantment> DeathMagister = register("death_magister", 32, 2);
    /**
     * 灵魂鞭挞
     */
    public static final DeferredHolder<Enchantment, Enchantment> SoulFlail = register("soul_flail", 32, 2);
    /**
     * 星坠 - 命中生成流星
     */
    public static final DeferredHolder<Enchantment, Enchantment> Starfall = register("starfall", 32, 2);
    /**
     * 荆棘 - 命中加速+中毒+凋零
     */
    public static final DeferredHolder<Enchantment, Enchantment> Bramble = register("bramble", 32, 2);
    /**
     * 脉管 - 击杀回血
     */
    public static final DeferredHolder<Enchantment, Enchantment> Vein = register("vein", 8, 4);
    /**
     * 脊柱
     */
    public static final DeferredHolder<Enchantment, Enchantment> Spine = register("spine", 8, 4);
    /**
     * 鞭炮 - 命中爆炸
     */
    public static final DeferredHolder<Enchantment, Enchantment> Firecracker = register("firecracker", 8, 4);
    /**
     * 霜雪 - 命中减速2
     */
    public static final DeferredHolder<Enchantment, Enchantment> FrostSnow = register("frost_snow", 8, 4);
    /**
     * 神圣裁决 - 击杀溅射
     */
    public static final DeferredHolder<Enchantment, Enchantment> DivineJudgment = register("divine_judgment", 8, 4);
    /**
     * 花儿的粗暴展示 - 花瓣射弹
     */
    public static final DeferredHolder<Enchantment, Enchantment> RoughDisplayOfFlowers = register("rough_display_of_flowers", 8, 4);
    /**
     * 支配预兆 - 星核射弹+月噬
     */
    public static final DeferredHolder<Enchantment, Enchantment> DominanceOmen = register("dominance_omen", 2, 8);
    /**
     * 暗黑收割 - 溅射
     */
    public static final DeferredHolder<Enchantment, Enchantment> DarkHarvest = register("dark_harvest", 2, 8);
    /**
     * 星座 - 星核射弹
     */
    public static final DeferredHolder<Enchantment, Enchantment> Constellation = register("constellation", 2, 8);
    /**
     * 电流翻涌 - 连锁溅射
     */
    public static final DeferredHolder<Enchantment, Enchantment> CurrentSurge = register("current_surge", 2, 8);
    /**
     * 晨星
     */
    public static final DeferredHolder<Enchantment, Enchantment> MorningStar = register("morning_star", 2, 8);
    /**
     * 万花筒
     */
    public static final DeferredHolder<Enchantment, Enchantment> Kaleidoscope = register("kaleidoscope", 2, 8);
    /**
     * 安魂曲 - 1标记伤害，15%暴击率，命中消耗40标记能量造成目标护甲值250%召唤伤害
     */
    public static final DeferredHolder<Enchantment, Enchantment> Requiem = register("requiem", 2, 8);
    /**
     * 银河 - 标记能量积累+33%，命中消耗40标记能量造成400%额外召唤伤害
     */
    public static final DeferredHolder<Enchantment, Enchantment> Galaxy = register("galaxy", 2, 8);
    /**
     * 沉默的终时 - 3标记伤害，5%暴击率，标记持续时间+200%
     */
    public static final DeferredHolder<Enchantment, Enchantment> SilentEnd = register("silent_end", 2, 8);
    /**
     * 月噬 - 1标记伤害，10%暴击率，能量>5时命中施加2秒月噬，击杀时目标附近其他目标施加10秒月噬
     */
    public static final DeferredHolder<Enchantment, Enchantment> MoonBite = register("moon_bite", 2, 8);
    /**
     * 天使 - 命中时生命不满则消耗20标记能量恢复生命
     */
    public static final DeferredHolder<Enchantment, Enchantment> Angel = register("angel", 2, 8);
    /**
     * 弑神者 - 50护甲穿透，命中施加10秒弑神怒焰，能量积累+33%
     */
    public static final DeferredHolder<Enchantment, Enchantment> GodSlayer = register("god_slayer", 2, 8);
    /**
     * 水蓝色黎明 - 15%暴击率，暴击伤害+50%
     */
    public static final DeferredHolder<Enchantment, Enchantment> CeruleanDawn = register("cerulean_dawn", 2, 8);
    /**
     * 秘仪 - 1.5标记伤害，暴击伤害+15%，每2%标记暴击率额外提升1%暴击伤害
     */
    public static final DeferredHolder<Enchantment, Enchantment> ArcaneRite = register("arcane_rite", 2, 8);

    private static DeferredHolder<Enchantment, Enchantment> register(String id, int weight, int cost) {
        HolderSet<Item> holders = HolderSet.direct(SummonerWeaponRegister.GildedWand);
        Enchantment.EnchantmentDefinition definition = Enchantment.definition(holders, holders, weight, 1, Enchantment.constantCost(cost), Enchantment.constantCost(cost * 2), 1, EquipmentSlotGroup.HAND);
        DeferredHolder<Enchantment, Enchantment> register = Register.register(id, Enchantment.enchantment(definition)::build);
        if (!FMLLoader.isProduction()) {
            RegisterCache.put(register, definition);
        }
        return register;
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        RegisterCache.forEach((enchantment, definition) -> context.register(enchantment.getKey(), Enchantment.enchantment(definition).build(enchantment.getId())));
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
