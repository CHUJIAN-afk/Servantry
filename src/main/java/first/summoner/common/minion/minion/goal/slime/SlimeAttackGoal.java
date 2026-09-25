package first.summoner.common.minion.minion.goal.slime;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.Slime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 攻击：始终看向目标并持续朝目标跳跃靠近，伤害由身体碰撞结算。
 */
public class SlimeAttackGoal extends AttachmentEntityGoal<Slime> {

    public SlimeAttackGoal(Slime minion) {
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
        minion.moveTo(targetPos, 0.4f);
    }
}
