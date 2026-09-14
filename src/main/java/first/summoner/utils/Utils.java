package first.summoner.utils;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.MomentumMinion;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Host-side replacements for helpers removed from the public Lyra API.
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Re-selects a hovering point around a target without using the deprecated
     * {@link MomentumMinion#getWanderPos(Vec3, Vec3, float, float)} method.
     */
    public static Vec3 getWanderPos(MomentumMinion minion, Vec3 lastWanderPos, Vec3 targetPos, float distance, float height) {
        if (lastWanderPos.equals(Vec3.ZERO) || minion.getRandom().nextDouble() < 0.025 || lastWanderPos.distanceToSqr(targetPos) > distance * distance) {
            Vec3 newPos = targetPos.add(targetPos.offsetRandom(minion.getRandom(), 1).subtract(targetPos).normalize().scale(distance));
            while (newPos.y() < targetPos.y() + height) {
                newPos = newPos.add(0, 1, 0);
            }
            return newPos;
        }
        return lastWanderPos;
    }

    /**
     * Mirrors the old four-tick MomentumMinion teleport, which Lyra no longer
     * exposes as a public method.
     */
    public static void teleportTo(MomentumMinion minion, Vec3 targetPos) {
        minion.setVelocity(Vec3.ZERO);
        Vec3 start = minion.getPos();
        float yaw = minion.getYaw();
        float pitch = minion.getPitch();
        float roll = minion.getRoll();
        List<PathNode> path = new ArrayList<>();
        int ticks = 4;
        for (int i = 1; i <= ticks; i++) {
            float delta = (float) i / ticks;
            path.add(new PathNode(start.lerp(targetPos, delta), yaw, pitch, roll));
        }
        minion.setPath(path);
    }

}
