package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Terraprism;

import java.util.Collections;

public class TerraprismIdleGoal extends ServantGoal<Terraprism> {

    public TerraprismIdleGoal(Terraprism servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void start() {
        servant.idle = true;
        servant.attacking = false;
    }

    @Override
    public void stop() {
        servant.idle = false;
    }

    @Override
    public void tick() {
        servant.setPath(Collections.singletonList(servant.getCurrentPathNode().lerp(servant.getInterpolatedIdleState(1f), 0.35f)));
    }

}
