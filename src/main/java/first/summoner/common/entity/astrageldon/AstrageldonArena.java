package first.summoner.common.entity.astrageldon;

import first.summoner.common.entity.Astrageldon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AstrageldonArena {

    public static final double ENTRY_RANGE = 256;
    public static final double SOFT_EDGE = 4;

    private AstrageldonArena() {
    }

    public static @Nullable Astrageldon nearest(Iterable<? extends Entity> entities, Vec3 position, double range, float partialTick) {
        Astrageldon nearest = null;
        double nearestEdge = Double.MAX_VALUE;
        double nearestCenter = Double.MAX_VALUE;
        for (Entity entity : entities) {
            if (entity instanceof Astrageldon boss && boss.hasArena()) {
                double distance = position.distanceTo(boss.getRenderArenaCenter(partialTick));
                double edge = Math.max(0, distance - boss.getRenderArenaRadius(partialTick));
                if (edge <= range && (edge < nearestEdge || edge == nearestEdge && distance < nearestCenter)) {
                    nearest = boss;
                    nearestEdge = edge;
                    nearestCenter = distance;
                }
            }
        }
        return nearest;
    }

    public static double playerRadius(Player player, double radius, Vec3 offset) {
        Vec3 normal = offset.normalize();
        double width = player.getBbWidth() / 2;
        double height = player.getBbHeight() / 2;
        double support = (Math.abs(normal.x) + Math.abs(normal.z)) * width + Math.abs(normal.y) * height;
        double diagonal = width * width * 2 + height * height;
        return Math.max(0, Math.sqrt(Math.max(0, radius * radius - diagonal + support * support)) - support - 0.05);
    }

    public static double edgeWeight(double distance, double radius) {
        double value = Mth.clamp((distance - radius + SOFT_EDGE) / SOFT_EDGE, 0, 1);
        return value * value * (3 - 2 * value);
    }

    public static Vec3 constrainMovement(Player player, Vec3 center, double sphereRadius, Vec3 movement, Vec3 carry) {
        Vec3 offset = player.getBoundingBox().getCenter().subtract(center);
        double radius = playerRadius(player, sphereRadius, offset);
        double weight = edgeWeight(offset.length(), radius);
        Vec3 normal = offset.normalize();
        double damping = weight * weight;
        Vec3 slide = movement.subtract(normal.scale(Math.max(movement.dot(normal), 0) * damping));
        Vec3 next = offset.add(slide).add(carry.scale(weight));
        double nextRadius = playerRadius(player, sphereRadius, next);
        if (next.lengthSqr() > nextRadius * nextRadius) {
            next = next.normalize().scale(nextRadius);
        }
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.subtract(normal.scale(Math.max(motion.dot(normal), 0) * damping)));
        player.resetFallDistance();
        return next.subtract(offset);
    }

    public static Vec3 entryPosition(Player player, Astrageldon boss) {
        Vec3 offset = player.getBoundingBox().getCenter().subtract(boss.getArenaCenter());
        double radius = playerRadius(player, boss.getArenaBoundaryRadius(), offset) - 0.25;
        return boss.getArenaCenter().add(offset.normalize().scale(radius)).add(0, -player.getBbHeight() / 2, 0);
    }
}
