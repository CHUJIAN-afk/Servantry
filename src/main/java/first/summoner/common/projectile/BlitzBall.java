package first.summoner.common.projectile;


import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.SummonerStreamCodecs;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlitzBall extends Projectile {

    private final Set<Integer> idList = new HashSet<>();

    public BlitzBall() {
        super(SummonerAttachmentEntityRegister.BLITZ_BALL);
        setDrag(1);
        setGravity(0);
        setMaxTickCount(40);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(SummonerStreamCodecs.SET_INT, this::getIdList, value -> {
            idList.clear();
            idList.addAll(value);
        });
    }

    @Override
    public void tick() {
        super.tick();

        idList.clear();
        TargetCache targetCache = getTargetCache();
        List<LivingEntity> entities = targetCache.getEntitiesInRadius(getPos(), 3, living -> targetCache.isTarget(living));
        for (LivingEntity living : entities) {
            idList.add(living.getId());
            InvincibleData.attack(living)
                    .attacker(getUuid())
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .invincibleTime(3)
                    .apply();
            ParticleHelper.create(getLevel())
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

    @Override
    public void onRemove() {
        ParticleHelper.create(getLevel())
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

    public Set<Integer> getIdList() {
        return idList;
    }
}
