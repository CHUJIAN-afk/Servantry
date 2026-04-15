package first.servantry.api.item;

import first.servantry.api.PathNode;
import first.servantry.api.register.ServantType;
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

/**
 * 为物品实现此接口，在右键时召唤仆从，潜行右键时移除最早召唤的该类型的一个仆从
 * 同时将会在物品提示中显示类型和仆从栏数量
 */
public interface IServantWeapon<T extends Servant> {

    static <T extends Servant> void handleSummon(Player player, IServantWeapon<T> weapon) {
        // 使用一个默认的空节点进行初始化
        T servant = weapon.getType().factory().apply(new PathNode("", Vec3.ZERO, 0, 0, 0));
        servant.setOwner(player);
        ServantData data = player.getData(AttachmentRegister.ServantData);
        if (data.summon(player, servant)) {
            weapon.summon(servant);
        }
    }

    /**
     * 获取武器的仆从类型
     */
    ServantType<T> getType();

    /**
     * 获取武器面板，只做显示
     */
    default float getDamage() {
        return 0;
    }

    /**
     * 成功召唤仆从时触发，可以根据需要对仆从位置进行首次修正
     */
    default void summon(T servant) {}

    /**
     * 获得使用音效
     */
    default SoundEvent getSoundEvent() {
        return null;
    }

    /**
     * 潜行右键试图移除仆从时
     */
    default void remove(Player player) {
        player.getData(AttachmentRegister.ServantData).remove(getType());
    }

    // ======================================================================
    // 🛠️ 高度优雅的建造者内部类，彻底消灭冗余的 Item 类！
    // ======================================================================
    class Builder<T extends Servant> {
        private final Supplier<ServantType<T>> typeSupplier;
        private float damage = 0f;
        private Supplier<SoundEvent> soundEventSupplier = () -> null;
        private Consumer<T> onSummon = servant -> {};
        private Consumer<Player> onRemove = null;

        public Builder(@NotNull Supplier<ServantType<T>> typeSupplier) {
            this.typeSupplier = typeSupplier;
        }

        public Builder<T> damage(float damage) {
            this.damage = damage;
            return this;
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
                public BuiltServantWeaponItem(Properties p) { super(p); }

                @Override public ServantType<T> getType() { return typeSupplier.get(); }
                @Override public float getDamage() { return damage; }
                @Override public SoundEvent getSoundEvent() { return soundEventSupplier.get(); }

                @Override
                public void summon(T servant) {
                    onSummon.accept(servant);
                }

                @Override
                public void remove(Player player) {
                    if (onRemove != null) onRemove.accept(player);
                    else IServantWeapon.super.remove(player); // 默认移除逻辑
                }
            }
            return new BuiltServantWeaponItem(properties);
        }
    }
}