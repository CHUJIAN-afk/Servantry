package first.summoner.common.minion.minion.goal.twins;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.Twins;
import first.summoner.common.projectile.DemonFlame;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TwinsCursedFlameAttackGoal extends AttachmentEntityGoal<Twins> {

    private Vec3 offset = Vec3.ZERO;
    private int cooldown = 0;
    private int phaseTicks = 0;
    private boolean charging = true;

    public TwinsCursedFlameAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return minion.isFlameEye() && minion.getTarget() != null;
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
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 minionPos = minion.getPos();
        Vec3 toTarget = targetPos.subtract(minion.getPos());
        Player owner = minion.getOwner();
        Level level = minion.getLevel();
        minion.lookAtPos(targetPos);
        RandomSource random = minion.getRandom();
        if (charging) {
            if (targetPos.distanceTo(minionPos) < 8) {
                if (--cooldown <= 0) {
                    phaseTicks += 2 + random.nextIntBetweenInclusive(0, 3);
                    if (phaseTicks > 40) {
                        charging = false;
                        start();
                    } else {
                        cooldown = 14 + random.nextInt(-2, 2);
                        Vec3 direction = toTarget.offsetRandom(random, 0.25f).normalize();
                        minion.addVelocity(direction);
                    }
                }
            } else {
                minion.addVelocity(targetPos.subtract(minionPos).normalize().scale(0.08));
            }
        } else {
            Vec3 wanderPos = targetPos.add(offset);
            if (minionPos.distanceTo(wanderPos) > 0.5) {
                minion.addVelocity(wanderPos.subtract(minionPos).normalize().scale(0.04));
            } else if (minion.getVelocity().length() < 0.1) {
                start();
            }
            if (--phaseTicks < 0) {
                charging = true;
            }
            if (phaseTicks % 2 == 0) {
                Vec3 direction = targetPos.subtract(minionPos).normalize();
                DemonFlame demonFlame = new DemonFlame();
                demonFlame.setOwner(owner);
                demonFlame.init(new PathNode(minionPos, direction));
                demonFlame.copyAttributes(minion);
                demonFlame.setVelocity(direction);
                LyraHelper.get(owner).add(demonFlame);
                ParticleHelper.create(level)
                        .generic(GenericParticleBuilder.create()
                                .centerColor(0x24d509)
                                .edgeColor(0x1FF109)
                                .lifetime(5)
                                .lifetimeRandom(15)
                                .spin(0.4f)
                                .spinRandom(0.1F)
                                .friction(0.85F)
                                .scale(0.035f)
                                .scaleRandom(0.005f)
                        )
                        .pos(minion.getPos())
                        .offset(0.025)
                        .velocity(targetPos.subtract(minion.getPos()))
                        .count(4)
                        .speed(1.25)
                        .spread(0.15)
                        .emit();
            }
        }
    }

}
