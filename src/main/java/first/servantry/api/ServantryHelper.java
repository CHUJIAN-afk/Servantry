package first.servantry.api;

import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.common.attachment.TargetCache;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.servant.Servant;
import first.servantry.register.ServantryAttachmentRegister;
import first.servantry.register.ServantryAttributeRegister;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;

public final class ServantryHelper {

    private final Player player;
    private final EntityData entityData;

    private ServantryHelper(Player player) {
        this.player = player;
        this.entityData = player.getData(ServantryAttachmentRegister.EntityData);
    }

    public static ServantryHelper get(Player player) {
        return new ServantryHelper(player);
    }

    public TargetCache getTargetCache(){
        return player.getData(ServantryAttachmentRegister.TargetCache);
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
                AttributeInstance attributeInstance = player.getAttribute(ServantryAttributeRegister.ServantMaxCount);
                yield attributeInstance != null ? (int) attributeInstance.getValue() : 0;
            }
            case SentryServant -> {
                AttributeInstance attributeInstance = player.getAttribute(ServantryAttributeRegister.SentryServantMaxCount);
                yield attributeInstance != null ? (int) attributeInstance.getValue() : 0;
            }
            default -> 0;
        };

    }

    public int getUsedSlots(EntityData.Type type) {
        return entityData.getGroups()
                .getOrDefault(type, new HashMap<>())
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(entity -> entity instanceof Servant)
                .map(entity -> (Servant) entity)
                .filter(servant -> !servant.isRemove())
                .mapToInt(Servant::getSlotCost)
                .sum();
    }

    public Player getPlayer() {
        return player;
    }
}
