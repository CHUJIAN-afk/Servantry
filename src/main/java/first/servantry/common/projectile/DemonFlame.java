package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.*;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantryMobEffectRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DemonFlame extends Projectile implements ICollideAttack<DemonFlame>, IBlockCollision<DemonFlame> {

    public DemonFlame() {
        super();
    }

    public DemonFlame(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(0.92f);
        setMaxSpeed(4);
        setMaxLife(7);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hitContext : hitContexts) {
                LivingEntity target = hitContext.entity();
                InvincibleData.attack(target)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .invincibleTime(10)
                        .effect(new MobEffectInstance(ServantryMobEffectRegister.CursedFlame, 100, 0))
                        .apply();
            }
        }
    }

    @Override
    public boolean isValidCollisionTarget(DemonFlame entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            return servant.isTarget(target);
        }
        return false;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.DEMON_FLAME.get();
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
