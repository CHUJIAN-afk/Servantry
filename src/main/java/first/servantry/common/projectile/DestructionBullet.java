package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.entity.PathNode;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.MobEffectRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DestructionBullet extends Projectile implements ICollideAttack<DestructionBullet>, IBlockCollision<DestructionBullet> {

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
                                 .color(0xff3d00)
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
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            return servant.isTarget(target);
        }
        return ICollideAttack.super.isValidCollisionTarget(entity, target);
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
                    .effect(new MobEffectInstance(MobEffectRegister.ArmorCrunch, 100))
                    .apply();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .color(0xff3d00)
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
        return AttachmentEntityRegister.DestructionBullet.get();
    }
}
