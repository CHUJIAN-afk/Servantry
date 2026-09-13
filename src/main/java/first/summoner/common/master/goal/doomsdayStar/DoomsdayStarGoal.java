package first.summoner.common.master.goal.doomsdayStar;

import first.summoner.common.master.DoomsdayStar;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class DoomsdayStarGoal extends Goal {

    protected final DoomsdayStar.GoalState goalState;
    protected final DoomsdayStar doomsdayStar;

    public DoomsdayStarGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        this.goalState = goalState;
        this.doomsdayStar = doomsdayStar;
    }

    @Override
    public boolean canUse() {
        return doomsdayStar.isGoalEnabled(goalState);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
