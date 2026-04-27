package first.servantry.api.item;

import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 仆从武器接口，定义可召唤仆从的武器物品行为。
 * <p>
 * 实现此接口的物品将获得召唤/移除仆从的能力。
 * 通常与 {@link Builder} 配合使用，通过构建器模式创建武器物品。
 * </p>
 *
 * @param <T> 仆从类型
 * @see Servant
 * @see Builder
 */
public interface IServantWeapon<T extends Servant> {

    // ===================== 静态工具方法 =====================

    /**
     * 处理仆从召唤逻辑。
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

    ServantType<T> getType();

    T getDummyServant();

    // ===================== 默认实现方法 =====================

    default float getDamage() {
        return getDummyServant().getDamage();
    }

    default float getKnockback() {
        return getDummyServant().getKnockback();
    }

    default void summon(T servant) {
    }

    default SoundEvent getSoundEvent() {
        return null;
    }

    default void remove(Player player) {
        player.getData(AttachmentRegister.EntityData).removeServant(getType());
    }

    // ===================== 构建器 =====================

    /**
     * 仆从武器构建器，用于快速创建武器物品。
     * <p>
     * 支持两种模式：
     * <ul>
     *   <li>普通模式：每次召唤创建一个仆从</li>
     *   <li>多体节模式：首次召唤创建多个体节，重复召唤增加体节</li>
     * </ul>
     * </p>
     *
     * @param <T> 仆从类型
     */
    class Builder<T extends Servant> {

        private final Supplier<ServantType<T>> typeSupplier;
        private Supplier<SoundEvent> soundEventSupplier = () -> null;
        private Consumer<T> onSummon = servant -> {};
        private Consumer<Player> onRemove = null;
        private int maxCount = Integer.MAX_VALUE;

        // 多体节模式配置
        private boolean multiSegmentMode = false;
        private int initialSegments = 1;
        private int slotCostPerSegment = 1;
        private double segmentDistance = 0.8;
        private BiConsumer<Player, T> onSegmentInit = null;

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

        public Builder<T> maxCount(int max) {
            this.maxCount = max;
            return this;
        }

        /**
         * 启用多体节模式。
         * <p>
         * 首次召唤创建多个体节，重复召唤增加体节。
         * 需要仆从类实现 {@link ISegmentServant} 接口。
         * </p>
         *
         * @param initialSegments 首次召唤的体节数量
         * @param segmentDistance 体节之间的固定距离
         * @param slotCostPerSegment 每个体节占用的栏位数
         * @return this，用于链式调用
         */
        public Builder<T> multiSegment(int initialSegments, double segmentDistance, int slotCostPerSegment) {
            this.multiSegmentMode = true;
            this.initialSegments = initialSegments;
            this.segmentDistance = segmentDistance;
            this.slotCostPerSegment = slotCostPerSegment;
            return this;
        }

        /**
         * 设置体节初始化回调（多体节模式专用）。
         *
         * @param action 回调，参数为玩家和体节实例
         * @return this，用于链式调用
         */
        public Builder<T> onSegmentInit(BiConsumer<Player, T> action) {
            this.onSegmentInit = action;
            return this;
        }

        public Item buildItem(Item.Properties properties) {
            if (multiSegmentMode) {
                return new MultiSegmentServantWeaponItem(properties);
            } else {
                return new SimpleServantWeaponItem(properties);
            }
        }

        // ===================== 简单模式武器 =====================

        private class SimpleServantWeaponItem extends Item implements IServantWeapon<T> {
            private T dummyServant = null;

            public SimpleServantWeaponItem(Properties p) {
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
                if (dummyServant == null) {
                    dummyServant = getType().factory().get();
                }
                return dummyServant;
            }

