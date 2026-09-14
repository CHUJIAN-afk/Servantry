package first.summoner.common.minion.minion.goal.terraprism;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.Terraprism;

import java.util.Collections;

public class TerraprismIdleGoal extends AttachmentEntityGoal<Terraprism> {

    public TerraprismIdleGoal(Terraprism minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {
        minion.attacking = false;
    }

    @Override
    public void tick() {
        minion.setPath(Collections.singletonList(minion.getCurrentPathNode().lerp(minion.getInterpolatedIdleState(1), 0.35f)));
    }
}
