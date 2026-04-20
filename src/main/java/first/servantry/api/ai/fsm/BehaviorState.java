package first.servantry.api.ai.fsm;

import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;

/**
 * 仆从行为状态。每次进入状态都会实例化一份新的 BehaviorState，
 * 因此实现类可以安全地持有状态局部数据（如 stateTick、prepPos 等）。
 *
 * 所有方法均为默认实现，实现类按需覆盖；与旧版 ServantAction 相比，
 * 不再强制子类处理 network-id、isFinished 等与框架耦合的逻辑。
 */
public interface BehaviorState<T extends Servant> {
    String id();

    default void onEnter(T servant, LivingEntity target) {}

    default void onTick(T servant, LivingEntity target) {}

    default void onExit(T servant) {}

    default boolean canBeInterrupted() { return true; }

    default boolean isAttack() { return false; }
}
