package first.servantry.api.item;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
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

    /** 获取仆从护甲穿透。 */
    float getArmorPierce();

    /** 获取召唤时播放的音效。 */
    default SoundEvent getSoundEvent() {
        return null;
    }

    /**
     * 构建一个已初始化属性的仆从实例。
     */
    default T createServant(Player player) {
        T servant = getType().factory().get();
        servant.setOwner(player);
        servant.setDamage(getDamage());
        servant.setKnockback(getKnockback());
        servant.setArmorPierce(getArmorPierce());
        return servant;
    }

    /**
     * 处理仆从召唤逻辑。
     */
    void summon(Player player);

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
        private float armorPierce = 0;
        private Supplier<SoundEvent> soundEventSupplier = () -> null;
        private BiConsumer<IServantWeapon<T>, Player> summonAction = (weapon, player) -> {
            T servant = weapon.createServant(player);
            if (ServantryHelper.get(player).summonServant(servant)) {
                RandomSource random = player.getRandom();
                servant.init(new PathNode(player.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0));
            }
        };
        private Consumer<Player> onRemove = null;
        private Consumer<Item.Properties> properties = null;

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

        /** 设置仆从护甲穿透。 */
        public Builder<T> armorPierce(float armorPierce) {
            this.armorPierce = armorPierce;
            return this;
        }

        /** 设置召唤时播放的音效。 */
        public Builder<T> sound(Supplier<SoundEvent> soundEventSupplier) {
            this.soundEventSupplier = soundEventSupplier;
            return this;
        }

        /**
         * 完整重写召唤逻辑，weapon 可调用 createServant 构建实例。
         */
        public Builder<T> summon(BiConsumer<IServantWeapon<T>, Player> action) {
            this.summonAction = action;
            return this;
        }

        /** 设置仆从移除回调。 */
        public Builder<T> onRemove(Consumer<Player> action) {
            this.onRemove = action;
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
            public float getArmorPierce() {
                return armorPierce;
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
            public void summon(Player player) {
                summonAction.accept(this, player);
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
