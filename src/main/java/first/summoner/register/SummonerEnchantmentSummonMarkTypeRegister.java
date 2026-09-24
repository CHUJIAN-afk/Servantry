package first.summoner.register;

import first.lyra.common.summonMark.SummonMarkType;
import first.summoner.Summoner;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Summoner.MODID)
public class SummonerEnchantmentSummonMarkTypeRegister {

    private static final Map<Holder<Enchantment>, Holder<SummonMarkType>> registers = new HashMap<>();

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        register(SummonerEnchantmentRegister.FoodChain, SummonerSummonMarkRegister.FoodChain);
        register(SummonerEnchantmentRegister.JungleHunter, SummonerSummonMarkRegister.JungleHunter);
        register(SummonerEnchantmentRegister.DeathMagister, SummonerSummonMarkRegister.DeathMagister);
        register(SummonerEnchantmentRegister.SoulFlail, SummonerSummonMarkRegister.SoulFlail);
        register(SummonerEnchantmentRegister.Starfall, SummonerSummonMarkRegister.Starfall);
        register(SummonerEnchantmentRegister.Bramble, SummonerSummonMarkRegister.Bramble);
        register(SummonerEnchantmentRegister.Vein, SummonerSummonMarkRegister.Vein);
        register(SummonerEnchantmentRegister.Spine, SummonerSummonMarkRegister.Spine);
        register(SummonerEnchantmentRegister.Firecracker, SummonerSummonMarkRegister.Firecracker);
        register(SummonerEnchantmentRegister.FrostSnow, SummonerSummonMarkRegister.FrostSnow);
        register(SummonerEnchantmentRegister.DivineJudgment, SummonerSummonMarkRegister.DivineJudgment);
        register(SummonerEnchantmentRegister.RoughDisplayOfFlowers, SummonerSummonMarkRegister.RoughDisplayOfFlowers);
        register(SummonerEnchantmentRegister.DominanceOmen, SummonerSummonMarkRegister.DominanceOmen);
        register(SummonerEnchantmentRegister.DarkHarvest, SummonerSummonMarkRegister.DarkHarvest);
        register(SummonerEnchantmentRegister.Constellation, SummonerSummonMarkRegister.Constellation);
        register(SummonerEnchantmentRegister.CurrentSurge, SummonerSummonMarkRegister.CurrentSurge);
        register(SummonerEnchantmentRegister.MorningStar, SummonerSummonMarkRegister.MorningStar);
        register(SummonerEnchantmentRegister.Kaleidoscope, SummonerSummonMarkRegister.Kaleidoscope);
        register(SummonerEnchantmentRegister.Requiem, SummonerSummonMarkRegister.Requiem);
        register(SummonerEnchantmentRegister.Galaxy, SummonerSummonMarkRegister.Galaxy);
        register(SummonerEnchantmentRegister.SilentEnd, SummonerSummonMarkRegister.SilentEnd);
        register(SummonerEnchantmentRegister.MoonBite, SummonerSummonMarkRegister.MoonBite);
        register(SummonerEnchantmentRegister.Angel, SummonerSummonMarkRegister.Angel);
        register(SummonerEnchantmentRegister.GodSlayer, SummonerSummonMarkRegister.GodSlayer);
    }

    public static void register(Holder<Enchantment> enchantmentHolder, Holder<SummonMarkType> summonMarkTypeHolder) {
        if (!registers.containsKey(enchantmentHolder)) {
            registers.put(enchantmentHolder, summonMarkTypeHolder);
        }
    }

    public static Holder<SummonMarkType> getSummonMarkType(Holder<Enchantment> enchantmentHolder) {
        return registers.get(enchantmentHolder);
    }

    public static Set<SummonMarkType> resolveActiveMarkType(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Set<SummonMarkType> set = new HashSet<>();
        if (!enchantments.isEmpty()) {
            Set<Holder<Enchantment>> holders = enchantments.keySet();
            for (Holder<Enchantment> holder : holders) {
                Holder<SummonMarkType> summonMarkTypeHolder = registers.get(holder);
                if (summonMarkTypeHolder != null) {
                    set.add(summonMarkTypeHolder.value());
                }
            }
        }
        return set;
    }
}
