package first.summoner.common.minion.minion.goal;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.minion.MomentumMinion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MomentumMinionIdleGoal extends AttachmentEntityGoal<MomentumMinion> {

    private Vec3 offset = Vec3.ZERO;

    public MomentumMinionIdleGoal(MomentumMinion minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {
        LivingEntity owner = minion.getOwner();
        Vec3 center = owner.getBoundingBox().getCenter().add(0, owner.getBbHeight() + 1, 0).add(Vec3.ZERO.offsetRandom(minion.getRandom(), 0.25f));
        offset = center.add(center.subtract(minion.getPos()).normalize().scale(1.25f));
        offset = offset.subtract(owner.getBoundingBox().getCenter());
    }

    @Override
    public void tick() {
        Vec3 targetPos = minion.getOwner().getBoundingBox().getCenter().add(offset);
        if (minion.getPos().distanceTo(targetPos) > 0.5) {
            minion.addVelocity(targetPos.subtract(minion.getPos()).normalize().scale(0.02));
            minion.lookAtPos(targetPos);
        } else {
            minion.setVelocity(minion.getVelocity().scale(0.8));
            if (minion.getVelocity().length() < 0.1) {
                start();
            }
        }
    }
}
