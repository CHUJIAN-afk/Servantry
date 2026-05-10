package first.servantry.api.item;

import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 仆从武器接口，定义可召唤仆从的武器物品行为。
 * <p>
 * 实现此接口的物品将获得召唤/移除仆从的能力。
 * 通过 {@link Builder} 配合使用，支持链式配置创建武器物品，无需手动创建子类。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * Item weapon = new IServantWeapon.Builder<>(ServantRegister.MyServant)
 *     .sound(SoundRegister.UseServantWeapon)
 *     .onSummon(servant -> servant.init(new PathNode(...)))
 *     .maxCount(5)
 *     .buildItem(new Item.Properties().rarity(Rarity.EPIC));
 * }</pre>
 *
 * @param <T> 仆从类型，必须继承 {@link Servant}
 * @see Servant
 * @see Builder
 */
public interface IServantWeapon<T extends Servant> {

    // ===================== 静态工具方法 =====================

    /**
     * 处理仆从召唤逻辑的默认实现。
     * <p>
     * 由 Builder 创建的武器会覆盖此方法实现具体逻辑。
     * </p>
     *
     * @param player 召唤仆从的玩家
     */
    default void handleSummon(Player player) {
        T servant = this.getType().factory().get();
        EntityData data = player.getData(AttachmentRegister.EntityData);
        servant.setOwner(player);
        if (data.summonServant(player, servant)) {
            this.summon(servant);
        }
    }

    // ===================== 核心抽象方法 =====================

    /**
     * 获取此武器对应的仆从类型。
     *
     * @return 仆从类型注册项
     */
    AttachmentEntityType<T> getType();

    /**
     * 获取临时仆从实例，用于预览或获取基础属性。
     * <p>
     * 实现应缓存此实例避免重复创建。
     * </p>
     *
     * @return 临时仆从实例
     */
    T getDummyServant();

    // ===================== 默认实现方法 =====================

    /**
     * 获取仆从伤害值。
     *
     * @return 伤害值
     */
    default float getDamage() {
        return getDummyServant().getDamage();
    }

    /**
     * 获取仆从击退力度。
     *
     * @return 击退力度
     */
    default float getKnockback() {
        return getDummyServant().getKnockback();
    }

    /**
     * 仆从被召唤时的回调。
     * <p>
     * 子类可覆盖此方法实现自定义初始化逻辑。
     * </p>
     *
     * @param servant 被召唤的仆从实例
     */
    default void summon(T servant) {
    }

    /**
     * 获取召唤时播放的音效。
     *
     * @return 音效事件，可为 null
     */
    default SoundEvent getSoundEvent() {
        return null;
    }

    /**
     * 移除玩家拥有的此类型仆从。
     *
     * @param player 拥有仆从的玩家
     */
    default void remove(Player player) {
        player.getData(AttachmentRegister.EntityData).removeServant(getType());
    }

    // ===================== 构建器 =====================

    /**
     * 仆从武器构建器，通过链式配置创建武器物品，无需手动创建子类。
     * <p>
     * 每次召唤创建一个仆从，受 {@link #maxCount} 限制数量。
     * </p>
     *
     * <h3>配置项说明</h3>
     * <table border="1">
     *   <tr><th>方法</th><th>说明</th><th>默认值</th></tr>
     *   <tr><td>{@link #sound}</td><td>召唤音效</td><td>null</td></tr>
     *   <tr><td>{@link #onSummon}</td><td>召唤回调</td><td>空操作</td></tr>
     *   <tr><td>{@link #onRemove}</td><td>移除回调</td><td>默认移除逻辑</td></tr>
     *   <tr><td>{@link #maxCount}</td><td>最大数量</td><td>无限制</td></tr>
     * </table>
     *
     * @param <T> 仆从类型
     */
    class Builder<T extends Servant> {

        // -------------------- 核心配置 --------------------

        /** 仆从类型供应器 */
        private final Supplier<AttachmentEntityType<T>> typeSupplier;

        /** 召唤音效供应器 */
        private Supplier<SoundEvent> soundEventSupplier = () -> null;

        /** 召唤回调 */
        private Consumer<T> onSummon = servant -> {};

        /** 移除回调，null 表示使用默认逻辑 */
        private Consumer<Player> onRemove = null;

        /** 最大数量 */
        private int maxCount = Integer.MAX_VALUE;

        // -------------------- 构造方法 --------------------

        /**
         * 创建构建器实例。
         *
         * @param typeSupplier 仆从类型供应器，通常传入 DeferredHolder::get
         */
        public Builder(@NotNull Supplier<AttachmentEntityType<T>> typeSupplier) {
            this.typeSupplier = typeSupplier;
        }

        // -------------------- 配置方法 --------------------

        /**
         * 设置召唤时播放的音效。
         *
         * @param soundEventSupplier 音效供应器
         * @return this，用于链式调用
         */
        public Builder<T> sound(Supplier<SoundEvent> soundEventSupplier) {
            this.soundEventSupplier = soundEventSupplier;
            return this;
        }

        /**
         * 设置仆从召唤回调。
         * <p>
         * 在仆从成功召唤后调用，可用于初始化位置、朝向等。
         * </p>
         *
         * @param action 回调函数，参数为被召唤的仆从
         * @return this，用于链式调用
         */
        public Builder<T> onSummon(Consumer<T> action) {
            this.onSummon = action;
            return this;
        }

        /**
         * 设置仆从移除回调。
         * <p>
         * 不设置时使用默认逻辑：移除所有此类型的仆从。
         * </p>
         *
         * @param action 回调函数，参数为玩家
         * @return this，用于链式调用
         */
        public Builder<T> onRemove(Consumer<Player> action) {
            this.onRemove = action;
            return this;
        }

        /**
         * 设置仆从最大数量。
         * <p>
         * 达到最大数量后再次召唤将被忽略。
         * </p>
         *
         * @param max 最大数量
         * @return this，用于链式调用
         */
        public Builder<T> maxCount(int max) {
            this.maxCount = max;
            return this;
        }

        // -------------------- 构建方法 --------------------

        /**
         * 构建武器物品。
         *
         * @return 配置完成的武器物品
         */
        public Item buildItem() {
            return new ServantWeaponItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
        }

        // ===================== 武器实现 =====================

        /**
         * 仆从武器物品实现。
         * <p>
         * 每次召唤创建一个仆从，受最大数量限制。
         * </p>
         */
        private class ServantWeaponItem extends Item implements IServantWeapon<T> {

            /** 缓存的临时仆从实例 */
            private T dummyServant = null;

            public ServantWeaponItem(Properties p) {
                super(p);
            }

            @Override
            public AttachmentEntityType<T> getType() {
                return typeSupplier.get();
            }

            @Override
            public SoundEvent getSoundEvent() {
                return soundEventSupplier.get();
            }

            @Override
            public T getDummyServant() {
                if (dummyServant == null) {
                    dummyServant = getType().factory().get();
                }
                return dummyServant;
            }

            @Override
            public void handleSummon(Player player) {
                EntityData data = player.getData(AttachmentRegister.EntityData);
                AttachmentEntityType<T> type = getType();

                // 检查当前数量是否已达上限
                long currentCount = data.getEntities().stream()
                        .filter(e -> type.equals(e.getType()))
                        .count();

                if (currentCount >= maxCount) {
                    return; // 已达到最大数量，忽略召唤
                }

                // 创建并召唤仆从
                T servant = type.factory().get();
                servant.setOwner(player);
                if (data.summonServant(player, servant)) {
                    this.summon(servant);
                }
            }

            @Override
            public void summon(T servant) {
                onSummon.accept(servant);
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
