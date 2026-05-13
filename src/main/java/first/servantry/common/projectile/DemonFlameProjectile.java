package first.servantry.common.projectile;

import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.MobEffectRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class DemonFlameProjectile extends Projectile implements ICollideAttack<DemonFlameProjectile>, IBlockCollision<DemonFlameProjectile> {

    public DemonFlameProjectile() {
        super();
    }

    public DemonFlameProjectile(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1);
        setMaxSpeed(4);
        setMaxLife(6);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            UUID uuid = null;
            if (source instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof Servant servant) {
                uuid = servant.getUuid();
            }
            for (HitContext hitContext : hitContexts) {
                LivingEntity target = hitContext.entity();
                InvincibleData.criteriaAttack(target, uuid, 2, source, getDamage(), InvincibleData.Type.PARTIAL);
                target.addEffect(new MobEffectInstance(MobEffectRegister.CursedFlame, 100, 0));
            }
        }
    }

    @Override
    public boolean isValidCollisionTarget(DemonFlameProjectile entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            if (servant != null) {
                return servant.isTarget(target);
            }
        }
        return false;
    }

    @Override
    public float getDamage() {
        float damage = super.getDamage();
        return damage != 0 ? damage : 2.4f;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return AttachmentEntityRegister.DemonFlameProjectile.get();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        currentPathNode = new PathNode(context.position(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }
}
