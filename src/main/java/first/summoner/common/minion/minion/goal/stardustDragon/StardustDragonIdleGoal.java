package first.summoner.common.minion.minion.goal.stardustDragon;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.StardustDragon;
import first.summoner.utils.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙待机目标。
 */
public class StardustDragonIdleGoal extends AttachmentEntityGoal<StardustDragon> {

    private Vec3 wanderPos = Vec3.ZERO;

    public StardustDragonIdleGoal(StardustDragon minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isHead() && minion.getTarget() == null;
    }

    @Override
    public void tick() {
        LivingEntity owner = minion.getOwner();
        Vec3 anchor = minion.getOwner().position();
        do {
            wanderPos = Utils.getWanderPos(minion, wanderPos, anchor, 8, 0);
        } while (wanderPos.distanceToSqr(minion.getPos()) < 2);
        minion.orbitToward(wanderPos, 120f, 0.05 + minion.getPos().distanceTo(wanderPos) * 0.005);
        if (owner.distanceToSqr(minion.getPos()) > 128 * 128) {
            Utils.teleportTo(minion, owner.getBoundingBox().getCenter());
            wanderPos = Vec3.ZERO;
        }
    }
}
