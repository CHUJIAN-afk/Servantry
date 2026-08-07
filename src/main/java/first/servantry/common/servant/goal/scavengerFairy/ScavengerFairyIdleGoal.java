package first.servantry.common.servant.goal.scavengerFairy;

import first.lyra.common.servant.ServantGoal;
import first.servantry.common.servant.ScavengerFairy;

import java.util.List;

public class ScavengerFairyIdleGoal extends ServantGoal<ScavengerFairy> {

    public ScavengerFairyIdleGoal(ScavengerFairy servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        servant.setPath(List.of(servant.getCurrentPathNode().lerp(servant.getInterpolatedIdleState(1.0f), 0.15f)));
    }
}
