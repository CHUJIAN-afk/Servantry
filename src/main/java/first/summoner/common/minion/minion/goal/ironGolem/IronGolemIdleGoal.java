package first.summoner.common.minion.minion.goal.ironGolem;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.IronGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 待机：跟随主人身后一字排开，主人腾空 6 格以上时改为飞行跟随。
 */
public class IronGolemIdleGoal extends AttachmentEntityGoal<IronGolem> {

    public IronGolemIdleGoal(IronGolem minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() == null;
    }

    @Override
    public void tick() {
        Player owner = minion.getOwner();
        Vec3 forward = Vec3.directionFromRotation(0.0F, owner.yBodyRot);
        Vec3 right = forward.cross(new Vec3(0.0, 1.0, 0.0));
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
        double lateral = (minion.getOrder() - (minion.getSameSize() - 1) * 0.5) * 1.8;
        Vec3 idlePos = Vec3.atBottomCenterOf(BlockPos.containing(owner.position().subtract(forward.scale(2.5)).add(right.scale(lateral))));
        Vec3 subtract = idlePos.subtract(minion.getPos());
        if (!owner.onGround() && owner.getY() > minion.getPos().y + 6.0) {
            minion.lookAtPos(owner.getEyePosition());
            minion.flyTo(idlePos, 0.06f);
        } else {
            if (subtract.horizontalDistance() > 1) {
                minion.lookAtPos(idlePos);
                minion.moveTo(idlePos, 0.05f);
            } else {
                minion.lookAtPos(owner.getEyePosition());
            }
        }
        if (subtract.length() > 64) {
            minion.setVelocity(Vec3.ZERO);
            minion.init(minion.getCurrentPathNode().modifyPos(owner.getBoundingBox().getCenter().offsetRandom(minion.getRandom(), 3)));
        }
    }
}
