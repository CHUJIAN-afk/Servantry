package first.summoner.common.minion.goal.enchantedThrowingKnives;

import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.EnchantedThrowingKnives;

/**
 * 附魔飞刀空闲目标。
 * <p>
 * 空闲状态下，飞刀围绕玩家旋转悬浮。
 * 当发现目标时，切换到攻击状态。
 * </p>
 */
public class EnchantedThrowingKnivesIdleGoal extends MinionGoal<EnchantedThrowingKnives> {

    public EnchantedThrowingKnivesIdleGoal(EnchantedThrowingKnives minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        // 当没有目标或不在攻击状态时进入空闲
        return minion.getTarget() == null || !minion.attacking;
    }

    @Override
    public boolean canContinueToUse() {
        return minion.getTarget() == null;
    }

    @Override
    public void start() {
        minion.attacking = false;
    }

    @Override
    public void tick() {
        PathNode currentPathNode = minion.getCurrentPathNode();
        PathNode targetPathNode = minion.getInterpolatedIdleState(1.0f);
        PathNode lerpedPathNode = currentPathNode.lerp(targetPathNode, 0.25f);
        minion.setCurrentPathNode(lerpedPathNode);
    }
}
