package first.summoner.common.minion.minion.goal.bloodBat;

import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.minion.BloodBat;

import java.util.Random;

/**
 * 血蝙蝠空闲Goal - 位置保持在待机位置（环绕玩家）。
 */
public class BloodBatIdleGoal extends MinionGoal<BloodBat> {

    private final float speed;
    private int prepTick = 0;

    public BloodBatIdleGoal(BloodBat minion) {
        super(minion);
        this.speed = new Random().nextFloat(0.4f, 0.6f);
    }

    @Override
    public boolean canUse() {
        return !minion.isTarget(minion.getTarget());
    }

    @Override
    public void start() {
        prepTick = 5 + minion.getRandom().nextIntBetweenInclusive(0, 5);
    }

    @Override
    public void tick() {
        PathNode currentPathNode = minion.getCurrentPathNode();
        PathNode targetPathNode = minion.getInterpolatedIdleState(1.0f);
        minion.setCurrentPathNode(currentPathNode.lerp(targetPathNode, speed));
        if (--prepTick < 0) {
            minion.prep = true;
        }
    }
}
