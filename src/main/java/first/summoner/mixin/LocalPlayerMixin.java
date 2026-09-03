package first.summoner.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import first.summoner.common.item.GildedWandItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 镀金魔杖长按引导时豁免使用物品的移动减速（1.21.1 的 LocalPlayer.aiStep 会把
 * 使用物品中的玩家移动输入降为 0.2 倍；26.2 无此减速，此处对齐 26.2 行为）。
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Shadow
    private boolean startedUsingItem;

    @WrapMethod(method = "aiStep")
    private void lyra$aiStep(Operation<Void> original) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        ItemStack useItem = self.getUseItem();
        boolean exempt = self.isUsingItem() && useItem.getItem() instanceof GildedWandItem;
        if (exempt) {
            // 临时退出“使用中”状态，跳过 aiStep 内的移动减速与疾跑限制，随后恢复
            boolean wasUsing = this.startedUsingItem;
            this.startedUsingItem = false;
            original.call();
            this.startedUsingItem = wasUsing;
            return;
        }
        original.call();
    }
}
