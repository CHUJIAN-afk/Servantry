package first.summoner.common.projectile;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MiniChlorophyteCrystal extends Projectile implements IEntityCollision<MiniChlorophyteCrystal> {

    public MiniChlorophyteCrystal() {
        super(SummonerAttachmentEntityRegister.MINI_CHLOROPHYTE_CRYSTAL);
        setDrag(1f);
        setMaxLife(20);
    }

    public int getTrailDuration() {
        return 15;
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            currentPathNode = new PathNode(currentPathNode.pos(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll() + 30);
        }
        super.tick();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            TargetCache targetCache = LyraHelper.get(owner).getTargetCache();
            HitContext context = hitContexts.getFirst();
            LivingEntity target = context.entity();
            Vec3 pos = target.getBoundingBox().getCenter();
            List<LivingEntity> list = targetCache.getEntitiesInRadius(pos, 2.5, living -> isValidCollisionTarget(this, living));
            for (LivingEntity living : list) {
                InvincibleData.attack(living)
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .apply();
            }
            setCurrentPathNode(getCollisionAfterPathNode(context.hitPoint(), getCurrentPathNode()));
            setRemove();
            ParticleHelper.create(getLevel())
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0x1bff10)
                            .edgeColor(0x17b70e)
                            .lifetime(6)
                            .lifetimeRandom(6)
                            .spin(0.1f)
                            .spinRandom(0.5F)
                            .friction(0.75F)
                            .scale(0.025f)
                            .scaleRandom(0.005f)
                    )
                    .pos(pos)
                    .offset(0.05)
                    .velocity(getVelocity())
                    .count(30)
                    .speed(0.85)
                    .spread(Math.TAU)
                    .emit();
        }
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.08, -0.08, -0.2, 0.08, 0.08, 0.2);
    }
}
