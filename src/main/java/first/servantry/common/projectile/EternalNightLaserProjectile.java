package first.servantry.common.projectile;

import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class EternalNightLaserProjectile extends Projectile implements ICollideAttack<EternalNightLaserProjectile>, IBlockCollision<EternalNightLaserProjectile> {

    private LivingEntity chaseTarget = null;

    public EternalNightLaserProjectile() {
        super();
    }

    public EternalNightLaserProjectile(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(0.92f);
        setMaxSpeed(4);
        setMaxLife(30);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.03, -0.03, -0.06, 0.03, 0.03, 0.06);
    }

    @Override
    public boolean isValidCollisionTarget(EternalNightLaserProjectile entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            if (servant != null) {
                return servant.isTarget(target);
            }
        }
        return false;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        DamageSource source = getDamageSource();
        if (source != null) {
            UUID uuid = null;
            if (source instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof Servant servant) {
                uuid = servant.getUuid();
            }
            InvincibleData.criteriaAttack(target, uuid, 0, source, getDamage(), InvincibleData.Type.PARTIAL);
        }
        currentPathNode = new PathNode(hit.hitPoint(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public float getDamage() {
        float damage = super.getDamage();
        return damage != 0 ? damage : 4f;
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (chaseTarget != null && chaseTarget.isAlive()) {
                if (life >= 10) {
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
    public int getTrailDuration() {
        return 10;
    }

    @Override
    public AttachmentEntityType<EternalNightLaserProjectile> getType() {
        return AttachmentEntityRegister.EternalNightLaserProjectile.get();
    }

    public void setChaseTarget(LivingEntity chaseTarget) {
        this.chaseTarget = chaseTarget;
    }
}
