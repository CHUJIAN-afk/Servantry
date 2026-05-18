package first.servantry.common.servant.goal.sharknado;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Sharknado;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 鲨鱼龙卷攻击Goal - 在玩家周围移动并发射鲨鱼射弹。
 */
public class SharknadoAttackGoal extends ServantGoal<Sharknado> {

    private Vec3 orbitTarget = Vec3.ZERO;

    public SharknadoAttackGoal(Sharknado servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        LivingEntity target = servant.getTarget();

        // 在玩家周围选择一个点
        if (orbitTarget.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.02 || orbitTarget.distanceToSqr(owner.position()) > 8 * 8) {
            double angle = owner.getRandom().nextDouble() * Math.PI * 2;
            double radius = 3 + owner.getRandom().nextDouble() * 2;
            double height = owner.getRandom().nextDouble() * 2 - 1;
            orbitTarget = owner.position().add(
                    Math.cos(angle) * radius,
                    height + owner.getBbHeight() / 2,
                    Math.sin(angle) * radius
            );
        }

        // 向目标点靠近（离玩家越远越快）
        Vec3 servantPos = servant.getPos();
        double distToOwner = servantPos.distanceTo(owner.position());
        double distToTarget = servantPos.distanceTo(orbitTarget);

        Vec3 toTarget = orbitTarget.subtract(servantPos);
        if (toTarget.lengthSqr() > 0.01) {
            // 离玩家越远，靠近速度越快
            double speedFactor = Math.min(0.05 + distToOwner * 0.02, 0.25);
            double force = Math.min(distToTarget * 0.03, speedFactor);
            servant.applyForce(toTarget.normalize().scale(force));
        }

        // 每10(+-1)tick发射射弹
        if (servant.getShootCooldown() <= 0 && target != null) {
            servant.shootAtTarget(target);
            servant.setShootCooldown(10 + owner.getRandom().nextIntBetweenInclusive(-1, 1));
        }
    }

}