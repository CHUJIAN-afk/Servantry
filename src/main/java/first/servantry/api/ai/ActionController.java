package first.servantry.api.ai;

import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;

public class ActionController<T extends Servant> {
    private final T servant;
    private final ServantAction<T> defaultAction;
    private ServantAction<T> currentAction;
    private boolean isChangingAction = false;

    public ActionController(T servant, ServantAction<T> defaultAction) {
        this.servant = servant;
        this.defaultAction = defaultAction;
        this.currentAction = defaultAction;
        this.currentAction.onStart(null);
    }

    public boolean trySetAction(ServantAction<T> newAction, LivingEntity target) {
        if (currentAction != null && !currentAction.canBeInterrupted()) return false;
        forceAction(newAction, target);
        return true;
    }

    public void forceAction(ServantAction<T> newAction, LivingEntity target) {
        if (isChangingAction) {
            throw new IllegalStateException("禁止在 onStop() 或 onStart() 中触发新状态！请在 tick() 中通过检测条件进行状态转换。");
        }
        isChangingAction = true;
        try {
            if (currentAction != null) currentAction.onStop();
            currentAction = newAction;
            currentAction.onStart(target);
        } finally {
            isChangingAction = false;
        }
    }

    // 【新增】：客户端专用同步方法。直接替换动作实例，跳过 onStart()，防止客户端运算路径导致抽搐
    public void setClientAction(ServantAction<T> action) {
        this.currentAction = action;
    }

    public void tick(LivingEntity target) {
        if (currentAction == null) return;
        if (currentAction.isFinished() && currentAction != defaultAction) {
            forceAction(defaultAction, target);
        }
        if (!isChangingAction) {
            currentAction.tick(target);
        }
    }

    public ServantAction<T> getCurrentAction() { return currentAction; }
}