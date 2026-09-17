package first.summoner.common.projectile;


import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
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
        super(SummonerAttachmentEntityRegister.MINI_RAINBOW_CRYSTAL);
        setDrag(1);
        setMaxTickCount(20);
    }

    public int getLife() {
        return getTickCount();
    }

    @Override
    public void tick() {
        super.tick();

        if (getTickCount() > 0 && getTickCount() % 9 == 0) {
            Vec3 pos = getPos();
            Vec3 direction = pos.subtract(pos.offsetRandom(getRandom(), 1)).normalize();
            ParticleHelper.create(getLevel())
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
            DamageSource damageSource = getDamageSource();
            if (damageSource != null) {
                TargetCache targetCache = getTargetCache();
                List<LivingEntity> entities = targetCache.getEntitiesInRadius(pos, 3.5, living -> targetCache.isTarget(living));
                for (LivingEntity living : entities) {
                    InvincibleData.attack(living)
                            .attacker(getUuid())
                            .damageSource(damageSource)
                            .damageAmount(getDamage())
                            .apply();
                }
            }
        }
    }

    public int getColor(float partialTick) {
        float t = (float) ((Math.toRadians(getUuid().hashCode() + tickCount + partialTick) % (Math.PI * 2)) / (Math.PI * 2));
        float r = 0.5f + 0.5f * (float) Math.sin(t * Math.PI * 2);
        float g = 0.5f + 0.5f * (float) Math.sin(t * Math.PI * 2 - Math.PI * 2 / 3);
        float b = 0.5f + 0.5f * (float) Math.sin(t * Math.PI * 2 - Math.PI * 4 / 3);
        return FastColor.ARGB32.color(255, (int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    @Override
    public void tickPhysics() {
    }
}
