package first.servantry.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.ICollideAttack;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.lyra.common.servant.Servant;
import first.lyra.common.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class MiniChlorophyteCrystal extends Projectile implements ICollideAttack<MiniChlorophyteCrystal> {

    public MiniChlorophyteCrystal() {
        super();
    }

    public MiniChlorophyteCrystal(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
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
            GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                    .centerColor(0x1bff10)
                    .edgeColor(0x17b70e);
            ParticleHelper.create(owner.level())
                    .generic(genericParticleBuilder
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
    public boolean isValidCollisionTarget(MiniChlorophyteCrystal entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            return servant.isTarget(target);
        }
        return false;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.MINI_CHLOROPHYTE_CRYSTAL.get();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.08, -0.08, -0.2, 0.08, 0.08, 0.2);
    }
}
