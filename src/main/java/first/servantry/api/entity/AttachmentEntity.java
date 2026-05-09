package first.servantry.api.entity;

import first.servantry.api.PathNode;
import first.servantry.api.PlannedPath;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 附件实体抽象基类，为仆从和射弹提供统一的存储架构和渲染支持。
 * <p>
 * 附件实体采用"数据存储在玩家附件中"的设计模式，而非传统的独立实体。
 * 这种设计适合大量小型战斗单位，避免了实体注册和同步的复杂性。
 * </p>
 *
 * <h3>核心架构</h3>
 * <ul>
 *   <li><b>服务端</b>：执行行为逻辑（AI、物理），更新路径节点</li>
 *   <li><b>客户端</b>：接收网络同步数据，渲染视觉表现</li>
 *   <li><b>数据同步</b>：通过玩家附件数据包自动同步</li>
 * </ul>
 *
 * <h3>子类分类</h3>
 * <ul>
 *   <li>{@link first.servantry.api.servant.Servant} - 仆从：AI驱动，自主行动，碰撞攻击</li>
 *   <li>{@link first.servantry.api.projectile.Projectile} - 射弹：动量物理，追踪目标，可黏贴</li>
 * </ul>
 *
 * <h3>子类需实现的方法</h3>
 * <ul>
 *   <li>{@link #tick()} - 每tick更新逻辑（服务端行为 + 客户端渲染）</li>
 *   <li>{@link #getDamage()} - 返回伤害值</li>
 *   <li>{@link #getType()} - 返回注册类型</li>
 *   <li>{@link #writeAdditional(RegistryFriendlyByteBuf)} / {@link #readAdditional(RegistryFriendlyByteBuf)} - 自定义数据同步</li>
 * </ul>
 *
 * @see first.servantry.api.servant.Servant
 * @see first.servantry.api.projectile.Projectile
 */
public abstract class AttachmentEntity {

    // ===================== 基础标识 =====================

    /** 实体的唯一标识符，用于网络同步和持久化识别 */
    protected UUID uuid;

    /** 拥有该实体的玩家引用，由附件数据管理器在tick前设置 */
    protected Player owner;

    // ===================== 路径与轨迹 =====================

    /** 当前正在执行的计划路径，null 表示无路径任务 */
    protected PlannedPath currentPlannedPath = null;

    /**
     * 历史节点队列，用于拖尾渲染。
     * <p>
     * 队列结构：头部为最新节点，尾部为最旧节点。
     * 容量由 {@link #getHistoryNodesSize()} 控制。
     * </p>
     */
    protected final ArrayList<PathNode> historyNodes = new ArrayList<>();

    /** 服务端当前精确的路径节点（位置 + 三轴旋转） */
    protected PathNode currentPathNode;

    /** 客户端接收到的目标节点，用于插值渲染 */
    protected PathNode clientTargetNode;

    /** 标记客户端是否已完成首次位置同步 */
    protected boolean clientInitialized = false;

    protected boolean remove = false;

    // ===================== 构造方法 =====================

    /**
     * 构造附件实体，初始化默认状态。
     * <p>
     * 默认位置为零点，历史队列初始化为两个相同节点以保证渲染安全。
     * </p>
     */
    public AttachmentEntity() {
        this.uuid = UUID.randomUUID();
        this.currentPathNode = new PathNode(Vec3.ZERO, 0, 0, 0);
        historyNodes.addFirst(currentPathNode);
        historyNodes.addFirst(currentPathNode);
    }

    // ===================== 抽象方法 =====================

    /**
     * 每tick更新方法，由附件数据管理器调用。
     * <p>
     * 实现模式：
     * <ul>
     *   <li><b>服务端</b>：执行行为逻辑、物理更新、路径推进，更新历史节点</li>
     *   <li><b>客户端</b>：使用 clientTargetNode 更新 currentPathNode，更新历史节点</li>
     * </ul>
     * </p>
     */
    @SuppressWarnings("unchecked")
    public void tick() {
        if (!owner.level().isClientSide()) {
            // 路径推进
            if (currentPlannedPath != null && !currentPlannedPath.isFinished()) {
                currentPathNode = currentPlannedPath.advance();
            }
            // 方块碰撞检测（使用历史轨迹的上一tick位置）
            if (this instanceof IBlockCollision<?> blockCollision) {
                ((IBlockCollision<AttachmentEntity>) blockCollision).processBlockCollision(this);
            }
            // 碰撞攻击检测
            if (this instanceof ICollideAttack<?> collideAttack) {
                ((ICollideAttack<AttachmentEntity>) collideAttack).processCollision(this);
            }
        }
        // 更新历史轨迹
        updateHistoryNodes();
    }

    /**
     * 获取实体的单次攻击伤害值。
     *
     * @return 伤害数值
     */
    public abstract float getDamage();

    /**
     * 返回该实体对应的注册类型，用于网络序列化与工厂创建。
     *
     * @return 实体类型
     */
    public abstract EntityType<? extends AttachmentEntity> getType();

    /**
     * 写入实体特有的附加同步数据。
     * <p>
     * 在 {@link #writeBase(RegistryFriendlyByteBuf)} 中被调用，
     * 子类应在此写入自定义字段（如状态、目标UUID等）。
     * </p>
     *
     * @param buf 数据包缓冲区
     */
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
    }

    /**
     * 读取实体特有的附加同步数据。
     * <p>
     * 在 {@link #readBase(RegistryFriendlyByteBuf)} 中被调用，
     * 子类应在此读取自定义字段，顺序需与 writeAdditional 一致。
     * </p>
     *
     * @param buf 数据包缓冲区
     */
    public void readAdditional(RegistryFriendlyByteBuf buf) {
    }

    // ===================== 路径管理 =====================

    /**
     * 设置当前要执行的计划路径。
     *
     * @param path 计划路径
     */
    public void setPlannedPath(PlannedPath path) {
        this.currentPlannedPath = path;
    }

    /**
     * 获取当前正在执行的路径。
     *
     * @return 当前路径，可能为 null
     */
    public PlannedPath getCurrentPath() {
        return this.currentPlannedPath;
    }

    /**
     * 获取当前所在的路径节点。
     *
     * @return 当前节点
     */
    public PathNode getCurrentPathNode() {
        return currentPathNode;
    }

    /**
     * 检查是否正在执行路径且未完成。
     *
     * @return 是否正在执行路径
     */
    public boolean isExecutingPath() {
        return this.currentPlannedPath != null && !this.currentPlannedPath.isFinished();
    }

    /**
     * 设置一组路径节点，创建默认计划路径。
     *
     * @param nodes 路径节点列表
     */
    public void setPath(List<PathNode> nodes) {
        this.currentPlannedPath = new PlannedPath("default", nodes);
        if (!nodes.isEmpty()) {
            this.currentPathNode = nodes.getLast();
        }
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove() {
        this.remove = true;
    }

    /**
     * 初始化实体的路径节点和历史队列。
     * <p>
     * 通常在实体创建或重新定位时调用。
     * </p>
     *
     * @param node 初始节点
     */
    public void init(PathNode node) {
        this.currentPathNode = node;
        this.historyNodes.clear();
        this.historyNodes.addFirst(node);
        this.historyNodes.addFirst(node);
    }

    // ===================== 历史轨迹 =====================

    /**
     * 返回历史节点队列的最大容量。
     * <p>
     * 子类可重写以改变拖尾渲染的历史长度。
     * 默认 16，射弹通常使用较小的值（如 8）。
     * </p>
     *
     * @return 队列最大长度
     */
    public int getHistoryNodesSize() {
        return 16;
    }

    /**
     * 获取历史节点队列。
     *
     * @return 历史节点队列
     */
    public ArrayList<PathNode> getHistoryNodes() {
        return historyNodes;
    }

    /**
     * 更新历史节点队列，将当前节点添加到头部。
     * <p>
     * 应在每tick结束时调用，用于拖尾渲染。
     * 队列超出容量时自动移除尾部节点。
     * </p>
     */
    protected void updateHistoryNodes() {
        this.historyNodes.addFirst(this.currentPathNode);
        if (this.historyNodes.size() > getHistoryNodesSize()) {
            this.historyNodes.removeLast();
        }
    }

    // ===================== 渲染支持 =====================

    /**
     * 获取用于渲染的插值节点。
     * <p>
     * 使用历史队列中最近两个节点进行线性插值，
     * 使视觉运动更平滑，消除帧间跳跃。
     * </p>
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

    // ===================== 网络序列化 =====================

    /**
     * 写入实体的基础同步数据（位置与朝向），并调用子类的附加数据写入。
     * <p>
     * 基础数据包含：位置（x, y, z）、旋转（yaw, pitch, roll）。
     * </p>
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
     * 读取实体的基础同步数据，并调用子类的附加数据读取。
     * <p>
     * 首次同步时，会初始化当前位置和历史队列。
     * </p>
     *
     * @param buf 数据包缓冲区
     */
    public void readBase(RegistryFriendlyByteBuf buf) {
        this.clientTargetNode = new PathNode(
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(), buf.readFloat(), buf.readFloat()
        );

        // 首次同步时初始化位置
        if (!clientInitialized) {
            clientInitialized = true;
            init(clientTargetNode);
        }

        readAdditional(buf);
    }

    // ===================== 便捷访问器 =====================

    /** @return 当前位置 */
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

    /** @return 实体 UUID */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * 设置 UUID，用于从网络数据恢复。
     *
     * @param uuid UUID值
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /** @return 所有者玩家 */
    public Player getOwner() {
        return owner;
    }

    /**
     * 设置所有者玩家，由附件数据管理器调用。
     *
     * @param owner 玩家实例
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }
}