package first.servantry.api.item;

import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 仆从武器接口，定义可召唤仆从的武器物品行为。
 * <p>
 * 实现此接口的物品将获得召唤/移除仆从的能力。
 * 通常与 {@link Builder} 配合使用，通过构建器模式创建武器物品。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用 Builder 创建武器物品
 * public static final Item MY_WEAPON = new IServantWeapon.Builder<>(() -> ServantRegister.MyServant.get())
 *     .sound(() -> SoundEvents.EVOKER_CAST_SPELL)
 *     .onSummon(servant -> {
 *         servant.init(new PathNode(player.position(), 0, 0, 0));
 *     })
 *     .buildItem(new Item.Properties().stacksTo(1));
 *
 * // 手动实现接口
 * public class MyWeaponItem extends Item implements IServantWeapon<MyServant> {
 *     public MyWeaponItem(Properties properties) { super(properties); }
 *
 *     @Override public ServantType<MyServant> getType() { return ServantRegister.MyServant.get(); }
 *     @Override public MyServant getDummyServant() { return new MyServant(); }
 * }
 * }</pre>
 *
 * @param <T> 仆从类型
 * @see Servant
 * @see Builder
 */
public interface IServantWeapon<T extends Servant> {

    // ===================== 静态工具方法 =====================

    /**
     * 处理仆从召唤逻辑。
     * <p>
     * 执行流程：
     * <ol>
     *   <li>通过类型工厂创建仆从实例</li>
     *   <li>设置所有者为当前玩家</li>
     *   <li>尝试将仆从添加到玩家的附件数据中</li>
     *   <li>若添加成功，调用武器的 {@link #summon(Servant)} 回调</li>
     * </ol>
     * </p>
     * <p>
     * 召唤可能失败的情况：
     * <ul>
     *   <li>已达到该类型仆从的数量上限</li>
     *   <li>已达到总仆从数量上限</li>
     * </ul>
     * </p>
     *
     * @param player 召唤仆从的玩家
     * @param weapon 仆从武器实例
     * @param <T>    仆从类型
     */
    static <T extends Servant> void handleSummon(Player player, IServantWeapon<T> weapon) {
        // 创建仆从实例
        T servant = weapon.getType().factory().get();

        // 设置所有者
        EntityData data = player.getData(AttachmentRegister.EntityData);
        servant.setOwner(player);

        // 尝试召唤，成功后执行初始化回调
        if (data.summonServant(player, servant)) {
            weapon.summon(servant);
        }
    }

    // ===================== 核心抽象方法 =====================

    /**
     * 获取武器对应的仆从类型。
     * <p>
     * 返回的类型用于：
     * <ul>
     *   <li>创建仆从实例</li>
     *   <li>识别和移除特定类型的仆从</li>
     *   <li>查询该类型仆从的数量</li>
     * </ul>
     * </p>
     *
     * @return 仆从类型
     */
    ServantType<T> getType();

    /**
     * 获取用于读取静态面板数据的占位仆从实例。
     * <p>
     * 该实例不参与实际游戏逻辑，仅用于：
     * <ul>
     *   <li>在物品提示框中显示伤害、击退等属性</li>
     *   <li>提供默认的属性值计算</li>
     * </ul>
     * </p>
     * <p>
     * 实现建议：使用懒加载缓存占位实例，避免重复创建。
     * </p>
     *
     * @return 占位仆从实例
     */
    T getDummyServant();

    // ===================== 默认实现方法 =====================

    /**
     * 获取武器面板显示的伤害值。
     * <p>
     * 默认实现从占位仆从读取真实伤害值。
     * 子类可重写以提供自定义计算（如基于玩家属性的动态伤害）。
     * </p>
     *
     * @return 伤害值
     */
    default float getDamage() {
        return getDummyServant().getDamage();
    }

    /**
     * 获取武器面板显示的击退值。
     * <p>
     * 默认实现从占位仆从读取真实击退值。
     * 子类可重写以提供自定义计算。
     * </p>
     *
     * @return 击退系数
     */
    default float getKnockback() {
        return getDummyServant().getKnockback();
    }

