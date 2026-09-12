package first.summoner.common.sentry;

import first.lyra.common.entity.IBlockCollision;
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
        setGravity(-0.05f);
        setDrag(0.9f);
        setRotationSpeed(18f);
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            if (owner != null && getPos().distanceToSqr(owner.position()) > 128 * 128) {
                setRemove();
            }
            LivingEntity target = getTarget();
            cooldown--;
            if (isTarget(target)) {
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
        super.tick();
    }

    private void fire(Vec3 targetPos) {
        Vec3 start = getPos();
        Vec3 direction = targetPos.subtract(start).normalize();
        FrostBolt bolt = new FrostBolt();
        bolt.copyDamageSource(this);
        bolt.setPos(start);
        bolt.setVelocity(direction);
        if (owner != null) {
            bolt.join(owner);
        } else {
            bolt.join(getLevel());
        }
    }

    private double getAngleDeg(Vec3 a, Vec3 b) {
        if (a.lengthSqr() > 1e-7 || b.lengthSqr() > 1e-7) {
            return Math.toDegrees(Math.atan2(a.cross(b).length(), a.dot(b)));
        }
        return 0.0;
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        if (context.bottomSupported()) {
            Vec3 v = getVelocity();
            setVelocity(new Vec3(v.x(), 0, v.z()));
        }
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.4, -0.4, -0.4, 0.4, 0.4, 0.4);
    }

        public void dimensionChange() {
        setRemove();
    }
}
