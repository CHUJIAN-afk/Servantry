package first.servantry.common.servant.goal.terraprism;

import first.lyra.common.servant.ServantGoal;
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
    public void start() {
        servant.attacking = false;
    }

    @Override
    public void tick() {
        servant.setPath(Collections.singletonList(servant.getCurrentPathNode().lerp(servant.getInterpolatedIdleState(1), 0.35f)));
    }
}
