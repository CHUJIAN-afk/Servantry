package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Twins;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TwinsIdleGoal extends ServantGoal<Twins> {

    private Vec3 wanderOffset = Vec3.ZERO;

    public TwinsIdleGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return servant.getTarget() == null;
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        if (wanderOffset.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || wanderOffset.distanceToSqr(servant.getPos()) < 1) {
            wanderOffset = owner.getBoundingBox().getCenter().offsetRandom(owner.getRandom(), (float) owner.getBoundingBox().getSize() * 4);
        }
        Vec3 dir = wanderOffset.subtract(servant.getPos());
        double dist = dir.length();
        if (dist > 0.05) {
            dir = dir.normalize();
            double force = Math.min(dist * 0.01, 0.1);
            servant.applyForce(dir.scale(force));
        }
        wanderOffset = owner.getBoundingBox().getCenter();
        // 缓慢朝向运动方向
        if (servant.getVelocity().lengthSqr() > 0.01) {
            Vec3 vel = servant.getVelocity().normalize();
            servant.setDesiredRotation((float) Math.toDegrees(Math.atan2(-vel.x, vel.z)), servant.getDesiredPitch(), servant.getDesiredRoll());
        }
    }

}
