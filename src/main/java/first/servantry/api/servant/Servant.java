package first.servantry.api.servant;

import first.servantry.api.PathNode;
import first.servantry.api.PlannedPath;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.api.register.ServantType;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.DamageRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 仆从实体的抽象基类，代表一个由玩家拥有、可自主行动并拥有视觉轨迹的“仆从”。
 * <p>
 * 仆从的行为通过 {@link ServantGoalSelector} 驱动，支持路径移动、目标搜索与攻击。
 * 服务端控制逻辑，客户端通过同步数据驱动视觉表现。
 * </p>
 * <p>
 * 子类需要实现：
 * <ul>
 *   <li>{@link #registerGoals(ServantGoalSelector)} - 注册 AI 目标</li>
 *   <li>{@link #getDamage()} / {@link #getKnockback()} - 攻击属性</li>
 *   <li>{@link #getType()} - 返回对应的注册类型</li>
 *   <li>{@link #writeAdditional(RegistryFriendlyByteBuf)} / {@link #readAdditional(RegistryFriendlyByteBuf)} - 自定义数据同步</li>
 * </ul>
 * </p>
 */
public abstract class Servant {

    // ===================== 基础标识字段 =====================

    /** 仆从的唯一标识符，用于网络同步和持久化 */
    private UUID uuid;

    /** 拥有该仆从的玩家 */
    private Player owner;

    // ===================== 路径与历史轨迹 =====================

    /** 当前正在执行的计划路径，若为 null 则表示没有路径任务 */
    private PlannedPath currentPlannedPath = null;

    /**
     * 历史节点队列，用于拖尾渲染。
     * 队列头部为最新节点，尾部为最旧节点。
     * 服务端每 tick 更新，客户端通过网络同步更新。
     */
    private final LinkedList<PathNode> historyNodes = new LinkedList<>();

    /** 服务端当前精确的路径节点（位置 + 旋转） */
    protected PathNode currentPathNode;

    /** 客户端接收到的目标节点，用于插值渲染 */
    private PathNode clientTargetNode;

    /** 标记客户端是否已完成首次位置同步 */
    private boolean clientInitialized = false;

    // ===================== AI 与目标系统 =====================

    /** AI 目标选择器，管理所有 AI 行为的优先级和执行 */
    private final ServantGoalSelector goalSelector = new ServantGoalSelector();

    /** 当前攻击目标（可能为 null） */
    private LivingEntity target = null;

    /** 标记目标是否在本 tick 发生了改变（用于触发响应逻辑） */
    private boolean targetChange = false;

    // ===================== 构造与初始化 =====================

    /**
     * 构造一个仆从，并初始化其起始位置。
     */
    public Servant() {
        this.uuid = UUID.randomUUID();
        this.currentPathNode = new PathNode(Vec3.ZERO, 0, 0, 0);
        historyNodes.addFirst(currentPathNode);
        historyNodes.addFirst(currentPathNode);
        registerGoals(goalSelector);
    }

    // ===================== 抽象方法（子类必须实现） =====================

    /**
     * 注册该仆从的 AI 目标。
     * 子类应在此方法内通过 {@link ServantGoalSelector#addGoal} 添加行为。
     *
     * @param goalSelector 目标选择器实例
     */
    public abstract void registerGoals(ServantGoalSelector goalSelector);

    /**
     * 写入仆从特有的附加同步数据（由子类实现）。
     *
     * @param buf 数据包缓冲区
     */
    public abstract void writeAdditional(RegistryFriendlyByteBuf buf);

    /**
     * 读取仆从特有的附加同步数据（由子类实现）。
     *
     * @param buf 数据包缓冲区
     */
    public abstract void readAdditional(RegistryFriendlyByteBuf buf);

    /**
     * 获取仆从的单次攻击伤害值。
     *
     * @return 伤害数值
     */
    public abstract float getDamage();

    /**
     * 获取仆从攻击造成的击退力度。
     *
     * @return 击退系数
     */
    public abstract float getKnockback();

    /**
     * 返回该仆从对应的注册类型，用于网络序列化与工厂创建。
     *
     * @return 仆从类型
     */
    public abstract ServantType<? extends Servant> getType();

    // ===================== 每 Tick 更新 =====================

    /**
     * 仆从的主更新方法，每 tick 调用一次。
     * <p>
     * 服务端：更新 AI 目标、执行碰撞处理、推进路径节点、更新历史队列。<br>
     * 客户端：使用网络同步的目标节点更新当前视觉节点，并维护历史队列。
     * </p>
     */
    public void tick() {
        if (!owner.level().isClientSide()) {
            // 服务端逻辑
            setTargetChange(false);
            setTarget(searchTarget());
            this.goalSelector.tick();
            if (this instanceof ICollideAttack iCollideAttack) {
                iCollideAttack.processCollision(this);
            }
            if (this.currentPlannedPath != null && !this.currentPlannedPath.isFinished()) {
                this.currentPathNode = this.currentPlannedPath.advance();
            }
        } else {
            // 客户端逻辑：直接使用网络同步来的目标节点
            this.currentPathNode = clientTargetNode;
        }
        // 更新历史轨迹（两端共用）
        this.historyNodes.addFirst(this.currentPathNode);
        if (this.historyNodes.size() > getHistoryNodesSize()) {
            this.historyNodes.removeLast();
        }
    }

    // ===================== 排序与攻击 =====================

    /**
     * 获取该仆从在其所有者仆从列表中的顺序索引。
     *
     * @return 顺序值（通常由 Attachment 管理）
     */
    public int getOrder() {
        return getOwner().getData(AttachmentRegister.ServantData).getOrder(this);
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
        return historyNodes.get(1).lerp(currentPathNode, partialTick);
    }

    /**
     * 设置一组路径节点，将创建一个新的默认名称计划路径。
     *
     * @param nodes 路径节点列表
     */
    public void setPath(List<PathNode> nodes) {
        this.currentPlannedPath = new PlannedPath("default", nodes);
    }

    /**
     * 初始化路径节点
     * @param node
     */
    public void init(PathNode node) {
        this.currentPathNode = node;
        this.historyNodes.clear();
        this.historyNodes.addFirst(node);
        this.historyNodes.addFirst(node);
    }

    /**
     * 获取当前计划路径中尚未执行的剩余节点队列。
     *
     * @return 剩余节点列表（若没有路径则返回空列表）
     */
    public LinkedList<PathNode> getPathQueue() {
        if (currentPlannedPath == null) return new LinkedList<>();
        return new LinkedList<>(currentPlannedPath.getNodes().subList(currentPlannedPath.getCurrentIndex(), currentPlannedPath.getNodes().size()));
    }

    // ===================== 目标搜索 =====================

    /**
     * 在所有者周围搜索一个有效目标。
     * 使用 TargetSelector 进行高性能目标筛选。
     *
     * @return 找到的目标实体，若没有则返回 null
     */
    public LivingEntity searchTarget() {
        return TargetSelector.create(this)
                .maxDistance(getTargetDistance())
                .requireLineOfSight(requireLineOfSight())
                .filter(this::isTarget)
                .preferCloseTo(getPos())
                .preferCurrentTarget(getTarget())
                .find();
    }

    /**
     * 获取仆从搜索目标的最大距离（默认64格）
     * @return 最大距离（平方）
     */
    public int getTargetDistance() {
        return 64;
    }

    /**
     * 搜索目标时是否要求目标可见（默认true）
     * @return 是否要求目标可见
     */
    public boolean requireLineOfSight() {
        return true;
    }

    /**
     * 判断某个生物是否为有效的攻击目标。
     * 有效条件：
     * <ul>
     *   <li>目标非空、非所有者且存活</li>
     *   <li>是敌对生物（{@link Enemy}）</li>
     *   <li>或目标正以所有者为攻击对象</li>
     *   <li>或目标是最后伤害所有者的生物</li>
     * </ul>
     *
     * @param target 待检测的生物
     * @return 是否为有效目标
     */
    public boolean isTarget(LivingEntity target) {
        if (target != null && owner != target && target.isAlive()) {
            boolean condition0 = target instanceof Enemy;
            boolean condition1 = target instanceof Targeting targeting && targeting.getTarget() == owner;
            boolean condition2 = owner.getLastHurtByMob() == target;
            return condition0 || condition1 || condition2;
        }
        return false;
    }

    // ===================== 伤害来源 =====================

    /**
     * 构造仆从专属的伤害来源对象。
     * 伤害类型取自注册表 {@link DamageRegister#Servant}。
     *
     * @return 仆从伤害源
     */
    public ServantDamageSource getDamageSource() {
        Registry<DamageType> damageTypes = owner.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE);
        return new ServantDamageSource(
                damageTypes.getHolderOrThrow(DamageRegister.Servant),
                null,
                owner,
                currentPathNode.pos(),
                this
        );
    }

    // ===================== 网络序列化 =====================

    /**
     * 写入仆从的基础同步数据（位置与朝向），并调用子类的附加数据写入。
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
        writeAdditional(buf);
    }

    /**
     * 读取仆从的基础同步数据，并调用子类的附加数据读取。
     *
     * @param buf 数据包缓冲区
     */
    public void readBase(RegistryFriendlyByteBuf buf) {
        this.clientTargetNode = new PathNode(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat(), buf.readFloat(), buf.readFloat());
        // 首次同步时，直接将当前位置和历史节点设置为同步位置，避免从 (0,0,0) 插值
        if (!clientInitialized) {
            clientInitialized = true;
            this.currentPathNode = this.clientTargetNode;
            this.historyNodes.clear();
            this.historyNodes.addFirst(this.clientTargetNode);
            this.historyNodes.addFirst(this.clientTargetNode);
        }
        readAdditional(buf);
    }

    // ===================== 历史轨迹配置 =====================

    /**
     * 返回历史节点队列的最大容量。
     * 默认 16，子类可重写以改变拖尾渲染的历史长度。
     *
     * @return 队列最大长度
     */
    public int getHistoryNodesSize() {
        return 16;
    }

    /** @return 历史节点队列（不可变视图） */
    public LinkedList<PathNode> getHistoryNodes() {
        return historyNodes;
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

    /** @return AI 目标选择器 */
    public ServantGoalSelector getGoalSelector() {
        return goalSelector;
    }

    /** @return 仆从 UUID */
    public UUID getUuid() {
        return uuid;
    }

    /** 设置 UUID（通常用于从网络数据恢复） */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /** @return 所有者玩家 */
    public Player getOwner() {
        return owner;
    }

    /** 设置所有者玩家 */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /** @return 当前攻击目标 */
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * 设置攻击目标，若目标发生变化则标记 {@link #targetChange} 为 true。
     *
     * @param target 新目标
     */
    public void setTarget(LivingEntity target) {
        if (this.target != target) {
            setTargetChange(true);
        }
        this.target = target;
    }

    /** @return 目标是否在本 tick 发生了变化 */
    public boolean isTargetChange() {
        return targetChange;
    }

    /** 设置目标变化标记 */
    public void setTargetChange(boolean targetChange) {
        this.targetChange = targetChange;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Servant servant)) return false;
        return targetChange == servant.targetChange && Objects.equals(uuid, servant.uuid) && Objects.equals(owner, servant.owner) && Objects.equals(currentPlannedPath, servant.currentPlannedPath) && Objects.equals(historyNodes, servant.historyNodes) && Objects.equals(currentPathNode, servant.currentPathNode) && Objects.equals(clientTargetNode, servant.clientTargetNode) && Objects.equals(goalSelector, servant.goalSelector) && Objects.equals(target, servant.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, owner, currentPlannedPath, historyNodes, currentPathNode, clientTargetNode, goalSelector, target, targetChange);
    }

    // ===================== 数学工具方法 =====================

    /**
     * 对两个向量进行球面线性插值（Slerp）。
     * 用于平滑旋转轨迹的插值。
     *
     * @param v1 起始向量
     * @param v2 终止向量
     * @param t  插值系数（0~1）
     * @return 插值后的单位向量
     */
    public Vec3 slerpVector(Vec3 v1, Vec3 v2, float t) {
        double dot = Mth.clamp(v1.dot(v2), -1.0, 1.0);
        double theta = Math.acos(dot) * t;
        Vec3 relativeVec = v2.subtract(v1.scale(dot));
        if (relativeVec.lengthSqr() < 1e-5) return v1;
        relativeVec = relativeVec.normalize();
        return v1.scale(Math.cos(theta)).add(relativeVec.scale(Math.sin(theta)));
    }

    /**
     * 根据位置、尖端朝向和叶片法向量计算对应的欧拉角节点。
     * <p>
     * 用于将自定义的朝向信息转换为标准的三轴旋转表示。
     * </p>
     *
     * @param pos         位置坐标
     * @param tipDir      尖端方向（局部 Z 轴）
     * @param bladeNormal 叶片法向量（用于计算滚转角）
     * @return 包含计算出的偏航、俯仰、滚转角的节点
     */
    public PathNode getEulerNode(Vec3 pos, Vec3 tipDir, Vec3 bladeNormal) {
        if (tipDir.lengthSqr() < 1e-4) tipDir = new Vec3(0, 0, 1);
        tipDir = tipDir.normalize();

        // 计算偏航和俯仰
        float yaw = (float) (Math.atan2(-tipDir.x, tipDir.z) * (180D / Math.PI));
        double horiz = Math.sqrt(tipDir.x * tipDir.x + tipDir.z * tipDir.z);
        float pitch = (float) (Math.atan2(-tipDir.y, horiz) * (180D / Math.PI));

        // 计算默认的上方向（基于偏航俯仰后的 Y 轴）
        Vec3 defaultUp = new Vec3(0, 1, 0)
                .xRot((float) Math.toRadians(pitch))
                .yRot((float) Math.toRadians(yaw));
        Vec3 projNormal = bladeNormal.subtract(tipDir.scale(bladeNormal.dot(tipDir))).normalize();
        if (projNormal.lengthSqr() < 1e-4) projNormal = defaultUp;

        // 计算滚转角（绕 Z 轴旋转）
        double dot = defaultUp.dot(projNormal);
        Vec3 cross = defaultUp.cross(projNormal);
        float roll = (float) (Math.atan2(cross.dot(tipDir), dot) * (180D / Math.PI));

        return new PathNode(pos, yaw, pitch, roll);
    }

}