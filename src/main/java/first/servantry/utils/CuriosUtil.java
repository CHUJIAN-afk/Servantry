package first.servantry.utils;

import first.servantry.common.item.CurioItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.*;

public class CuriosUtil {

    public static final Map<UUID, Map<Item, Boolean>> CACHE = new HashMap<>();

    public static void handler(CurioChangeEvent event) {
        Map<Item, Boolean> cache = CuriosUtil.CACHE.get(event.getEntity().getUUID());
        if (cache != null && !cache.isEmpty()) {
            cache.clear();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isEquipped(LivingEntity livingEntity, Item item) {
        return CACHE.computeIfAbsent(livingEntity.getUUID(), k -> new HashMap<>()).computeIfAbsent(item, k -> {
            ICuriosItemHandler handler = CuriosApi.getCuriosInventory(livingEntity).orElse(null);
            return handler != null && handler.isEquipped(item);
        });
    }

    public static List<CurioItem> getCuriosItemList(LivingEntity livingEntity) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(livingEntity).orElse(null);
        List<CurioItem> list = new ArrayList<>();
        if (handler != null) {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            int slots = curios.getSlots();
            for (int i = 0; i < slots; i++) {
                if (curios.getStackInSlot(i).getItem() instanceof CurioItem curioItem) {
                    list.add(curioItem);
                }
            }
        }
        return list;
    }
}