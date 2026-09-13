package first.summoner.common.master;

import first.lyra.common.entity.PathNode;
import first.lyra.common.entity.PlannedPath;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.network.SyncFieldPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractMaster extends Mob implements Enemy {

    private static final String PHYSICS_PATH_IDENTIFIER = "physics";
    private static final double MIN_MOVEMENT_SQR = 1.0E-10;

    protected final ArrayList<PathNode> historyNodes = new ArrayList<>();
    protected final AtomicReference<List<byte[]>> pendingPayloads = new AtomicReference<>(List.of());
    protected PlannedPath currentPlannedPath = null;
    protected SyncFieldDispatcher syncFields = null;

    protected PathNode currentPathNode = new PathNode(Vec3.ZERO, 0.0F, 0.0F, 0.0F);
    private Vec3 velocity = Vec3.ZERO;
    private float drag = 0.95F;
    private float gravity = 0.0F;
    private boolean applyingPathPosition = false;

    protected AbstractMaster(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
        resetHistory(currentPathNode);
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (!isExecutingPath()) {
                tickPhysics();
            }
        } else {
            applyPendingSync();
        }

        super.tick();

        if (!level().isClientSide()) {
            if (currentPlannedPath != null && !currentPlannedPath.isFinished()) {
                currentPathNode = currentPlannedPath.advance();
            }
            syncEntityToPathNode();

            byte[] payload = encodeSyncPayload(false);
            if (hasFieldChanges(payload)) {
                PacketDistributor.sendToPlayersTrackingEntity(this, new SyncFieldPacket(getId(), payload));
            }
        }

        historyNodes.addFirst(currentPathNode);
        if (historyNodes.size() > getHistoryNodesSize()) {
            historyNodes.removeLast();
        }
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        // 位移完全由路径节点推进，禁用原版重力、阻力和移动速度。
    }

    private void tickPhysics() {
        velocity = velocity.add(0.0, gravity, 0.0).scale(drag);
        Vec3 movement = resolveBlockCollisions(velocity);
        if (movement.lengthSqr() < MIN_MOVEMENT_SQR) {
            movement = Vec3.ZERO;
        }
        velocity = movement;

        PathNode physicsNode = new PathNode(
                currentPathNode.pos().add(movement),
                currentPathNode.yaw(),
                currentPathNode.pitch(),
                currentPathNode.roll()
        );
        currentPlannedPath = new PlannedPath(PHYSICS_PATH_IDENTIFIER, List.of(physicsNode));
        currentPathNode = physicsNode;
    }

    private Vec3 resolveBlockCollisions(Vec3 movement) {
        if (movement.lengthSqr() < MIN_MOVEMENT_SQR) {
            return Vec3.ZERO;
        }

        Vec3 corrected = Entity.collideBoundingBox(this, movement, getBoundingBox(), level(), List.of());
        boolean collidedY = Math.abs(corrected.y - movement.y) > 1.0E-7;
        if (collidedY && movement.y < 0.0) {
            setOnGround(true);
            return new Vec3(0.0D, corrected.y, 0.0D);
        }
        if (collidedY) {
            setOnGround(false);
        } else if (movement.y > 0.0) {
            setOnGround(false);
        }
        return corrected;
    }

    private void syncEntityToPathNode() {
        applyingPathPosition = true;
        try {
            super.setPos(currentPathNode.pos());
        } finally {
            applyingPathPosition = false;
        }
        setYRot(currentPathNode.yaw());
        setYHeadRot(currentPathNode.yaw());
        setXRot(currentPathNode.pitch());
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        seedPathFromEntity();
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if (!applyingPathPosition && currentPathNode != null && !level().isClientSide()) {
            seedPathFromEntity();
        }
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        super.moveTo(x, y, z, yRot, xRot);
        if (currentPathNode != null && !level().isClientSide()) {
            seedPathFromEntity();
        }
    }

    private void seedPathFromEntity() {
        currentPathNode = new PathNode(position(), getYRot(), getXRot(), currentPathNode.roll());
        resetHistory(currentPathNode);
    }

    private void resetHistory(PathNode node) {
        historyNodes.clear();
        historyNodes.add(node);
        historyNodes.add(node);
    }

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer player) {
        super.startSeenByPlayer(player);
        PacketDistributor.sendToPlayer(player, new SyncFieldPacket(getId(), encodeSyncPayload(true)));
    }

    protected void registerSyncFields(SyncFieldDispatcher fields) {
        fields.field(LyraStreamCodecs.PATH_NODE, this::getCurrentPathNode, this::setCurrentPathNode);
    }

    public SyncFieldDispatcher syncFieldRegistrar() {
        if (syncFields == null) {
            syncFields = new SyncFieldDispatcher();
            registerSyncFields(syncFields);
        }
        return syncFields;
    }

    public void enqueueSyncPayload(byte[] payload) {
        if (payload.length == 0) {
            return;
        }
        byte[] copy = payload.clone();
        pendingPayloads.updateAndGet(payloads -> {
            List<byte[]> updated = new ArrayList<>(payloads.size() + 1);
            updated.addAll(payloads);
            updated.add(copy);
            return List.copyOf(updated);
        });
    }

    private byte[] encodeSyncPayload(boolean initialSync) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), level().registryAccess(), ConnectionType.NEOFORGE);
        syncFieldRegistrar().encode(buf, level(), initialSync);
        byte[] payload = new byte[buf.readableBytes()];
        buf.readBytes(payload);
        return payload;
    }

    private void applyPendingSync() {
        List<byte[]> snapshot = pendingPayloads.getAndSet(List.of());
        for (byte[] payload : snapshot) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload), level().registryAccess(), ConnectionType.NEOFORGE);
            syncFieldRegistrar().decode(buf, level());
        }
    }

    private static boolean hasFieldChanges(byte[] payload) {
        for (byte value : payload) {
            if (value != 0) {
                return true;
            }
        }
        return false;
    }

    public void setPlannedPath(PlannedPath plannedPath) {
        if (plannedPath == null || plannedPath.getNodes().isEmpty()) {
            return;
        }
        this.currentPlannedPath = plannedPath;
        this.currentPathNode = plannedPath.getNodes().getFirst();
    }

    public void setPath(List<PathNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }
        setPlannedPath(new PlannedPath("default", new ArrayList<>(nodes)));
    }

    public boolean isExecutingPath() {
        return currentPlannedPath != null
                && !currentPlannedPath.isFinished()
                && !PHYSICS_PATH_IDENTIFIER.equals(currentPlannedPath.getIdentifier());
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getCustomGravity() {
        return gravity;
    }

    public void setDrag(float drag) {
        this.drag = Mth.clamp(drag, 0.0F, 1.0F);
    }

    public float getDrag() {
        return drag;
    }

    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity == null ? Vec3.ZERO : velocity;
    }

    public Vec3 getVelocity() {
        return velocity;
    }

    public void applyForce(Vec3 force) {
        if (force != null) {
            this.velocity = this.velocity.add(force);
        }
    }

    public void applyForce(Vec3 targetPos, float force) {
        Vec3 direction = targetPos.subtract(currentPathNode.pos());
        if (direction.lengthSqr() > MIN_MOVEMENT_SQR) {
            applyForce(direction.normalize().scale(force));
        }
    }

    public void setPathRotation(float yaw, float pitch, float roll) {
        currentPathNode = new PathNode(currentPathNode.pos(), yaw, pitch, roll);
        setYRot(yaw);
        setYHeadRot(yaw);
        setXRot(pitch);
    }

    public void lookAtPathTarget(Vec3 targetPos) {
        Vec3 direction = targetPos.subtract(currentPathNode.pos());
        if (direction.lengthSqr() <= MIN_MOVEMENT_SQR) {
            return;
        }
        direction = direction.normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.asin(-direction.y));
        setPathRotation(yaw, pitch, currentPathNode.roll());
    }

    public void setCurrentPathNode(PathNode currentPathNode) {
        this.currentPathNode = currentPathNode;
    }

    public PathNode getCurrentPathNode() {
        return currentPathNode;
    }

    public PathNode getRenderNode(float partialTick) {
        if (historyNodes.size() < 2) {
            return currentPathNode;
        }
        return historyNodes.get(1).lerp(currentPathNode, partialTick);
    }

    public int getHistoryNodesSize() {
        return 16;
    }

    public ArrayList<PathNode> getHistoryNodes() {
        return historyNodes;
    }
}
