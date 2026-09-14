package first.summoner.common.master.goal.doomsdayStar;

import first.summoner.common.master.DoomsdayStar;
import net.minecraft.world.phys.Vec3;

public class DoomsdayStarSummonGoal extends DoomsdayStarGoal {

    private boolean landed;
    private int jumpDelay;

    public DoomsdayStarSummonGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(goalState, doomsdayStar);
    }

    @Override
    public void start() {
        landed = false;
        jumpDelay = 10;
        doomsdayStar.setFallLaserIndicatorRender(true);
    }

    @Override
    public void tick() {
        if (!landed) {
            doomsdayStar.addVelocity(new Vec3(0, -0.1, 0));
            if (doomsdayStar.onGround()) {
                landed = true;
                doomsdayStar.setFallLaserIndicatorRender(false);
            }
        } else if (--jumpDelay <= 0) {
            doomsdayStar.setGoalState(DoomsdayStar.GoalState.JUMP);
        }
    }

    @Override
    public void stop() {
        doomsdayStar.setFallLaserIndicatorRender(false);
    }
}
