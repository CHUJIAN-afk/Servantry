package first.servantry.common.servant.goal.sharknado;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Sharknado;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 鲨鱼龙卷攻击Goal - 在玩家周围移动并发射鲨鱼射弹。
 */
public class SharknadoAttackGoal extends ServantGoal<Sharknado> {

    public SharknadoAttackGoal(Sharknado servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 startPos = servant.getPos();
        Vec3 targetPos = servant.calculateBezierPoint(0.4f, startPos, startPos.add(servant.getVelocity()), target.getBoundingBox().getCenter());
        servant.applyForce(targetPos.subtract(startPos).normalize().scale(0.075));
        if (servant.getShootCooldown() <= 0) {
            servant.shootAtTarget(target);
            servant.setShootCooldown(10 + servant.getOwner().getRandom().nextIntBetweenInclusive(-1, 1));
        }
    }
}