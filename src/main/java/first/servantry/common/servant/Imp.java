package first.servantry.common.servant;

import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.servant.MomentumServant;
import first.lyra.common.servant.ServantGoalSelector;
import first.lyra.common.sound.Playable;
import first.servantry.common.projectile.ImpFireball;
import first.servantry.common.servant.goal.MomentumServantIdleGoal;
import first.servantry.common.servant.goal.imp.ImpAttackGoal;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantrySoundRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 小鬼仆从 - 在目标周围盘旋，发射穿透火球。
 */
public class Imp extends MomentumServant implements IBlockCollision<Imp> {

    public Imp() {
        super();
        setDrag(0.75f);
        setGravity(0);
        setRotationSpeed(10f);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new ImpAttackGoal(this));
        goalSelector.addGoal(1, new MomentumServantIdleGoal(this, 4, 0.02f, 48, true));
    }

    @Override
    public void tick() {
        Level level = owner.level();
        if (!level.isClientSide()){
            ParticleHelper.create(level)
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xe1c316)
                                     .edgeColor(0xff7506)
                                     .lifetime(6)
                                     .lifetimeRandom(4)
                                     .spin(0.25f)
                                     .spinRandom(0.25F)
                                     .scale(0.015f)
                                     .scaleRandom(0.015f)
                    )
                    .pos(getPos())
                    .offset(0.2)
                    .emit();
        }
        super.tick();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public void lookAtDirection(Vec3 direction) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        setDesiredRotation(targetYaw, 0, getRoll());
    }

    /**
     * 向目标发射火球射弹。
     */
    public void shootFireballAt(LivingEntity target) {
        Vec3 start = getPos();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 direction = targetCenter.subtract(start).normalize();

        ImpFireball fireball = new ImpFireball(getDamageSource(), start.add(direction.scale(-1)), direction);
        fireball.join(owner);
        Playable.play(ServantrySoundRegister.ImpFire, owner.level(), start, owner.getSoundSource());
    }

    public int getFireballCooldown() {
        return 20 + owner.getRandom().nextIntBetweenInclusive(0, 6);
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return ServantryAttachmentEntityRegister.IMP.get();
    }
}
