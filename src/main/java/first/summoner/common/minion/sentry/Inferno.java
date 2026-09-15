package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.projectile.ExplosionFlameBall;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 爆炸烈焰哨兵 - 具有三个等级，朝敌人发射爆炸烈焰火球。
 * <p>
 * 行为：
 * - 不移动，落地后静止
 * - 等级 1/2/3，发射间隔 1.73/1.7/1.53 秒（随等级降低）
 * - 无法以俯仰角大于 45° 的敌人为目标
 * - 射击前检查瞄准，对准后才开火
 */
public class Inferno extends MomentumMinion implements IBlockCollision<Inferno> {

    private int level = 1;
    private int aiming = 0;
    private int cooldown = 0;

    public Inferno() {
        super(SummonerAttachmentEntityRegister.INFERNO);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getSentryLevel, this::setSentryLevel);
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    private boolean isValidTarget(LivingEntity target) {
        if (target == null) {
            return false;
        }
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(getPos());
        double pitch = Math.toDegrees(Math.atan2(toTarget.y, Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z)));
        return Math.abs(pitch) <= 45;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getLevel().isClientSide()) {
            LivingEntity target = getTarget();
            cooldown--;
            if (isTargetChange()) {
                aiming = 0;
            }
            if (isValidTarget(target)) {
                lookAtPos(target.getBoundingBox().getCenter());
                aiming++;
                if (cooldown < 0 && aiming > 10) {
                    cooldown = switch (level) {
                        case 1 -> 35;
                        case 2 -> 34;
                        default -> 31;
                    };
                    Vec3 targetDirection = target.getBoundingBox().getCenter().subtract(getPos());
                    Vec3 lookAngle = getLookAngle();
                    if (getAngleDeg(targetDirection, lookAngle) < 5) {
                        fire(target.getBoundingBox().getCenter());
                    }
                }
            }
        }
    }

    private void fire(Vec3 targetPos) {
        Vec3 start = getPos();
        Vec3 direction = targetPos.subtract(start).normalize();
        ExplosionFlameBall ball = new ExplosionFlameBall();
        ball.setOwner(owner);
        ball.init(new PathNode(start, direction));
        ball.copyAttributes(this);
        ball.setVelocity(direction);
        LyraHelper.get(getLevel()).add(ball);
    }

    private double getAngleDeg(Vec3 a, Vec3 b) {
        if (a.lengthSqr() > 1e-7 || b.lengthSqr() > 1e-7) {
            return Math.toDegrees(Math.atan2(a.cross(b).length(), a.dot(b)));
        }
        return 0.0;
    }

    public void dimensionChange() {
        setRemove();
    }

    public int getSentryLevel() {
        return level;
    }

    public void setSentryLevel(int level) {
        this.level = level;
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.4, -0.4, -0.4, 0.4, 0.4, 0.4);
    }
}
