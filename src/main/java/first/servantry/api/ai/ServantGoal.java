package first.servantry.api.ai;

public abstract class ServantGoal {

    public abstract boolean canUse();

    public boolean canContinueToUse() {
        return canUse();
    }

    public boolean isInterruptable() {
        return true;
    }

    public void start() {}

    public void tick() {}

    public void stop() {}

}
