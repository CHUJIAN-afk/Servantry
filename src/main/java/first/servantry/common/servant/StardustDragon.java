package first.servantry.common.servant;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonAttackGoal;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonFollowGoal;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonIdleGoal;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 星尘龙 - 多体节龙形仆从。
 * <p>
 * 由多个体节组成的长条形仆从，头部控制移动方向，尾部跟随。
 * 首次召唤创建多个体节，重复召唤可增加体节数量。
 * </p>
 */
public class StardustDragon extends MomentumServant implements ICollideAttack<StardustDragon> {

    /** 当前体节索引 */
    private int segmentIndex = 0;
    /** 总体节数 */
    private int totalSegments = 1;
    /** 螺旋相位 */
    private float spiralPhase = 0;

    public StardustDragon() {
        setDrag(0.92f);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new StardustDragonAttackGoal(this));
        goalSelector.addGoal(1, new StardustDragonIdleGoal(this));
        goalSelector.addGoal(2, new StardustDragonFollowGoal(this));
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (owner.getRandom().nextFloat() < 0.2 * getScale()) {
                ParticleHelper.create(owner.level())
                        .generic(GenericParticleBuilder.create()
                                .color(0x2fb2e1)
                                .edgeColor(0x33ccff)
                                .colorRandom(0.2F, 0.2F, 0.0F)
                                .lifetime(5)
                                .lifetimeRandom(25)
                                .spin(0.1f)
                                .spinRandom(0.5F)
                                .friction(0.75F)
                                .scale(0.035f)
                                .scaleRandom(0.005f)
                        )
                        .pos(getPos().offsetRandom(owner.getRandom(), 0.2f * getScale()))
                        .velocity(getVelocity().scale(-1))
                        .spread(0)
                        .speed(0)
                        .emit();
            }
            // 限制最大速度
            double maxSpeed = 1.0;
            Vec3 vel = getVelocity();
            double speed = vel.length();
            if (speed > maxSpeed) {
                setVelocity(vel.normalize().scale(maxSpeed));
            }
        }
        super.tick();
    }

    @Override
    public void onRemove() {
        if (isHead()) {
            List<StardustDragon> dragons = ServantryHelper.get(owner).getEntityData().get(EntityData.Type.Servant, StardustDragon.class);
            for (StardustDragon dragon : dragons) {
                dragon.setRemove();
            }
        }
    }

    @Nullable
    private StardustDragon getHead() {
        List<StardustDragon> dragons = ServantryHelper.get(owner).getEntityData().get(EntityData.Type.Servant, StardustDragon.class);
        if (!dragons.isEmpty()) {
            return dragons.getFirst();
        }
        return null;
    }

    /**
     * 限制推力方向在运动方向的角度范围内。
     */
    @Override
    public void applyForce(Vec3 force) {
        super.applyForce(force);
    }

    public boolean isHead() {
        return segmentIndex == 0;
    }

    @Nullable
    public StardustDragon getPrecedingSegment() {
        if (!isHead()) {
            List<StardustDragon> dragons = ServantryHelper.get(owner).getEntityData().get(EntityData.Type.Servant, StardustDragon.class);
            return dragons.get(segmentIndex - 1);
        }
        return null;
    }

    @Override
    public @NotNull AABB getHitbox() {
        int segment = getSegmentIndex();
        int total = getTotalSegments();
        boolean isHead = segment == 0;
        boolean isTail = segment == total - 1;
        double minZ = isHead ? -0.3 : isTail ? -1.45 : -0.35;
        double maxZ = isHead ? 0.65 : isTail ? 0.3 : 0.35;
        double scale = getScale();
        return new AABB(-0.25 * scale, -0.25 * scale, minZ * scale, 0.25 * scale, 0.25 * scale, maxZ * scale);
    }

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public boolean isValidCollisionTarget(StardustDragon entity, LivingEntity target) {
        return isTarget(target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        StardustDragon head = getHead();
        if (head != null) {
            for (HitContext hit : hitContexts) {
                InvincibleData.criteriaAttack(hit.entity(), head.getUuid(), 2, getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
            }
        }
    }

    @Override
    public int getTargetDistance() { return 24; }

    @Override
    public boolean requireLineOfSight() { return false; }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(segmentIndex);
        buf.writeInt(totalSegments);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        segmentIndex = buf.readInt();
        totalSegments = buf.readInt();
    }

    @Override
    public float getDamage() {
        return super.getDamage() * (1 + 0.23f * (getTotalSegments() - 1));
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return AttachmentEntityRegister.StardustDragon.get();
    }

    public int getSegmentIndex() {
        return segmentIndex;
    }

    public void setSegmentIndex(int index) {
        this.segmentIndex = index;
    }

    public int getTotalSegments() {
        return totalSegments;
    }

    public void setTotalSegments(int total) {
        this.totalSegments = total;
    }

    public float getScale() {
        return 1 + getTotalSegments() * 0.025f;
    }

    public double getSegmentDistance() {
        return 0.65 * getScale();
    }

    /**
     * 螺旋游动向目标位置。
     *
     * @param targetPos    目标位置
     * @param acceleration 加速度大小
     */
    public void spiralToward(Vec3 targetPos, double acceleration) {
        Vec3 currentPos = getPos();
        Vec3 toTarget = targetPos.subtract(currentPos);
        double distance = toTarget.length();

        if (distance < 1) return;

        // 更新螺旋相位
        double speed = getVelocity().length() / getScale();
        spiralPhase += 0.2f * (float) speed;

        // 计算到目标的方向
        Vec3 forward = toTarget.normalize();

        // 计算右方向和上方向（构建局部坐标系）
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(worldUp);
        if (right.lengthSqr() < 0.001) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();

        // 螺旋偏移：左右和上下同时变化，相位差90度形成螺旋
        double spiralAmplitude = Math.min(0.4, distance * 0.08);
        double horizontalOffset = Math.cos(spiralPhase) * spiralAmplitude;
        double verticalOffset = Math.sin(spiralPhase) * spiralAmplitude;

        // 合成移动方向
        Vec3 moveDir = forward
                .add(right.scale(horizontalOffset))
                .add(up.scale(verticalOffset))
                .normalize();

        // 施加推力
        applyForce(moveDir.scale(acceleration));

        // 计算翻滚角：根据螺旋运动
        float roll = (float) (-Math.sin(spiralPhase) * spiralAmplitude * 90);

        // 更新朝向
        if (speed > 0.01) {
            Vec3 motionDir = getVelocity().normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-motionDir.x, motionDir.z));
            float targetPitch = (float) Math.toDegrees(Math.asin(-motionDir.y));

            setDesiredRotation(targetYaw, targetPitch, roll);
        }
    }
}