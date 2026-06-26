package first.servantry.common.servant.goal.stardustDragon;

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
        return servant.isHead() && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        servant.orbitToward(targetPos, 125f, 0.1 + Math.min(servant.getPos().distanceTo(targetPos) * 0.01, 0.05));
    }
}