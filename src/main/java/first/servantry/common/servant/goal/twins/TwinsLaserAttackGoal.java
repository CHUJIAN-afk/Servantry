package first.servantry.common.servant.goal.twins;

import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.projectile.CustomLaserProjectile;
import first.servantry.common.projectile.LaserProjectile;
import first.servantry.common.servant.Twins;
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

    private CustomLaserProjectile projectile = null;
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
    public void start() {
        projectile = new CustomLaserProjectile(servant.getDamageSource(), servant.getCurrentPathNode(), 0xb70700);
        projectile.join(servant.getOwner());
        projectile.setDamage(servant.getDamage());
        projectile.setKnockback(servant.getKnockback());
    }

    @Override
    public void stop() {
        if (projectile != null) {
            projectile.setRemove();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Player owner = servant.getOwner();

        // 计算环绕位置
        if (wanderTarget.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025) {
            wanderTarget = target.getBoundingBox().getCenter().offsetRandom(target.getRandom(), (float) target.getBoundingBox().getSize() * 6);
            wanderTarget.add(0, target.getBoundingBox().getSize() * 12, 0);
            double height = target.position().y() + target.getBoundingBox().getYsize() / 2;
            while (wanderTarget.y() < height) {
                wanderTarget = wanderTarget.add(0, 1, 0);
            }
        }
        Vec3 toAnchor = wanderTarget.subtract(servant.getPos());
        double dist = toAnchor.length();

        if (dist > 0.05) {
            double force = Math.min(dist * 0.08, 0.4);
            servant.applyForce(toAnchor.normalize().scale(force));
        }
        servant.lookAtPos(target.getBoundingBox().getCenter());

        if (projectile != null) {
            PathNode pathNode = servant.getCurrentPathNode();
            projectile.setCurrentPathNode(new PathNode(pathNode.pos(), pathNode.yaw(), pathNode.pitch(), projectile.getCurrentPathNode().roll() + 30));
            projectile.setHitbox(projectile.getPos(), target.getBoundingBox().getCenter(), 0.02f);
        }

        // 射击冷却
        if (shootCooldown <= 0) {
            shootAtTarget(owner, target);
            shotCount = (shotCount + 1) % 12;
            shootCooldown = shotCount < 5 ? 18 + owner.getRandom().nextInt(-2, 2) : 1;
        } else {
            shootCooldown--;
        }
    }

    private void shootAtTarget(Player owner, LivingEntity target) {
        Vec3 start = servant.getPos();
        RandomSource random = owner.getRandom();
        Vec3 direction = target.getBoundingBox().getCenter().offsetRandom(random, shotCount < 5 ? 0.3f : 1).subtract(start).normalize();
        LaserProjectile projectile = new LaserProjectile(servant.getDamageSource(), start, direction);
        projectile.setDamage(servant.getDamage() * 1.1f);
        projectile.join(owner);
        ServerLevel level = (ServerLevel) owner.level();
        level.playSound(null, start.x(), start.y(), start.z(), SoundRegister.Laser.get(), owner.getSoundSource());
        // 后坐力
        servant.applyForce(direction.scale(-0.1));
        ParticleHelper.create(level)
                .generic(GenericParticleBuilder.create()
                        .color(0xb70700)
                        .edgeColor(0xFF0700)
                        .colorRandom(0, 0.2F, 0.2F)
                        .lifetime(5)
                        .lifetimeRandom(5)
                        .spin(0.1f)
                        .spinRandom(0.05F)
                        .friction(0.75F)
                        .scale(0.045f)
                        .scaleRandom(0.005f)
                )
                .pos(start)
                .velocity(direction)
                .count(2)
                .speed(0.65)
                .spread(0.2)
                .emit();
    }

}
