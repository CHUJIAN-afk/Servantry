package first.servantry.common.servant.goal.twins;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Twins;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 魔焰眼冲刺攻击Goal。
 * <p>
 * 咒焰眼（Spazmatism）的攻击行为：
 * <ul>
 *   <li>冷却期间：看向目标并逐渐加速靠近</li>
 *   <li>冲刺时：施加巨大冲量冲向目标身后</li>
 *   <li>冷却时长：固定14tick</li>
 *   <li>冲刺条件：距离目标小于16格</li>
 * </ul>
 * </p>
 */
public class TwinsCursedFlameAttackGoal extends ServantGoal<Twins> {

    private static final double MAX_DASH_DISTANCE = 16.0;

    private int cooldown;

    public TwinsCursedFlameAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return !servant.isLaserEye() && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(servant.getPos());
        double dist = toTarget.length();
        servant.lookAt(toTarget);
        cooldown--;
        if (cooldown <= 0) {
            if (target.getBoundingBox().getCenter().distanceToSqr(servant.getPos()) < MAX_DASH_DISTANCE * MAX_DASH_DISTANCE) {
                Vec3 direction = toTarget.offsetRandom(servant.getOwner().getRandom(), (float) target.getBoundingBox().getSize()).normalize();
                servant.applyForce(direction.scale(2));
                cooldown = 14 + servant.getOwner().getRandom().nextInt(-2, 2);
                servant.setTrailTimer(10);
            }
        } else {
            // 冷却期间缓慢靠近目标
            if (dist > 1.0) {
                double force = Math.min(0.05, dist * 0.01);
                servant.applyForce(toTarget.normalize().scale(force));
            }
        }
    }

}