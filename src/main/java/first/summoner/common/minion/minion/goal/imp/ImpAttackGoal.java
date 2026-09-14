package first.summoner.common.minion.minion.goal.imp;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.Imp;
import first.summoner.utils.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 小鬼攻击Goal - 在目标周围盘旋，发射穿透火球。
 * 使用 getWanderPos 计算悬停位置，冷却完成后发射火球。
 */
public class ImpAttackGoal extends AttachmentEntityGoal<Imp> {

    private int cooldown = 0;
    private Vec3 wanderPos = Vec3.ZERO;

    public ImpAttackGoal(Imp minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        assert target != null;
        Vec3 targetPos = target.getBoundingBox().getCenter();
        wanderPos = Utils.getWanderPos(minion, wanderPos, targetPos, 8, 3);
        double distance = minion.getPos().distanceTo(wanderPos);
        minion.addVelocity(wanderPos.subtract(minion.getPos()).normalize().scale(Math.min(distance * 0.02, 0.2)));
        minion.lookAtPos(targetPos);
        if (--cooldown <= 0) {
            cooldown = minion.getFireballCooldown();
            minion.shootFireballAt(target);
        }
    }
}
