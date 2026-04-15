package first.servantry.api.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.ServantDamageSource;
import first.servantry.api.register.ServantType;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public abstract class Servant {

    private UUID uuid;
    private Player owner;
    private final LinkedList<PathNode> historyNodes = new LinkedList<>();
    private final LinkedList<PathNode> futureNodes = new LinkedList<>();
    private boolean firstSync = true;

    // ========================================================
    // 【底层 AI 状态管理】：规范化游戏开发中的状态机数据
    // ========================================================
    protected int targetId = -1;
    protected int stateTick = 0;

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

    public boolean isExecutingPath() {
        return !this.futureNodes.isEmpty();
    }

    // ========================================================================
    // 🧠 统一 AI 感知系统：所有仆从共享的感知大脑
    // ========================================================================

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

    /**
     * AI 感知方法：使用高性能管理器缓存查询最优目标
     */
    public void findNewTarget(double maxRange, boolean requireVisibility) {
        if (owner == null) return;
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        List<LivingEntity> targets = data.getNearbyTargets(owner, this, maxRange, requireVisibility);

        if (targets.isEmpty()) {
            setTarget(null);
            return;
        }

        int order = data.getOrder(this);
        targets.sort(Comparator.comparingDouble(e -> {
            double score = e.distanceToSqr(getPos());
            if (e.distanceToSqr(owner) < 36.0) score -= 10000.0;
            if (e.getId() == targetId) score -= 1000.0;
            // 阵型防扎堆偏移
            score += ((e.getId() * 31 + order * 17) % 5) * 40.0;
            return score;
        }));

        setTarget(targets.getFirst());
    }

    // ========================================================================
    // 🛠 通用底层行动控制辅助方法 (大大降低子类开发难度)
    // ========================================================================

    public Vec3 getCurrentTip() {
        return Vec3.directionFromRotation(this.getPitch(), this.getYaw()).normalize();
    }

    public Vec3 getCurrentNormal() {
        Quaternionf q = new Quaternionf()
                .rotateY((float) Math.toRadians(-this.getYaw()))
                .rotateX((float) Math.toRadians(this.getPitch()))
                .rotateZ((float) Math.toRadians(this.getRoll()));
        Vector3f upV = new Vector3f(0, 1, 0).rotate(q);
        return new Vec3(upV.x(), upV.y(), upV.z()).normalize();
    }

    public Vec3 getCurrentVelocityDir() {
        if (this.historyNodes.size() > 1) {
            Vec3 vel = this.getPos().subtract(this.historyNodes.get(1).pos());
            if (vel.lengthSqr() > 1e-5) return vel.normalize();
        }
        return getCurrentTip();
    }

    public double getCurrentSpeed() {
        if (this.historyNodes.size() > 1) {
            return this.getPos().subtract(this.historyNodes.get(1).pos()).length();
        }
        return 1.0;
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

    public List<PathNode> buildBezierPath(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, int ticks, Vec3 startTip, Vec3 endTip, Vec3 startNormal, Vec3 endNormal, String startFeature) {
        List<PathNode> nodes = new ArrayList<>(ticks);
        for (int i = 1; i <= ticks; i++) {
            float t = (float) i / ticks;
            float mt = 1.0f - t;
            Vec3 pos = p0.scale(mt * mt * mt).add(p1.scale(3 * mt * mt * t)).add(p2.scale(3 * mt * t * t)).add(p3.scale(t * t * t));
            Vec3 tip = slerpVector(startTip, endTip, t);
            Vec3 normal = slerpVector(startNormal, endNormal, t);
            nodes.add(getEulerNode(pos, tip, normal, i == 1 ? startFeature : null));
        }
        return nodes;
    }

    public List<PathNode> buildLinearPath(Vec3 start, Vec3 end, int ticks, Vec3 startTip, Vec3 endTip, Vec3 startNormal, Vec3 endNormal, String startFeature, boolean easeIn, boolean easeOut) {
        List<PathNode> nodes = new ArrayList<>(ticks);
        for (int i = 1; i <= ticks; i++) {
            float t = (float) i / ticks;
            float easeT = t;
            if (easeIn && !easeOut) easeT = t * t;
            else if (!easeIn && easeOut) easeT = t * (2.0f - t);
            else if (easeIn && easeOut) easeT = t * t * (3.0f - 2.0f * t);

            Vec3 pos = start.lerp(end, easeT);
            Vec3 tip = slerpVector(startTip, endTip, easeT);
            Vec3 normal = slerpVector(startNormal, endNormal, easeT);
            nodes.add(getEulerNode(pos, tip, normal, i == 1 ? startFeature : null));
        }
        return nodes;
    }

    // ========================================================================
    // 渲染与网络同步
    // ========================================================================

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

        // 写入AI基础数据
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

        // 读入AI基础数据
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

    // Getters & Setters
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
}