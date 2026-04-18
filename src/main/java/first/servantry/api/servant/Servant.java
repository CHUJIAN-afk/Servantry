package first.servantry.api.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.ai.ActionController;
import first.servantry.api.ai.ServantAction;
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
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
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

    protected int targetId = -1;
    protected int stateTick = 0;

    // 【新增】：下沉到基类的动作控制器
    protected ActionController<?> ai;

    public Servant(PathNode node) {
        this.uuid = UUID.randomUUID();
        this.owner = null;
        this.historyNodes.addFirst(node);
    }

    public abstract float getBaseDamage();
    public abstract float getBaseKnockback();
    public abstract void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode);
    public abstract ServantType<? extends Servant> getType();
    public void onPathNodeConsumed(PathNode node) {}

    // 【新增】：强制子类提供实例化特定 ID Action 的工厂方法
    public abstract ServantAction<?> createAction(String id);

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
            PathNode consumed = this.futureNodes.poll();
            this.historyNodes.set(0, consumed);
            this.onPathNodeConsumed(consumed);
        }
    }

    public ServantDamageSource getDamageSource() {
        Registry<DamageType> damageTypes = getOwner().level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new ServantDamageSource(damageTypes.getHolderOrThrow(DamageRegister.Servant), null, getOwner(), getPos(), this);
    }

    public int getHistoryNodesSize() { return 16; }
    public LinkedList<PathNode> getHistoryNodes() { return historyNodes; }
    public LinkedList<PathNode> getFutureNodes() { return futureNodes; }

    public void setPath(List<PathNode> nodes) {
        this.futureNodes.clear();
        if (nodes == null || nodes.isEmpty()) return;

        int originalSize = nodes.size();
        double speed = 1.0;

        if (getOwner() != null && getOwner().getAttribute(AttributeRegister.ServantSpeed) != null) {
            speed = getOwner().getAttribute(AttributeRegister.ServantSpeed).getValue();
        }
        speed = Math.max(0.01, speed);

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

            Vec3 pos = p0.pos().lerp(p1.pos(), fraction);
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

    public boolean isExecutingPath() { return !this.futureNodes.isEmpty(); }

    public LivingEntity getTarget() {
        if (owner == null || targetId == -1) return null;
        Entity e = owner.level().getEntity(targetId);
        if (e instanceof LivingEntity le && le.isAlive()) return le;
        targetId = -1;
        return null;
    }

    public void setTarget(LivingEntity target) {
        this.targetId = target != null ? target.getId() : -1;
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
        PathNode current = this.historyNodes.getFirst();
        buf.writeUtf(current.feature());
        buf.writeDouble(current.pos().x);
        buf.writeDouble(current.pos().y);
        buf.writeDouble(current.pos().z);
        buf.writeFloat(current.yaw());
        buf.writeFloat(current.pitch());
        buf.writeFloat(current.roll());

        buf.writeInt(this.targetId);
        buf.writeInt(this.stateTick);

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

        // 【核心修改】：序列化写入当前的 Action ID
        buf.writeUtf(this.ai != null && this.ai.getCurrentAction() != null ? this.ai.getCurrentAction().getId() : "idle");

        writeAdditional(buf);
    }

    @SuppressWarnings("unchecked")
    public void readBase(RegistryFriendlyByteBuf buf) {
        PathNode syncCurrent = new PathNode(
                buf.readUtf(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );

        this.targetId = buf.readInt();
        this.stateTick = buf.readInt();

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

        // 【核心修改】：读取 Action ID 并执行幽灵同步更新客户端状态
        String actionId = buf.readUtf();
        if (this.ai != null) {
            if (this.ai.getCurrentAction() == null || !this.ai.getCurrentAction().getId().equals(actionId)) {
                ((ActionController<Servant>) this.ai).setClientAction((ServantAction<Servant>) this.createAction(actionId));
            }
        }

        if (this.firstSync || this.historyNodes.isEmpty() || this.historyNodes.getFirst().pos().distanceToSqr(syncCurrent.pos()) > 100.0) {
            this.historyNodes.clear();
            this.historyNodes.addFirst(syncCurrent);
            this.historyNodes.addFirst(syncCurrent);
            this.firstSync = false;
        }
        readAdditional(buf);
    }

    public void writeAdditional(RegistryFriendlyByteBuf buf) {}
    public void readAdditional(RegistryFriendlyByteBuf buf) {}

    public LinkedList<PathNode> getPathQueue() { return this.futureNodes; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }
    public Vec3 getPos() { return this.historyNodes.getFirst().pos(); }
    public void setPos(Vec3 pos) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), pos, n.yaw(), n.pitch(), n.roll()));
    }
    public Vec3 getLastPos() { return this.historyNodes.size() > 1 ? this.historyNodes.get(1).pos() : getPos(); }
    public float getYaw() { return this.historyNodes.getFirst().yaw(); }
    public void setYaw(float yaw) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), n.pos(), yaw, n.pitch(), n.roll()));
    }
    public float getPitch() { return this.historyNodes.getFirst().pitch(); }
    public void setPitch(float pitch) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), n.pos(), n.yaw(), pitch, n.roll()));
    }
    public float getRoll() { return this.historyNodes.getFirst().roll(); }
    public void setRoll(float roll) {
        PathNode n = this.historyNodes.getFirst();
        this.historyNodes.set(0, new PathNode(n.feature(), n.pos(), n.yaw(), n.pitch(), roll));
    }

    public Vec3 slerpVector(Vec3 v1, Vec3 v2, float t) {
        double dot = Mth.clamp(v1.dot(v2), -1.0, 1.0);
        double theta = Math.acos(dot) * t;
        Vec3 relativeVec = v2.subtract(v1.scale(dot));
        if (relativeVec.lengthSqr() < 1e-5) return v1;
        relativeVec = relativeVec.normalize();
        return v1.scale(Math.cos(theta)).add(relativeVec.scale(Math.sin(theta)));
    }

    public PathNode getEulerNode(Vec3 pos, Vec3 tipDir, Vec3 bladeNormal, String feature) {
        if (tipDir.lengthSqr() < 1e-4) tipDir = new Vec3(0, 0, 1);
        tipDir = tipDir.normalize();

        float yaw = (float) (Math.atan2(-tipDir.x, tipDir.z) * (180D / Math.PI));
        double horiz = Math.sqrt(tipDir.x * tipDir.x + tipDir.z * tipDir.z);
        float pitch = (float) (Math.atan2(-tipDir.y, horiz) * (180D / Math.PI));

        Vec3 defaultUp = new Vec3(0, 1, 0).xRot((float) Math.toRadians(pitch)).yRot((float) Math.toRadians(yaw));
        Vec3 projNormal = bladeNormal.subtract(tipDir.scale(bladeNormal.dot(tipDir))).normalize();
        if (projNormal.lengthSqr() < 1e-4) projNormal = defaultUp;

        double dot = defaultUp.dot(projNormal);
        Vec3 cross = defaultUp.cross(projNormal);
        float roll = (float) (Math.atan2(cross.dot(tipDir), dot) * (180D / Math.PI));

        return new PathNode(feature == null ? "" : feature, pos, yaw, pitch, roll);
    }

}