package first.summoner.utils;

import first.summoner.common.attachment.CurioData;
import first.summoner.common.item.CurioItem;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Set;

public class CuriosUtil {

    public static void handler(CurioChangeEvent event) {
        LivingEntity entity = event.getEntity();
        CurioData curioData = entity.getData(SummonerAttachmentRegister.CurioData);
        Set<CurioItem> data = curioData.curioItems();
        if (event.getFrom().getItem() instanceof CurioItem from) {
            data.remove(from);
        }
        if (event.getTo().getItem() instanceof CurioItem to) {
            data.add(to);
        }
    }

    public static boolean isEquipped(LivingEntity livingEntity, CurioItem curioItem) {
        return livingEntity.getData(SummonerAttachmentRegister.CurioData).isEquipped(curioItem);
    }

    public static Set<CurioItem> getCuriosItemList(LivingEntity livingEntity) {
        return livingEntity.getData(SummonerAttachmentRegister.CurioData).getCurioItemList();
    }
}
