package first.summoner.common.projectile;


import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DestructionBullet extends Projectile implements IEntityCollision<DestructionBullet>, IBlockCollision<DestructionBullet> {

    public DestructionBullet() {
        super(SummonerAttachmentEntityRegister.DESTRUCTION_BULLET);
        setDrag(1);
        setGravity(0);
        setMaxTickCount(100);
    }

    @Override
    public void onRemove() {
        ParticleHelper.create(getLevel())
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
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.05, -0.05, -0.5, 0.05, 0.05, 0);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        HitContext hitContext = hitContexts.getFirst();
        LivingEntity living = hitContext.entity();
        InvincibleData.attack(living)
                .attacker(getUuid())
                .damageSource(source)
                .damageAmount(getDamage())
                .invincibleTime(20)
                .effect(new MobEffectInstance(SummonerMobEffectRegister.ArmorCrunch, 100))
                .apply();
        ParticleHelper.create(getLevel())
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
        currentPathNode = currentPathNode.modifyPos(hitContext.hitPoint());
        setRemove();
    }
}
