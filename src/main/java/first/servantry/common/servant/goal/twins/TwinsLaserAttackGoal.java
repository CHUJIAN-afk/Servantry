package first.servantry.common.servant.goal.twins;

import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.projectile.Laser;
import first.servantry.common.servant.Twins;
import first.servantry.register.ServantrySoundRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TwinsLaserAttackGoal extends ServantGoal<Twins> {

    private Vec3 wanderPos = Vec3.ZERO;
    private int shootCooldown = 0;
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
        Player owner = servant.getOwner();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        wanderPos = servant.getWanderPos(wanderPos, targetPos, 6, 3);
        double distance = servant.getPos().distanceTo(wanderPos);
        servant.applyForce(wanderPos.subtract(servant.getPos()).normalize().scale(Math.min(distance * 0.04, 0.4)));
        servant.lookAtPos(targetPos);
        if (--shootCooldown <= 0) {
            shootAtTarget(owner, target);
            shotCount = (shotCount + 1) % 12;
            shootCooldown = shotCount < 5 ? 18 + owner.getRandom().nextInt(-2, 2) : 1;
        }
    }

    private void shootAtTarget(Player owner, LivingEntity target) {
        Vec3 start = servant.getPos();
        RandomSource random = owner.getRandom();
        Vec3 direction = target.getBoundingBox().getCenter().offsetRandom(random, shotCount < 5 ? 0.3f : 1).subtract(start).normalize();
        Laser projectile = new Laser(servant.getDamageSource(), start, direction);
        projectile.setDamage(servant.getDamage() * 1.1f);
        projectile.join(owner);
        ServerLevel level = (ServerLevel) owner.level();
        level.playSound(null, start.x(), start.y(), start.z(), ServantrySoundRegister.Laser.get(), owner.getSoundSource());
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
