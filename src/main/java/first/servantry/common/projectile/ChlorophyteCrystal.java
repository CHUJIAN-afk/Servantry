package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.entity.PathNode;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ChlorophyteCrystal extends Projectile implements ICollideAttack<ChlorophyteCrystal> {

    public ChlorophyteCrystal() {
        super();
    }

    public ChlorophyteCrystal(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1f);
        setMaxSpeed(4);
        setMaxLife(20);
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            currentPathNode = new PathNode(currentPathNode.pos(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll() + 30);
        }
        super.tick();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            UUID uuid = null;
            if (source instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof Servant servant) {
                uuid = servant.getUuid();
            }
            HitContext context = hitContexts.getFirst();
            LivingEntity target = context.entity();
            InvincibleData.attack(target)
                    .attacker(uuid)
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .apply();
            Vec3 pos = getPos();
            Vec3 direction = getVelocity();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                            .color(0x1bff10)
                            .edgeColor(0x17b70e)
                            .colorRandom(0.2F, 0.2F, 0.0F)
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
                    .velocity(direction)
                    .count(30)
                    .speed(0.85)
                    .spread(2)
                    .emit();
            currentPathNode = new PathNode(context.hitPoint(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
            setRemove();
        }
    }

    @Override
    public boolean isValidCollisionTarget(ChlorophyteCrystal entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            return servant.isTarget(target);
        }
        return false;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.ChlorophyteCrystalProjectile.get();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.08, -0.08, -0.2, 0.08, 0.08, 0.2);
    }
}
