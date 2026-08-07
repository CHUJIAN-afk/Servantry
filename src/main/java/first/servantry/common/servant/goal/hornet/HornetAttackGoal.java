package first.servantry.common.servant.goal.hornet;

import first.lyra.common.servant.ServantGoal;
import first.servantry.common.servant.Hornet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 黄蜂攻击Goal - 在目标上方盘旋，发射毒刺。
 * 使用 getWanderPos 计算悬停位置，抵达一定水平距离时悬停。
 * 无论是否抵达悬停位置，只要冷却完成就发射毒刺。
 */
public class HornetAttackGoal extends ServantGoal<Hornet> {

    private int cooldown = 0;
    private Vec3 wanderPos = Vec3.ZERO;

    public HornetAttackGoal(Hornet servant) {
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
            cooldown = servant.getStingerCooldown();
            servant.shootStingerAt(target);
        }
    }
}
