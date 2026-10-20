package first.summoner.common.minion.minion.goal.sanguineBat;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.SanguineBat;

public class SanguineBatIdleGoal extends AttachmentEntityGoal<SanguineBat> {

    private int prepTick;

    public SanguineBatIdleGoal(SanguineBat minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return !minion.isAttacking();
    }

    @Override
    public void start() {
        prepTick = 5 + minion.getRandom().nextIntBetweenInclusive(0, 5);
    }

    @Override
    public void tick() {
        if (--prepTick <= 0) {
            minion.prep = true;
        }
    }
}
