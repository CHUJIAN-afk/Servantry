package first.summoner.common.minion.minion.goal.stardustDragon;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.StardustDragon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙攻击目标。
 */
public class StardustDragonAttackGoal extends AttachmentEntityGoal<StardustDragon> {

    public StardustDragonAttackGoal(StardustDragon minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isHead() && minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        minion.orbitToward(targetPos, 125f, 0.1 + Math.min(minion.getPos().distanceTo(targetPos) * 0.01, 0.05));
    }
}