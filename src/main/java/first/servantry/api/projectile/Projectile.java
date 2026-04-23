package first.servantry.api.projectile;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.EntityType;
import first.servantry.api.register.ProjectileType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.UUID;

/**
 * 射弹实体抽象基类，代表由玩家拥有、动量驱动、可追踪目标的飞行攻击物。
 * <p>
 * 射弹采用与仆从相同的附件存储架构，内置动量物理系统和状态管理。
 * 支持追踪目标、黏贴到目标身上、自转等效果。
 * </p>
 *
 * <h3>状态管理</h3>
 * <ul>
 *   <li>{@link ProjectileState#FLYING} - 飞行状态：追踪目标、渲染拖尾</li>
 *   <li>{@link ProjectileState#ATTACHED} - 黏贴状态：附着在目标身上</li>
 *   <li>{@link ProjectileState#DEAD} - 死亡状态：等待被移除</li>
 * </ul>
 *
 * <h3>子类需实现的方法</h3>
 * <ul>
 *   <li>{@link #tickBehavior(Player)} - 射弹行为逻辑（追踪、命中检测等）</li>
 *   <li>{@link #getDamage()} - 返回伤害值</li>
 *   <li>{@link #getProjectileType()} - 返回注册类型</li>
 *   <li>{@link #writeAdditional(RegistryFriendlyByteBuf)} / {@link #readAdditional(RegistryFriendlyByteBuf)} - 自定义数据同步</li>
 * </ul>
 *
 * @see ProjectileState
 */
public abstract class Projectile extends AttachmentEntity {

    // ===================== 动量物理系统 =====================

    /** 当前速度向量（米/tick） */
    protected Vec3 velocity = Vec3.ZERO;

    /** 速度衰减系数 [0, 1]，值越小阻力越大 */
    protected float drag = 0.95f;

    /** 最大速度上限（米/tick） */
    protected float maxSpeed = 1.2f;

    /** 期望朝向角度（独立于运动方向） */
    protected float desiredYaw;
    protected float desiredPitch;
    protected float desiredRoll;

    /** 朝向插值速度（度/tick） */
    protected float rotationSpeed = 15.0f;

    // ===================== 目标追踪 =====================

    /** 追踪目标UUID */
    protected UUID targetUuid;

    /** 缓存的追踪目标实体 */
    protected LivingEntity cachedTarget;

    // ===================== 状态管理 =====================

    /** 当前状态 */
    protected ProjectileState state = ProjectileState.FLYING;

    /** 黏贴目标UUID */
    protected UUID attachedTargetUuid;

    /** 黏贴位置相对于目标的偏移 */
    protected Vec3 attachedOffset;

    /** 缓存的黏贴目标实体 */
    protected LivingEntity cachedAttachedTarget;

    // ===================== 生命周期 =====================

    /** 拖尾计时器，>0 时渲染拖尾 */
    protected int trailTimer = 0;

    /** 待移除标记（并发安全） */
    private boolean markedForRemoval = false;

    /** 生命周期计数器 */
    public int life = 0;

    /** 来源仆从UUID（可选） */
    protected UUID sourceServantUuid;

    /** 所有者玩家UUID */
    protected UUID ownerUuid;

    // ===================== 构造方法 =====================

    /**
     * 构造射弹，初始化默认状态。
     */
    public Projectile() {
        super();
    }

    /**
     * 构造射弹，指定起始位置和目标。
     *
     * @param startPos 起始位置
     * @param target   追踪目标（可为null）
     */
    public Projectile(Vec3 startPos, LivingEntity target) {
        super();
        this.currentPathNode = new PathNode(startPos, 0, 0, 0);

        if (target != null) {
            this.targetUuid = target.getUUID();
            this.cachedTarget = target;

            // 初始化朝向为目标方向
            Vec3 dir = target.getBoundingBox().getCenter().subtract(startPos).normalize();
            this.desiredYaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
            this.desiredPitch = (float) Math.toDegrees(Math.atan2(-dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));
            this.currentPathNode = new PathNode(startPos, desiredYaw, desiredPitch, 0);
        }
    }

    // ===================== 抽象方法 =====================

    /**
     * 射弹行为逻辑，每tick调用一次。
     * <p>
     * 子类在此实现追踪、命中检测、状态切换等逻辑。
     * </p>
     *
     * @param owner 拥有该射弹的玩家
     */
    public abstract void tickBehavior(Player owner);

