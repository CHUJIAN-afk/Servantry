package first.summoner.common.minion.minion.goal.imp;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.Imp;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ImpIdleGoal extends AttachmentEntityGoal<Imp> {

    private Vec3 offset = Vec3.ZERO;

    public ImpIdleGoal(Imp minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {
        LivingEntity owner = minion.getOwner();
        Vec3 ownerCenter = owner.getBoundingBox().getCenter();
        Vec3 center = ownerCenter.add(0.0, owner.getBbHeight() + 1.0, 0.0).add(Vec3.ZERO.offsetRandom(minion.getRandom(), owner.getBbWidth() * 2));
        Vec3 direction = center.subtract(minion.getPos());
        offset = (direction.lengthSqr() < 1.0E-6 ? Vec3.ZERO : direction.normalize().scale(1.25F)).add(center).subtract(ownerCenter);
    }

    @Override
    public void tick() {
        Vec3 targetPos = minion.getOwner().getBoundingBox().getCenter().add(offset);
        Vec3 toDestination = targetPos.subtract(minion.getPos());
        if (toDestination.lengthSqr() > 0.25) {
            minion.addVelocity(toDestination.normalize().scale(0.02 * Math.min(1.0 + toDestination.length() * 0.1, 8.0)));
            minion.lookAtPos(targetPos);
        } else {
            minion.setVelocity(minion.getVelocity().scale(0.6));
            if (minion.getVelocity().lengthSqr() < 0.01) {
                start();
            }
        }
    }
}
