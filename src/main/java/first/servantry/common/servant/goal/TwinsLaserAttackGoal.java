package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.projectile.LaserProjectile;
import first.servantry.common.servant.Twins;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.SoundRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 双子魔眼激光攻击Goal。
 * <p>
 * 激光眼（Retinazer）的攻击行为：
 * <ul>
 *   <li>环绕目标飞行</li>
 *   <li>射击18次为一个循环：前6次间隔10tick，后12次间隔2tick</li>
 *   <li>切换目标不重置攻击次数</li>
 * </ul>
 * </p>
 */
public class TwinsLaserAttackGoal extends ServantGoal<Twins> {

    private Vec3 wanderTarget = Vec3.ZERO;
    private int shootCooldown = 0;
    /**
     * 当前循环内已射击次数（0-17）
     */
    private int shotCount = 0;

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
            shotCount = (shotCount + 1) % 12;
            shootCooldown = shotCount < 5 ? 10 : 1;
        } else {
            shootCooldown--;
        }
    }

    private void shootAtTarget(Player owner, LivingEntity target) {
        Vec3 start = servant.getPos();
        RandomSource random = owner.getRandom();
        Vec3 direction = target.getBoundingBox().getCenter().offsetRandom(random, shotCount < 5 ? 0.3f : 1).subtract(start).normalize();
        LaserProjectile projectile = new LaserProjectile(servant.getDamageSource(), start.add(direction.scale(-0.75)), direction);
        owner.getData(AttachmentRegister.EntityData).addProjectile(projectile);
        ServerLevel level = (ServerLevel) owner.level();
        level.playSound(null, start.x(), start.y(), start.z(), SoundRegister.Laser.get(), owner.getSoundSource());
        // 后坐力
        servant.applyForce(direction.scale(-0.1));
        // 喷射粒子 - 星尘调色：青蓝色带随机偏差
        ParticleHelper.create(level)
                .generic(b -> b.color(0x33CCFF).colorRandom(0.2F, 0.2F, 0.0F).lifetime(15).friction(0.75F).spin(0.1F).spinRandom(0.05F))
                .pos(start)
                .velocity(direction)
                .count(5)
                .speed(0.85)
                .spread(0.2)
                .emit();
    }

    /**
     * 计算光环锚点位置（用于攻击目标周围环绕）。
     */
    private Vec3 getHaloAnchorPos(Player owner, LivingEntity target, int order) {
        if (wanderTarget.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025) {
            wanderTarget = target.getBoundingBox().getCenter().offsetRandom(target.getRandom(), (float) target.getBoundingBox().getSize() * 6);
            wanderTarget.add(0, target.getBoundingBox().getSize() * 12, 0);
        }
        return wanderTarget;
    }

}
