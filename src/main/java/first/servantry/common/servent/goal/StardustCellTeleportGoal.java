package first.servantry.common.servent.goal;

import first.servantry.api.ai.ServantGoal;
import first.servantry.api.servant.PathNode;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servent.StardustCell;
import first.servantry.register.AttachmentRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * 星尘细胞瞬移Goal。
 * <p>
 * 当目标距离过远或仆从距离玩家过远时触发瞬移。
 * 瞬移后发射星细胞射弹而非直接造成伤害。
 * </p>
 */
public class StardustCellTeleportGoal extends ServantGoal<StardustCell> {

    private static final int TELEPORT_DURATION = 6;
    private static final double MIN_TELEPORT_DISTANCE = 28.75;
    private static final double MAX_TELEPORT_DISTANCE = 53.75;
    private static final double PLAYER_TELEPORT_DISTANCE = 128.0;

    public StardustCellTeleportGoal(StardustCell servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        if (servant.getTeleportTimer() > 0) return true;

        Player owner = servant.getOwner();
        LivingEntity target = servant.getTarget();

        // 检查是否需要瞬移回玩家
        double distToOwnerSqr = servant.getPos().distanceToSqr(owner.position());
        if (distToOwnerSqr > PLAYER_TELEPORT_DISTANCE * PLAYER_TELEPORT_DISTANCE) {
            initTeleportToOwner(owner);
            return true;
        }

        // 检查是否需要瞬移到目标
        if (target == null) return false;

        double distSqr = target.distanceToSqr(servant.getPos());
        boolean shouldTeleport = (distSqr >= MIN_TELEPORT_DISTANCE * MIN_TELEPORT_DISTANCE && distSqr <= MAX_TELEPORT_DISTANCE * MAX_TELEPORT_DISTANCE)
                || distSqr > PLAYER_TELEPORT_DISTANCE * PLAYER_TELEPORT_DISTANCE;

        if (!shouldTeleport) return false;

        initTeleportToTarget(owner, target);
        return true;
    }

    private void initTeleportToOwner(Player owner) {
        servant.setTeleportTimer(TELEPORT_DURATION);
        servant.setTeleportStart(servant.getPos());
        servant.setTeleportTarget(owner.position().add(0, 2, 0));
        servant.setTrailTimer(15);
    }

    private void initTeleportToTarget(Player owner, LivingEntity target) {
        servant.setTeleportTimer(TELEPORT_DURATION);
        servant.setTeleportStart(servant.getPos());
        servant.setTeleportTarget(target.getBoundingBox().getCenter());
        servant.setTrailTimer(15);
        servant.setTarget(target);
    }

    @Override
    public boolean canContinueToUse() {
        return servant.getTeleportTimer() > 0;
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();

        servant.setTeleportTimer(servant.getTeleportTimer() - 1);
        float progress = 1.0f - (float) servant.getTeleportTimer() / TELEPORT_DURATION;
        float ease = 1.0f - (float) Math.pow(1.0f - progress, 3);
        Vec3 currentPos = servant.getTeleportStart().lerp(servant.getTeleportTarget(), ease);
        servant.setPath(Collections.singletonList(new PathNode(currentPos, servant.getYaw(), servant.getPitch(), servant.getRoll())));

        if (servant.getTeleportTimer() == 0) {
            LivingEntity target = servant.getTarget();
            if (target != null && target.isAlive()) {
                // 发射星细胞射弹而非直接造成伤害
                StardustProjectile projectile = new StardustProjectile(
                        owner.getUUID(),
                        servant.getUuid(),
                        servant.getPos(),
                        target
                );
                owner.getData(AttachmentRegister.ProjectileData).add(projectile);
            }
            servant.setTeleportTimer(0);
        }
    }

    @Override
    public void stop() {
        servant.setTeleportTimer(0);
    }
}