    /**
     * 召唤仆从时的初始化回调。
     * <p>
     * 在仆从成功添加到玩家附件数据后调用。
     * 子类可重写以执行自定义初始化逻辑，如：
     * <ul>
     *   <li>设置仆从初始位置</li>
     *   <li>配置仆从状态</li>
     *   <li>播放特效</li>
     * </ul>
     * </p>
     *
     * @param servant 新召唤的仆从实例
     */
    default void summon(T servant) {}

    /**
     * 获取召唤时播放的音效。
     *
     * @return 音效事件，默认为 null（不播放音效）
     */
    default SoundEvent getSoundEvent() {
        return null;
    }

    /**
     * 移除玩家拥有的该类型仆从。
     * <p>
     * 默认实现从玩家附件数据中移除一个该类型的仆从。
     * 子类可重写以添加额外逻辑（如返还资源）。
     * </p>
     *
     * @param player 玩家
     */
    default void remove(Player player) {
        player.getData(AttachmentRegister.EntityData).removeServant(getType());
    }

    // ===================== 构建器 =====================

    /**
     * 仆从武器构建器，用于快速创建武器物品。
     * <p>
     * 通过构建器模式配置武器属性，无需手动创建 Item 子类。
     * 构建器内部会创建一个匿名 Item 子类实现 IServantWeapon 接口。
     * </p>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * public static final Item MY_WEAPON = new IServantWeapon.Builder<>(() -> ServantRegister.MyServant.get())
     *     .sound(() -> SoundEvents.EVOKER_CAST_SPELL)
     *     .onSummon(servant -> servant.init(new PathNode(player.position(), 0, 0, 0)))
     *     .onRemove(player -> player.give(new ItemStack(Items.DIAMOND)))
     *     .buildItem(new Item.Properties().stacksTo(1));
     * }</pre>
     *
     * @param <T> 仆从类型
     */
    class Builder<T extends Servant> {

        /** 仆从类型供应器 */
        private final Supplier<ServantType<T>> typeSupplier;

        /** 召唤音效供应器 */
        private Supplier<SoundEvent> soundEventSupplier = () -> null;

        /** 召唤回调 */
        private Consumer<T> onSummon = servant -> {};

        /** 移除回调（可选） */
        private Consumer<Player> onRemove = null;

        /**
         * 创建构建器。
         *
         * @param typeSupplier 仆从类型供应器，通常使用注册表的 get() 方法引用
         */
        public Builder(@NotNull Supplier<ServantType<T>> typeSupplier) {
            this.typeSupplier = typeSupplier;
        }

        /**
         * 设置召唤音效。
         *
         * @param soundEventSupplier 音效供应器
         * @return this，用于链式调用
         */
        public Builder<T> sound(Supplier<SoundEvent> soundEventSupplier) {
            this.soundEventSupplier = soundEventSupplier;
            return this;
        }

        /**
         * 设置召唤回调。
         * <p>
         * 回调在仆从成功添加到玩家附件数据后执行。
         * </p>
         *
         * @param action 召唤回调
         * @return this，用于链式调用
         */
        public Builder<T> onSummon(Consumer<T> action) {
            this.onSummon = action;
            return this;
        }

        /**
         * 设置移除回调。
         * <p>
         * 回调在移除仆从时执行，可用于返还资源等。
         * 若不设置，将使用默认的移除逻辑。
         * </p>
         *
         * @param action 移除回调
         * @return this，用于链式调用
         */
        public Builder<T> onRemove(Consumer<Player> action) {
            this.onRemove = action;
            return this;
        }

        /**
         * 构建武器物品。
         *
         * @param properties 物品属性
         * @return 配置好的武器物品实例
         */
        public Item buildItem(Item.Properties properties) {
            // 匿名类实现 IServantWeapon 接口
            class BuiltServantWeaponItem extends Item implements IServantWeapon<T> {

                /** 缓存的占位仆从实例 */
                private T dummyServant = null;

                public BuiltServantWeaponItem(Properties p) {
                    super(p);
                }

                @Override
                public ServantType<T> getType() {
                    return typeSupplier.get();
                }

                @Override
                public SoundEvent getSoundEvent() {
                    return soundEventSupplier.get();
                }

                @Override
                public T getDummyServant() {
                    // 懒加载缓存
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
                    if (onRemove != null) {
                        onRemove.accept(player);
                    } else {
                        IServantWeapon.super.remove(player);
                    }
                }
            }

            return new BuiltServantWeaponItem(properties);
        }
    }
}
