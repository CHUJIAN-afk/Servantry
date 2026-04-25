package first.servantry.common.servant;

import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.StardustCellAttackGoal;
import first.servantry.common.servant.goal.StardustDragonAttackGoal;
import first.servantry.common.servant.goal.StardustDragonFollowGoal;
import first.servantry.common.servant.goal.StardustDragonIdleGoal;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ParticleRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 星尘龙 - 多体节龙形仆从。
 */
public class StardustDragon extends MomentumServant implements ICollideAttack<StardustDragon> {

    /** 当前体节索引 */
    private int segmentIndex = 0;
    /** 总体节数 */
    private int totalSegments = 3;

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
        if (owner != null && !owner.level().isClientSide()) {
            updateTotalSegments();
            // 非头部体节检查头部是否存在
            if (!isHead() && getHead() == null) {
                EntityData data = owner.getData(AttachmentRegister.EntityData);
                data.remove(uuid);
                return;
            }
            if (owner.getRandom().nextFloat() < 0.25) {
                Vec3 pos = getPos().offsetRandom(owner.getRandom(), 0.25f);
                Vec3 velocity = getVelocity().scale(-1);
                ((ServerLevel) owner.level()).sendParticles(
                        ParticleRegister.StardustScatter.get(),
                        pos.x, pos.y, pos.z,
                        0,
                        velocity.x, velocity.y, velocity.z,
                        1.0
                );
            }
        }
        super.tick();

        // 限制最大速度
        double maxSpeed = 1.0;
        Vec3 vel = getVelocity();
        double speed = vel.length();
        if (speed > maxSpeed) {
            setVelocity(vel.normalize().scale(maxSpeed));
        }
    }

    private StardustDragon getHead() {
        EntityData data = owner.getData(AttachmentRegister.EntityData);
        return data.getEntities().stream()
                .filter(e -> e instanceof StardustDragon)
                .map(e -> (StardustDragon) e)
                .filter(StardustDragon::isHead)
                .findFirst()
                .orElse(null);
    }

    /**
     * 限制推力方向在运动方向的角度范围内。
     */
    @Override
    public void applyForce(Vec3 force) {
        double forceLength = force.length();
        if (forceLength < 0.001) {
            super.applyForce(force);
            return;
        }

        // 限制最大推力
        double maxForce = 0.5;
        if (forceLength > maxForce) {
            force = force.normalize().scale(maxForce);
            forceLength = maxForce;
        }

        Vec3 velocity = getVelocity();
        double speed = velocity.length();

        // 静止时直接施加
        if (speed < 0.01) {
            super.applyForce(force);
            return;
        }
        Vec3 forceDir = force.normalize();

        super.applyForce(forceDir.scale(forceLength).scale(0.5));
    }

    private Vec3 slerp(Vec3 from, Vec3 to, double t) {
        double dot = Mth.clamp(from.dot(to), -1.0, 1.0);
        double theta = Math.acos(dot);

        // 角度太小，直接线性插值避免除零
        if (theta < 0.01) {
            return from.lerp(to, t);
        }

        double sin = Math.sin(theta);
        return from.scale(Math.sin((1 - t) * theta) / sin)
                .add(to.scale(Math.sin(t * theta) / sin));
    }

    private void updateTotalSegments() {
        if (owner == null) return;
        EntityData data = owner.getData(AttachmentRegister.EntityData);
        int count = 0;
        for (var entity : data.getEntities()) {
            if (entity instanceof StardustDragon) count++;
        }
        this.totalSegments = Math.max(3, count);
    }

    public boolean isHead() { return segmentIndex == 0; }

    public StardustDragon getPrecedingSegment() {
        if (isHead() || owner == null) return null;

        EntityData data = owner.getData(AttachmentRegister.EntityData);
        List<StardustDragon> dragons = data.getEntities().stream()
                .filter(e -> e instanceof StardustDragon)
                .map(e -> (StardustDragon) e)
                .sorted(Comparator.comparingInt(a -> a.segmentIndex))
                .toList();

        for (int i = 0; i < dragons.size(); i++) {
            if (dragons.get(i) == this && i > 0) {
                return dragons.get(i - 1);
            }
        }
        return null;
    }

    @Override
    public AABB getHitbox() {
        return new AABB(-0.25, -0.25, -0.75, 0.25, 0.25, -0.25);
    }

    @Override
    public void onCollisionAttack(Set<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            InvincibleData.servantAttack(target, getHead(), 2, getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
        }
    }

    @Override
    public boolean isValidCollisionTarget(StardustDragon entity, LivingEntity target) {
        return isTarget(target);
    }

    @Override
    public int getTargetDistance() { return 24; }

    @Override
    public boolean requireLineOfSight() { return false; }

    // 网络
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
        return 8f * (1 + 0.23f * (getTotalSegments() - 1));
    }

    @Override
    public float getKnockback() { return 0.5f; }

    @Override
    public ServantType<? extends MomentumServant> getServantType() {
        return ServantRegister.StardustDragon.get();
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
        return 0.5;
    }

}