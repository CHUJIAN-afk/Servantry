package first.servantry.common.servant.goal.eyeofeternalnight;

import first.servantry.api.PathNode;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.EyeOfEternalNight;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;

/**
 * 永夜之眼环绕Goal。
 * <p>
 * 始终环绕玩家旋转，有目标时通过射线对路径上所有敌人造成伤害。
 * 射线检测严格要求射线经过目标碰撞箱。
 * </p>
 */
public class EyeOfEternalNightAttackGoal extends ServantGoal<EyeOfEternalNight> {

    public EyeOfEternalNightAttackGoal(EyeOfEternalNight servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        PathNode idleState = servant.getInterpolatedIdleState(1f);
        servant.setPath(Collections.singletonList(servant.getCurrentPathNode().lerp(idleState, 0.2f)));
        LivingEntity target = servant.getTarget();
        if (servant.getShootCooldown() <= 0 && servant.isTarget(target)) {
            servant.shootTarget(target);
        }
    }
}
