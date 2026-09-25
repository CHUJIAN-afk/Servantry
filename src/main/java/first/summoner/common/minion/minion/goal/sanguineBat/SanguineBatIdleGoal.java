package first.summoner.common.minion.minion.goal.sanguineBat;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.SanguineBat;

public class SanguineBatIdleGoal extends AttachmentEntityGoal<SanguineBat> {

    private final float speed = 0.4F + (float) Math.random() * 0.2F;
    private int prepTick;

    public SanguineBatIdleGoal(SanguineBat minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() == null;
    }

    @Override
    public void start() {
        prepTick = 5 + minion.getRandom().nextIntBetweenInclusive(0, 5);
    }

    @Override
    public void tick() {
        minion.setCurrentPathNode(minion.getCurrentPathNode().lerp(minion.getInterpolatedIdleState(1.0F), speed));
        if (--prepTick < 0) {
            minion.prep = true;
        }
    }
}
