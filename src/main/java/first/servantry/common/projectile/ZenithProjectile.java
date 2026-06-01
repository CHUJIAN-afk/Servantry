package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.*;
import first.servantry.api.projectile.Projectile;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ZenithProjectile extends Projectile implements ICollideAttack<ZenithProjectile> {

    public float lerp = 0;
    private LivingEntity chaseEnd = null;
    private Vec3 startPos = null;
    private Vec3 endPos = null;

    public ZenithProjectile() {
        super();
    }

    public ZenithProjectile(DamageSource damageSource, Vec3 startPos, Vec3 endPos) {
        super(Vec3.ZERO, null);
        setDamageSource(damageSource);
        this.startPos = startPos;
        this.endPos = endPos;
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (isValidCollisionTarget(this, chaseEnd)) {
                endPos = chaseEnd.getBoundingBox().getCenter();
            }
            Vec3 center = owner.getBoundingBox().getCenter();
            startPos = center.add(center.subtract(endPos).normalize());
            lerp += 0.05f;
            PathNode node = getEllipseSlashNode(Math.min(lerp, 1));
            setCurrentPathNode(node);
            if (lerp >= 1.1) {
                setRemove();
            }
        }
        super.tick();
    }

    /**
     * 规划椭圆斩击攻击路径。
     */
    public PathNode getEllipseSlashNode(float progress) {
        if (endPos != null && startPos != null) {
            RandomSource random = owner.getRandom();
            random.setSeed(getUuid().hashCode());
            Vec3 planeNormal = Ellipse.randomPlaneNormal(random, startPos, endPos);
            float curvature = random.nextFloat() * 0.25f + 0.25f;
            Ellipse ellipse = new Ellipse(endPos, startPos, planeNormal, curvature);
            Vec3 point = ellipse.getPoint(progress);
            Vec3 tipDir = point.subtract(ellipse.getCenter()).normalize();
            return getEulerNode(point, tipDir, ellipse.getPlaneNormal());
        }
        return currentPathNode;
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(lerp);
        buf.writeVec3(endPos);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        lerp = buf.readFloat();
        endPos = buf.readVec3();
    }

    /**
     * 根据位置、尖端朝向和叶片法向量计算欧拉角节点
     */
    public PathNode getEulerNode(Vec3 pos, Vec3 tipDir, Vec3 bladeNormal) {
        if (tipDir.lengthSqr() < 1e-4) tipDir = new Vec3(0, 0, 1);
        tipDir = tipDir.normalize();

        float yaw = (float) (Math.atan2(-tipDir.x, tipDir.z) * (180D / Math.PI));
        double horiz = Math.sqrt(tipDir.x * tipDir.x + tipDir.z * tipDir.z);
        float pitch = (float) (Math.atan2(-tipDir.y, horiz) * (180D / Math.PI));

        Vec3 defaultUp = new Vec3(0, 1, 0)
                .xRot((float) Math.toRadians(pitch))
                .yRot((float) Math.toRadians(yaw));
        Vec3 projNormal = bladeNormal.subtract(tipDir.scale(bladeNormal.dot(tipDir))).normalize();
        if (projNormal.lengthSqr() < 1e-4) projNormal = defaultUp;

        double dot = defaultUp.dot(projNormal);
        Vec3 cross = defaultUp.cross(projNormal);
        float roll = (float) (Math.atan2(cross.dot(tipDir), dot) * (180D / Math.PI));

        return new PathNode(pos, yaw, pitch, roll);
    }

    @Override
    protected void tickPhysics() {
    }

    public void setChaseEnd(LivingEntity chaseEnd) {
        this.chaseEnd = chaseEnd;
        this.endPos = chaseEnd.getBoundingBox().getCenter();
    }

    public void setEndPos(Vec3 endPos) {
        this.endPos = endPos;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return AttachmentEntityRegister.ZenithProjectile.get();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.1, -0.04, -0.25, 0.1, 0.04, 0.75);
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
                InvincibleData.criteriaAttack(living, getUuid(), 0, source, getDamage(), InvincibleData.Type.PARTIAL);
            }
        }
    }
}
