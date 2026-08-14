package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.lyra.register.LyraAttachmentRegister;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
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
            List<LivingEntity> entities = owner.getData(LyraAttachmentRegister.TargetCache).getEntities();
            for (LivingEntity living : entities) {
                if (living.getBoundingBox().getCenter().distanceToSqr(getPos()) < 9 + living.getBoundingBox().getSize() * 0.5) {
                    boolean isTarget = true;
                    if (damageSource instanceof MinionDamageSource minionDamageSource) {
                        Minion minion = minionDamageSource.getMinion();
                        isTarget = minion.isTarget(living);
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
                                             .centerColor(0x38ffec)
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
                                 .centerColor(0x38ffec)
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
        return SummonerAttachmentEntityRegister.BLITZ_BALL.get();
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
