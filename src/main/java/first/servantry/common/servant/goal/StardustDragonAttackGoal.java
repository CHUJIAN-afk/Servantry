package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙攻击目标。
 */
public class StardustDragonAttackGoal extends ServantGoal<StardustDragon> {

    public StardustDragonAttackGoal(StardustDragon servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isHead() && servant.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return servant.isHead() && servant.getTarget() != null && servant.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (target == null || !target.isAlive()) return;

        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        servant.spiralToward(targetPos, 0.25);
    }
}