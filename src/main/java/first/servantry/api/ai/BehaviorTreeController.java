package first.servantry.api.ai;

import first.servantry.api.servant.Servant;

public class BehaviorTreeController<T extends Servant> {
    private final BehaviorNode<T> rootNode;
    private int currentClientActionId = 0; // 0 代表 IDLE
    
    public BehaviorTreeController(BehaviorNode<T> rootNode) {
        this.rootNode = rootNode;
    }

    public void tick(T servant) {
        // 服务端运算整棵树
        rootNode.tick(servant);
        // 注：在实际 ActionNode 的 tick 中，你可以更新 currentClientActionId
    }

    public int getCurrentClientActionId() {
        return currentClientActionId;
    }
    
    public void setClientActionId(int id) {
        this.currentClientActionId = id;
    }
}