package first.servantry.common.attachment;

import first.servantry.api.servant.PathNode;
import first.servantry.api.register.Registries;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttributeRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ServantData implements AttachmentSyncHandler<ServantData> {

    private final List<Servant> servants = new ArrayList<>();
    private boolean change = true;

    private int lastCacheTick = -1;
    private final List<LivingEntity> cachedEnemies = new ArrayList<>();

    public List<Servant> getServants() {
        return servants;
    }

    public boolean summon(Player player, Servant servant) {
        if (getMaxSize(player) > getServants().size()) {
            servants.add(servant);
            change = true;
            return true;
        }
        return false;
    }

    public void remove(ServantType<?> type) {
        Servant target = null;
        for (Servant servant : servants) {
            if (servant.getType() == type) {
                target = servant;
                break;
            }
        }
        if (target != null) {
            servants.remove(target);
            change = true;
        }
    }

    public int getMaxSize(Player player) {
        AttributeInstance attribute = player.getAttribute(AttributeRegister.ServantMaxCount);
        if (attribute != null) {
            return (int) attribute.getValue();
        }
        return 0;
    }

    public int getOrder(Servant target) {
        int order = 0;
        for (Servant s : servants) {
            if (s == target) return order;
            if (s.getType().equals(target.getType())) order++;
        }
        return -1;
    }

    public int getSameSize(Servant target) {
        int count = 0;
        for (Servant s : servants) {
            if (s.getType().equals(target.getType())) count++;
        }
        return count;
    }

    public List<LivingEntity> getNearbyTargets(Player player, Servant servant, double distance, boolean requireLineOfSight) {
        if (player.tickCount != lastCacheTick) {
            cachedEnemies.clear();
            AABB searchBox = player.getBoundingBox().inflate(64.0);
            cachedEnemies.addAll(player.level().getEntitiesOfClass(LivingEntity.class, searchBox, living -> living.isAlive() && living != player));
            lastCacheTick = player.tickCount;
        }
        List<LivingEntity> result = new ArrayList<>();
        Vec3 servantPos = servant.getPos();
        for (LivingEntity target : cachedEnemies) {
            if (servant.isTarget(target) && target.distanceToSqr(servantPos) <= distance * distance) {
                if (requireLineOfSight) {
                    ClipContext context = new ClipContext(servantPos, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
                    if (player.level().clip(context).getType() != HitResult.Type.MISS) {
                        continue;
                    }
                }
                result.add(target);
            }
        }
        return result;
    }

    public boolean isChange() { return change; }
    public void setChange(boolean change) { this.change = change; }

    @Override
    public void write(RegistryFriendlyByteBuf buf, ServantData data, boolean isSelf) {
        buf.writeVarInt(data.servants.size());
        for (Servant servant : data.servants) {
            ResourceLocation location = Registries.SERVANT_TYPES.getKey(servant.getType());
            assert location != null;
            buf.writeResourceLocation(location);
            buf.writeUUID(servant.getUuid());
            servant.writeBase(buf);
        }
    }

    @Override
    public ServantData read(@NotNull IAttachmentHolder holder, @NotNull RegistryFriendlyByteBuf buf, @Nullable ServantData oldData) {
        ServantData data = oldData != null ? oldData : new ServantData();
        Map<UUID, Servant> existingServants = new HashMap<>();
        for (Servant s : data.getServants()) {
            existingServants.put(s.getUuid(), s);
        }
        data.getServants().clear();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            ResourceLocation typeId = buf.readResourceLocation();
            UUID uuid = buf.readUUID();
            ServantType<?> type = Registries.SERVANT_TYPES.get(typeId);
            assert type != null;
            Servant servant = existingServants.get(uuid);
            if (servant == null) {
                servant = type.factory().apply(new PathNode(Vec3.ZERO, 0, 0, 0));
                servant.setUuid(uuid);
            }
            servant.readBase(buf);
            data.getServants().add(servant);
        }
        return data;
    }
}