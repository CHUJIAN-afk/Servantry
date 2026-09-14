package first.summoner.common.minion.minion.goal.hornet;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.Hornet;
import first.summoner.utils.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 黄蜂攻击Goal - 在目标上方盘旋，发射毒刺。
 * 使用 getWanderPos 计算悬停位置，抵达一定水平距离时悬停。
 * 无论是否抵达悬停位置，只要冷却完成就发射毒刺。
 */
public class HornetAttackGoal extends AttachmentEntityGoal<Hornet> {

    private int cooldown = 0;
    private Vec3 wanderPos = Vec3.ZERO;

    public HornetAttackGoal(Hornet minion) {
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
            cooldown = minion.getStingerCooldown();
            minion.shootStingerAt(target);
        }
    }
}
