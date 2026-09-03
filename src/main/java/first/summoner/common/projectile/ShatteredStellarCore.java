package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionDamageSource;
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
import java.util.UUID;

public class ShatteredStellarCore extends Projectile implements IEntityCollision<ShatteredStellarCore> {

    private LivingEntity chaseTarget = null;

    public ShatteredStellarCore() {
        super();
    }

    public ShatteredStellarCore(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(0.8f);
        setMaxSpeed(4);
        setMaxLife(100);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.06, -0.06, -0.06, 0.06, 0.06, 0.06);
    }

    @Override
    public boolean isValidCollisionTarget(ShatteredStellarCore entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof MinionDamageSource minionDamageSource) {
            Minion minion = minionDamageSource.getMinion();
            return minion.isTarget(target);
        }
        return target == chaseTarget;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        DamageSource source = getDamageSource();
        if (source != null) {
            UUID uuid = null;
            if (source instanceof MinionDamageSource minionDamageSource && minionDamageSource.getMinion() instanceof Minion minion) {
                uuid = minion.getUuid();
            }
            InvincibleData.attack(target)
                    .attacker(uuid)
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .apply();
        }
        currentPathNode = new PathNode(hit.hitPoint(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (chaseTarget != null && chaseTarget.isAlive()) {
                if (life >= 15) {
                    Vec3 targetCenter = chaseTarget.getBoundingBox().getCenter();
                    applyForce(targetCenter.subtract(getPos()).normalize().scale(0.6));
                }
            } else {
                setRemove();
            }
        }
        super.tick();
    }

    @Override
    public void onRemove() {
        GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                         .centerColor(0x2fb2e1)
                         .edgeColor(0x33ccff);
        ParticleHelper.create(owner.level())
                .generic(genericParticleBuilder
                                 .lifetime(4)
                                 .lifetimeRandom(8)
                                 .spin(0.1f)
                                 .spinRandom(0.25F)
                                 .friction(0.75F)
                                 .scale(0.025f)
                                 .scaleRandom(0.005f)
                )
                .pos(getPos())
                .offset(0.15f)
                .velocity(getVelocity())
                .count(3)
                .speed(0.55)
                .spread(2)
                .emit();
    }

    @Override
    public int getTrailDuration() {
        return 10;
    }

    @Override
    public AttachmentEntityType<ShatteredStellarCore> getType() {
        return SummonerAttachmentEntityRegister.SHATTERED_STELLAR_CORE.get();
    }

    public void setChaseTarget(LivingEntity chaseTarget) {
        this.chaseTarget = chaseTarget;
    }
}
