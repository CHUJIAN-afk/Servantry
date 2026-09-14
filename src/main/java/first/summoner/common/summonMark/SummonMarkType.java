package first.summoner.common.summonMark;

import first.lyra.common.attachmentEntity.AttachmentEntityDamageSource;
import first.summoner.common.attachment.SummonMarkData;
import first.summoner.register.SummonerSummonMarkRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public record SummonMarkType(ResourceLocation identifier, float additionalDamage, float additionalArmorPierce, float criticalHitRate, @Nullable BiConsumer<LivingEntity, Player> tickConsumer, @Nullable SummonMarkDamageConsumer damageConsumer, @Nullable BiConsumer<LivingEntity, AttachmentEntityDamageSource> killConsumer) {

    public List<Component> getTooltips() {
        List<Component> toolTips = new ArrayList<>();
        if (additionalDamage > 0) {
            toolTips.add(Component.literal(String.format("%.1f ", additionalDamage)).withStyle(ChatFormatting.BLUE).append(Component.translatable("mark.additional_damage").withStyle(ChatFormatting.GRAY)));
        }
        if (additionalArmorPierce > 0) {
            toolTips.add(Component.literal(String.format("%.1f ", additionalArmorPierce)).withStyle(ChatFormatting.BLUE).append(Component.translatable("mark.additional_armor_pierce").withStyle(ChatFormatting.GRAY)));
        }
        if (criticalHitRate > 0) {
            toolTips.add(Component.literal(String.format("%.0f%% ", criticalHitRate * 100)).withStyle(ChatFormatting.BLUE).append(Component.translatable("mark.critical_hit_rate").withStyle(ChatFormatting.GRAY)));
        }
        return toolTips;
    }

    public void tick(LivingEntity living, Player player) {
        if (this.tickConsumer != null) {
            this.tickConsumer.accept(living, player);
        }
    }

    public void damage(SummonMarkData summonMarkData, LivingEntity livingEntity, AttachmentEntityDamageSource damageSource, float damage) {
        if (this.damageConsumer != null) {
            this.damageConsumer.accept(summonMarkData, livingEntity, damageSource, damage);
        }
    }

    public void kill(LivingEntity livingEntity, AttachmentEntityDamageSource damageSource) {
        if (this.killConsumer != null) {
            this.killConsumer.accept(livingEntity, damageSource);
        }
    }

    public boolean isEmpty() {
        return this == SummonerSummonMarkRegister.Empty.get();
    }

    @FunctionalInterface
    public interface SummonMarkDamageConsumer {
        void accept(SummonMarkData summonMark, LivingEntity livingEntity, AttachmentEntityDamageSource damageSource, float damage);
    }
}
