package first.servantry.api.projectile;

import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.PlannedPath;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * 射弹实体的抽象基类，代表一个由玩家拥有、可自主飞行并拥有视觉轨迹的"射弹"。
 * <p>
 * 射弹采用与仆从相同的架构：数据存储在玩家附件中，服务端tick逻辑，客户端渲染。
 * 内置动量控制系统，支持路径节点历史轨迹渲染。
 * </p>
 * <p>
 * 子类需要实现：
 * <ul>
 *   <li>{@link #tickBehavior(Player)} - 射弹的行为逻辑（追踪、命中检测等）</li>
 *   <li>{@link #getDamage()} - 返回伤害值</li>
 *   <li>{@link #getType()} - 返回对应的注册类型</li>
 *   <li>{@link #writeAdditional(RegistryFriendlyByteBuf)} / {@link #readAdditional(RegistryFriendlyByteBuf)} - 自定义数据同步</li>
 * </ul>
 * </p>
 * <p>
 * 射弹状态管理：
 * <ul>
 *   <li>{@link ProjectileState#FLYING} - 飞行状态，追踪目标、渲染拖尾</li>
 *   <li>{@link ProjectileState#ATTACHED} - 黏贴状态，附着在目标身上</li>
 *   <li>{@link ProjectileState#DEAD} - 死亡状态，等待被移除</li>
 * </ul>
 * </p>
 */
public abstract class Projectile {

    // ===================== 基础标识字段 =====================

    /**
     * 射弹的唯一标识符，用于网络同步和持久化
     */
    private UUID uuid;

    public int life = 0;

    /** 拥有该射弹的玩家UUID */
    private UUID ownerUuid;

    /** 发射该射弹的来源仆从UUID（可选） */
    private UUID sourceServantUuid;

    /** 拥有该射弹的玩家（运行时引用） */
    private Player owner;

    // ===================== 路径与历史轨迹 =====================

    /** 当前正在执行的计划路径，若为 null 则表示没有路径任务 */
    private PlannedPath currentPlannedPath = null;

    /**
     * 历史节点队列，用于拖尾渲染。
     * 队列头部为最新节点，尾部为最旧节点。
     */
    private final LinkedList<PathNode> historyNodes = new LinkedList<>();

    /** 服务端当前精确的路径节点（位置 + 旋转） */
    protected PathNode currentPathNode;

    /** 客户端接收到的目标节点，用于插值渲染 */
    private PathNode clientTargetNode;

    /** 标记客户端是否已完成首次位置同步 */
    private boolean clientInitialized = false;

    // ===================== 动量物理系统 =====================

    /** 当前速度向量（米/秒） */
    protected Vec3 velocity = Vec3.ZERO;

    /** 每 tick 速度的衰减系数，范围 [0, 1]，值越小阻力越大 */
    protected float drag = 0.95f;

    /** 最大速度标量上限（米/秒），防止速度无限增长 */
    protected float maxSpeed = 1.2f;

    /** 期望的朝向角度（偏航、俯仰、滚转），独立于运动方向 */
    protected float desiredYaw;
    protected float desiredPitch;
    protected float desiredRoll;

    /** 朝向插值速度（度/tick），控制转向平滑程度 */
    protected float rotationSpeed = 15.0f;

    // ===================== 目标追踪 =====================

    /** 追踪目标的UUID */
    protected UUID targetUuid;

    /** 缓存的追踪目标实体 */
    protected LivingEntity cachedTarget;

    // ===================== 状态管理 =====================

    /** 射弹当前状态 */
    protected ProjectileState state = ProjectileState.FLYING;

    /** 黏贴目标的UUID */
    protected UUID attachedTargetUuid;

    /** 黏贴位置相对于目标的偏移 */
    protected Vec3 attachedOffset;

    /** 缓存的黏贴目标实体 */
    protected LivingEntity cachedAttachedTarget;

    // ===================== 渲染状态 =====================

    /** 拖尾计时器，大于0时渲染拖尾 */
    protected int trailTimer = 0;

    /** 标记射弹是否需要被移除（用于并发安全） */
    private boolean markedForRemoval = false;

    // ===================== 射弹状态枚举 =====================

    public enum ProjectileState {
        /** 飞行状态：追踪目标、渲染拖尾 */
        FLYING,
        /** 黏贴状态：附着在目标身上 */
        ATTACHED,
        /** 死亡状态：等待被移除 */
        DEAD
    }

    // ===================== 构造与初始化 =====================

    /**
     * 构造一个射弹，并初始化其起始位置。
     */
    public Projectile() {
        this.uuid = UUID.randomUUID();
        this.currentPathNode = new PathNode(Vec3.ZERO, 0, 0, 0);
        historyNodes.addFirst(currentPathNode);
        historyNodes.addFirst(currentPathNode);
    }

    /**
     * 构造一个射弹，指定起始位置和目标。
     *
     * @param startPos 起始位置
     * @param target   追踪目标（可为null）
     */
    public Projectile(Vec3 startPos, LivingEntity target) {
        this.uuid = UUID.randomUUID();
        this.currentPathNode = new PathNode(startPos, 0, 0, 0);
        historyNodes.addFirst(currentPathNode);
        historyNodes.addFirst(currentPathNode);

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

    // ===================== 抽象方法（子类必须实现） =====================

    /**
     * 射弹的行为逻辑，每 tick 调用一次。
     * <p>
     * 子类在此方法中实现追踪、命中检测、状态切换等逻辑。
     * </p>
     *
     * @param owner 拥有该射弹的玩家
     */
    public abstract void tickBehavior(Player owner);

    /**
     * 写入射弹特有的附加同步数据（由子类实现）。
     *
     * @param buf 数据包缓冲区
     */
    public abstract void writeAdditional(RegistryFriendlyByteBuf buf);

    /**
     * 读取射弹特有的附加同步数据（由子类实现）。
     *
     * @param buf 数据包缓冲区
     */
    public abstract void readAdditional(RegistryFriendlyByteBuf buf);

    /**
     * 获取射弹的单次攻击伤害值。
     *
     * @return 伤害数值
     */
    public abstract float getDamage();

    /**
     * 返回该射弹对应的注册类型，用于网络序列化与工厂创建。
     *
     * @return 射弹类型
     */
    public abstract ProjectileType<? extends Projectile> getType();

    // ===================== 默认值方法（子类可重写） =====================

    /**
     * 返回历史节点队列的最大容量。
     * 默认 8，子类可重写以改变拖尾渲染的历史长度。
     *
     * @return 队列最大长度
     */
    public int getHistoryNodesSize() {
        return 8;
    }

    /**
     * 返回命中检测半径。
     * 默认 0.5，子类可重写以改变命中判定范围。
     *
     * @return 命中半径
     */
    public float getHitRadius() {
        return 0.5f;
    }

    /**
     * 返回射弹远离玩家的最大距离。
     * 超过此距离射弹将被移除。默认 128。
     *
     * @return 最大距离
     */
    public double getMaxDistance() {
        return 128.0;
    }

    /**
     * 返回射弹的自转速度（度/tick）。
     * 默认 5，子类可重写以改变自转效果。
     *
     * @return 自转速度
     */
    public float getSpinSpeed() {
        return 5f;
    }

    /**
     * 返回拖尾持续时间（tick）。
     * 默认 15，子类可重写以改变拖尾效果持续时间。
     *
     * @return 拖尾持续时间
     */
    public int getTrailDuration() {
        return 15;
    }

    // ===================== 每 Tick 更新 =====================

    /**
     * 射弹的主更新方法，每 tick 调用一次。
     * <p>
     * 服务端：更新行为逻辑、动量物理、历史节点。<br>
     * 客户端：使用网络同步的目标节点更新当前视觉节点，并维护历史队列。
     * </p>
     *
     * @param owner 拥有该射弹的玩家
     */
    public void tick(Player owner) {
        this.owner = owner;
        if (!owner.level().isClientSide()) {
            // 服务端逻辑
            tickBehavior(owner);
            tickPhysics();
            updateHistoryNodes();
            if (++life >= 400) {
                markForRemoval();
            }
        } else {
            // 客户端逻辑：直接使用网络同步来的目标节点
            this.currentPathNode = clientTargetNode;
            updateHistoryNodes();
        }

        // 拖尾计时器衰减
        if (trailTimer > 0) {
            trailTimer--;
        }
    }

    /**
     * 动量物理更新。
     * <p>
     * 应用阻力、限制速度、更新位置和朝向。
     * </p>
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
        if (velocity.lengthSqr() > 1e-8) {
            Vec3 newPos = getPos().add(velocity);
            setPath(Collections.singletonList(new PathNode(newPos, desiredYaw, desiredPitch, desiredRoll + getSpinSpeed())));
        } else {
            setPath(Collections.singletonList(new PathNode(getPos(), desiredYaw, desiredPitch, desiredRoll + getSpinSpeed())));
        }
    }

    /**
     * 更新历史节点队列。
     */
    protected void updateHistoryNodes() {
        this.historyNodes.addFirst(this.currentPathNode);
        if (this.historyNodes.size() > getHistoryNodesSize()) {
            this.historyNodes.removeLast();
        }
    }

    /**
     * 更新射弹的朝向角度，使其平滑趋向期望角度。
     */
    protected void updateOrientation() {
        float deltaYaw = Mth.wrapDegrees(desiredYaw - getYaw());
        float deltaPitch = Mth.wrapDegrees(desiredPitch - getPitch());
        float deltaRoll = Mth.wrapDegrees(desiredRoll - getRoll());

        float newYaw = getYaw() + Mth.clamp(deltaYaw, -rotationSpeed, rotationSpeed);
        float newPitch = getPitch() + Mth.clamp(deltaPitch, -rotationSpeed, rotationSpeed);
        float newRoll = getRoll() + Mth.clamp(deltaRoll, -rotationSpeed, rotationSpeed);

        this.desiredYaw = newYaw;
        this.desiredPitch = newPitch;
        this.desiredRoll = newRoll;
    }

    // ===================== 状态管理 =====================

    /**
     * 标记射弹为待移除状态。
     * <p>
     * 此方法用于并发安全，不会立即修改列表，而是在tick结束后统一清理。
     * </p>
     */
    public void markForRemoval() {
        this.markedForRemoval = true;
        this.state = ProjectileState.DEAD;
    }

    /**
     * 检查射弹是否需要被移除。
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
        this.state = ProjectileState.ATTACHED;
        this.attachedTargetUuid = target.getUUID();
        this.cachedAttachedTarget = target;
        this.attachedOffset = getPos().subtract(target.position());
        this.velocity = Vec3.ZERO;
        this.trailTimer = 0;
    }

    /**
     * 切换到黏贴状态，指定自定义偏移位置。
     *
     * @param target 黏贴目标
     * @param customOffset 自定义的附着偏移位置
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
            setPath(Collections.singletonList(new PathNode(targetPos.add(attachedOffset), desiredYaw, desiredPitch, desiredRoll + getSpinSpeed())));
        }
    }

    // ===================== 路径管理 =====================

    /** 设置当前要执行的计划路径 */
    public void setPlannedPath(PlannedPath path) {
        this.currentPlannedPath = path;
    }

    /** @return 当前正在执行的路径，可能为 null */
    public PlannedPath getCurrentPath() {
        return this.currentPlannedPath;
    }

    /** @return 当前所在的位置节点 */
    public PathNode getCurrentPathNode() {
        return currentPathNode;
    }

    /** @return 是否正在执行一条路径（且未完成） */
    public boolean isExecutingPath() {
        return this.currentPlannedPath != null && !this.currentPlannedPath.isFinished();
    }

    /**
     * 获取用于渲染的插值节点。
     * 使用历史队列中最近两个节点进行线性插值，使视觉运动更平滑。
     *
     * @param partialTick 部分 tick 进度（0~1）
     * @return 插值后的渲染节点
     */
    public PathNode getRenderNode(float partialTick) {
        if (historyNodes.size() < 2) {
            return currentPathNode;
        }
        return historyNodes.get(1).lerp(currentPathNode, partialTick);
    }

    /**
     * 设置一组路径节点，将创建一个新的默认名称计划路径。
     *
     * @param nodes 路径节点列表
     */
    public void setPath(List<PathNode> nodes) {
        this.currentPlannedPath = new PlannedPath("default", nodes);
        if (!nodes.isEmpty()) {
            this.currentPathNode = nodes.get(nodes.size() - 1);
        }
    }

    /**
     * 初始化路径节点。
     *
     * @param node 初始节点
     */
    public void init(PathNode node) {
        this.currentPathNode = node;
        this.historyNodes.clear();
        this.historyNodes.addFirst(node);
        this.historyNodes.addFirst(node);
    }

    /** @return 历史节点队列 */
    public LinkedList<PathNode> getHistoryNodes() {
        return historyNodes;
    }

    // ===================== 网络序列化 =====================

    /**
     * 写入射弹的基础同步数据（位置与朝向），并调用子类的附加数据写入。
     *
     * @param buf 数据包缓冲区
     */
    public void writeBase(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(currentPathNode.pos().x());
        buf.writeDouble(currentPathNode.pos().y());
        buf.writeDouble(currentPathNode.pos().z());
        buf.writeFloat(currentPathNode.yaw());
        buf.writeFloat(currentPathNode.pitch());
        buf.writeFloat(currentPathNode.roll());

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

    /**
     * 读取射弹的基础同步数据，并调用子类的附加数据读取。
     *
     * @param buf 数据包缓冲区
     */
    public void readBase(RegistryFriendlyByteBuf buf) {
        this.clientTargetNode = new PathNode(
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(), buf.readFloat(), buf.readFloat()
        );

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

        // 首次同步时，直接将当前位置和历史节点设置为同步位置
        if (!clientInitialized) {
            clientInitialized = true;
            this.currentPathNode = this.clientTargetNode;
            this.historyNodes.clear();
            this.historyNodes.addFirst(this.clientTargetNode);
            this.historyNodes.addFirst(this.clientTargetNode);
        }

        readAdditional(buf);
    }

    // ===================== 便捷访问器 =====================

    /** @return 当前位置（Vec3） */
    public Vec3 getPos() {
        return currentPathNode.pos();
    }

    /** @return 当前偏航角（度） */
    public float getYaw() {
        return currentPathNode.yaw();
    }

    /** @return 当前俯仰角（度） */
    public float getPitch() {
        return currentPathNode.pitch();
    }

    /** @return 当前翻滚角（度） */
    public float getRoll() {
        return currentPathNode.roll();
    }

    /** @return 射弹 UUID */
    public UUID getUuid() {
        return uuid;
    }

    /** 设置 UUID（通常用于从网络数据恢复） */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /** @return 所有者玩家UUID */
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    /** 设置所有者玩家UUID */
    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    /** @return 来源仆从UUID */
    public UUID getSourceServantUuid() {
        return sourceServantUuid;
    }

    /** 设置来源仆从UUID */
    public void setSourceServantUuid(UUID sourceServantUuid) {
        this.sourceServantUuid = sourceServantUuid;
    }

    /** @return 所有者玩家 */
    public Player getOwner() {
        return owner;
    }

    /** @return 当前速度向量 */
    public Vec3 getVelocity() {
        return velocity;
    }

    /** 设置速度向量 */
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    /** @return 当前状态 */
    public ProjectileState getState() {
        return state;
    }

    /** @return 追踪目标UUID */
    public UUID getTargetUuid() {
        return targetUuid;
    }

    /** 设置追踪目标UUID */
    public void setTargetUuid(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    /** @return 缓存的追踪目标 */
    public LivingEntity getCachedTarget() {
        return cachedTarget;
    }

    /** 设置缓存的追踪目标 */
    public void setCachedTarget(LivingEntity cachedTarget) {
        this.cachedTarget = cachedTarget;
    }

    /** @return 黏贴目标UUID */
    public UUID getAttachedTargetUuid() {
        return attachedTargetUuid;
    }

    /** @return 缓存的黏贴目标 */
    public LivingEntity getCachedAttachedTarget() {
        return cachedAttachedTarget;
    }

    /** 设置缓存的黏贴目标 */
    public void setCachedAttachedTarget(LivingEntity cachedAttachedTarget) {
        this.cachedAttachedTarget = cachedAttachedTarget;
    }

    /** @return 拖尾计时器 */
    public int getTrailTimer() {
        return trailTimer;
    }

    /** 设置拖尾计时器 */
    public void setTrailTimer(int trailTimer) {
        this.trailTimer = trailTimer;
    }

    /** @return 阻力系数 */
    public float getDrag() {
        return drag;
    }

    /** 设置阻力系数 */
    public void setDrag(float drag) {
        this.drag = Mth.clamp(drag, 0.0f, 1.0f);
    }

    /** @return 最大速度 */
    public float getMaxSpeed() {
        return maxSpeed;
    }

    /** 设置最大速度 */
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /** @return 期望偏航角 */
    public float getDesiredYaw() {
        return desiredYaw;
    }

    /** @return 期望俯仰角 */
    public float getDesiredPitch() {
        return desiredPitch;
    }

    /** @return 期望滚转角 */
    public float getDesiredRoll() {
        return desiredRoll;
    }

    /**
     * 设置期望的朝向角度（度）。
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
     * 施加一个力，直接修改速度向量。
     *
     * @param force 力向量
     */
    public void applyForce(Vec3 force) {
        this.velocity = this.velocity.add(force);
    }
}