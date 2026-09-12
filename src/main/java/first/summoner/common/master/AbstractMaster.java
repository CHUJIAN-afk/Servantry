package first.summoner.common.master;

import first.lyra.common.entity.PathNode;
import first.lyra.common.entity.PlannedPath;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.network.SyncFieldPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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

    protected final ArrayList<PathNode> historyNodes = new ArrayList<>();
    protected final AtomicReference<List<byte[]>> pendingPayloads = new AtomicReference<>(List.of());
    protected PlannedPath currentPlannedPath = null;
    protected SyncFieldDispatcher syncFields = null;

    protected PathNode currentPathNode = new PathNode(Vec3.ZERO, 0.0F, 0.0F, 0.0F);

    protected AbstractMaster(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        historyNodes.add(currentPathNode);
        historyNodes.add(currentPathNode);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            byte[] payload = encodeSyncPayload(false);
            if (hasFieldChanges(payload)) {
                PacketDistributor.sendToPlayersTrackingEntity(this, new SyncFieldPacket(getId(), payload));
            }
        } else {
            applyPendingSync();
        }
        historyNodes.addFirst(currentPathNode);
        if (historyNodes.size() > getHistoryNodesSize()) {
            historyNodes.removeLast();
        }
        // 路径推进
        if (!level().isClientSide() && currentPlannedPath != null && !currentPlannedPath.isFinished()) {
            currentPathNode = currentPlannedPath.advance();
        }
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
