package first.servantry.common.dataComponent;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * 剑鞘容器 - 存储剑鞘中的物品。
 */
public record ScabbardContainer(ItemStack itemStack) {

    public static final Codec<ScabbardContainer> CODEC = ItemStack.CODEC
            .xmap(ScabbardContainer::new, ScabbardContainer::itemStack);

    public static final StreamCodec<RegistryFriendlyByteBuf, ScabbardContainer> STREAM_CODEC = ItemStack.STREAM_CODEC
            .map(ScabbardContainer::new, ScabbardContainer::itemStack);

    public static final ScabbardContainer EMPTY = new ScabbardContainer(ItemStack.EMPTY);

    public boolean isEmpty() {
        return itemStack.isEmpty();
    }
}
