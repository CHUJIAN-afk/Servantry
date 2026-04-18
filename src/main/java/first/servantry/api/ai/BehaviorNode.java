package first.servantry.api.ai;

import first.servantry.api.servant.Servant;

public abstract class BehaviorNode<T extends Servant> {
    
    // 每 tick 调用，返回当前节点的状态
    public abstract NodeStatus tick(T servant);

    // 可选：重置节点状态（用于被中断或重新执行时）
    public void reset() {}
}