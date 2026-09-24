package first.summoner.common.minion.minion.goal.ufo;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.PlannedPath;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.UFO;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * UFO攻击Goal - 悬浮在目标上方，发射蓝色激光瞬间命中。
 * 目标在6格范围以外时瞬移到目标上方。
 */
public class UFOAttackGoal extends AttachmentEntityGoal<UFO> {

    private int cooldown = 0;
    private Vec3 offset = Vec3.ZERO;

    public UFOAttackGoal(UFO minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void start() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 pos = targetPos.add(0, target.getBbHeight() + 2, 0).offsetRandom(minion.getRandom(), 1f);
        offset = pos.subtract(targetPos);
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 minionPos = minion.getPos();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 wanderPos = targetPos.add(offset);
        double distance = minionPos.distanceTo(wanderPos);
        if (distance > 0.5) {
            minion.addVelocity(wanderPos.subtract(minionPos).normalize().scale(0.04));
        } else {
            minion.setVelocity(minion.getVelocity().scale(0.8));
            if (minion.getVelocity().length() > 0.1) {
                start();
            }
        }
        if (distance > 12 && distance < 16 && !minion.isExecutingPath()) {
            PathNode start = minion.getCurrentPathNode();
            PathNode end = start.modifyPos(wanderPos);
            List<PathNode> pathNodeList = new ArrayList<>();
            PlannedPath plannedPath = new PlannedPath(pathNodeList);
            for (int i = 0; i < 4; i++) {
                pathNodeList.add(start.lerp(end, (float) i / 3));
            }
            plannedPath.run(0, () -> {
                minion.setPhysics(false);
                minion.setVelocity(Vec3.ZERO);
                minion.setTrailTimer(4);
            });
            plannedPath.run(3, () -> minion.setPhysics(true));
            minion.setPlannedPath(plannedPath);
        }
        // 激光攻击
        if (--cooldown <= 0 && distance < 8) {
            cooldown = 7 + minion.getRandom().nextIntBetweenInclusive(1, 2);
            minion.attack(target, minion.getDamage(), 4);

            Level level = minion.getLevel();
            Vec3 direction = targetPos.subtract(minionPos);
            double laserDistance = direction.length();
            direction = direction.normalize();
            int particleCount = Math.max(3, (int) (laserDistance / 0.1));
            for (int i = 0; i < particleCount; i++) {
                double t = (double) i / particleCount;
                Vec3 particlePos = minionPos.add(direction.scale(laserDistance * t));
                ParticleHelper.create(level)
                        .generic(GenericParticleBuilder.create()
                                .centerColor(0x46f7ff)
                                .edgeColor(0x3dd6dd)
                                .lifetime(3)
                                .lifetimeRandom(2)
                                .scale(0.015f)
                                .scaleRandom(0.005f)
                        )
                        .pos(particlePos)
                        .offset(0.05)
                        .count(5)
                        .emit();
            }
        }
    }
}
