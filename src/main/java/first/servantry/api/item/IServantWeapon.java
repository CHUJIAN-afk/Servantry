package first.servantry.api.item;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.Servant;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 仆从武器接口，定义可召唤仆从的武器物品行为。
 */
public interface IServantWeapon<T extends Servant> {

    /**
     * 获取此武器对应的仆从类型。
     */
    AttachmentEntityType<T> getType();

    /** 获取仆从伤害值。 */
    float getDamage();

    /** 获取仆从击退力度。 */
    float getKnockback();

    /**
     * 获取每次召唤的仆从数量，默认为1。
     */
    default int getSummonCount() {
        return 1;
    }

    /** 获取召唤时播放的音效。 */
    default SoundEvent getSoundEvent() {
        return null;
    }

    /**
     * 召唤前的回调，返回false可取消召唤。
     */
    default boolean summonPre(Player player, T servant) {
        return true;
    }

    /**
     * 仆从被召唤后的回调。
     */
    default void summonPost(T servant) {
    }

    /**
     * 处理仆从召唤逻辑。
     */
    default void handleSummon(Player player) {
        ServantryHelper servantryHelper = ServantryHelper.get(player);
        AttachmentEntityType<T> type = getType();
        for (int i = 0; i < getSummonCount(); i++) {
            T servant = type.factory().get();
            servant.setOwner(player);
            servant.setDamage(getDamage());
            servant.setKnockback(getKnockback());
            if (summonPre(player, servant)) {
                if (servantryHelper.summonServant(servant)) {
                    summonPost(servant);
                }
            }
        }
    }

    /**
     * 移除玩家拥有的此类型仆从。
     */
    default void remove(Player player) {
        ServantryHelper.get(player).getEntityData().remove(EntityData.Type.Servant, getType());
    }

    // ===================== 构建器 =====================

    /**
     * 仆从武器构建器，通过链式配置创建武器物品。
     */
    class Builder<T extends Servant> {

        private final Supplier<AttachmentEntityType<T>> typeSupplier;
        private float damage = 0;
        private float knockback = 0;
        private Supplier<SoundEvent> soundEventSupplier = () -> null;
        private BiPredicate<Player, T> summonPre = (player, servant) -> true;
        private Consumer<T> summonPost = servant -> {
        };
        private Consumer<Player> onRemove = null;
        private Consumer<Item.Properties> properties = null;
        private int summonCount = 1;

        public Builder(@NotNull Supplier<AttachmentEntityType<T>> typeSupplier) {
            this.typeSupplier = typeSupplier;
        }

        /** 设置仆从伤害值。 */
        public Builder<T> damage(float damage) {
            this.damage = damage;
            return this;
        }

        /** 设置仆从击退力度。 */
        public Builder<T> knockback(float knockback) {
            this.knockback = knockback;
            return this;
        }

        /** 设置召唤时播放的音效。 */
        public Builder<T> sound(Supplier<SoundEvent> soundEventSupplier) {
            this.soundEventSupplier = soundEventSupplier;
            return this;
        }

        /**
         * 设置召唤前回调，返回false取消召唤。
         */
        public Builder<T> summonPre(BiPredicate<Player, T> predicate) {
            this.summonPre = predicate;
            return this;
        }

        /**
         * 设置仆从召唤后回调。
         */
        public Builder<T> summonPost(Consumer<T> action) {
            this.summonPost = action;
            return this;
        }

        /** 设置仆从移除回调。 */
        public Builder<T> onRemove(Consumer<Player> action) {
            this.onRemove = action;
            return this;
        }

        /** 设置每次召唤的仆从数量。 */
        public Builder<T> summonCount(int count) {
            this.summonCount = count;
            return this;
        }

        public Builder<T> properties(Consumer<Item.Properties> properties) {
            this.properties = properties;
            return this;
        }

        /** 构建武器物品。 */
        public Item build() {
            Item.Properties proper = new Item.Properties().stacksTo(1);
            if (properties != null) {
                properties.accept(proper);
            }
            return new ServantWeaponItem(proper);
        }

        private class ServantWeaponItem extends Item implements IServantWeapon<T> {

            public ServantWeaponItem(Properties p) {
                super(p);
            }

            @Override
            public AttachmentEntityType<T> getType() {
                return typeSupplier.get();
            }

            @Override
            public float getDamage() {
                return damage;
            }

            @Override
            public float getKnockback() {
                return knockback;
            }

            @Override
            public SoundEvent getSoundEvent() {
                return soundEventSupplier.get();
            }

            @Override
            public int getSummonCount() {
                return summonCount;
            }

            @Override
            public boolean summonPre(Player player, T servant) {
                return summonPre.test(player, servant);
            }

            @Override
            public void summonPost(T servant) {
                summonPost.accept(servant);
            }

            @Override
            public void remove(Player player) {
                if (onRemove != null) {
                    onRemove.accept(player);
                } else {
                    IServantWeapon.super.remove(player);
                }
            }
        }
    }
}
