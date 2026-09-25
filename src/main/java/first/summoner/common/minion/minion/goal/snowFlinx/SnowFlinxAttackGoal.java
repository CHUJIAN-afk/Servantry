package first.summoner.common.minion.minion.goal.snowFlinx;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.SnowFlinx;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SnowFlinxAttackGoal extends AttachmentEntityGoal<SnowFlinx> {

    public SnowFlinxAttackGoal(SnowFlinx minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        minion.lookAtPos(targetPos);
        if (target.position().subtract(minion.getPos()).horizontalDistance() < target.getBbWidth()) {
            Vec3 vec3 = targetPos.offsetRandom(minion.getRandom(), 0.25f).add(0, target.getBbHeight() - 0.5, 0);
            minion.addVelocity(vec3.subtract(minion.getPos()).scale(0.1f));
            if (minion.getPos().distanceTo(vec3) < 1) {
                minion.setVelocity(minion.getVelocity().scale(0.25f));
            }
        }
        minion.moveTo(targetPos, 0.09f);
    }
}