    /**
     * 返回射弹的注册类型。
     *
     * @return 射弹类型
     */
    public abstract ProjectileType<? extends Projectile> getProjectileType();

    // ===================== AttachmentEntity 实现 =====================

    @Override
    public EntityType<? extends AttachmentEntity> getType() {
        return getProjectileType();
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            // 服务端：执行行为逻辑和物理更新
            tickBehavior(owner);
            tickPhysics();

            // 生命周期检查
            if (++life >= 400) {
                markForRemoval();
            }
        } else {
            // 客户端：使用同步数据更新位置
            currentPathNode = clientTargetNode;
        }

        // 拖尾计时器衰减
        if (trailTimer > 0) {
            trailTimer--;
        }
        super.tick();
    }

    // ===================== 可重写配置 =====================

    @Override
    public int getHistoryNodesSize() {
        return 8;
    }

    /**
     * 获取命中检测半径。
     *
     * @return 命中半径
     */
    public float getHitRadius() {
        return 0.5f;
    }

    /**
     * 获取远离玩家的最大距离。
     *
     * @return 最大距离
     */
    public double getMaxDistance() {
        return 128.0;
    }

    /**
     * 获取自转速度（度/tick）。
     *
     * @return 自转速度
     */
    public float getSpinSpeed() {
        return 5f;
    }

    /**
     * 获取拖尾持续时间（tick）。
     *
     * @return 拖尾持续时间
     */
    public int getTrailDuration() {
        return 15;
    }

    // ===================== 物理更新 =====================

    /**
     * 动量物理更新：应用阻力、限制速度、更新位置和朝向。
     */
    protected void tickPhysics() {
        // 应用阻力
        velocity = velocity.scale(drag);

        // 限制速度
        double speed = velocity.length();
        if (speed > maxSpeed) {
            velocity = velocity.scale(maxSpeed / speed);
        }

        // 更新朝向
        updateOrientation();

        // 根据速度更新位置
        Vec3 newPos = getPos().add(velocity);
        float newRoll = desiredRoll + getSpinSpeed();
        setPath(Collections.singletonList(new PathNode(newPos, desiredYaw, desiredPitch, newRoll)));
    }

    /**
     * 更新朝向角度，平滑趋向期望值。
     */
    protected void updateOrientation() {
        float deltaYaw = Mth.wrapDegrees(desiredYaw - getYaw());
        float deltaPitch = Mth.wrapDegrees(desiredPitch - getPitch());
        float deltaRoll = Mth.wrapDegrees(desiredRoll - getRoll());

        desiredYaw = getYaw() + Mth.clamp(deltaYaw, -rotationSpeed, rotationSpeed);
        desiredPitch = getPitch() + Mth.clamp(deltaPitch, -rotationSpeed, rotationSpeed);
        desiredRoll = getRoll() + Mth.clamp(deltaRoll, -rotationSpeed, rotationSpeed);
    }

    // ===================== 状态管理 =====================

    /**
     * 标记射弹为待移除状态。
     */
    public void markForRemoval() {
        this.markedForRemoval = true;
        this.state = ProjectileState.DEAD;
    }

    /**
     * 检查是否待移除。
     *
     * @return 是否待移除
     */
    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    /**
     * 切换到黏贴状态。
     *
     * @param target 黏贴目标
     */
    public void attachTo(LivingEntity target) {
        attachTo(target, getPos().subtract(target.position()));
    }

    /**
     * 切换到黏贴状态，指定自定义偏移。
     *
     * @param target       黏贴目标
     * @param customOffset 自定义偏移
     */
    public void attachTo(LivingEntity target, Vec3 customOffset) {
        this.state = ProjectileState.ATTACHED;
        this.attachedTargetUuid = target.getUUID();
        this.cachedAttachedTarget = target;
        this.attachedOffset = customOffset;
        this.velocity = Vec3.ZERO;
        this.trailTimer = 0;
    }

    /**
     * 更新黏贴位置（跟随目标）。
     */
    public void updateAttachedPosition() {
        if (cachedAttachedTarget != null && cachedAttachedTarget.isAlive()) {
            Vec3 targetPos = cachedAttachedTarget.position();
            float newRoll = desiredRoll + getSpinSpeed();
            setPath(Collections.singletonList(new PathNode(targetPos.add(attachedOffset), desiredYaw, desiredPitch, newRoll)));
        }
    }

    // ===================== 网络序列化 =====================

    @Override
    public void writeBase(RegistryFriendlyByteBuf buf) {
        super.writeBase(buf);

        buf.writeDouble(velocity.x);
        buf.writeDouble(velocity.y);
        buf.writeDouble(velocity.z);

        buf.writeBoolean(targetUuid != null);
        if (targetUuid != null) buf.writeUUID(targetUuid);

        buf.writeEnum(state);

        buf.writeBoolean(attachedTargetUuid != null);
        if (attachedTargetUuid != null) {
            buf.writeUUID(attachedTargetUuid);
            buf.writeDouble(attachedOffset.x);
            buf.writeDouble(attachedOffset.y);
            buf.writeDouble(attachedOffset.z);
        }

        buf.writeInt(trailTimer);

        writeAdditional(buf);
    }

    @Override
    public void readBase(RegistryFriendlyByteBuf buf) {
        super.readBase(buf);

        velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());

        if (buf.readBoolean()) {
            targetUuid = buf.readUUID();
        }

        state = buf.readEnum(ProjectileState.class);

        if (buf.readBoolean()) {
            attachedTargetUuid = buf.readUUID();
            attachedOffset = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        trailTimer = buf.readInt();

        readAdditional(buf);
    }

    // ===================== 访问器 =====================

    /** @return 当前速度 */
    public Vec3 getVelocity() { return velocity; }

    /** 设置速度 */
    public void setVelocity(Vec3 velocity) { this.velocity = velocity; }

    /** @return 当前状态 */
    public ProjectileState getState() { return state; }

    /** @return 追踪目标UUID */
    public UUID getTargetUuid() { return targetUuid; }

    /** 设置追踪目标UUID */
    public void setTargetUuid(UUID targetUuid) { this.targetUuid = targetUuid; }

    /** @return 缓存的追踪目标 */
    public LivingEntity getCachedTarget() { return cachedTarget; }

    /** 设置缓存的追踪目标 */
    public void setCachedTarget(LivingEntity cachedTarget) { this.cachedTarget = cachedTarget; }

    /** @return 黏贴目标UUID */
    public UUID getAttachedTargetUuid() { return attachedTargetUuid; }

    /** @return 缓存的黏贴目标 */
    public LivingEntity getCachedAttachedTarget() { return cachedAttachedTarget; }

    /** 设置缓存的黏贴目标 */
    public void setCachedAttachedTarget(LivingEntity cachedAttachedTarget) { this.cachedAttachedTarget = cachedAttachedTarget; }

    /** @return 拖尾计时器 */
    public int getTrailTimer() { return trailTimer; }

    /** 设置拖尾计时器 */
    public void setTrailTimer(int trailTimer) { this.trailTimer = trailTimer; }

    /** @return 阻力系数 */
    public float getDrag() { return drag; }

    /** 设置阻力系数 */
    public void setDrag(float drag) { this.drag = Mth.clamp(drag, 0.0f, 1.0f); }

    /** @return 最大速度 */
    public float getMaxSpeed() { return maxSpeed; }

    /** 设置最大速度 */
    public void setMaxSpeed(float maxSpeed) { this.maxSpeed = maxSpeed; }

    /** @return 期望偏航角 */
    public float getDesiredYaw() { return desiredYaw; }

    /** @return 期望俯仰角 */
    public float getDesiredPitch() { return desiredPitch; }

    /** @return 期望滚转角 */
    public float getDesiredRoll() { return desiredRoll; }

    /**
     * 设置期望朝向角度。
     *
     * @param yaw   期望偏航角
     * @param pitch 期望俯仰角
     * @param roll  期望滚转角
     */
    public void setDesiredRotation(float yaw, float pitch, float roll) {
        this.desiredYaw = yaw;
        this.desiredPitch = pitch;
        this.desiredRoll = roll;
    }

    /**
     * 施加力，修改速度。
     *
     * @param force 力向量
     */
    public void applyForce(Vec3 force) {
        this.velocity = this.velocity.add(force);
    }

    /** @return 来源仆从UUID */
    public UUID getSourceServantUuid() { return sourceServantUuid; }

    /** 设置来源仆从UUID */
    public void setSourceServantUuid(UUID sourceServantUuid) { this.sourceServantUuid = sourceServantUuid; }

    /** @return 所有者玩家UUID */
    public UUID getOwnerUuid() { return ownerUuid; }

    /** 设置所有者玩家UUID */
    public void setOwnerUuid(UUID ownerUuid) { this.ownerUuid = ownerUuid; }
}