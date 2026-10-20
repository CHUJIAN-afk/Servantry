package org.confluence.mod.common.summoner.minion.goal.twins;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.confluence.mod.common.init.ModSoundEvents;
import org.confluence.mod.common.summoner.SummonerHelper;
import org.confluence.mod.common.summoner.attachmentEntity.AttachmentEntityGoal;
import org.confluence.mod.common.summoner.attachmentEntity.PathNode;
import org.confluence.mod.common.summoner.minion.Twins;
import org.confluence.mod.common.summoner.particle.GenericParticleBuilder;
import org.confluence.mod.common.summoner.particle.ParticleHelper;
import org.confluence.mod.common.summoner.projectile.Laser;

public class TwinsLaserAttackGoal extends AttachmentEntityGoal<Twins> {

    private Vec3 offset = Vec3.ZERO;
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
    public void start() {
        LivingEntity target = minion.getTarget();
        Vec3 minionPos = minion.getPos().offsetRandom(minion.getRandom(), 1f);
        Vec3 targetPos = target.getBoundingBox().getCenter();
        offset = minionPos.subtract(targetPos).normalize().scale(4);
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Player owner = minion.getOwner();
        Level level = minion.getLevel();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 minionPos = minion.getPos();
        Vec3 wanderPos = targetPos.add(offset);
        if (minionPos.distanceTo(wanderPos) > 0.5) {
            minion.addVelocity(wanderPos.subtract(minionPos).normalize().scale(0.04));
        } else {
            minion.setVelocity(minion.getVelocity().scale(0.8));
            if (minion.getVelocity().length() < 0.1) {
                start();
            }
        }
        minion.lookAtPos(targetPos);
        if (--shootCooldown <= 0) {
            shootAtTarget(owner, target, level);
            shotCount = (shotCount + 1) % 12;
            boolean attacking = shotCount >= 5;
            minion.setAttacking(attacking);
            shootCooldown = attacking ? 3 : 18 + level.getRandom().nextIntBetweenInclusive(-2, 2);
        }
    }

    @Override
    public void stop() {
        minion.setAttacking(false);
    }

    private void shootAtTarget(Player owner, LivingEntity target, Level level) {
        Vec3 start = minion.getPos();
        RandomSource random = level.getRandom();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        Laser projectile = new Laser();
        projectile.setOwner(owner);
        projectile.init(new PathNode(start.add(direction), direction));
        projectile.copyAttributes(minion);
        projectile.setVelocity(direction);
        projectile.setDamage(minion.getDamage() * 1.1f);
        SummonerHelper.get(owner).add(projectile);
        level.playSound(null, start.x, start.y, start.z, ModSoundEvents.ITEM_LASER_SHOOT.get(), minion.getSoundSource(), 0.9F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.2F);
        ParticleHelper.create(level)
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xb70700)
                        .edgeColor(0xFF0700)
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
