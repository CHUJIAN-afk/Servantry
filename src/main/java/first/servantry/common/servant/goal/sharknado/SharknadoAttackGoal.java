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

    private Vec3 wanderTarget = Vec3.ZERO;

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
        Player owner = servant.getOwner();

        // 计算环绕位置
        if (wanderTarget.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025) {
            wanderTarget = target.getBoundingBox().getCenter().offsetRandom(target.getRandom(), (float) target.getBoundingBox().getSize() * 6);
            wanderTarget.add(0, target.getBoundingBox().getSize() * 12, 0);
            double height = target.position().y() + target.getBoundingBox().getYsize() / 2;
            while (wanderTarget.y() < height) {
                wanderTarget = wanderTarget.add(0, 1, 0);
            }
        }
        Vec3 toAnchor = wanderTarget.subtract(servant.getPos());
        double dist = toAnchor.length();

        if (dist > 0.05) {
            double force = Math.min(dist * 0.08, 0.4);
            servant.applyForce(toAnchor.normalize().scale(force));
        }

        // 每10(+-1)tick发射射弹
        if (servant.getShootCooldown() <= 0) {
            servant.shootAtTarget(target);
            servant.setShootCooldown(10 + owner.getRandom().nextIntBetweenInclusive(-1, 1));
        }
    }

}