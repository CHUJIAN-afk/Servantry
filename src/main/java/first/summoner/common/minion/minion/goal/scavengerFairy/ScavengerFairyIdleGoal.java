package first.summoner.common.minion.minion.goal.scavengerFairy;

import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.minion.ScavengerFairy;

import java.util.List;

public class ScavengerFairyIdleGoal extends MinionGoal<ScavengerFairy> {

    public ScavengerFairyIdleGoal(ScavengerFairy minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        minion.setPath(List.of(minion.getCurrentPathNode().lerp(minion.getInterpolatedIdleState(1.0f), 0.15f)));
    }
}
