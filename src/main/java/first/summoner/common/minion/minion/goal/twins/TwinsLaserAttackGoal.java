package first.summoner.common.minion.minion.goal.twins;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.sound.Playable;
import first.summoner.common.minion.minion.Twins;
import first.summoner.common.projectile.Laser;
import first.summoner.register.SummonerSoundRegister;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.Utils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TwinsLaserAttackGoal extends AttachmentEntityGoal<Twins> {

    private Vec3 wanderPos = Vec3.ZERO;
    private int shootCooldown = 0;
    private int shotCount = 0;

    public TwinsLaserAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return minion.isLaserEye() && minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        assert target  != null;
        LivingEntity owner = minion.getOwner();
        Level level = minion.getLevel();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        wanderPos = Utils.getWanderPos(minion, wanderPos, targetPos, 6, 3);
        double distance = minion.getPos().distanceTo(wanderPos);
        minion.addVelocity(wanderPos.subtract(minion.getPos()).normalize().scale(Math.min(distance * 0.04, 0.4)));
        minion.lookAtPos(targetPos);
        if (--shootCooldown <= 0) {
            shootAtTarget(owner, target, level);
            shotCount = (shotCount + 1) % 12;
            shootCooldown = shotCount < 5 ? 18 + level.getRandom().nextInt(-2, 2) : 1;
        }
    }

    private void shootAtTarget(LivingEntity owner, LivingEntity target, Level level) {
        Vec3 start = minion.getPos();
        RandomSource random = level.getRandom();
        Vec3 direction = target.getBoundingBox().getCenter().offsetRandom(random, shotCount < 5 ? 0.3f : 1).subtract(start).normalize();
        Laser projectile = new Laser();
        projectile.setOwner(owner);
        projectile.init(new PathNode(start.add(direction), direction));
        projectile.copyAttributes(minion);
        projectile.setVelocity(direction);
        projectile.setDamage(minion.getDamage() * 1.1f);
        LyraHelper.get(level).add(projectile);
        Playable.play(SummonerSoundRegister.Laser, level, start, minion.getSoundSource());
        // 后坐力
        minion.addVelocity(direction.scale(-0.1));
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
