package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.projectile.FrostBolt;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 寒霜九头蛇哨兵 - 原地不动，有重力，瞄准目标后每 1 秒发射一枚寒霜弹。
 * <p>
 * 行为：
 * - 不移动，落地后静止
 * - 每 1 秒（20 tick）发射一枚寒霜弹
 * - 需要先看向目标，夹角小于 5° 才发射（参考脉冲炮塔）
 */
public class Hydra extends MomentumMinion implements IBlockCollision<Hydra> {

    private int cooldown = 0;

    public Hydra() {
        super(SummonerAttachmentEntityRegister.HYDRA);
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = getTarget();
        cooldown--;
        if (target != null) {
            Vec3 targetPos = target.getBoundingBox().getCenter();
            lookAtPos(targetPos);
            if (cooldown < 0) {
                Vec3 targetDirection = targetPos.subtract(getPos());
                Vec3 lookAngle = getLookAngle();
                if (getAngleDeg(targetDirection, lookAngle) < 5) {
                    cooldown = 20;
                    fire(targetPos);
                }
            }
        }
    }

    private void fire(Vec3 targetPos) {
        Vec3 start = getPos();
        Vec3 direction = targetPos.subtract(start).normalize();
        FrostBolt bolt = new FrostBolt();
        bolt.setOwner(owner);
        bolt.init(new PathNode(start, direction));
        bolt.copyAttributes(this);
        bolt.setVelocity(direction);
        LyraHelper.get(owner).add(bolt);
    }

    private double getAngleDeg(Vec3 a, Vec3 b) {
        if (a.lengthSqr() > 1e-7 || b.lengthSqr() > 1e-7) {
            return Math.toDegrees(Math.atan2(a.cross(b).length(), a.dot(b)));
        }
        return 0.0;
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.4, -0.4, -0.4, 0.4, 0.4, 0.4);
    }

        public void dimensionChange() {
        setRemove();
    }
}
