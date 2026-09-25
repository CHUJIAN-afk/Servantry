package first.summoner.common.minion.minion.goal.sanguineBat;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.Ellipse;
import first.summoner.common.minion.minion.SanguineBat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SanguineBatAttackGoal extends AttachmentEntityGoal<SanguineBat> {

    public SanguineBatAttackGoal(SanguineBat minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void start() {
        minion.progress = 0;
        minion.hasDamaged = false;
        minion.hitTargets.clear();
        Vec3 targetPos = minion.getTarget().getBoundingBox().getCenter();
        minion.normal = Ellipse.randomPlaneNormal(minion.getRandom(), targetPos, minion.getInterpolatedIdleState(1).pos());
    }

    @Override
    public void stop() {
        minion.progress = 0;
        minion.hasDamaged = false;
        minion.hitTargets.clear();
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        if (minion.isTargetChange() && !minion.hasDamaged) {
            minion.prep = false;
        } else {
            minion.progress += 1.0F / 22.0F;
            if (minion.progress >= 1) {
                minion.prep = false;
            } else {
                minion.setCurrentPathNode(minion.getEllipseNode(minion.progress, minion.getInterpolatedIdleState(1).pos(), target.getBoundingBox().getCenter()));
            }
        }
    }
}
