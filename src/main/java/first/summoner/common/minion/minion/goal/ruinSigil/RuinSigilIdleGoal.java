package first.summoner.common.minion.minion.goal.ruinSigil;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.RuinRelic;

/**
 * 禁戒遗迹空闲Goal - 在玩家背后竖直圆环上环绕。
 */
public class RuinSigilIdleGoal extends AttachmentEntityGoal<RuinRelic> {

    public RuinSigilIdleGoal(RuinRelic minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() == null;
    }

    @Override
    public void tick() {
        PathNode currentPathNode = minion.getCurrentPathNode();
        PathNode targetPathNode = minion.getIdleRingNode(1.0f);
        minion.setCurrentPathNode(currentPathNode.lerp(targetPathNode, 0.3f));
    }
}
