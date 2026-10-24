package first.summoner.common.item;

import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class StarlightCommunicator extends Item {

    public StarlightCommunicator(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            Astrageldon astrageldon = new Astrageldon(SummonerEntityRegister.ASTRAGELDON.get(), level);
            astrageldon.setPos(player.getX(), player.getY() + 4, player.getZ());
            level.addFreshEntity(astrageldon);
        }
        return InteractionResultHolder.success(itemStack);
    }
}
