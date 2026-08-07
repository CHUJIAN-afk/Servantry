package first.servantry.common.servant;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.ICollideAttack;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.servant.MomentumServant;
import first.lyra.common.servant.ServantGoalSelector;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonAttackGoal;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonFollowGoal;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonIdleGoal;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        super();
        setDrag(0.92f);
        setRotationSpeed(0);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new StardustDragonAttackGoal(this));
        goalSelector.addGoal(1, new StardustDragonIdleGoal(this));
        goalSelector.addGoal(2, new StardustDragonFollowGoal(this));
    }

    @Override
    public LivingEntity searchTarget() {
        if (isHead()) {
            LyraHelper helper = LyraHelper.get(owner);
            TargetCache targetCache = helper.getTargetCache();
            if (!targetCache.isEmpty()) {
                float searchRange = targetCache.getServantSearchRange(this.getOwner(), this.getSearchDistance());
                List<LivingEntity> targets = new ArrayList<>();
                List<LivingEntity> entities = targetCache.getEntities();
                for (LivingEntity living : entities) {
                    if (targetCache.getDistance(owner, living) < searchRange) {
                        if (isTarget(living)) {
                            targets.add(living);
                        }
                    }
                }
                return targetCache.getNewTarget(this, targets, 0, true);
            }
        }
        return null;
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public void tick() {
        emitParticle();
        super.tick();
    }

    protected void emitParticle() {
        if (!owner.level().isClientSide()) {
            if (owner.getRandom().nextFloat() < 0.2) {
                ParticleHelper.create(owner.level())
                        .generic(GenericParticleBuilder.create()
                                .centerColor(0x2fb2e1)
                                .edgeColor(0x33ccff)
                                .lifetime(5)
                                .lifetimeRandom(25)
                                .spin(0.1f)
                                .spinRandom(0.5F)
                                .friction(0.75F)
                                .scale(0.035f)
                                .scaleRandom(0.005f)
                        )
                        .pos(getPos().offsetRandom(owner.getRandom(), 0.2f))
                        .velocity(getVelocity().scale(-1))
                        .spread(0)
                        .speed(0)
                        .emit();
            }
        }
    }

    @Override
    public void onRemove() {
        if (isHead()) {
            List<StardustDragon> dragons = LyraHelper.get(owner)
                    .getEntityData()
                    .get(AttachmentEntityData.Type.Servant, ServantryAttachmentEntityRegister.STARDUST_DRAGON.get());
            for (StardustDragon dragon : dragons) {
                dragon.setRemove();
            }
        }
    }

    @Nullable
    public StardustDragon getHead() {
        List<StardustDragon> dragons = LyraHelper.get(owner)
                .getEntityData()
                .get(AttachmentEntityData.Type.Servant, ServantryAttachmentEntityRegister.STARDUST_DRAGON.get());
        if (!dragons.isEmpty()) {
            return dragons.getFirst();
        }
        return null;
    }

    public boolean isHead() {
        return segmentIndex == 0;
    }

    @Nullable
    public StardustDragon getPrecedingSegment() {
        if (!isHead()) {
            List<StardustDragon> dragons = LyraHelper.get(owner)
                    .getEntityData()
                    .get(AttachmentEntityData.Type.Servant, ServantryAttachmentEntityRegister.STARDUST_DRAGON.get());
            int index = segmentIndex - 1;
            if (index >= 0 && index < dragons.size()) {
                return dragons.get(index);
            }
        }
        return null;
    }

    @Nullable
    public StardustDragon getNextSegment() {
        if (segmentIndex < totalSegments - 1) {
            List<StardustDragon> dragons = LyraHelper.get(owner)
                    .getEntityData()
                    .get(AttachmentEntityData.Type.Servant, ServantryAttachmentEntityRegister.STARDUST_DRAGON.get());
            int index = segmentIndex + 1;
            if (index >= 0 && index < dragons.size()) {
                return dragons.get(index);
            }
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
        return new AABB(-0.25, -0.25, minZ, 0.25, 0.25, maxZ);
    }

    @Override
    public boolean canCollideAttack() {
        StardustDragon head = getHead();
        if (head != null) {
            return head.isTarget(head.getTarget());
        }
        return false;
    }

    @Override
    public boolean isValidCollisionTarget(StardustDragon entity, LivingEntity target) {
        StardustDragon head = getHead();
        if (head != null) {
            return head.isTarget(target);
        }
        return false;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        StardustDragon head = getHead();
        if (head != null) {
            for (HitContext hit : hitContexts) {
                InvincibleData.attack(hit.entity())
                        .attacker(head.getUuid())
                        .damageSource(getDamageSource())
                        .damageAmount(getDamage())
                        .invincibleTime(2)
                        .apply();
            }
        }
    }

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
        return ServantryAttachmentEntityRegister.STARDUST_DRAGON.get();
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

    public double getSegmentDistance() {
        return 0.65;
    }

    /**
     * 球形穿梭目标位置。
     * <p>
     * 加速度方向从当前朝向出发，朝目标方向有限偏转（最小转弯半径），
     * 穿过目标后不会直接调头，而是受转弯半径约束自然绕行。
     * </p>
     *
     * @param targetPos       目标位置
     * @param maxTurnDegrees  最大偏转角度
     * @param acceleration    加速度大小
     * @param spiral          螺旋偏移是否影响位置；false时仅影响翻滚角
     */
    public void orbitToward(Vec3 targetPos, float maxTurnDegrees, double acceleration, boolean spiral) {
        Vec3 currentPos = getPos();
        Vec3 velocity = getVelocity();
        Vec3 toTarget = targetPos.subtract(currentPos);
        double distance = toTarget.length();

        spiralPhase += 0.2f * (float) velocity.length();

        Vec3 forward = getCurrentVelocity();

        // 最大偏转角：综合下一体节姿态约束，避免头部与体节重叠
        double maxTurn;
        StardustDragon nextSeg = getNextSegment();
        if (nextSeg != null) {
            Vec3 nextDir = nextSeg.getCurrentVelocity();
            double angleToNext = Math.acos(Mth.clamp(forward.dot(nextDir), -1, 1));
            maxTurn = Math.max(Math.toRadians(2.0), Math.toRadians(maxTurnDegrees) - angleToNext);
        } else {
            maxTurn = Math.toRadians(12.0);
        }

        if (distance > 0.01) {
            Vec3 desiredDir = toTarget.normalize();
            double angleDiff = Math.acos(Mth.clamp(forward.dot(desiredDir), -1, 1));
            if (angleDiff > maxTurn) {
                Vec3 rotAxis = forward.cross(desiredDir);
                if (rotAxis.lengthSqr() < 0.0001) rotAxis = forward.cross(new Vec3(0, 1, 0));
                if (rotAxis.lengthSqr() < 0.0001) rotAxis = forward.cross(new Vec3(1, 0, 0));
                rotAxis = rotAxis.normalize();
                double cosT = Math.cos(maxTurn), sinT = Math.sin(maxTurn);
                forward = forward.scale(cosT)
                        .add(rotAxis.cross(forward).scale(sinT))
                        .add(rotAxis.scale(rotAxis.dot(forward) * (1 - cosT)))
                        .normalize();
            } else {
                forward = desiredDir;
            }
        }

        double amplitude = spiral ? Math.max(0.15, Math.min(0.4, distance * 0.08)) : 0;

        if (spiral) {
            Vec3 right = forward.cross(new Vec3(0, 1, 0));
            if (right.lengthSqr() < 0.001) {
                right = new Vec3(1, 0, 0);
            } else {
                right = right.normalize();
            }
            Vec3 up = right.cross(forward).normalize();
            forward = forward
                    .add(right.scale(Math.cos(spiralPhase) * amplitude))
                    .add(up.scale(Math.sin(spiralPhase) * amplitude))
                    .normalize();
        }

        applyForce(forward.scale(acceleration));
        lookAtDirection(velocity.normalize());
    }

    public void orbitToward(Vec3 targetPos, float maxTurnDegrees, double acceleration) {
        orbitToward(targetPos, maxTurnDegrees, acceleration, true);
    }
}