package first.summoner.common.minion.minion.goal.finch;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.Finch;
import net.minecraft.world.phys.Vec3;

/**
 * 待机：未锁定姿态时飞向编队待机点，姿态混合完成后直接吸附到待机节点。
 */
public class FinchIdleGoal extends AttachmentEntityGoal<Finch> {

    public FinchIdleGoal(Finch minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        if (minion.idleBlend == 0) {
            Vec3 targetPos = minion.getInterpolatedIdleState(1).pos();
            Vec3 minionPos = minion.getPos();
            Vec3 normalize = targetPos.subtract(minionPos).normalize();
            minion.lookAtDirection(normalize);
            minion.addVelocity(normalize.scale(0.01 * Math.min(1 + targetPos.distanceTo(minionPos) * 0.2, 16)));
        } else {
            minion.setVelocity(Vec3.ZERO);
            minion.setCurrentPathNode(minion.getCurrentPathNode().lerp(minion.getInterpolatedIdleState(1), minion.idleBlend));
        }
    }
}
