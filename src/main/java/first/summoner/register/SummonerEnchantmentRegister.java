package first.summoner.register;

import first.summoner.Summoner;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SummonerEnchantmentRegister {

    public static final DeferredRegister<Enchantment> Register = DeferredRegister.create(Registries.ENCHANTMENT, Summoner.MODID);
    /** 声明顺序即注册顺序，LinkedHashMap 保证数据生成产物稳定可复现。 */
    public static final Map<DeferredHolder<Enchantment, Enchantment>, Enchantment.EnchantmentDefinition> RegisterCache = new LinkedHashMap<>();

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
     * 安魂曲 - 1标记伤害，15%暴击率，命中额外造成目标护甲值250%伤害
     */
    public static final DeferredHolder<Enchantment, Enchantment> Requiem = register("requiem", 2, 8);
    /**
     * 银河 - 命中额外造成400%召唤伤害
     */
    public static final DeferredHolder<Enchantment, Enchantment> Galaxy = register("galaxy", 2, 8);
    /**
     * 沉默的终时 - 3标记伤害，5%暴击率
     */
    public static final DeferredHolder<Enchantment, Enchantment> SilentEnd = register("silent_end", 2, 8);
    /**
     * 月噬 - 1标记伤害，10%暴击率，命中施加2秒月噬，击杀时目标附近其他目标施加10秒月噬
     */
    public static final DeferredHolder<Enchantment, Enchantment> MoonBite = register("moon_bite", 2, 8);
    /**
     * 天使 - 命中时生命不满则恢复生命
     */
    public static final DeferredHolder<Enchantment, Enchantment> Angel = register("angel", 2, 8);
    /**
     * 弑神者 - 50护甲穿透，命中施加10秒弑神怒焰
     */
    public static final DeferredHolder<Enchantment, Enchantment> GodSlayer = register("god_slayer", 2, 8);

    private static DeferredHolder<Enchantment, Enchantment> register(String id, int weight, int cost) {
        HolderSet<Item> holders = HolderSet.direct(SummonerWeaponRegister.GildedWand);
        Enchantment.EnchantmentDefinition definition = Enchantment.definition(holders, holders, weight, 1, Enchantment.constantCost(cost), Enchantment.constantCost(cost * 2), 1, EquipmentSlotGroup.HAND);
        DeferredHolder<Enchantment, Enchantment> register = Register.register(id, Enchantment.enchantment(definition)::build);
        if (!FMLLoader.isProduction()) {
            RegisterCache.put(register, definition);
        }
        return register;
    }

    /**
     * 附魔是数据包注册表（{@link Registries#ENCHANTMENT}），运行时不经过 {@code RegisterEvent}，
     * 实际数据来自数据生成产出的 {@code data/summoner/enchantment/*.json}，
     * 因此互斥集合只能在这里写入。
     * <p>
     * 全部召唤标记附魔互斥：每个标记附魔的互斥集合都包含本模组注册的全部标记附魔，
     * 于是同一把鎏金法杖最多只能生效一个标记（与 {@code #minecraft:exclusive_set/*} 的写法一致，集合包含自身也无妨，
     * {@link Enchantment#areCompatible} 会先排除同一附魔）。
     * <p>
     * 集合元素必须取本注册集内的 {@link Holder.Reference}：{@code context.lookup} 按 key 复用同一实例，
     * 与 {@code register} 返回的 Holder 是同一个对象；若直接放 {@code DeferredHolder}，
     * 写文件时会因 {@code canSerializeIn} 失败而报 "not valid in current registry set"。
     */
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Enchantment> lookup = context.lookup(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> marks = RegisterCache.keySet().stream()
                .map(holder -> (Holder<Enchantment>) lookup.getOrThrow(holder.getKey()))
                .toList();
        HolderSet<Enchantment> exclusiveSet = HolderSet.direct(marks);
        RegisterCache.forEach((enchantment, definition) -> context.register(enchantment.getKey(),
                Enchantment.enchantment(definition).exclusiveWith(exclusiveSet).build(enchantment.getId())));
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
