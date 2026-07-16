package first.servantry.common.servant.goal.stardustCell;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustCell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 星尘细胞攻击Goal。
 * <p>
 * 追踪目标并在射击冷却结束时发射星细胞射弹。
 * </p>
 */
public class StardustCellAttackGoal extends ServantGoal<StardustCell> {

    public StardustCellAttackGoal(StardustCell servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        double distanceToSqr = servant.getPos().distanceToSqr(target.getBoundingBox().getCenter());
        if (distanceToSqr > 14 * 14 && distanceToSqr < 26 * 26) {
            servant.teleportTo(target.getBoundingBox().getCenter());
        } else {
            Player owner = servant.getOwner();
            Vec3 wanderPos = getHaloAnchorPos(target);
            double distance = servant.getPos().distanceTo(wanderPos);
            servant.applyForce(wanderPos.subtract(servant.getPos()).normalize().scale(Math.min(distance * 0.1, 0.6)));
            float scale = distance < 1.5 ? 0.55f : 0.95f;
            servant.setVelocity(servant.getVelocity().scale(scale));
            if (!servant.isExecutingPath()) {
                if (servant.getShootCooldown() <= 0) {
                    servant.shootAtTarget(target);
                    servant.setShootCooldown(12 + owner.getRandom().nextInt(4));
                }
            } else if (servant.getTrailTimer() == 1) {
                servant.shootAtTarget(target);
                servant.setShootCooldown(12 + owner.getRandom().nextInt(4));
            }
        }
    }

    /**
     * 计算光环锚点位置（用于攻击目标周围环绕）。
     */
    public Vec3 getHaloAnchorPos(LivingEntity target) {
        int tickCount = servant.getTickCount();

        long seed = target.getId() * 31337L + servant.getOrder() * 1021L;
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