package first.summoner.common.minion.goal.twins;

import first.lyra.common.minion.MinionGoal;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.sound.Playable;
import first.summoner.common.minion.Twins;
import first.summoner.common.projectile.Laser;
import first.summoner.register.SummonerSoundRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TwinsLaserAttackGoal extends MinionGoal<Twins> {

    private Vec3 wanderPos = Vec3.ZERO;
    private int shootCooldown = 0;
    private int shotCount = 0;

    public TwinsLaserAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return minion.isLaserEye() && minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Player owner = minion.getOwner();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        wanderPos = minion.getWanderPos(wanderPos, targetPos, 6, 3);
        double distance = minion.getPos().distanceTo(wanderPos);
        minion.applyForce(wanderPos.subtract(minion.getPos()).normalize().scale(Math.min(distance * 0.04, 0.4)));
        minion.lookAtPos(targetPos);
        if (--shootCooldown <= 0) {
            shootAtTarget(owner, target);
            shotCount = (shotCount + 1) % 12;
            shootCooldown = shotCount < 5 ? 18 + owner.getRandom().nextInt(-2, 2) : 1;
        }
    }

    private void shootAtTarget(Player owner, LivingEntity target) {
        Vec3 start = minion.getPos();
        RandomSource random = owner.getRandom();
        Vec3 direction = target.getBoundingBox().getCenter().offsetRandom(random, shotCount < 5 ? 0.3f : 1).subtract(start).normalize();
        Laser projectile = new Laser();
        projectile.copyDamageSource(minion);
        projectile.setPos(start);
        projectile.setVelocity(direction);
        projectile.setDamage(minion.getDamage() * 1.1f);
        projectile.join(owner);
        ServerLevel level = (ServerLevel) owner.level();
        Playable.play(SummonerSoundRegister.Laser, level, start, owner.getSoundSource());
        // 后坐力
        minion.applyForce(direction.scale(-0.1));
        GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                .centerColor(0xb70700)
                .edgeColor(0xFF0700);
        ParticleHelper.create(level)
                .generic(genericParticleBuilder
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
