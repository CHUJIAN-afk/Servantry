package first.servantry.api.servant;

public abstract class ServantTask {
    // 任务优先级：数值越小优先级越高
    public abstract int getPriority();
    
    // 启动条件：每 tick 检查，如果符合且优先级最高，则接管 AI
    public abstract boolean canStart(Servant servant, ServantMemory memory);
    
    // 持续条件：如果返回 false，任务结束，AI 重新决策
    public abstract boolean shouldContinue(Servant servant, ServantMemory memory);

    public void start(Servant servant, ServantMemory memory) {}
    public void tick(Servant servant, ServantMemory memory) {}
    public void stop(Servant servant, ServantMemory memory) {}
}