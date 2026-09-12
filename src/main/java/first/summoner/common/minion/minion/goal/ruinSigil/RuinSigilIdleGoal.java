package first.summoner.common.minion.minion.goal.ruinSigil;

import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.minion.RuinRelic;

/**
 * 禁戒遗迹空闲Goal - 在玩家背后竖直圆环上环绕。
 */
public class RuinSigilIdleGoal extends MinionGoal<RuinRelic> {

    public RuinSigilIdleGoal(RuinRelic minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return !minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        PathNode currentPathNode = minion.getCurrentPathNode();
        PathNode targetPathNode = minion.getIdleRingNode(1.0f);
        minion.setCurrentPathNode(currentPathNode.lerp(targetPathNode, 0.3f));
    }
}
