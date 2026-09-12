package first.summoner.common.minion.sentry;

import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.sound.Playable;
import first.summoner.common.projectile.DestructionBullet;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PulseTurret extends MomentumMinion implements IBlockCollision<PulseTurret> {

    private int cooldown = 0;

    public PulseTurret() {
        super(SummonerAttachmentEntityRegister.PULSE_TURRET);
        setGravity(-0.05f);
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
                        cooldown = 10 + getRandom().nextInt(-1, 1);
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
        Vec3 direction = start.add(getLookAngle()).offsetRandom(getRandom(), 0.05f).subtract(getPos()).normalize();
        DestructionBullet projectile = new DestructionBullet();
        projectile.copyDamageSource(this);
        projectile.setPos(start);
        projectile.setVelocity(direction);
        projectile.join(getLevel());
        if (owner != null) {
            Playable.play(SummonerSoundRegister.BallistaShot, getLevel(), start, owner.getSoundSource());
        } else {
            Playable.play(SummonerSoundRegister.BallistaShot, getLevel(), start, net.minecraft.sounds.SoundSource.PLAYERS);
        }
    }

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
