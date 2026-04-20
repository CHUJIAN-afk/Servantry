package first.servantry.api.ai.fsm;

import first.servantry.api.ai.ServantAi;
import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.function.Supplier;

/**
 * 声明式状态机。通过 {@link Builder} 集中声明所有状态工厂与转换规则，
 * 彻底消除状态之间的硬编码互相引用。
 *
 * 使用要点：
 *   - 所有状态必须先用 state(id, factory) 注册工厂，供网络同步时反查。
 *   - from(id).on(cond).to(id) 声明本地转换；anyState().on(...) 声明全局转换。
 *   - tick() 先评估全局/局部转换，再执行当前状态的 onTick。
 *   - setClientState(id) 用于客户端幽灵同步，跳过 onEnter，避免客户端自行计算路径。
 */
public final class StateMachine<T extends Servant> implements ServantAi {

    private final T servant;
    private final Map<String, Supplier<? extends BehaviorState<T>>> factories;
    private final Map<String, List<TransitionRule<T>>> localTransitions;
    private final List<TransitionRule<T>> globalTransitions;
    private final String defaultStateId;

    private BehaviorState<T> current;
    private boolean isChanging = false;

    private StateMachine(T servant,
                         Map<String, Supplier<? extends BehaviorState<T>>> factories,
                         Map<String, List<TransitionRule<T>>> localTransitions,
                         List<TransitionRule<T>> globalTransitions,
                         String defaultStateId) {
        this.servant = servant;
        this.factories = factories;
        this.localTransitions = localTransitions;
        this.globalTransitions = globalTransitions;
        this.defaultStateId = defaultStateId;
        this.current = instantiate(defaultStateId);
        this.current.onEnter(servant, null);
    }

    public BehaviorState<T> getCurrent() { return current; }

    public String getCurrentId() { return current != null ? current.id() : defaultStateId; }

    public boolean hasState(String id) { return factories.containsKey(id); }

    public BehaviorState<T> instantiate(String id) {
        Supplier<? extends BehaviorState<T>> f = factories.get(id);
        if (f == null) f = factories.get(defaultStateId);
        return f.get();
    }

    /** 尝试切换（尊重 canBeInterrupted） */
    public boolean trySetState(BehaviorState<T> next, LivingEntity target) {
        if (current != null && !current.canBeInterrupted()) return false;
        forceState(next, target);
        return true;
    }

    /** 强制切换 */
    public void forceState(BehaviorState<T> next, LivingEntity target) {
        if (isChanging) {
            throw new IllegalStateException("禁止在 onEnter()/onExit() 中触发新状态！");
        }
        isChanging = true;
        try {
            if (current != null) current.onExit(servant);
            current = next;
            current.onEnter(servant, target);
        } finally {
            isChanging = false;
        }
    }

    /** 客户端同步：替换实例但不触发 onEnter/onExit */
    public void setClientState(String id) {
        if (current != null && current.id().equals(id)) return;
        this.current = instantiate(id);
    }

    /** 每刻推进：先求值转换规则，再执行状态 tick */
    public void tick(LivingEntity target) {
        if (current == null) return;

        // 全局转换优先
        if (evaluateTransitions(globalTransitions, target)) return;
        // 当前状态的局部转换
        List<TransitionRule<T>> locals = localTransitions.get(current.id());
        if (locals != null && evaluateTransitions(locals, target)) return;

        if (!isChanging) current.onTick(servant, target);
    }

    private boolean evaluateTransitions(List<TransitionRule<T>> rules, LivingEntity target) {
        for (TransitionRule<T> r : rules) {
            if (!r.isForce() && current != null && !current.canBeInterrupted()) continue;
            if (r.test(servant, target, current.id())) {
                BehaviorState<T> next = r.instantiate();
                if (r.isForce()) forceState(next, target);
                else trySetState(next, target);
                return true;
            }
        }
        return false;
    }

    // ================== Builder ==================

    public static <T extends Servant> Builder<T> builder(T servant) {
        return new Builder<>(servant);
    }

    public static final class Builder<T extends Servant> {
        private final T servant;
        private final Map<String, Supplier<? extends BehaviorState<T>>> factories = new HashMap<>();
        private final Map<String, List<TransitionRule<T>>> localTransitions = new HashMap<>();
        private final List<TransitionRule<T>> globalTransitions = new ArrayList<>();
        private String defaultStateId;

        private Builder(T servant) { this.servant = servant; }

        public Builder<T> state(String id, Supplier<? extends BehaviorState<T>> factory) {
            factories.put(id, factory);
            return this;
        }

        public Builder<T> initial(String id) {
            this.defaultStateId = id;
            return this;
        }

        public FromClause<T> from(String id) { return new FromClause<>(this, id); }

        public AnyClause<T> anyState() { return new AnyClause<>(this); }

        public StateMachine<T> build() {
            Objects.requireNonNull(defaultStateId, "initial state must be set");
            return new StateMachine<>(servant, factories, localTransitions, globalTransitions, defaultStateId);
        }

        void addLocal(String from, TransitionRule<T> rule) {
            localTransitions.computeIfAbsent(from, k -> new ArrayList<>()).add(rule);
        }

        void addGlobal(TransitionRule<T> rule) { globalTransitions.add(rule); }
    }

    public static final class FromClause<T extends Servant> {
        private final Builder<T> b;
        private final String from;
        FromClause(Builder<T> b, String from) { this.b = b; this.from = from; }
        public OnClause<T> on(TransitionRule.Condition<T> cond) { return new OnClause<>(b, from, cond, false); }
        public OnClause<T> onForce(TransitionRule.Condition<T> cond) { return new OnClause<>(b, from, cond, true); }
    }

    public static final class AnyClause<T extends Servant> {
        private final Builder<T> b;
        AnyClause(Builder<T> b) { this.b = b; }
        public OnClause<T> on(TransitionRule.Condition<T> cond) { return new OnClause<>(b, null, cond, false); }
        public OnClause<T> onForce(TransitionRule.Condition<T> cond) { return new OnClause<>(b, null, cond, true); }
    }

    public static final class OnClause<T extends Servant> {
        private final Builder<T> b;
        private final String from;
        private final TransitionRule.Condition<T> cond;
        private final boolean force;
        OnClause(Builder<T> b, String from, TransitionRule.Condition<T> cond, boolean force) {
            this.b = b; this.from = from; this.cond = cond; this.force = force;
        }
        public Builder<T> to(String targetId) {
            Supplier<? extends BehaviorState<T>> sup = () -> {
                Supplier<? extends BehaviorState<T>> f = b.factories.get(targetId);
                if (f == null) throw new IllegalStateException("Unknown state id: " + targetId);
                return f.get();
            };
            TransitionRule<T> rule = new TransitionRule<>(cond, sup, force);
            if (from == null) b.addGlobal(rule); else b.addLocal(from, rule);
            return b;
        }
    }
}
