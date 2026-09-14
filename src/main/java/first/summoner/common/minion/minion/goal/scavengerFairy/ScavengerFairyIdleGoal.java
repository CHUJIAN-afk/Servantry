package first.summoner.common.minion.minion.goal.scavengerFairy;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.ScavengerFairy;

import java.util.List;

public class ScavengerFairyIdleGoal extends AttachmentEntityGoal<ScavengerFairy> {

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
