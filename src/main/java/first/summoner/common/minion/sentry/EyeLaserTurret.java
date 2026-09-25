package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.projectile.EyeFireball;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class EyeLaserTurret extends MomentumMinion implements IBlockCollision<EyeLaserTurret> {

    private int cooldown = 0;

    public EyeLaserTurret() {
        super(SummonerAttachmentEntityRegister.EYE_LASER_TURRET);
    }

    @Override
    public int getSearchDistance() {
        return 16;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = getTarget();
        if (target != null) {
            lookAtPos(target.getBoundingBox().getCenter());
            if (getCooldown() <= 0) {
                shootFireball(target);
            }
        } else {
            if (getPos().distanceTo(getOwner().position()) < 6.0) {
                lookAtPos(getOwner().getEyePosition());
            }
        }
        if (cooldown > 0) {
            cooldown--;
        }
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * 朝目标发射一颗火球，并在炮口喷出淡红色粒子。
     */
    public void shootFireball(LivingEntity target) {
        Vec3 start = getPos();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        EyeFireball fireball = new EyeFireball();
        fireball.setOwner(getOwner());
        fireball.init(new PathNode(start, direction));
        fireball.copyAttributes(this);
        fireball.setVelocity(direction.scale(1.0));
        LyraHelper.get(getOwner()).add(fireball);
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xFF6B6B)
                        .edgeColor(0xFF2A2A)
                        .lifetime(6)
                        .lifetimeRandom(5)
                        .spin(0.2F)
                        .spinRandom(0.1F)
                        .friction(0.7F)
                        .scale(0.03F)
                        .scaleRandom(0.008F)
                )
                .pos(start)
                .offset(0.15)
                .velocity(direction)
                .count(4)
                .speed(0.5)
                .spread(0.3)
                .emit();
        setCooldown(30);
    }

    /**
     * 碰撞箱覆盖整个双部件模型：原点位于基座顶部，模型向下延伸约 1.7 格。
     */
    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.25, -1.7, -0.25, 0.25, 0.25, 0.25);
    }

    public void dimensionChange() {
        setRemove();
    }
}
