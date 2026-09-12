package first.summoner.common.minion.minion.goal.stardustCell;

import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.minion.StardustCell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 星尘细胞攻击Goal。
 * <p>
 * 追踪目标并在射击冷却结束时发射星细胞射弹。
 * </p>
 */
public class StardustCellAttackGoal extends MinionGoal<StardustCell> {

    public StardustCellAttackGoal(StardustCell minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        double distanceToSqr = minion.getPos().distanceToSqr(target.getBoundingBox().getCenter());
        if (distanceToSqr > 14 * 14 && distanceToSqr < 26 * 26) {
            minion.teleportTo(target.getBoundingBox().getCenter());
        } else {
            Vec3 wanderPos = getHaloAnchorPos(target);
            double distance = minion.getPos().distanceTo(wanderPos);
            minion.applyForce(wanderPos.subtract(minion.getPos()).normalize().scale(Math.min(distance * 0.1, 0.6)));
            float scale = distance < 1.5 ? 0.55f : 0.95f;
            minion.setVelocity(minion.getVelocity().scale(scale));
            if (!minion.isExecutingPath()) {
                if (minion.getShootCooldown() <= 0) {
                    minion.shootAtTarget(target);
                    minion.setShootCooldown(12 + minion.getRandom().nextInt(4));
                }
            } else if (minion.getTrailTimer() == 1) {
                minion.shootAtTarget(target);
                minion.setShootCooldown(12 + minion.getRandom().nextInt(4));
            }
        }
    }

    /**
     * 计算光环锚点位置（用于攻击目标周围环绕）。
     */
    public Vec3 getHaloAnchorPos(LivingEntity target) {
        int tickCount = minion.getTickCount();

        long seed = target.getId() * 31337L + minion.getSameOrder() * 1021L;
        Random rand = new Random(seed);
        double baseTheta = rand.nextDouble() * Math.PI * 2;
        double phi = Math.acos(1.0 - rand.nextDouble() * 1.4);
        double radius = 3.5 + rand.nextDouble() * 4.0;
        double rotationSpeed = (rand.nextDouble() * 0.02 + 0.01) * (rand.nextBoolean() ? 1 : -1);

        double currentTheta = baseTheta + tickCount * rotationSpeed;

        double offsetX = radius * Math.sin(phi) * Math.cos(currentTheta);
        double offsetY = radius * Math.cos(phi) + Math.sin(tickCount * 0.05 + rand.nextDouble() * Math.PI) * 0.5;
        double offsetZ = radius * Math.sin(phi) * Math.sin(currentTheta);

        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        return targetCenter.add(offsetX, offsetY, offsetZ);
    }
}
