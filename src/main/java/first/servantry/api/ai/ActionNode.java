package first.servantry.api.ai;

import first.servantry.api.servant.Servant;

public abstract class ActionNode<T extends Servant> extends BehaviorNode<T> {

    // 【强类型约束】返回一个枚举或注册表 ID，用于客户端动画同步
    public abstract int getSyncActionId();

    @Override
    public abstract NodeStatus tick(T servant);

    // 当动作刚开始或被强制打断时调用
    public void start(T servant) {
    }

    public void stop(T servant) {
    }

}