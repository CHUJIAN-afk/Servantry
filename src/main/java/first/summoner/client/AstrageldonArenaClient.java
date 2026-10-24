package first.summoner.client;

import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import first.summoner.common.entity.Astrageldon;
import first.summoner.common.entity.astrageldon.AstrageldonArena;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class AstrageldonArenaClient {

    private static UUID arena;
    private static Vec3 lastCenter = Vec3.ZERO;

    private AstrageldonArenaClient() {
    }

    public static void beginTick() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != null && !minecraft.isPaused()) {
            // 在玩家移动前固定本 tick 的球面，避免实体更新顺序导致边界与玩家错开一帧。
            for (var entity : minecraft.level.entitiesForRendering()) {
                if (entity instanceof Astrageldon boss) {
                    boss.captureArenaFrame();
                }
            }
        }
    }

    public static @Nullable Astrageldon find(Player player, float partialTick) {
        Astrageldon result = null;
        if (player.isAlive() && !player.isSpectator()) {
            Vec3 position = player.getPosition(partialTick).add(0, player.getBbHeight() / 2, 0);
            Astrageldon nearest = AstrageldonArena.nearest(Minecraft.getInstance().level.entitiesForRendering(), position, Double.MAX_VALUE, partialTick);
            if (nearest != null && position.distanceTo(nearest.getRenderArenaCenter(partialTick))
                    <= nearest.getRenderArenaRadius(partialTick) + nearest.getArenaSyncMargin()) {
                result = nearest;
            }
        }
        return result;
    }

    public static Vec3 constrainMovement(LocalPlayer player, Vec3 movement) {
        Astrageldon boss = player.isPassenger() ? null : find(player, 1);
        Vec3 result = movement;
        if (boss != null) {
            Vec3 center = boss.getRenderArenaCenter(1);
            Vec3 carry = boss.getUUID().equals(arena) ? center.subtract(lastCenter) : Vec3.ZERO;
            arena = boss.getUUID();
            lastCenter = center;
            result = AstrageldonArena.constrainMovement(player, center, boss.getRenderArenaRadius(1), movement, carry);
        } else {
            clear();
        }
        return result;
    }

    public static float cameraZoom(Camera camera, float zoom) {
        float result = zoom;
        if (!IrisCompat.isShadowPass() && camera.getEntity() instanceof Player player) {
            Astrageldon boss = find(player, camera.getPartialTickTime());
            if (boss != null) {
                float partialTick = camera.getPartialTickTime();
                Vec3 offset = camera.getPosition().subtract(boss.getRenderArenaCenter(partialTick));
                Vec3 direction = new Vec3(camera.getLookVector()).scale(-1);
                double radius = cameraRadius(camera, boss);
                double b = offset.dot(direction);
                double h = b * b - offset.lengthSqr() + radius * radius;
                double allowed = h > 0 ? Math.max(0, -b + Math.sqrt(h)) : 0;
                double blend = Math.max(0, 0.75 - Math.abs(zoom - allowed));
                result = (float) Math.max(0, Math.min(zoom, allowed) - blend * blend / 3);
            }
        }
        return result;
    }

    public static Vec3 cameraPosition(Camera camera) {
        Vec3 result = camera.getPosition();
        if (!IrisCompat.isShadowPass() && camera.getEntity() instanceof Player player) {
            Astrageldon boss = find(player, camera.getPartialTickTime());
            if (boss != null) {
                Vec3 center = boss.getRenderArenaCenter(camera.getPartialTickTime());
                Vec3 offset = result.subtract(center);
                double radius = cameraRadius(camera, boss);
                if (offset.lengthSqr() > radius * radius) {
                    result = center.add(offset.normalize().scale(radius));
                }
            }
        }
        return result;
    }

    private static double cameraRadius(Camera camera, Astrageldon boss) {
        return Math.max(0, boss.getRenderArenaRadius(camera.getPartialTickTime()) - camera.getNearPlane().getTopLeft().length() - 0.05);
    }

    public static void clear() {
        arena = null;
        lastCenter = Vec3.ZERO;
    }
}
