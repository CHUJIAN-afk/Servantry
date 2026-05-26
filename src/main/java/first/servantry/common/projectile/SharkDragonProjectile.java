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
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SharkDragonProjectile extends Projectile implements ICollideAttack<SharkDragonProjectile>, IBlockCollision<SharkDragonProjectile> {

    public SharkDragonProjectile() {
        super();
    }

    public SharkDragonProjectile(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1f);
        setGravity(-0.002f);
        setMaxLife(100);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        currentPathNode = new PathNode(context.position(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.6, 0.2, 0.2, 0.6);
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
        currentPathNode = new PathNode(hit.hitPoint().add(getVelocity().scale(0.5)), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public void onRemove() {
        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                        .color(0xca1214)
                        .edgeColor(0xa21011)
                        .colorRandom(0.2F, 0.2F, 0.0F)
                        .lifetime(4)
                        .lifetimeRandom(8)
                        .spin(0.1f)
                        .spinRandom(0.25F)
                        .friction(0.75F)
                        .scale(0.025f)
                        .scaleRandom(0.005f)
                )
                .pos(getPos())
                .velocity(getVelocity())
                .count(5)
                .speed(0.55)
                .spread(0.25)
                .emit();
    }

    @Override
    public boolean isValidCollisionTarget(SharkDragonProjectile entity, LivingEntity target) {
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
        return damage != 0 ? damage : 5f;
    }

    @Override
    public AttachmentEntityType<SharkDragonProjectile> getType() {
        return AttachmentEntityRegister.SharkDragonProjectile.get();
    }
}
