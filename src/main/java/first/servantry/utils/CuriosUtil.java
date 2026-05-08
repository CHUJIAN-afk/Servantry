package first.servantry.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CuriosUtil {

    public static final Map<UUID, Map<Item, Boolean>> CACHE = new HashMap<>();

    public static boolean isEquipped(LivingEntity livingEntity, Item item) {
        return CACHE.computeIfAbsent(livingEntity.getUUID(), k -> new HashMap<>()).computeIfAbsent(item, k -> {
            ICuriosItemHandler handler = CuriosApi.getCuriosInventory(livingEntity).orElse(null);
            return handler != null && handler.isEquipped(item);
        });
    }

}
