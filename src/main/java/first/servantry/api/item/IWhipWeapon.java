package first.servantry.api.item;

import first.servantry.api.Marker;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 为物品实现此接口，将会获得鞭子特性
 */
public interface IWhipWeapon {

    WhipProperties getWhipProperties();

    /**
     * 鞭子的基础数据面板
     */
    record WhipProperties(
            int useTime,
            float damage,
            double length,
            ResourceLocation texture,
            @Nullable Marker marker,
            float damageFalloff,
            boolean penetrateBlocks,
            @Nullable SoundEvent swingSound,
            int baseSoundDurationTicks
    ) {}

    /**
     * 当鞭子在挥动过程中命中任何敌人时触发 (每个敌人在单次挥击中只触发一次)。
     */
    default void onHitEntity(Player player, LivingEntity target) {

    }

    /**
     * 单次挥击动作结束时，针对最后一个被命中的敌人触发。
     */
    default void onLastTargetHit(Player player, LivingEntity target) {

    }

    /**
     * 鞭子末端每tick双端触发
     */
    default void sweepTip(Player player, Vec3 pos) {

    }

    class Builder {

        private int useTime = 20;
        private float damage = 1.0f;
        private double length = 6.0;
        private ResourceLocation texture = null;
        private Marker marker = null;
        private float damageFalloff = 0.5f;
        private boolean penetrateBlocks = false;
        private SoundEvent swingSound = null;
        private int baseSoundDurationTicks = 12;

        private BiConsumer<Player, LivingEntity> onHit = null;
        private BiConsumer<Player, LivingEntity> onLastHit = null;
        private BiConsumer<Player, Vec3> sweepTipAction = null;

        public Builder useTime(int useTime) { this.useTime = useTime; return this; }
        public Builder damage(float damage) { this.damage = damage; return this; }
        public Builder length(double length) { this.length = length; return this; }
        public Builder texture(ResourceLocation texture) { this.texture = texture; return this; }
        public Builder marker(Marker marker) { this.marker = marker; return this; }
        public Builder damageFalloff(float falloff) { this.damageFalloff = falloff; return this; }
        public Builder penetrateBlocks(boolean penetrateBlocks) { this.penetrateBlocks = penetrateBlocks; return this; }

        public Builder onHitEntity(BiConsumer<Player, LivingEntity> onHitAction) {
            this.onHit = onHitAction;
            return this;
        }

        public Builder onLastTargetHit(BiConsumer<Player, LivingEntity> onLastHitAction) {
            this.onLastHit = onLastHitAction;
            return this;
        }

        public Builder sweepTip(BiConsumer<Player, Vec3> sweepTipAction){
            this.sweepTipAction = sweepTipAction;
            return this;
        }

        public Builder swingSound(SoundEvent sound, int baseDuration) {
            this.swingSound = sound;
            this.baseSoundDurationTicks = baseDuration;
            return this;
        }

        public IWhipWeapon build() {
            WhipProperties properties = new WhipProperties(useTime, damage, length, texture, marker, damageFalloff, penetrateBlocks, swingSound, baseSoundDurationTicks);

            return new IWhipWeapon() {
                @Override
                public WhipProperties getWhipProperties() {
                    return properties;
                }

                @Override
                public void onHitEntity(Player player, LivingEntity target) {
                    if (onHit != null) onHit.accept(player, target);
                }

                @Override
                public void onLastTargetHit(Player player, LivingEntity target) {
                    if (onLastHit != null) onLastHit.accept(player, target);
                }
            };
        }

        public Item buildItem(Item.Properties itemProperties) {
            WhipProperties properties = new WhipProperties(useTime, damage, length, texture, marker, damageFalloff, penetrateBlocks, swingSound, baseSoundDurationTicks);

            class BuiltWhipItem extends Item implements IWhipWeapon {
                public BuiltWhipItem(Properties p) {
                    super(p);
                }

                @Override
                public WhipProperties getWhipProperties() {
                    return properties;
                }

                @Override
                public void onHitEntity(Player player, LivingEntity target) {
                    if (onHit != null) onHit.accept(player, target);
                }

                @Override
                public void onLastTargetHit(Player player, LivingEntity target) {
                    if (onLastHit != null) onLastHit.accept(player, target);
                }

            }

            return new BuiltWhipItem(itemProperties);
        }

    }

}