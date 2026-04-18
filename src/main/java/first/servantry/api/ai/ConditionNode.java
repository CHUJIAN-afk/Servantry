package first.servantry.api.ai;

import first.servantry.api.servant.Servant;

import java.util.function.Predicate;

public class ConditionNode<T extends Servant> extends BehaviorNode<T> {

    private final Predicate<T> condition;

    public ConditionNode(Predicate<T> condition) {
        this.condition = condition;
    }

    @Override
    public NodeStatus tick(T servant) {
        return condition.test(servant) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }

}