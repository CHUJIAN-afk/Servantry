package first.servantry.common.sentryServant;

import first.servantry.api.common.sound.Playable;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.common.projectile.Corn;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.Ballistics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 玉米加农炮哨兵 - 不移动，有重力，35秒冷却，发射抛物线玉米炮精准命中目标。
 */
public class Cannon extends MomentumServant implements IBlockCollision<Cannon> {

    /** 冷却计时器，初始700（召唤时视为刚发射过） */
    private int cooldown = 700;

    public Cannon() {
        setGravity(-0.05f);
        setRotationSpeed(18f);
    }

    @Override
    public int getSearchDistance() {
        return 64;
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
                fire(target);
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

    @Override
    public void dimensionChange() {
        setRemove();
    }

    public void fire(LivingEntity target) {
        Vec3 start = getPos();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        if (cooldown <= 0) {
            Vec3 velocity = Ballistics.solveVelocity(start, targetCenter, 85f, 0.99f, -0.05f);
            if (velocity != null) {
                Corn projectile = new Corn(getDamageSource(), start, velocity);
                cooldown = 700;
                projectile.join(owner);
                Playable.play(SoundEvents.GENERIC_EXPLODE, owner.level(), start, owner.getSoundSource());
            }
        }
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.Cannon.get();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }
}
