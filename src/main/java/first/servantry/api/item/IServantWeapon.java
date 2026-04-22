package first.servantry.api.item;

import first.servantry.api.register.ServantType;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IServantWeapon<T extends Servant> {

    static <T extends Servant> void handleSummon(Player player, IServantWeapon<T> weapon) {
        T servant = weapon.getType().factory().get();
        ServantData data = player.getData(AttachmentRegister.ServantData);
        servant.setOwner(player);
        if (data.summon(player, servant)) {
            weapon.summon(servant);
        }
    }

    ServantType<T> getType();

    /**
     * 获取一个用于读取静态面板数据的占位仆从实例
     */
    T getDummyServant();

    /**
     * 武器面板伤害：直接读取仆从的真实伤害
     */
    default float getDamage() {
        return getDummyServant().getDamage();
    }

    /**
     * 武器面板击退：直接读取仆从的真实击退
     */
    default float getKnockback() {
        return getDummyServant().getKnockback();
    }

    default void summon(T servant) {}

    default SoundEvent getSoundEvent() {
        return null;
    }

    default void remove(Player player) {
        player.getData(AttachmentRegister.ServantData).remove(getType());
    }

    class Builder<T extends Servant> {
        private final Supplier<ServantType<T>> typeSupplier;
        private Supplier<SoundEvent> soundEventSupplier = () -> null;
        private Consumer<T> onSummon = servant -> {};
        private Consumer<Player> onRemove = null;

        public Builder(@NotNull Supplier<ServantType<T>> typeSupplier) {
            this.typeSupplier = typeSupplier;
        }

        public Builder<T> sound(Supplier<SoundEvent> soundEventSupplier) {
            this.soundEventSupplier = soundEventSupplier;
            return this;
        }

        public Builder<T> onSummon(Consumer<T> action) {
            this.onSummon = action;
            return this;
        }

        public Builder<T> onRemove(Consumer<Player> action) {
            this.onRemove = action;
            return this;
        }

        public Item buildItem(Item.Properties properties) {
            class BuiltServantWeaponItem extends Item implements IServantWeapon<T> {
                private T dummyServant = null;

                public BuiltServantWeaponItem(Properties p) { super(p); }

                @Override public ServantType<T> getType() { return typeSupplier.get(); }
                @Override public SoundEvent getSoundEvent() { return soundEventSupplier.get(); }

                @Override
                public T getDummyServant() {
                    if (dummyServant == null) {
                        dummyServant = getType().factory().get();
                                           }
                    return dummyServant;
                }

                @Override
                public void summon(T servant) {
                    onSummon.accept(servant);
                }

                @Override
                public void remove(Player player) {
                    if (onRemove != null) onRemove.accept(player);
                    else IServantWeapon.super.remove(player);
                }
            }
            return new BuiltServantWeaponItem(properties);
        }
    }
}