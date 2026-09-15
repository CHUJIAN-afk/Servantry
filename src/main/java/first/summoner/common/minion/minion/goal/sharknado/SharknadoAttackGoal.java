package first.summoner.common.minion.minion.goal.sharknado;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Sharknado;
import first.summoner.common.projectile.SharkDragon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 鲨鱼龙卷攻击Goal - 在玩家周围移动并发射鲨鱼射弹。
 */
public class SharknadoAttackGoal extends AttachmentEntityGoal<Sharknado> {

    private Vec3 offset = Vec3.ZERO;
    private int cooldown = 0;

    public SharknadoAttackGoal(Sharknado minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
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
        Vec3 wanderPos = targetPos.add(offset);
        if (minionPos.distanceTo(wanderPos) > 0.5) {
            minion.addVelocity(wanderPos.subtract(minionPos).normalize().scale(0.04));
        } else if (minion.getVelocity().length() < 0.1) {
            start();
        }

        if (--cooldown <= 0) {
            cooldown = 10 + minion.getRandom().nextIntBetweenInclusive(-1, 1);
            Vec3 direction = targetPos.subtract(minionPos).normalize();
            SharkDragon projectile = new SharkDragon();
            projectile.setOwner(minion.getOwner());
            projectile.init(new PathNode(minionPos, direction));
            projectile.copyAttributes(minion);
            projectile.setVelocity(direction);
            LyraHelper.get(minion.getLevel()).add(projectile);
        }
    }
}
