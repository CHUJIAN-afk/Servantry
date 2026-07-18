package first.servantry.common.projectile;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
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
                if (damageSource != null) {
                    List<LivingEntity> entities = ServantryHelper.get(owner).getTargetCache().getEntities();
                    for (LivingEntity living : entities) {
                        if (living.getBoundingBox().getCenter().distanceToSqr(pos) < 9 + living.getBoundingBox().getSize() * 0.5) {
                            boolean isTarget = true;
                            if (damageSource instanceof ServantDamageSource servantDamageSource) {
                                Servant servant = servantDamageSource.getServant();
                                isTarget = servant.isTarget(living);
                            }
                            if (isTarget) {
                                InvincibleData.attack(living)
                                        .attacker(uuid)
                                        .damageSource(damageSource)
                                        .damageAmount(getDamage())
                                        .apply();
                            }
                        }
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
        return FastColor.ARGB32.colorFromFloat(1, r, g, b);
    }

    @Override
    protected void tickPhysics() {
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.MiniRainbowCrystalProjectile.get();
    }
}
