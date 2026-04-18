package first.servantry.api.ai;

import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;

public abstract class ServantAction<T extends Servant> {
    protected final T servant;

    public ServantAction(T servant) {
        this.servant = servant;
    }

    // 【新增】：强制子类提供标识，用于网络同步
    public abstract String getId();

    public void onStart(LivingEntity target) {}
    public void tick(LivingEntity target) {}
    public void onStop() {}

    public boolean canBeInterrupted() { return true; }
    public boolean isAttack() { return false; }
    public boolean isFinished() { return !servant.isExecutingPath(); }
}