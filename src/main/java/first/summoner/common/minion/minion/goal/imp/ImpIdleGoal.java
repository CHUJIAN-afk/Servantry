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
        Vec3 back = Vec3.directionFromRotation(0, owner.getYRot()).scale(-1.25f);
        Vec3 side = new Vec3(-back.z, 0, back.x).normalize();
        float lateral = (minion.getOrder() - (minion.getSameSize() - 1) / 2f) * 0.6f;
        offset = new Vec3(0, owner.getBbHeight() + 1, 0).add(back).add(side.scale(lateral));
    }

    @Override
    public void tick() {
        Vec3 targetPos = minion.getOwner().getBoundingBox().getCenter().add(offset);
        Vec3 minionPos = minion.getPos();
        if (minionPos.distanceTo(targetPos) > 1) {
            minion.addVelocity(targetPos.subtract(minionPos).normalize().scale(0.04 * Math.min(1 + (targetPos.distanceTo(minionPos) * 0.1), 2)));
            minion.lookAtPos(targetPos);
        }
    }
}
