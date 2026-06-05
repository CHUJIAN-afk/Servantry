package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.*;
import first.servantry.api.projectile.Projectile;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ZenithProjectile extends Projectile implements ICollideAttack<ZenithProjectile> {

    public Vec3 lastOwnerPos = Vec3.ZERO;
    public int progress = 0;

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
            applyPositionCorrection();
            if (!isExecutingPath()) {
                setRemove();
            }
        }
        lastOwnerPos = owner.getPosition(1);
        super.tick();
    }

    @Override
    protected void tickPhysics() {
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return AttachmentEntityRegister.ZenithProjectile.get();
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

    /**
     * 位置修正机制
     */
    private void applyPositionCorrection() {
        Vec3 last = lastOwnerPos;
        if (!last.equals(Vec3.ZERO)) {
            Vec3 current = owner.getPosition(1);
            Vec3 offset = current.subtract(last);
            if (offset.lengthSqr() > 1e-5) {
                PlannedPath path = getCurrentPath();
                if (path != null) {
                    List<PathNode> nodes = path.getNodes();
                    int startIdx = path.getCurrentIndex();
                    int remaining = nodes.size() - startIdx;
                    for (int i = 0; i < remaining; i++) {
                        PathNode node = nodes.get(startIdx + i);
                        float weight = (float) (i + 1) / remaining;
                        Vec3 blendedOffset = offset.scale(weight);
                        Vec3 pos = node.pos();
                        nodes.set(startIdx + i, new PathNode(pos.add(blendedOffset), node.yaw(), node.pitch(), node.roll()));
                    }
                }
            }
        }
    }
}
