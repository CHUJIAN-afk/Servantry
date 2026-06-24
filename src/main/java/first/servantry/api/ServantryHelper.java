package first.servantry.api;

import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.common.attachment.TargetCache;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

public final class ServantryHelper {

    private final Player player;
    private final EntityData entityData;

    private ServantryHelper(Player player) {
        this.player = player;
        this.entityData = player.getData(AttachmentRegister.EntityData);
    }

    public static ServantryHelper get(Player player) {
        return new ServantryHelper(player);
    }

    public TargetCache getTargetCache(){
        return player.getData(AttachmentRegister.TargetCache);
    }

    public EntityData getEntityData() {
        return entityData;
    }

    public void add(EntityData.Type type, AttachmentEntity entity) {
        entityData.add(type, entity);
    }

    public boolean canSummon(EntityData.Type type, int slotCost) {
        return getMaxCount(type) - getUsedSlots(type) >= slotCost;
    }

    public int getMaxCount(EntityData.Type type) {
        return switch (type) {
            case Servant -> {
                AttributeInstance attributeInstance = player.getAttribute(AttributeRegister.ServantMaxCount);
                yield attributeInstance != null ? (int) attributeInstance.getValue() : 0;
            }
            case SentryServant -> {
                AttributeInstance attributeInstance = player.getAttribute(AttributeRegister.SentryServantMaxCount);
                yield attributeInstance != null ? (int) attributeInstance.getValue() : 0;
            }
            default -> 0;
        };
    }

    public int getUsedSlots(EntityData.Type type) {
        int slots = 0;
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> map = entityData.getGroups().get(type);
        if (map != null) {
            for (List<AttachmentEntity> list : map.values()) {
                for (AttachmentEntity attachmentEntity : list) {
                    if (attachmentEntity instanceof Servant servant && !servant.isRemove()) {
                        slots += servant.getSlotCost();
                    }
                }
            }
        }
        return slots;
    }

    public Player getPlayer() {
        return player;
    }
}
