package first.servantry.api.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.PathNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class AdvancedProjectile {

    private UUID uuid;
    private Level level;
    private LivingEntity owner;

    private int tickCount = 0;
    private int maxAge = 200;
    private boolean removed = false;

    private final LinkedList<PathNode> historyNodes = new LinkedList<>();
    private final LinkedList<PathNode> futureNodes = new LinkedList<>();
    private boolean firstSync = true;

    public AdvancedProjectile(Level level, LivingEntity owner, PathNode startNode) {
        this.uuid = UUID.randomUUID();
        this.level = level;
        this.owner = owner;
        this.historyNodes.addFirst(startNode);
    }

    public abstract ProjectileType<? extends AdvancedProjectile> getType();

    // 子类实现具体的模型渲染
    public abstract void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode);

    // 核心生命周期
    public void tick() {
        if (removed) return;

        tickCount++;
        if (tickCount >= maxAge) {
            discard();
            return;
        }

        // 1. 推进历史队列
        if (!this.historyNodes.isEmpty()) {
            this.historyNodes.addFirst(this.historyNodes.getFirst());
            if (this.historyNodes.size() > getHistoryNodesSize()) {
                this.historyNodes.removeLast();
            }
        }

        // 2. 消费未来队列
        if (!this.futureNodes.isEmpty()) {
            PathNode consumed = this.futureNodes.poll();
            this.historyNodes.set(0, consumed);
            onPathNodeConsumed(consumed);
        }
    }

    // ========== 开放给子类的事件钩子 ==========

    /**
     * 当路径节点被消费时触发，可用于触发 "fire" 等自定义特征事件
     */
    public void onPathNodeConsumed(PathNode node) {}

    // =======================================

    public void renderInternal(float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        PathNode current = this.historyNodes.getFirst();
        PathNode last = this.historyNodes.size() > 1 ? this.historyNodes.get(1) : current;
        PathNode renderNode = last.lerp(current, partialTick);

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(renderNode.pos().x - cameraPos.x, renderNode.pos().y - cameraPos.y, renderNode.pos().z - cameraPos.z);

        int packedLight = LevelRenderer.getLightColor(level, BlockPos.containing(renderNode.pos()));
        render(poseStack, bufferSource, partialTick, packedLight, renderNode);

        poseStack.popPose();
    }

    // ========== 序列化与反序列化 ==========

    public void writeSyncData(RegistryFriendlyByteBuf buf) {
        PathNode current = this.historyNodes.getFirst();
        buf.writeUtf(current.feature());
        buf.writeDouble(current.pos().x); buf.writeDouble(current.pos().y); buf.writeDouble(current.pos().z);
        buf.writeFloat(current.yaw()); buf.writeFloat(current.pitch()); buf.writeFloat(current.roll());

        buf.writeBoolean(removed);
        buf.writeInt(this.tickCount);

        buf.writeInt(this.futureNodes.size());
        for (PathNode node : this.futureNodes) {
            buf.writeUtf(node.feature());
            buf.writeDouble(node.pos().x); buf.writeDouble(node.pos().y); buf.writeDouble(node.pos().z);
            buf.writeFloat(node.yaw()); buf.writeFloat(node.pitch()); buf.writeFloat(node.roll());
        }
        writeAdditional(buf);
    }

    public void readSyncData(RegistryFriendlyByteBuf buf) {
        PathNode syncCurrent = new PathNode(buf.readUtf(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat(), buf.readFloat(), buf.readFloat());

        this.removed = buf.readBoolean();
        this.tickCount = buf.readInt();

        int pathSize = buf.readInt();
        this.futureNodes.clear();
        for (int i = 0; i < pathSize; i++) {
            this.futureNodes.add(new PathNode(buf.readUtf(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat(), buf.readFloat(), buf.readFloat()));
        }

        if (this.firstSync || this.historyNodes.isEmpty() || this.historyNodes.getFirst().pos().distanceToSqr(syncCurrent.pos()) > 100.0) {
            this.historyNodes.clear();
            this.historyNodes.addFirst(syncCurrent);
            this.historyNodes.addFirst(syncCurrent);
            this.firstSync = false;
        }
        readAdditional(buf);
    }

    protected void writeAdditional(RegistryFriendlyByteBuf buf) {}
    protected void readAdditional(RegistryFriendlyByteBuf buf) {}

    // ========== Getters & Setters ==========

    public int getHistoryNodesSize() { return 16; }
    public void discard() { this.removed = true; }
    public boolean isRemoved() { return removed; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public Level getLevel() { return level; }
    public LivingEntity getOwner() { return owner; }
    public int getTickCount() { return tickCount; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public LinkedList<PathNode> getHistoryNodes() { return historyNodes; }
    public LinkedList<PathNode> getFutureNodes() { return futureNodes; }

    public void setPath(List<PathNode> nodes) {
        this.futureNodes.clear();
        if (nodes != null) this.futureNodes.addAll(nodes);
    }

    public Vec3 getPos() { return this.historyNodes.getFirst().pos(); }
    public void setPos(Vec3 pos) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), pos, n.yaw(), n.pitch(), n.roll()));
    }
    public float getYaw() { return this.historyNodes.getFirst().yaw(); }
    public float getPitch() { return this.historyNodes.getFirst().pitch(); }
    public float getRoll() { return this.historyNodes.getFirst().roll(); }
}