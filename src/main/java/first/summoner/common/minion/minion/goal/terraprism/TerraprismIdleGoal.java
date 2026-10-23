package first.summoner.common.minion.minion.goal.terraprism;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Terraprism;
import net.minecraft.world.phys.Vec3;

public class TerraprismIdleGoal extends AttachmentEntityGoal<Terraprism> {

    public TerraprismIdleGoal(Terraprism minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        PathNode from = minion.getCurrentPathNode();
        boolean returning = minion.attackTimer < 0;
        minion.move(returning ? "terraprism_return" : "terraprism_idle", partialTick -> {
            PathNode idle = minion.getInterpolatedIdleState(partialTick);
            double distance = from.pos().distanceTo(idle.pos());
            double amount = returning ? Math.min(1, 6 * partialTick / Math.max(distance, 1.0E-6)) : 1 - Math.pow(0.65, partialTick);
            Vec3 pos = from.pos().lerp(idle.pos(), amount);
            float rotation = (float) (1 - Math.pow(returning ? 0.512 : 0.65, partialTick));
            return from.lerp(idle, rotation).modifyPos(pos);
        }, returning, false);
        if (returning) {
            if (from.pos().distanceTo(minion.getInterpolatedIdleState(1f).pos()) < 6.125) {
                minion.attackTimer = 0;
            }
        } else {
            if (minion.getRandom().nextFloat() < 1 - Math.pow(0.95, 3)) {
                minion.setTarget(minion.findAttackTarget(false));
                if (minion.getTarget() != null) {
                    minion.attacking = true;
                    minion.attackTimer = 80;
                }
            }
        }
    }
}
