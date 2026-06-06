package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.*;
import first.servantry.api.projectile.Projectile;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ZenithProjectile extends Projectile implements ICollideAttack<ZenithProjectile> {

    public LivingEntity chaseTarget = null;
    public Vec3 direction = Vec3.ZERO;
    public Vec3 normal = Vec3.ZERO;
    public float curvature = 1;
    public float lastProgress = 0;
    public float progress = 0;

    public ZenithProjectile() {
        super();
    }

    public ZenithProjectile(DamageSource damageSource) {
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
        }
        lastProgress = progress;
        progress = Math.min(progress + (float) 1 / 10, 1);
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
        return AttachmentEntityRegister.ZenithProjectile.get();
    }

    @Override
    protected void tickPhysics() {
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.6, -0.1, -1, 0.6, 0.1, 1.5);
    }

    @Override
    public float getDamage() {
        return 19;
    }

    @Override
    public float getKnockback() {
        return 0.65f;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hit : hitContexts) {
                LivingEntity living = hit.entity();
                InvincibleData.criteriaAttack(living, getUuid(), 2, source, getDamage(), InvincibleData.Type.PARTIAL);
            }
        }
    }
}
