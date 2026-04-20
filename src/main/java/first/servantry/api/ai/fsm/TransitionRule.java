package first.servantry.api.ai.fsm;

import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

/**
 * 状态转换规则：判定在给定上下文下是否应当切换到目标状态。
 *
 * 每条规则独立存在，不与 BehaviorState 耦合，从而可以集中声明、
 * 统一维护，新增/修改转换逻辑无需修改具体 State 类。
 */
public final class TransitionRule<T extends Servant> {

    @FunctionalInterface
    public interface Condition<T extends Servant> {
        boolean test(T servant, LivingEntity target, String currentStateId);
    }

    private final Condition<T> condition;
    private final Supplier<? extends BehaviorState<T>> target;
    private final boolean force;

    public TransitionRule(Condition<T> condition, Supplier<? extends BehaviorState<T>> target, boolean force) {
        this.condition = condition;
        this.target = target;
        this.force = force;
    }

    public boolean test(T servant, LivingEntity target, String currentStateId) {
        return condition.test(servant, target, currentStateId);
    }

    public BehaviorState<T> instantiate() {
        return target.get();
    }

    public boolean isForce() { return force; }
}