            @Override
            public void handleSummon(Player player) {
                EntityData data = player.getData(AttachmentRegister.EntityData);
                ServantType<T> type = getType();

                // 检查当前数量
                long currentCount = data.getEntities().stream()
                        .filter(e -> type.equals(e.getType()))
                        .count();

                if (currentCount >= maxCount) {
                    return; // 已达到最大数量
                }

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

        // ===================== 多体节模式武器 =====================

        private class MultiSegmentServantWeaponItem extends Item implements IServantWeapon<T> {
            private T dummyServant = null;

            public MultiSegmentServantWeaponItem(Properties p) {
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
                if (dummyServant == null) {
                    dummyServant = getType().factory().get();
                }
                return dummyServant;
            }

            @Override
            public void handleSummon(Player player) {
                EntityData data = player.getData(AttachmentRegister.EntityData);
                ServantType<T> type = getType();

                // 查找现有的体节
                List<T> existing = data.getEntities().stream()
                        .filter(e -> type.equals(e.getType()))
                        .map(e -> (T) e)
                        .sorted((a, b) -> Integer.compare(getSegmentIndex(a), getSegmentIndex(b)))
                        .toList();

                if (existing.isEmpty()) {
                    // 首次召唤：创建多个体节
                    int requiredSlots = initialSegments * slotCostPerSegment;
                    int availableSlots = data.getMaxServantSize(player) - data.getUsedSlots();

                    if (availableSlots < requiredSlots) {
                        return; // 栏位不足
                    }

                    // 创建头部
                    T head = type.factory().get();
                    head.setOwner(player);
                    setSegmentIndex(head, 0);
                    setTotalSegments(head, initialSegments);

                    if (!data.summonServant(player, head)) return;

                    // 初始化头部位置
                    if (onSegmentInit != null) {
                        onSegmentInit.accept(player, head);
                    } else {
                        onSummon.accept(head);
                    }

                    // 创建后续体节
                    Vec3 headPos = head.getPos();
                    for (int i = 1; i < initialSegments; i++) {
                        T segment = type.factory().get();
                        segment.setOwner(player);
                        setSegmentIndex(segment, i);
                        setTotalSegments(segment, initialSegments);

                        if (!data.summonServant(player, segment)) break;

                        // 在头部后方按固定距离排列
                        Vec3 spawnPos = headPos.subtract(0, 0, i * segmentDistance);
                        segment.init(new PathNode(spawnPos, 0, 0, 0));
                    }
                } else {
                    // 增加体节
                    int requiredSlots = slotCostPerSegment;
                    int availableSlots = data.getMaxServantSize(player) - data.getUsedSlots();

                    if (availableSlots < requiredSlots) {
                        return; // 栏位不足
                    }

                    T last = existing.getLast();
                    T newSegment = type.factory().get();
                    newSegment.setOwner(player);
                    setSegmentIndex(newSegment, getSegmentIndex(last) + 1);

                    if (data.summonServant(player, newSegment)) {
                        newSegment.init(last.getCurrentPathNode());
                    }
                }
            }

            @Override
            public void summon(T servant) {
                onSummon.accept(servant);
            }

            @Override
            public void remove(Player player) {
                EntityData data = player.getData(AttachmentRegister.EntityData);
                ServantType<T> type = getType();

                List<T> existing = data.getEntities().stream()
                        .filter(e -> type.equals(e.getType()))
                        .map(e -> (T) e)
                        .sorted((a, b) -> Integer.compare(getSegmentIndex(b), getSegmentIndex(a)))
                        .toList();

                if (!existing.isEmpty()) {
                    data.removeServant(type);
                }
            }

            private int getSegmentIndex(T servant) {
                if (servant instanceof ISegmentServant seg) {
                    return seg.getSegmentIndex();
                }
                return 0;
            }

            private void setSegmentIndex(T servant, int index) {
                if (servant instanceof ISegmentServant seg) {
                    seg.setSegmentIndex(index);
                }
            }

            private void setTotalSegments(T servant, int total) {
                if (servant instanceof ISegmentServant seg) {
                    seg.setTotalSegments(total);
                }
            }
        }
    }

    /**
     * 多体节仆从接口，用于多体节模式。
     */
    interface ISegmentServant {
        int getSegmentIndex();
        void setSegmentIndex(int index);
        int getTotalSegments();
        void setTotalSegments(int total);
    }
}
