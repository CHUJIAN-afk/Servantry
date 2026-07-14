package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantryAttachmentRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlitzBall extends Projectile {

    private final Set<Integer> idList = new HashSet<>();

    public BlitzBall() {
        super();
    }

    public BlitzBall(DamageSource damageSource, Vec3 startPos, Vec3 direction){
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1f);
        setMaxSpeed(1);
        setMaxLife(40);
    }

    @Override
    public void tick() {
        Level level = owner.level();
        if (!level.isClientSide() && damageSource != null) {
            idList.clear();
            List<LivingEntity> entities = owner.getData(ServantryAttachmentRegister.TargetCache).getEntities();
            for (LivingEntity living : entities) {
                if (living.getBoundingBox().getCenter().distanceToSqr(getPos()) < 9 + living.getBoundingBox().getSize() * 0.5) {
                    boolean isTarget = true;
                    if (damageSource instanceof ServantDamageSource servantDamageSource) {
                        Servant servant = servantDamageSource.getServant();
                        isTarget = servant.isTarget(living);
                    }
                    if (isTarget) {
                        idList.add(living.getId());
                        InvincibleData.attack(living)
                                .attacker(getUuid())
                                .damageSource(damageSource)
                                .damageAmount(getDamage())
                                .invincibleTime(3)
                                .apply();
                    }
                }
            }
        }
        if (level.isClientSide()){
            for (Integer id : idList) {
                if (level.getEntity(id) instanceof LivingEntity living) {
                    ParticleHelper.create(owner.level())
                            .generic(GenericParticleBuilder.create()
                                             .color(0x38ffec)
                                             .edgeColor(0x2fc1ae)
                                             .lifetime(5)
                                             .lifetimeRandom(5)
                                             .spin(0.3f)
                                             .spinRandom(0.05F)
                                             .friction(0.75F)
                                             .scale(0.025f)
                                             .scaleRandom(0.005f)
                            )
                            .pos(living.getBoundingBox().getCenter())
                            .offset(0.15)
                            .velocity(getVelocity())
                            .count(1)
                            .speed(0.25)
                            .spread(2)
                            .emit();
                }
            }
        }
        super.tick();
    }

    @Override
    public void onRemove() {
        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                                 .color(0x38ffec)
                                 .edgeColor(0x2fc1ae)
                                 .lifetime(5)
                                 .lifetimeRandom(5)
                                 .spin(0.3f)
                                 .spinRandom(0.05F)
                                 .friction(0.75F)
                                 .scale(0.025f)
                                 .scaleRandom(0.005f)
                )
                .pos(getPos())
                .offset(0.15)
                .velocity(getVelocity())
                .count(4)
                .speed(0.25)
                .spread(2)
                .emit();
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.BlitzBall.get();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(idList.size());
        for (Integer id : idList) {
            buf.writeInt(id);
        }
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        idList.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            idList.add(buf.readInt());
        }
    }

    public Set<Integer> getIdList() {
        return idList;
    }
}
