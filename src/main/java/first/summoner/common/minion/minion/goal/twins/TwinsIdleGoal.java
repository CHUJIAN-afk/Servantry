package first.summoner.common.minion.minion.goal.twins;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.Twins;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TwinsIdleGoal extends AttachmentEntityGoal<Twins> {

    private Vec3 offset = Vec3.ZERO;

    public TwinsIdleGoal(Twins minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {
        LivingEntity owner = minion.getOwner();
        Vec3 center = owner.getBoundingBox().getCenter().add(0, owner.getBbHeight() + 1, 0).add(Vec3.ZERO.offsetRandom(minion.getRandom(), 1f));
        offset = center.add(center.subtract(minion.getPos()).normalize().scale(1.25f));
        offset = offset.subtract(owner.getBoundingBox().getCenter());
    }

    @Override
    public void tick() {
        Vec3 targetPos = minion.getOwner().getBoundingBox().getCenter().add(offset);
        Vec3 minionPos = minion.getPos();
        if (minionPos.distanceTo(targetPos) > 0.5) {
            minion.addVelocity(targetPos.subtract(minionPos).normalize().scale(0.01 * Math.min(1 + (targetPos.distanceTo(minionPos) * 0.1), 2)));
            minion.lookAtPos(targetPos);
        } else {
            minion.setVelocity(minion.getVelocity().scale(0.9));
            if (minion.getVelocity().length() < 0.1) {
                start();
            }
        }
    }
}
