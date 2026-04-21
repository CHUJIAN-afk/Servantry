package first.servantry.api.servant;

import first.servantry.api.ai.ServantGoalSelector;
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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class Servant {

    private UUID uuid;
    private Player owner;
    private PlannedPath currentPlannedPath = null;
    private final LinkedList<PathNode> historyNodes = new LinkedList<>();
    private PathNode currentPathNode;
    private PathNode clientTargetNode;
    private final ServantGoalSelector goalSelector = new ServantGoalSelector();
    private LivingEntity target = null;
    private boolean targetChange = false;

    public Servant(PathNode pathNode) {
        this.uuid = UUID.randomUUID();
        this.currentPathNode = pathNode;
        historyNodes.addFirst(pathNode);
        historyNodes.addFirst(pathNode);
        registerGoals(goalSelector);
    }

    public abstract void registerGoals(ServantGoalSelector goalSelector);

    public abstract void writeAdditional(RegistryFriendlyByteBuf buf);

    public abstract void readAdditional(RegistryFriendlyByteBuf buf);

    public abstract float getDamage();

    public abstract float getKnockback();

    public abstract ServantType<? extends Servant> getType();

    public void tick() {
        if (!owner.level().isClientSide()) {
            setTargetChange(false);
            setTarget(searchTarget());
            this.goalSelector.tick();
            if (this instanceof ICollide iCollide) {
                iCollide.processCollision(this);
            }
            if (this.currentPlannedPath != null && !this.currentPlannedPath.isFinished()) {
                this.currentPathNode = this.currentPlannedPath.advance();
            }
        } else {
            this.currentPathNode = clientTargetNode;
        }
        this.historyNodes.addFirst(this.currentPathNode);
        if (this.historyNodes.size() > getHistoryNodesSize()) {
            this.historyNodes.removeLast();
        }
    }

    public int getOrder() {
        return getOwner().getData(AttachmentRegister.ServantData).getOrder(this);
    }

    public void attack(LivingEntity target) {
        int invulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        target.hurt(getDamageSource(), getDamage());
        target.invulnerableTime = invulnerableTime;
    }

    public void setPlannedPath(PlannedPath path) {
        this.currentPlannedPath = path;
    }

    public PlannedPath getCurrentPath() {
        return this.currentPlannedPath;
    }

    public PathNode getCurrentPathNode() {
        return currentPathNode;
    }

    public boolean isExecutingPath() {
        return this.currentPlannedPath != null && !this.currentPlannedPath.isFinished();
    }

    public PathNode getRenderNode(float partialTick) {
        return historyNodes.get(1).lerp(currentPathNode, partialTick);
    }

    public LivingEntity searchTarget() {
        return owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(16)).stream()
                .filter(this::isTarget)
                .findFirst()
                .orElse(null);
    }

    public boolean isTarget(LivingEntity target) {
        if (target != null && owner != target && target.isAlive()) {
            boolean condition0 = target instanceof Enemy;
            boolean condition1 = target instanceof Targeting targeting && targeting.getTarget() == owner;
            boolean condition2 = owner.getLastHurtByMob() == target;
            return condition0 || condition1 || condition2;
        }
        return false;
    }

    public ServantDamageSource getDamageSource() {
        Registry<DamageType> damageTypes = owner.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new ServantDamageSource(damageTypes.getHolderOrThrow(DamageRegister.Servant), null, owner, currentPathNode.pos(), this);
    }

    public void writeBase(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(currentPathNode.pos().x());
        buf.writeDouble(currentPathNode.pos().y());
        buf.writeDouble(currentPathNode.pos().z());
        buf.writeFloat(currentPathNode.yaw());
        buf.writeFloat(currentPathNode.pitch());
        buf.writeFloat(currentPathNode.roll());
        writeAdditional(buf);
    }

    public void readBase(RegistryFriendlyByteBuf buf) {
        this.clientTargetNode = new PathNode(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat(), buf.readFloat(), buf.readFloat());
        readAdditional(buf);
    }

    public int getHistoryNodesSize() {
        return 16;
    }

    public LinkedList<PathNode> getHistoryNodes() {
        return historyNodes;
    }

    public Vec3 getPos() { return currentPathNode.pos(); }
    public float getYaw() { return currentPathNode.yaw(); }
    public float getPitch() { return currentPathNode.pitch(); }
    public float getRoll() { return currentPathNode.roll(); }

    public void setPath(List<PathNode> nodes) {
        this.currentPlannedPath = new PlannedPath("default", nodes);
    }

    public LinkedList<PathNode> getPathQueue() {
        if (currentPlannedPath == null) return new LinkedList<>();
        return new LinkedList<>(currentPlannedPath.getNodes().subList(currentPlannedPath.getCurrentIndex(), currentPlannedPath.getNodes().size()));
    }

    public ServantGoalSelector getGoalSelector() { return goalSelector; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }
    public LivingEntity getTarget() { return target; }

    public void setTarget(LivingEntity target) {
        if (this.target != target) {
            setTargetChange(true);
        }
        this.target = target;
    }

    public boolean isTargetChange() {
        return targetChange;
    }

    public void setTargetChange(boolean targetChange) {
        this.targetChange = targetChange;
    }

    public Vec3 slerpVector(Vec3 v1, Vec3 v2, float t) {
        double dot = Mth.clamp(v1.dot(v2), -1.0, 1.0);
        double theta = Math.acos(dot) * t;
        Vec3 relativeVec = v2.subtract(v1.scale(dot));
        if (relativeVec.lengthSqr() < 1e-5) return v1;
        relativeVec = relativeVec.normalize();
        return v1.scale(Math.cos(theta)).add(relativeVec.scale(Math.sin(theta)));
    }

    public PathNode getEulerNode(Vec3 pos, Vec3 tipDir, Vec3 bladeNormal) {
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

        return new PathNode(pos, yaw, pitch, roll);
    }

}