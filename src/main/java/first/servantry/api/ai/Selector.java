package first.servantry.api.ai;

import first.servantry.api.servant.Servant;
import java.util.List;

public class Selector<T extends Servant> extends BehaviorNode<T> {
    private final List<BehaviorNode<T>> children;

    public Selector(List<BehaviorNode<T>> children) {
        this.children = children;
    }

    @Override
    public NodeStatus tick(T servant) {
        for (BehaviorNode<T> child : children) {
            NodeStatus status = child.tick(servant);
            if (status != NodeStatus.FAILURE) {
                return status; // 如果是 SUCCESS 或 RUNNING，直接返回
            }
        }
        return NodeStatus.FAILURE; // 全部失败
    }

}