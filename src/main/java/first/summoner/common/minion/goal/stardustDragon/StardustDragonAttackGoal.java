package first.summoner.common.minion.goal.stardustDragon;

import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.StardustDragon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙攻击目标。
 */
public class StardustDragonAttackGoal extends MinionGoal<StardustDragon> {

    public StardustDragonAttackGoal(StardustDragon minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isHead() && minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        minion.orbitToward(targetPos, 125f, 0.1 + Math.min(minion.getPos().distanceTo(targetPos) * 0.01, 0.05));
    }
}