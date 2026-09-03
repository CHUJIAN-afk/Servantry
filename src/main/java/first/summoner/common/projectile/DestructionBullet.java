package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DestructionBullet extends Projectile implements IEntityCollision<DestructionBullet>, IBlockCollision<DestructionBullet> {

    public DestructionBullet() {
        super();
    }

    public DestructionBullet(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1);
        setMaxSpeed(4);
        setMaxLife(40);
    }

    @Override
    public void onRemove() {
        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                                 .centerColor(0xff3d00)
                                 .edgeColor(0xd22f00)
                                 .lifetime(5)
                                 .lifetimeRandom(5)
                                 .spin(0.3f)
                                 .spinRandom(0.05F)
                                 .friction(0.75F)
                                 .scale(0.025f)
                                 .scaleRandom(0.005f))
                .pos(getPos())
                .offset(0.15)
                .velocity(getVelocity())
                .count(4)
                .speed(0.15)
                .spread(2)
                .emit();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.05, -0.05, -0.05, 0.05, 0.05, 0.05);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        currentPathNode = new PathNode(context.position(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.05, -0.05, -1, 0.05, 0.05, 0);
    }

    @Override
    public boolean isValidCollisionTarget(DestructionBullet entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof MinionDamageSource minionDamageSource) {
            Minion minion = minionDamageSource.getMinion();
            return minion.isTarget(target);
        }
        return IEntityCollision.super.isValidCollisionTarget(entity, target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            HitContext hitContext = hitContexts.getFirst();
            LivingEntity living = hitContext.entity();
            InvincibleData.attack(living)
                    .attacker(getUuid())
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .invincibleTime(20)
                    .effect(new MobEffectInstance(SummonerMobEffectRegister.ArmorCrunch, 100))
                    .apply();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xff3d00)
                                     .edgeColor(0xd22f00)
                                     .lifetime(5)
                                     .lifetimeRandom(5)
                                     .spin(0.3f)
                                     .spinRandom(0.05F)
                                     .friction(0.75F)
                                     .scale(0.025f)
                                     .scaleRandom(0.005f))
                    .pos(hitContext.hitPoint())
                    .offset(0.1)
                    .velocity(getVelocity())
                    .count(6)
                    .speed(0.75)
                    .spread(0.25)
                    .emit();
            currentPathNode = new PathNode(hitContext.hitPoint(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
            setRemove();
        }
    }

    @Override
    public AttachmentEntityType<? extends Projectile> getType() {
        return SummonerAttachmentEntityRegister.DESTRUCTION_BULLET.get();
    }
}
