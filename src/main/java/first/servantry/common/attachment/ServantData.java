package first.servantry.common.attachment;

import first.servantry.api.PathNode;
import first.servantry.api.register.Registries;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttributeRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ServantData implements AttachmentSyncHandler<ServantData> {

    private final List<Servant> servants = new ArrayList<>();

    public List<Servant> getServants() {
        return servants;
    }

    /**
     * 添加仆从
     */
    public boolean summon(Player player, Servant servant) {
        if (getMaxSize(player) > getServants().size()) {
            servants.add(servant);
            return true;
        }
        return false;
    }

    /**
     * 移除仆从栏中最早召唤的某一类仆从
     */
    public boolean remove(ServantType<?> type) {
        Servant target = null;
        for (Servant servant : servants) {
            if (servant.getType() == type) {
                target = servant;
                break;
            }
        }
        if (target != null) {
            servants.remove(target);
            return true;
        }
        return false;
    }

    /**
     * 获得玩家的最大仆从数量
     */
    public int getMaxSize(Player player) {
        AttributeInstance attribute = player.getAttribute(AttributeRegister.ServantMaxCount);
        if (attribute != null) {
            return (int) attribute.getValue();
        }
        return 0;
    }

    /**
     * 获得在同类型仆从中的次序
     */
    public int getOrder(Servant target) {
        int order = 0;
        for (Servant s : servants) {
            if (s == target) {
                return order;
            }
            if (s.getType().equals(target.getType())) {
                order++;
            }
        }
        return -1;
    }

    /**
     * 获得同类型仆从的总数量
     */
    public int getSameSize(Servant target) {
        int count = 0;
        for (Servant s : servants) {
            if (s.getType().equals(target.getType())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, ServantData data, boolean isSelf) {
        buf.writeVarInt(data.servants.size());
        for (Servant servant : data.servants) {
            ResourceLocation location = Registries.ServantTypes.getKey(servant.getType());
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
            ServantType<?> type = Registries.ServantTypes.get(typeId);
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