package first.servantry.common.sentryServant;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.common.projectile.DestructionBullet;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.SoundRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PulseTurret extends MomentumServant implements IBlockCollision<PulseTurret> {

    private int cooldown = 0;

    public PulseTurret() {
        super();
        setGravity(-0.05f);
        setRotationSpeed(18f);
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (getPos().distanceToSqr(owner.position()) > 128 * 128) {
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
                        cooldown = 10 + owner.getRandom().nextInt(-1, 1);
                        fire();
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        if (context.bottomSupported()) {
            Vec3 v = getVelocity();
            setVelocity(new Vec3(v.x(), 0, v.z()));
        }
    }

    public void fire() {
        Vec3 pos = getPos();
        Vec3 direction = pos.add(getLookAngle()).offsetRandom(owner.getRandom(), 0.05f).subtract(getPos()).normalize();
        DestructionBullet projectile = new DestructionBullet(getDamageSource(), pos, direction);
        projectile.copyDamageData(this);
        projectile.join(owner);
        owner.level().playSound(null, pos.x(), pos.y(), pos.z(), SoundRegister.BallistaShot.get(), owner.getSoundSource());
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return AttachmentEntityRegister.PulseTurret.get();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }

    /**
     * 计算两个向量之间的最小夹角（角度制）。
     * 如果任一向量为零向量，返回 0。
     *
     * @param a 向量 a
     * @param b 向量 b
     * @return 夹角度数，范围 [0, 180]
     */
    public double getAngleDeg(Vec3 a, Vec3 b) {
        if (a.lengthSqr() > 1e-7 || b.lengthSqr() > 1e-7) {
            return Math.toDegrees(Math.atan2(a.cross(b).length(), a.dot(b)));
        }
        return 0.0;
    }
}