package first.summoner.common.minion.minion.goal.vampireFrog;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.VampireFrog;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class VampireFrogAttackGoal extends AttachmentEntityGoal<VampireFrog> {

    private int cooldown;

    public VampireFrogAttackGoal(VampireFrog minion) {
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
        cooldown--;
        if (cooldown == 10) {
            minion.attack(target, minion.getDamage(), 3);
        }
        if (minion.getPos().distanceTo(targetPos) < 3) {
            if (cooldown <= 0) {
                cooldown = 20;
                minion.attackTime = minion.getTickCount();
            }
        } else {
            if (cooldown <= 0) {
                minion.moveTo(targetPos, 0.06f);
            }
        }
    }
}
