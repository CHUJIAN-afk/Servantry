package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.projectile.LaserProjectile;
import first.servantry.common.servant.Twins;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.SoundRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 双子魔眼激光攻击Goal。
 * <p>
 * 激光眼（Retinazer）的攻击行为：
 * <ul>
 *   <li>环绕目标飞行</li>
 *   <li>每15tick发射一道激光</li>
 *   <li>攻击期间持续看向目标</li>
 * </ul>
 * </p>
 */
public class TwinsLaserAttackGoal extends ServantGoal<Twins> {

    private static final int SHOOT_COOLDOWN = 15;

    private int shootCooldown = 0;

    public TwinsLaserAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return servant.isLaserEye() && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (target == null || !target.isAlive()) return;

        Player owner = servant.getOwner();
        int order = owner.getData(AttachmentRegister.EntityData).getOrder(servant);

        // 计算环绕位置
        Vec3 anchor = getHaloAnchorPos(owner, target, order);
        Vec3 toAnchor = anchor.subtract(servant.getPos());
        double dist = toAnchor.length();

        if (dist > 0.05) {
            double force = Math.min(dist * 0.08, 0.4);
            servant.applyForce(toAnchor.normalize().scale(force));
        }

        Vec3 motionDir = target.getBoundingBox().getCenter().subtract(servant.getPos()).normalize();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-motionDir.x, motionDir.z));
        float targetPitch = (float) Math.toDegrees(Math.asin(-motionDir.y));
        servant.setDesiredRotation(targetYaw, targetPitch, servant.getRoll());
        // 射击冷却
        if (shootCooldown <= 0) {
            shootAtTarget(owner, target);
            shootCooldown = SHOOT_COOLDOWN + owner.getRandom().nextInt(-2, 2);
        } else {
            shootCooldown--;
        }
    }

    private void shootAtTarget(Player owner, LivingEntity target) {
        Vec3 start = servant.getPos();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        LaserProjectile projectile = new LaserProjectile(servant.getDamageSource(), start, direction.scale(2));
        owner.getData(AttachmentRegister.EntityData).addProjectile(projectile);
        owner.level().playSound(null, start.x(), start.y(), start.z(), SoundRegister.Laser.get(), owner.getSoundSource());
        // 后坐力
        servant.applyForce(direction.scale(-0.1));
    }

    /**
     * 计算光环锚点位置（用于攻击目标周围环绕）。
     */
    private Vec3 getHaloAnchorPos(Player owner, LivingEntity target, int order) {
        long seed = target.getId() * 31337L + order * 1021L;
        Random rand = new Random(seed);
        double baseTheta = rand.nextDouble() * Math.PI * 2;
        double phi = Math.acos(1.0 - rand.nextDouble() * 1.4);
        double radius = 3.5 + rand.nextDouble() * 2.0;
        double rotationSpeed = (rand.nextDouble() * 0.02 + 0.01) * (rand.nextBoolean() ? 1 : -1);
        double currentTheta = baseTheta + owner.tickCount * rotationSpeed;

        double offsetX = radius * Math.sin(phi) * Math.cos(currentTheta);
        double offsetY = radius * 0.5 * Math.cos(phi) + Math.sin(owner.tickCount * 0.05 + rand.nextDouble() * Math.PI) * 0.5;
        double offsetZ = radius * Math.sin(phi) * Math.sin(currentTheta);

        Vec3 targetCenter = target.getBoundingBox().getCenter();
        return targetCenter.add(offsetX, offsetY, offsetZ);
    }

    @Override
    public void stop() {
        //shootCooldown = SHOOT_COOLDOWN;
    }
}
