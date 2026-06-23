package first.servantry.api.common.attachment;

import first.servantry.api.ServantryHelper;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 统一的附件实体数据附件。
 * <p>
 * 使用 Type → AttachmentEntityType 两级分组存储实体。
 * 移除通过 setRemove() 标记完成，添加通过延迟队列在 tick 后统一处理。
 * </p>
 */
public class EntityData implements AttachmentSyncHandler<EntityData> {

    private final Map<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> pendingAdd = new HashMap<>();
    private final Map<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> groups = new HashMap<>();
    private final List<AttachmentEntity> renderCache = new ArrayList<>();
    private boolean changed = false;

    public void tick(Player player) {
        updateServantSlot(player);
        tickEntity(player);
        syncToClient(player);
    }

    private void syncToClient(Player player) {
        if (!player.level().isClientSide() && (!groups.isEmpty() || changed)) {
            changed = false;
            player.syncData(AttachmentRegister.EntityData);
        }
    }

    private void tickEntity(Player player) {
        renderCache.clear();
        boolean clientSide = player.level().isClientSide();
        for (Map<AttachmentEntityType<?>, List<AttachmentEntity>> map : groups.values()) {
            for (List<AttachmentEntity> list : map.values()) {
                for (AttachmentEntity entity : list) {
                    entity.setOwner(player);
                    entity.tick();
                    if (clientSide) {
                        renderCache.add(entity);
                    }
                }
            }
        }
    }

    private void updateServantSlot(Player player) {
        // 检查仆从栏位溢出，标记多余仆从
        if (!player.level().isClientSide()) {
            // 将待添加队列合并到主分组
            if (!pendingAdd.isEmpty()) {
                for (Map.Entry<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> entry : pendingAdd.entrySet()) {
                    Type type = entry.getKey();
                    for (List<AttachmentEntity> value : entry.getValue().values()) {
                        for (AttachmentEntity attachmentEntity : value) {
                            groups.computeIfAbsent(type, key1 -> new HashMap<>()).computeIfAbsent(attachmentEntity.getType(), key -> new ArrayList<>()).add(attachmentEntity);
                        }
                    }
                }
                pendingAdd.clear();
                changed = true;
            }
            List<Servant> servants = get(Type.Servant, Servant.class);
            while (true) {
                if (servants.isEmpty()) {
                    break;
                }
                if (!ServantryHelper.get(player).canSummon(0)) {
                    servants.getFirst().setRemove();
                    changed = true;
                } else {
                    break;
                }
            }
            // 清理所有分组中标记移除的实体
            for (Map<AttachmentEntityType<?>, List<AttachmentEntity>> map : groups.values()) {
                map.values().removeIf(list -> {
                    while (true) {
                        list.removeIf(entity -> {
                            if (entity.isRemove()) {
                                entity.onRemove();
                                changed = true;
                                return true;
                            }
                            return false;
                        });
                        if (list.isEmpty()) {
                            break;
                        }
                        if (list.stream().noneMatch(AttachmentEntity::isRemove)) {
                            break;
                        }
                    }
                    return list.isEmpty();
                });
            }
        }
    }

    public void add(Type type, AttachmentEntity entity) {
        pendingAdd.computeIfAbsent(type, k -> new HashMap<>()).computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
    }

    public <T> List<T> get(Type type, Class<T> tClass) {
        List<T> result = new ArrayList<>();
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> map = groups.get(type);
        if (map != null) {
            Collection<List<AttachmentEntity>> values = map.values();
            for (List<AttachmentEntity> value : values) {
                for (AttachmentEntity attachmentEntity : value) {
                    if (!attachmentEntity.isRemove() && tClass.isInstance(attachmentEntity)) {
                        @SuppressWarnings("unchecked")
                        T t = (T) attachmentEntity;
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    public void remove(Type type, AttachmentEntityType<?> entityType) {
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> map = getGroups().get(type);
        if (map != null) {
            List<AttachmentEntity> entities = map.get(entityType);
            if (entities != null) {
                for (AttachmentEntity entity : entities) {
                    entity.setRemove();
                }
            }
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, EntityData data, boolean isSelf) {
        // 写入 Type → AttachmentEntityType → 实体列表的三层结构
        buf.writeVarInt(data.groups.size());
        for (Map.Entry<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> typeEntry : data.groups.entrySet()) {
            buf.writeEnum(typeEntry.getKey());
            Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = typeEntry.getValue();
            buf.writeVarInt(inner.size());
            for (Map.Entry<AttachmentEntityType<?>, List<AttachmentEntity>> entityEntry : inner.entrySet()) {
                ResourceLocation typeId = ServantryRegistries.ATTACHMENT_ENTITY_TYPES.getKey(entityEntry.getKey());
                assert typeId != null;
                buf.writeResourceLocation(typeId);
                List<AttachmentEntity> list = entityEntry.getValue();
                buf.writeVarInt(list.size());
                for (AttachmentEntity entity : list) {
                    buf.writeUUID(entity.getUuid());
                    entity.writeBase(buf);
                    entity.writeAdditional(buf);
                }
            }
        }
    }

    // ===================== 网络同步 =====================

    @Override
    public EntityData read(@NotNull IAttachmentHolder holder, @NotNull RegistryFriendlyByteBuf buf, @Nullable EntityData oldData) {
        EntityData data = oldData != null ? oldData : new EntityData();

        // 保留现有实体的缓存引用
        Map<UUID, AttachmentEntity> existing = new HashMap<>();
        for (Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner : data.groups.values()) {
            for (List<AttachmentEntity> list : inner.values()) {
                for (AttachmentEntity e : list) {
                    existing.put(e.getUuid(), e);
                }
            }
        }

        // 清空分组和待添加队列
        data.groups.clear();
        data.pendingAdd.clear();

        // 读取 Type → AttachmentEntityType → 实体列表的三层结构
        int typeCount = buf.readVarInt();
        for (int i = 0; i < typeCount; i++) {
            Type type = buf.readEnum(Type.class);
            Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = data.groups.computeIfAbsent(type, k -> new HashMap<>());
            int entityCount = buf.readVarInt();
            for (int j = 0; j < entityCount; j++) {
                ResourceLocation typeId = buf.readResourceLocation();
                AttachmentEntityType<?> entityType = ServantryRegistries.ATTACHMENT_ENTITY_TYPES.get(typeId);
                assert entityType != null;
                List<AttachmentEntity> list = inner.computeIfAbsent(entityType, k -> new ArrayList<>());
                int listSize = buf.readVarInt();
                for (int k = 0; k < listSize; k++) {
                    UUID uuid = buf.readUUID();
                    AttachmentEntity entity = existing.get(uuid);
                    if (entity == null) {
                        entity = entityType.factory().get();
                        entity.setUuid(uuid);
                    }
                    entity.readBase(buf);
                    entity.readAdditional(buf);
                    list.add(entity);
                }
            }
        }

        return data;
    }

    public Map<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> getGroups() {
        return groups;
    }

    public List<AttachmentEntity> getRenderCache() {
        return renderCache;
    }

    public enum Type {
        Servant,
        Projectile,
        ExtraServant
    }
}