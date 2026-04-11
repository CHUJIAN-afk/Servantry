package first.servantry.api.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.register.ServantType;
import first.servantry.register.AttributeRegister;
import first.servantry.register.DamageRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class Servant {

    private UUID uuid;
    private Player owner;
    private final LinkedList<PathNode> historyNodes = new LinkedList<>();
    private final LinkedList<PathNode> futureNodes = new LinkedList<>();
    private boolean firstSync = true;

    public Servant(PathNode node) {
        this.uuid = UUID.randomUUID();
        this.owner = null;
        this.historyNodes.addFirst(node);
    }

    public abstract void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode);

    public abstract ServantType<? extends Servant> getType();

    public boolean isTarget(LivingEntity target) {
        if (owner != target) {
            boolean isActive0 = target instanceof Enemy;
            boolean isActive1 = target instanceof Targeting targeting && targeting.getTarget() == owner;
            boolean isActive2 = owner.getLastHurtByMob() != null && owner.getLastHurtByMob() == target;
            return isActive0 || isActive1 || isActive2;
        }
        return false;
    }

    public void tick() {
        if (!this.historyNodes.isEmpty()) {
            this.historyNodes.addFirst(this.historyNodes.getFirst());
            if (this.historyNodes.size() > getHistoryNodesSize()) {
                this.historyNodes.removeLast();
            }
        }
        if (!this.futureNodes.isEmpty()) {
            this.historyNodes.set(0, this.futureNodes.poll());
        }
    }

    public DamageSource getDamageSource() {
        Registry<DamageType> damageTypes = getOwner().level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(damageTypes.getHolderOrThrow(DamageRegister.Servant), getOwner());
    }

    public int getHistoryNodesSize() {
        return 16;
    }

    public LinkedList<PathNode> getHistoryNodes() {
        return historyNodes;
    }

    public LinkedList<PathNode> getFutureNodes() {
        return futureNodes;
    }

    public void setPath(List<PathNode> nodes) {
        this.futureNodes.clear();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        int originalSize = nodes.size();
        double speed = 1.0;

        if (getOwner() != null && getOwner().getAttribute(AttributeRegister.ServantSpeed) != null) {
            speed = getOwner().getAttribute(AttributeRegister.ServantSpeed).getValue();
        }

        // 防止异常的负数或零导致游戏崩溃
        speed = Math.max(0.01, speed);

        // 只有1个节点，或者速度几乎没变时，直接装载原节点以节省性能
        if (originalSize < 2 || Math.abs(speed - 1.0) < 0.01) {
            this.futureNodes.addAll(nodes);
            return;
        }

        int newSize = Math.max(2, (int) Math.round(originalSize / speed));
        List<PathNode> resampledNodes = new ArrayList<>(newSize);

        for (int i = 0; i < newSize; i++) {
            float t = i / (float) (newSize - 1);
            float fIdx = t * (originalSize - 1);

            int idx0 = (int) Math.floor(fIdx);
            int idx1 = Math.min(idx0 + 1, originalSize - 1);
            float fraction = fIdx - idx0;

            PathNode p0 = nodes.get(idx0);
            PathNode p1 = nodes.get(idx1);

            // 【核心修正】：直接使用 LERP！
            // 因为原轨迹自带优美的加速度节奏，线性插值可以 100% 完美缩放时间轴，杜绝三次曲线的逆向打结。
            Vec3 pos = p0.pos().lerp(p1.pos(), fraction);

            // 欧拉角使用 SLERP 最短路径插值防万向节死锁
            float yaw = Mth.rotLerp(fraction, p0.yaw(), p1.yaw());
            float pitch = Mth.rotLerp(fraction, p0.pitch(), p1.pitch());
            float roll = Mth.rotLerp(fraction, p0.roll(), p1.roll());

            String feature = fraction < 0.5f ? p0.feature() : p1.feature();
            if (i == 0) feature = nodes.get(0).feature();
            if (i == newSize - 1) feature = nodes.get(originalSize - 1).feature();

            resampledNodes.add(new PathNode(feature, pos, yaw, pitch, roll));
        }

        this.futureNodes.addAll(resampledNodes);
    }

    public boolean isExecutingPath() {
        return !this.futureNodes.isEmpty();
    }

    public void renderInternal(float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        PathNode current = this.historyNodes.getFirst();
        PathNode last = this.historyNodes.size() > 1 ? this.historyNodes.get(1) : current;
        PathNode renderNode = last.lerp(current, partialTick);
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(renderNode.pos().x - cameraPos.x, renderNode.pos().y - cameraPos.y, renderNode.pos().z - cameraPos.z);
        int packedLight = LevelRenderer.getLightColor(owner.level(), BlockPos.containing(renderNode.pos().x, renderNode.pos().y, renderNode.pos().z));
        render(poseStack, bufferSource, partialTick, packedLight, renderNode);
        poseStack.popPose();
    }

    public void writeBase(RegistryFriendlyByteBuf buf) {
        // 同步当前的锚点现实（防漂移纠正基准）
        PathNode current = this.historyNodes.getFirst();
        buf.writeUtf(current.feature());
        buf.writeDouble(current.pos().x);
        buf.writeDouble(current.pos().y);
        buf.writeDouble(current.pos().z);
        buf.writeFloat(current.yaw());
        buf.writeFloat(current.pitch());
        buf.writeFloat(current.roll());
        // 同步未来轨迹
        buf.writeInt(this.futureNodes.size());
        for (PathNode node : this.futureNodes) {
            buf.writeUtf(node.feature());
            buf.writeDouble(node.pos().x);
            buf.writeDouble(node.pos().y);
            buf.writeDouble(node.pos().z);
            buf.writeFloat(node.yaw());
            buf.writeFloat(node.pitch());
            buf.writeFloat(node.roll());
        }
        writeAdditional(buf);
    }

    public void readBase(RegistryFriendlyByteBuf buf) {
        PathNode syncCurrent = new PathNode(
                buf.readUtf(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
        int pathSize = buf.readInt();
        this.futureNodes.clear();
        for (int i = 0; i < pathSize; i++) {
            this.futureNodes.add(new PathNode(
                    buf.readUtf(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat()
            ));
        }
        if (this.firstSync || this.historyNodes.isEmpty() || this.historyNodes.getFirst().pos().distanceToSqr(syncCurrent.pos()) > 100.0) {
            this.historyNodes.clear();
            this.historyNodes.addFirst(syncCurrent);
            this.historyNodes.addFirst(syncCurrent);
            this.firstSync = false;
        }
        readAdditional(buf);
    }

    public void writeAdditional(RegistryFriendlyByteBuf buf) {
    }

    public void readAdditional(RegistryFriendlyByteBuf buf) {
    }

    public LinkedList<PathNode> getPathQueue() {
        return this.futureNodes;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Vec3 getPos() {
        return this.historyNodes.getFirst().pos();
    }

    public void setPos(Vec3 pos) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), pos, n.yaw(), n.pitch(), n.roll()));
    }

    public Vec3 getLastPos() {
        return this.historyNodes.size() > 1 ? this.historyNodes.get(1).pos() : getPos();
    }

    public float getYaw() {
        return this.historyNodes.getFirst().yaw();
    }

    public void setYaw(float yaw) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), n.pos(), yaw, n.pitch(), n.roll()));
    }

    public float getPitch() {
        return this.historyNodes.getFirst().pitch();
    }

    public void setPitch(float pitch) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), n.pos(), n.yaw(), pitch, n.roll()));
    }

    public float getRoll() {
        return this.historyNodes.getFirst().roll();
    }

    public void setRoll(float roll) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), n.pos(), n.yaw(), n.pitch(), roll));
    }

}