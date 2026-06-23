package first.servantry.api;

import first.servantry.api.common.attachment.EntityData;
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

    public EntityData getEntityData() {
        return entityData;
    }

    public void add(EntityData.Type type, AttachmentEntity entity) {
        entityData.add(type, entity);
    }

    public boolean canSummon(int slotCost) {
        return getServantMaxCount() - getServantUsedSlots() >= slotCost;
    }

    public boolean summonServant(Servant servant) {
        if (canSummon(servant.getSlotCost())) {
            add(EntityData.Type.Servant, servant);
            return true;
        }
        return false;
    }

    public int getServantMaxCount() {
        AttributeInstance attributeInstance = player.getAttribute(AttributeRegister.ServantMaxCount);
        return attributeInstance != null ? (int) attributeInstance.getValue() : 0;
    }

    public int getServantUsedSlots() {
        int slots = 0;
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> map = entityData.getGroups().get(EntityData.Type.Servant);
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
