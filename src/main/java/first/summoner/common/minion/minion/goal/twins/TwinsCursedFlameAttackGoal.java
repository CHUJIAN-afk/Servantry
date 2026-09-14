package first.summoner.common.minion.minion.goal.twins;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.Twins;
import first.summoner.common.projectile.DemonFlame;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TwinsCursedFlameAttackGoal extends AttachmentEntityGoal<Twins> {

    private Vec3 wanderPos = Vec3.ZERO;
    private int cooldown = 0;
    private int emit = 0;
    private boolean emited = false;

    public TwinsCursedFlameAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return minion.isFlameEye() && minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        assert target != null;
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 toTarget = targetPos.subtract(minion.getPos());
        LivingEntity owner = minion.getOwner();
        Level level = minion.getLevel();
        minion.lookAtPos(targetPos);
        if (!emited) {
            if (--cooldown <= 0) {
                emit += 5;
                if (emit > 40) {
                    emited = true;
                } else {
                    cooldown = 12 + level.getRandom().nextInt(-2, 2);
                    Vec3 direction = toTarget.offsetRandom(level.getRandom(), (float) target.getBoundingBox().getSize()).normalize();
                    minion.addVelocity(direction.scale(2));
                    minion.setTrailTimer(10);
                }
            }
        } else {
            if (--emit < 0) {
                emited = false;
            }
            if (emit % 2 == 0) {
                wanderPos = Utils.getWanderPos(minion, wanderPos, targetPos, 4, 1);
                double distance = minion.getPos().distanceTo(wanderPos);
                minion.addVelocity(wanderPos.subtract(minion.getPos()).normalize().scale(Math.min(distance * 0.04, 0.4)));
                Vec3 direction = targetPos.subtract(minion.getPos()).normalize();
                DemonFlame demonFlame = new DemonFlame();
                demonFlame.setOwner(owner);
                demonFlame.init(new PathNode(minion.getPos(), direction));
                demonFlame.copyAttributes(minion);
                demonFlame.setVelocity(direction);
                LyraHelper.get(level).add(demonFlame);
                GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                        .centerColor(0x24d509)
                        .edgeColor(0x1FF109);
                ParticleHelper.create(level)
                        .generic(genericParticleBuilder
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
