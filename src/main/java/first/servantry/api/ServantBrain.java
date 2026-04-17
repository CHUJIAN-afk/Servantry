package first.servantry.api;

import first.servantry.api.servant.Servant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServantBrain {
    private final Servant owner;
    private final ServantMemory memory = new ServantMemory();
    private final List<ServantTask> tasks = new ArrayList<>();
    private ServantTask activeTask = null;

    public ServantBrain(Servant owner) {
        this.owner = owner;
    }

    public void addTask(ServantTask task) {
        tasks.add(task);
        tasks.sort(Comparator.comparingInt(ServantTask::getPriority));
    }

    public void tick() {
        // 1. 决策：寻找当前最高优先级的可选任务
        for (ServantTask task : tasks) {
            if (task.canStart(owner, memory)) {
                if (activeTask != task) {
                    if (activeTask != null) activeTask.stop(owner, memory);
                    activeTask = task;
                    activeTask.start(owner, memory);
                }
                break;
            }
        }

        // 2. 执行
        if (activeTask != null) {
            if (activeTask.shouldContinue(owner, memory)) {
                activeTask.tick(owner, memory);
            } else {
                activeTask.stop(owner, memory);
                activeTask = null;
            }
        }
    }
    
    public ServantMemory getMemory() { return memory; }
}