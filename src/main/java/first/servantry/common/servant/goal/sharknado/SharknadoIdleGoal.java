package first.servantry.common.servant.goal.sharknado;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Sharknado;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 鲨鱼龙卷空闲Goal - 向玩家另一侧靠近。
 */
public class SharknadoIdleGoal extends ServantGoal<Sharknado> {

    public SharknadoIdleGoal(Sharknado servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return !servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        Vec3 ownerCenter = owner.getBoundingBox().getCenter();
        Vec3 servantPos = servant.getPos();

        // 计算玩家到仆从的方向，延长线上的点
        Vec3 toServant = servantPos.subtract(ownerCenter);
        if (toServant.lengthSqr() < 1e-5) {
            toServant = new Vec3(1, 0, 0);
        }

        // 目标位置：玩家另一侧，距离2格
        Vec3 targetPos = ownerCenter.add(toServant.normalize().scale(2));

        // 向目标靠近
        Vec3 toTarget = targetPos.subtract(servantPos);
        double dist = toTarget.length();
        if (dist > 0.1) {
            double force = Math.min(dist * 0.05, 0.15);
            servant.applyForce(toTarget.normalize().scale(force));
        }

        // 距离过远时瞬移
        if (servantPos.distanceToSqr(ownerCenter) > 48 * 48) {
            servant.teleportTo(ownerCenter);
        }
    }

}