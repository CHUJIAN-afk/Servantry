package first.summoner.common.minion.goal.sharknado;

import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.Sharknado;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 鲨鱼龙卷攻击Goal - 在玩家周围移动并发射鲨鱼射弹。
 */
public class SharknadoAttackGoal extends MinionGoal<Sharknado> {

    public SharknadoAttackGoal(Sharknado minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 startPos = minion.getPos();
        Vec3 targetPos = minion.calculateBezierPoint(0.4f, startPos, startPos.add(minion.getVelocity()), target.getBoundingBox().getCenter());
        minion.applyForce(targetPos.subtract(startPos).normalize().scale(0.075));
        if (minion.getShootCooldown() <= 0) {
            minion.shootAtTarget(target);
            minion.setShootCooldown(10 + minion.getOwner().getRandom().nextIntBetweenInclusive(-1, 1));
        }
    }
}