package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.minion.MinionGoal;
import first.summoner.common.master.DoomsdayStar;

public abstract class DoomsdayStarGoal extends MinionGoal<DoomsdayStar> {

    protected final DoomsdayStar.GoalState goalState;
    protected final DoomsdayStar doomsdayStar;

    public DoomsdayStarGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(doomsdayStar);
        this.goalState = goalState;
        this.doomsdayStar = doomsdayStar;
    }

    @Override
    public boolean canUse() {
        return doomsdayStar.isGoalEnabled(goalState);
    }
}
