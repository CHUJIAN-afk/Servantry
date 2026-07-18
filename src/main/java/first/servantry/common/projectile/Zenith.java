package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.*;
import first.servantry.api.projectile.Projectile;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Zenith extends Projectile implements ICollideAttack<Zenith> {

    public LivingEntity chaseTarget = null;
    public Vec3 direction = Vec3.ZERO;
    public Vec3 normal = Vec3.ZERO;
    public float curvature = 1;
    public float lastProgress = 0;
    public float progress = 0;

    public Zenith() {
        super();
    }

    public Zenith(DamageSource damageSource) {
        super(Vec3.ZERO, null);
        setDamageSource(damageSource);
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            updateDirection(chaseTarget, 1);
            setCurrentPathNode(getNode(progress, 1));
            if (progress >= 1) {
                setRemove();
            }
        } else if (getHistoryNodes().size() > 2) {
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xffffff)
                                     .edgeColor(0xb7b7b7)
                                     .lifetime(30)
                                     .lifetimeRandom(5)
                                     .spin(3)
                                     .spinRandom(0.1F)
                                     .friction(0.7F)
                                     .scale(0.04f)
                                     .scaleRandom(0.004f)
                    )
                    .pos(getPos())
                    .offset(0.15)
                    .velocity(getCurrentVelocity().normalize())
                    .count(1)
                    .speed(2)
                    .spread(0.05)
                    .emit();
        }
        lastProgress = progress;
        progress = Math.min(progress + (float) 1 / 14, 1);
        super.tick();
    }

    public void updateDirection(LivingEntity chaseTarget, float partialTick) {
        if (chaseTarget != null && chaseTarget.isAlive()) {
            Vec3 center = owner.getPosition(partialTick).add(0, owner.getBbHeight() / 2, 0);
            Vec3 endPos = chaseTarget.getPosition(partialTick).add(0, chaseTarget.getBbHeight() / 2, 0);
            direction = endPos.subtract(center);
        }
    }

    public PathNode getNode(float progress, float partialTick) {
        Vec3 center = owner.getPosition(partialTick).add(0, owner.getBbHeight() / 2, 0);
        Vec3 endPos = center.add(direction);
        Vec3 startPos = center.add(direction.normalize().scale(-1));
        Ellipse ellipse = new Ellipse(endPos, startPos, normal, curvature);
        Vec3 point = ellipse.getPoint(progress);
        Vec3 ellipseCenter = ellipse.getCenter();
        Vec3 tipDir = point.subtract(ellipseCenter).normalize();
        return getEulerNode(point, tipDir, normal);
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeVec3(direction);
        buf.writeVec3(normal);
        buf.writeFloat(curvature);
        buf.writeInt(chaseTarget != null ? chaseTarget.getId() : -1);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        direction = buf.readVec3();
        normal = buf.readVec3();
        curvature = buf.readFloat();
        int targetId = buf.readInt();
        if (targetId != -1 && owner != null && owner.level().getEntity(targetId) instanceof LivingEntity living) {
            chaseTarget = living;
        }
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.ZENITH.get();
    }

    @Override
    protected void tickPhysics() {
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        PathNode renderNode = super.getRenderNode(partialTick);
        if (chaseTarget != null && chaseTarget.isAlive()) {
            updateDirection(chaseTarget, partialTick);
        }
        if (!direction.equals(Vec3.ZERO)) {
            return getNode(Mth.lerp(partialTick, lastProgress, progress), partialTick);
        }
        return renderNode;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.6, -0.1, -1, 0.6, 0.1, 1.5);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hit : hitContexts) {
                LivingEntity living = hit.entity();
                InvincibleData.attack(living)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .invincibleTime(2)
                        .apply();
            }
        }
    }
}
