package first.summoner.common.minion.minion.goal.sanguineBat;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.SanguineBat;

public class SanguineBatAttackGoal extends AttachmentEntityGoal<SanguineBat> {

    public SanguineBatAttackGoal(SanguineBat minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.prep && minion.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        float progress = minion.getProgress(0);
        return minion.isAttacking() && progress < 1;
    }

    @Override
    public void start() {
        minion.prep = false;
        minion.attackStart = minion.getLevel().getGameTime();
        minion.attackPoint = minion.getTarget().getBoundingBox().getCenter();
        minion.hitTargets.clear();
    }

    @Override
    public void stop() {
        minion.attackStart = -1;
        minion.prep = false;
        minion.hitTargets.clear();
    }
}
