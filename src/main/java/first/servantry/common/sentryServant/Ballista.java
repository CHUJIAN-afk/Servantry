package first.servantry.common.sentryServant;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.common.projectile.CrossbowBoltProjectile;
import first.servantry.register.ArmorSetRegister;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.MobEffectRegister;
import first.servantry.register.SoundRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Ballista extends MomentumServant implements IBlockCollision<Ballista> {

    private int level = 1;
    private int aiming = 0;
    private int cooldown = 0;

    public Ballista() {
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
            if (isTargetChange()) {
                aiming = 0;
            }
            if (isTarget(target)) {
                lookAtPos(target.getBoundingBox().getCenter());
                aiming++;
                if (cooldown < 0 && aiming > 10) {
                    cooldown = 53;
                    boolean full = ArmorSetRegister.ValhallaKnight.value().full(owner);
                    if (full) {
                        cooldown -= 20;
                    }
                    boolean hasEffect = owner.hasEffect(MobEffectRegister.BallistaPanicked);
                    if (hasEffect) {
                        cooldown -= 13;
                    }
                    if (full && hasEffect) {
                        cooldown -= 10;
                    }
                    fire();
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

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(level);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        level = buf.readInt();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void fire() {
        Vec3 direction = getLookAngle();
        Vec3 pos = getPos();
        CrossbowBoltProjectile projectile = new CrossbowBoltProjectile(getDamageSource(), pos, direction);
        projectile.copyDamageData(this);
        if (ArmorSetRegister.ValhallaKnight.value().full(owner)) {
            projectile.setMaxPierceCount(6);
            projectile.setDamage(projectile.getDamage() * 1.33f);
            direction = direction.scale(1.33f);
        }
        projectile.setVelocity(direction.scale(2));
        projectile.join(owner);
        owner.level().playSound(null, pos.x(), pos.y(), pos.z(), SoundRegister.BallistaShot.get(), owner.getSoundSource());
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return AttachmentEntityRegister.Ballista.get();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }
}
