package first.servantry.common.servant.goal.enchantedThrowingKnives;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.EnchantedThrowingKnives;

import java.util.Collections;

/**
 * 附魔飞刀空闲目标。
 * <p>
 * 空闲状态下，飞刀围绕玩家旋转悬浮。
 * 当发现目标时，切换到攻击状态。
 * </p>
 */
public class EnchantedThrowingKnivesIdleGoal extends ServantGoal<EnchantedThrowingKnives> {

    public EnchantedThrowingKnivesIdleGoal(EnchantedThrowingKnives servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        // 当没有目标或不在攻击状态时进入空闲
        return servant.getTarget() == null || !servant.attacking;
    }

    @Override
    public boolean canContinueToUse() {
        return servant.getTarget() == null;
    }

    @Override
    public void start() {
        servant.attacking = false;
    }

    @Override
    public void tick() {
        servant.setPath(Collections.singletonList(servant.getCurrentPathNode().lerp(servant.getInterpolatedIdleState(1.0f), 0.25f)));
    }

}
