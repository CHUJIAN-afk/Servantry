package first.servantry.common.servant.goal.deadlysphere;

import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.DeadlySphere;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class DeadlySphereIdleGoal extends ServantGoal<DeadlySphere> {

    private Vec3 wanderOffset = Vec3.ZERO;

    public DeadlySphereIdleGoal(DeadlySphere deadlySphere) {
        super(deadlySphere);
    }

    @Override
    public boolean canUse() {
        return servant.getTarget() == null;
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        if (wanderOffset.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || wanderOffset.distanceToSqr(servant.getPos()) < 1 || wanderOffset.distanceToSqr(owner.position()) > 8 * 8) {
            wanderOffset = owner.getBoundingBox().getCenter().offsetRandom(owner.getRandom(), (float) owner.getBoundingBox().getSize() * 6);
            while (wanderOffset.y() < owner.position().y()) {
                wanderOffset = wanderOffset.add(0, 1, 0);
            }
        }
        if (servant.getPos().distanceToSqr(owner.position()) > 32 * 32) {
            servant.teleportTo(wanderOffset);
        }
        Vec3 dir = wanderOffset.subtract(servant.getPos());
        double dist = dir.length();
        if (dist > 0.05) {
            dir = dir.normalize();
            double force = Math.min(dist * 0.02, 0.2);
            servant.applyForce(dir.scale(force));
        }
        ArrayList<PathNode> historyNodes = servant.getHistoryNodes();
        if (historyNodes.size() > 1) {
            PathNode pre = historyNodes.getFirst();
            PathNode current = servant.getCurrentPathNode();
            Vec3 subtract = current.pos().subtract(pre.pos());
            if (subtract.length() > 0.001) {
                servant.lookAtDirection(subtract.normalize());
            }
        }
    }
}
