package first.summoner.common.projectile;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.FastColor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MiniRainbowCrystal extends Projectile {

    public MiniRainbowCrystal(){
        super();
    }

    public MiniRainbowCrystal(DamageSource damageSource, PathNode pathNode) {
        super(pathNode.pos(), Vec3.directionFromRotation(pathNode.pitch(), pathNode.yaw()));
        setDamageSource(damageSource);
        setDrag(1);
        setMaxSpeed(0);
        setMaxLife(20);
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (life > 0 && life % 9 == 0) {
                Vec3 pos = getPos();
                Vec3 direction = pos.subtract(pos.offsetRandom(owner.getRandom(), 1)).normalize();
                ParticleHelper.create(owner.level())
                        .generic(GenericParticleBuilder.create()
                                         .centerColor(getColor(0))
                                         .edgeColor(getColor(0))
                                         .lifetime(5)
                                         .lifetimeRandom(5)
                                         .spin(0.1f)
                                         .spinRandom(0.05F)
                                         .friction(0.75F)
                                         .scale(0.06f)
                                         .scaleRandom(0.006f)
                        )
                        .pos(pos)
                        .offset(0.5)
                        .velocity(direction)
                        .count(100)
                        .speed(0.5)
                        .spread(2)
                        .emit();
                if (damageSource instanceof MinionDamageSource minionDamageSource) {
                    Minion minion = minionDamageSource.getMinion();
                    List<LivingEntity> entities = LyraHelper.get(owner).getTargetCache().getEntitiesInRadius(pos, 3.5, minion::isTarget);
                    for (LivingEntity living : entities) {
                        InvincibleData.attack(living)
                                .attacker(uuid)
                                .damageSource(damageSource)
                                .damageAmount(getDamage())
                                .apply();
                    }
                }
            }
        }
        super.tick();
    }

    public int getColor(float partialTick) {
        float t = (float) ((Math.toRadians(getUuid().hashCode() + tickCount + partialTick) % (Math.PI * 2)) / (Math.PI * 2));
        float r = 0.5f + 0.5f * (float) Math.sin(t * Math.PI * 2);
        float g = 0.5f + 0.5f * (float) Math.sin(t * Math.PI * 2 - Math.PI * 2 / 3);
        float b = 0.5f + 0.5f * (float) Math.sin(t * Math.PI * 2 - Math.PI * 4 / 3);
        return FastColor.ARGB32.color(255, (int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    @Override
    protected void tickPhysics() {
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return SummonerAttachmentEntityRegister.MINI_RAINBOW_CRYSTAL.get();
    }
}
