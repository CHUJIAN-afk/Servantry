package first.summoner.common.item;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.sound.Playable;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class GildedWandItem extends Item {

    public GildedWandItem(Supplier<Properties> properties) {
        super(properties.get());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (!player.isShiftKeyDown()) {
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(itemStack);
        } else {
            if (!level.isClientSide()) {
                AttachmentEntityData entityData = LyraHelper.get(player).getEntityData();
                entityData.get(AttachmentEntity.class).forEach(AttachmentEntity::setRemove);
                entityData.get(AttachmentEntity.class).forEach(AttachmentEntity::setRemove);
                Playable.play(SummonerSoundRegister.UseMinionWeapon, level, player.position(), player.getSoundSource());
                if (!player.isCreative()) {
                    itemStack.hurtAndBreak(60, player, EquipmentSlot.MAINHAND);
                }
            }
            return InteractionResultHolder.success(itemStack);
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack, @NotNull net.minecraft.world.entity.LivingEntity user) {
        return 72000;
    }

    @Override
    public @NotNull net.minecraft.world.item.UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        // 长按引导时显示拉弓动画（对齐 26.2）
        return net.minecraft.world.item.UseAnim.BOW;
    }
}
