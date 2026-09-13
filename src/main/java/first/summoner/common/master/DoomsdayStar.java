package first.summoner.common.master;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DoomsdayStar extends AbstractMaster {

    private static final double SEARCH_RANGE = 64.0D;
    private static final double HORIZONTAL_JUMP_SPEED = 0.25D;
    private static final double JUMP_VELOCITY = 0.42D;
    private static final float CUSTOM_GRAVITY = -0.08F;
    private static final float CUSTOM_DRAG = 0.98F;

    private int jumpCooldown;

    public DoomsdayStar(EntityType<? extends DoomsdayStar> entityType, Level level) {
        super(entityType, level);
        setGravity(CUSTOM_GRAVITY);
        setDrag(CUSTOM_DRAG);
        setPersistenceRequired();
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            Player target = findTarget();
            if (target != null) {
                faceTarget(target);
                if (onGround()) {
                    if (jumpCooldown > 0) {
                        jumpCooldown--;
                    }
                    if (jumpCooldown <= 0) {
                        jumpTowards(target);
                    }
                }
            }
        }
        super.tick();
    }

    private Player findTarget() {
        Player nearest = level().getNearestPlayer(this, SEARCH_RANGE);
        if (nearest == null || !nearest.isAlive() || nearest.isSpectator() || !canAttack(nearest)) {
            return null;
        }
        return nearest;
    }

    private void faceTarget(Player target) {
        Vec3 horizontal = new Vec3(target.getX() - getX(), 0.0D, target.getZ() - getZ());
        if (horizontal.lengthSqr() <= 1.0E-7) {
            return;
        }
        horizontal = horizontal.normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-horizontal.x, horizontal.z));
        setPathRotation(yaw, 0.0F, currentPathNode.roll());
    }

    private void jumpTowards(Player target) {
        Vec3 horizontal = new Vec3(target.getX() - getX(), 0.0D, target.getZ() - getZ());
        if (horizontal.lengthSqr() > 1.0E-7) {
            horizontal = horizontal.normalize();
        } else {
            horizontal = Vec3.ZERO;
        }

        setVelocity(new Vec3(
                horizontal.x * HORIZONTAL_JUMP_SPEED,
                JUMP_VELOCITY,
                horizontal.z * HORIZONTAL_JUMP_SPEED
        ));
        setOnGround(false);
        jumpCooldown = Math.max(1, getJumpDelay() / 3);
    }

    private int getJumpDelay() {
        return 10 + getRandom().nextInt(20);
    }
}
