package first.servantry.common.sentryServant;

import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.servant.MomentumServant;
import first.lyra.common.sound.Playable;
import first.servantry.common.projectile.DestructionBullet;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantrySoundRegister;
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
    public int getSearchDistance() {
        return 32;
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
        Vec3 start = getPos();
        Vec3 direction = start.add(getLookAngle()).offsetRandom(owner.getRandom(), 0.05f).subtract(getPos()).normalize();
        DestructionBullet projectile = new DestructionBullet(getDamageSource(), start, direction);
        projectile.join(owner);
        Playable.play(ServantrySoundRegister.BallistaShot, owner.level(), start, owner.getSoundSource());
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.PULSE_TURRET.get();
    }

    @Override
    public void dimensionChange() {
        setRemove();
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