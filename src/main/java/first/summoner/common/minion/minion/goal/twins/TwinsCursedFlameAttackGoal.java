package first.summoner.common.minion.minion.goal.twins;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Twins;
import first.summoner.common.projectile.DemonFlame;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
                        cooldown = 10 + random.nextInt(-2, 2);
                        Vec3 direction = toTarget.offsetRandom(random, 0.25f).normalize();
                        minion.setVelocity(direction);
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
            Vec3 direction = targetPos.offsetRandom(random, 0.1f).subtract(minionPos).normalize();
            DemonFlame demonFlame = new DemonFlame();
            demonFlame.setOwner(owner);
            demonFlame.init(new PathNode(minionPos.add(direction.scale(0.25)), direction));
            demonFlame.copyAttributes(minion);
            demonFlame.setVelocity(direction.add(minion.getVelocity()));
            LyraHelper.get(owner).add(demonFlame);
        }
    }

}
