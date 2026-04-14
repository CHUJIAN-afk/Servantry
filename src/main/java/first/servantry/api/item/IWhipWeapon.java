package first.servantry.api.item;

import first.servantry.api.register.MarkerType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 鞭子武器的接口，建议使用内置的buildItem
 */
public interface IWhipWeapon {

    int getUseTime();
    float getDamage();
    double getLength();
    ResourceLocation getTexture();

    float getDamageFalloff();
    boolean canPenetrateBlocks();

    @NotNull MarkerType getBoundMarker();
    @NotNull SoundEvent getSwingSound();
    @NotNull SoundEvent getTipHitSound();

    default void onHitEntity(Player player, LivingEntity target) {}
    default void onLastTargetHit(Player player, LivingEntity target) {}
    default void onTipRender(Player player, Vec3 tipPos, Vec3 movementVector) {}

    @FunctionalInterface
    interface HitEntityAction {
        void accept(Player player, LivingEntity target);
    }

    @FunctionalInterface
    interface TipRenderAction {
        void accept(Player player, Vec3 tipPos, Vec3 movementVector);
    }

    class Builder {
        private int useTime = 20;
        private float damage = 1.0f;
        private double length = 6.0;
        private ResourceLocation texture = null;
        private float damageFalloff = 0.5f;
        private boolean penetrateBlocks = false;

        private final Supplier<MarkerType> marker;
        private final SoundEvent swingSound;
        private final SoundEvent tipHitSound;

        private HitEntityAction onHit = (player, target) -> {};
        private HitEntityAction onLastHit = (player, target) -> {};
        private TipRenderAction onTipRender = (player, tipPos, movementVector) -> {};

        public Builder(@NotNull Supplier<MarkerType> marker, @NotNull SoundEvent swingSound, @NotNull SoundEvent tipHitSound) {
            this.marker = marker;
            this.swingSound = swingSound;
            this.tipHitSound = tipHitSound;
        }

        public Builder useTime(int useTime) { this.useTime = useTime; return this; }
        public Builder damage(float damage) { this.damage = damage; return this; }
        public Builder length(double length) { this.length = length; return this; }
        public Builder texture(ResourceLocation texture) { this.texture = texture; return this; }
        public Builder damageFalloff(float falloff) { this.damageFalloff = falloff; return this; }
        public Builder penetrateBlocks(boolean penetrateBlocks) { this.penetrateBlocks = penetrateBlocks; return this; }

        public Builder onHitEntity(HitEntityAction action) { this.onHit = action; return this; }
        public Builder onLastTargetHit(HitEntityAction action) { this.onLastHit = action; return this; }
        public Builder onTipRender(TipRenderAction action) { this.onTipRender = action; return this; }

        public Item buildItem(Item.Properties itemProperties) {
            class BuiltWhipItem extends Item implements IWhipWeapon {
                public BuiltWhipItem(Properties p) { super(p); }

                @Override public int getUseTime() { return useTime; }
                @Override public float getDamage() { return damage; }
                @Override public double getLength() { return length; }
                @Override public ResourceLocation getTexture() { return texture; }
                @Override public float getDamageFalloff() { return damageFalloff; }
                @Override public boolean canPenetrateBlocks() { return penetrateBlocks; }

                @Override public @NotNull MarkerType getBoundMarker() { return marker.get(); }
                @Override public @NotNull SoundEvent getSwingSound() { return swingSound; }
                @Override public @NotNull SoundEvent getTipHitSound() { return tipHitSound; }

                @Override public void onHitEntity(Player player, LivingEntity target) { onHit.accept(player, target); }
                @Override public void onLastTargetHit(Player player, LivingEntity target) { onLastHit.accept(player, target); }
                @Override public void onTipRender(Player player, Vec3 pos, Vec3 movementVector) { onTipRender.accept(player, pos, movementVector); }
                @Override public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
                    return false;
                }

            }
            return new BuiltWhipItem(itemProperties);
        }
    }
}