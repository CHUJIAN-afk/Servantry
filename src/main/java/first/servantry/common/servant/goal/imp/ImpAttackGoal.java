package first.servantry.common.servant.goal.imp;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Imp;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 小鬼攻击Goal - 在目标周围盘旋，发射穿透火球。
 * 使用 getWanderPos 计算悬停位置，冷却完成后发射火球。
 */
public class ImpAttackGoal extends ServantGoal<Imp> {

    private int cooldown = 0;
    private Vec3 wanderPos = Vec3.ZERO;

    public ImpAttackGoal(Imp servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        wanderPos = servant.getWanderPos(wanderPos, targetPos, 8, 3);
        double distance = servant.getPos().distanceTo(wanderPos);
        servant.applyForce(wanderPos.subtract(servant.getPos()).normalize().scale(Math.min(distance * 0.02, 0.2)));
        servant.lookAtPos(targetPos);
        if (--cooldown <= 0) {
            cooldown = servant.getFireballCooldown();
            servant.shootFireballAt(target);
        }
    }
}
