package first.servantry.api.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.PathNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class AdvancedProjectile {

    private UUID uuid;
    private final Level level;

    // 主人字段
    private final Player owner;

    private int tickCount = 0;
    private int maxAge = 200;
    private boolean removed = false;

    private final LinkedList<PathNode> historyNodes = new LinkedList<>();
    private final LinkedList<PathNode> futureNodes = new LinkedList<>();
    private boolean firstSync = true;

    // --- 动量属性 ---
    private Vec3 velocity = Vec3.ZERO;

    public AdvancedProjectile(Level level, Player owner, PathNode startNode) {
        this.uuid = UUID.randomUUID();
        this.level = level;
        this.owner = owner;
        this.historyNodes.addFirst(startNode != null ? startNode : PathNode.Empty);
    }

    public abstract ProjectileType<? extends AdvancedProjectile> getType();

    public abstract void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode);

    // --- 主人与伤害源 ---
    public Player getOwner() {
        return this.owner;
    }

    // --- 动量核心方法 ---
    public Vec3 getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    public float getMaxSpeed() {
        return 3.0f; // 默认最大速度，可被子类重写
    }

    public float getFriction() {
        return 0.99f; // 默认摩擦力，可被子类重写
    }

    public void applyForce(Vec3 force) {
        setVelocity(getVelocity().add(force));
    }

    public void processMomentum() {
        Vec3 vel = getVelocity();
        if (vel.lengthSqr() > getMaxSpeed() * getMaxSpeed()) {
            vel = vel.normalize().scale(getMaxSpeed());
        }

        Vec3 nextPos = getPos().add(vel);

        float yaw = getYaw();
        float pitch = getPitch();

        // 自动根据速度向量推导朝向
        if (vel.lengthSqr() > 1e-5) {
            Vec3 dir = vel.normalize();
            yaw = (float) (Math.atan2(-dir.x, dir.z) * (180D / Math.PI));
            double horiz = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
            pitch = (float) (Math.atan2(-dir.y, horiz) * (180D / Math.PI));
        }

        setPath(Collections.singletonList(
                new PathNode("", nextPos, yaw, pitch, getRoll())
        ));

        setVelocity(vel.scale(getFriction()));
    }
    // ---------------------------------------------

    public void tick() {
        if (removed) {
            return;
        }

        // 服务端主人离线则销毁
        if (getOwner() == null && !level.isClientSide()) {
            discard();
            return;
        }

        tickCount++;
        if (tickCount >= maxAge) {
            discard();
            return;
        }

        if (!this.historyNodes.isEmpty()) {
            this.historyNodes.addFirst(this.historyNodes.getFirst());
            if (this.historyNodes.size() > getHistoryNodesSize()) {
                this.historyNodes.removeLast();
            }
        }

        if (!this.futureNodes.isEmpty()) {
            PathNode consumed = this.futureNodes.poll();
            this.historyNodes.set(0, consumed);
            onPathNodeConsumed(consumed);
        }

        if (this instanceof IProjectileCollider iProjectileCollider) {
            iProjectileCollider.processCollision(this);
        }

        // 动量推算
        if (getFutureNodes().isEmpty()) {
            processMomentum();
        }
    }

    public void onPathNodeConsumed(PathNode node) {}

    public void renderInternal(float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        PathNode current = this.historyNodes.getFirst();
        PathNode last = this.historyNodes.size() > 1 ? this.historyNodes.get(1) : current;
        PathNode renderNode = last.lerp(current, partialTick);

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(renderNode.pos().x - cameraPos.x, renderNode.pos().y - cameraPos.y, renderNode.pos().z - cameraPos.z);

        int packedLight = LevelRenderer.getLightColor(level, BlockPos.containing(renderNode.pos()));
        render(poseStack, bufferSource, partialTick, packedLight, renderNode);

        // 渲染圆锥形轨迹
        if (this instanceof IProjectileConeTrail iProjectileConeTrail) {
            iProjectileConeTrail.processTrailRender(poseStack, bufferSource, partialTick, this, renderNode);
        }

        if (this instanceof IProjectileCollider iProjectileCollider) {
            if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                iProjectileCollider.renderDebugHitbox(poseStack, bufferSource, renderNode.yaw(), renderNode.pitch(), renderNode.roll());
            }
        }
        poseStack.popPose();
    }

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

        // 同步速度以维持客户端的平滑预测
        buf.writeDouble(this.velocity.x);
        buf.writeDouble(this.velocity.y);
        buf.writeDouble(this.velocity.z);

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

        this.velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());

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

    public int getHistoryNodesSize() { return 16; }
    public void discard() { this.removed = true; }
    public boolean isRemoved() { return removed; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public Level getLevel() { return level; }
    public int getTickCount() { return tickCount; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public LinkedList<PathNode> getHistoryNodes() { return historyNodes; }
    public LinkedList<PathNode> getFutureNodes() { return futureNodes; }

    public void setPath(List<PathNode> nodes) {
        this.futureNodes.clear();
        if (nodes != null) {
            this.futureNodes.addAll(nodes);
        }
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