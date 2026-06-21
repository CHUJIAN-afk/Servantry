package first.servantry.api.common.attachment;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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

    private final Map<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> groups = new HashMap<>();
    private final Map<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> pendingAdd = new HashMap<>();

    private boolean changed = false;

    // ===================== 分组操作 =====================

    /**
     * 将实体放入对应 Type 和 AttachmentEntityType 的待添加队列
     */
    public void add(Type type, AttachmentEntity entity) {
        pendingAdd
                .computeIfAbsent(type, k -> new HashMap<>())
                .computeIfAbsent(entity.getType(), k -> new ArrayList<>())
                .add(entity);
    }

    /**
     * 添加射弹（延迟添加到 Projectile 分组）
     */
    public void addProjectile(Projectile projectile) {
        add(Type.Projectile, projectile);
        changed = true;
    }

    /** 收集所有分组中的实体为扁平列表 */
    public List<AttachmentEntity> getEntities() {
        List<AttachmentEntity> result = new ArrayList<>();
        for (Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner : groups.values()) {
            for (List<AttachmentEntity> list : inner.values()) {
                result.addAll(list);
            }
        }
        return result;
    }

    // ===================== 扁平视图 =====================

    /** 收集所有 Servant 类型分组中的仆从 */
    public List<Servant> getServants() {
        List<Servant> result = new ArrayList<>();
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = groups.get(Type.Servant);
        if (inner != null) {
            for (List<AttachmentEntity> list : inner.values()) {
                for (AttachmentEntity entity : list) {
                    if (entity instanceof Servant servant) {
                        result.add(servant);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 收集所有 ExtraServant 类型分组中的仆从
     */
    public List<Servant> getExtraServants() {
        List<Servant> result = new ArrayList<>();
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = groups.get(Type.ExtraServant);
        if (inner != null) {
            for (List<AttachmentEntity> list : inner.values()) {
                for (AttachmentEntity e : list) {
                    if (e instanceof Servant servant) {
                        result.add(servant);
                    }
                }
            }
        }
        return result;
    }

    /** 收集所有 Projectile 类型分组中的射弹 */
    public List<Projectile> getProjectiles() {
        List<Projectile> result = new ArrayList<>();
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = groups.get(Type.Projectile);
        if (inner != null) {
            for (List<AttachmentEntity> list : inner.values()) {
                for (AttachmentEntity entity : list) {
                    if (entity instanceof Projectile projectile ) {
                        result.add(projectile);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取当前未标记移除的仆从已占用栏位数
     */
    public int getServantUsedSlots() {
        int slots = 0;
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = groups.get(Type.Servant);
        if (inner != null) {
            for (List<AttachmentEntity> list : inner.values()) {
                for (AttachmentEntity attachmentEntity : list) {
                    if (attachmentEntity instanceof Servant servant && !servant.isRemove()) {
                        slots += servant.getSlotCost();
                    }
                }
            }
        }
        return slots;
    }

    // ===================== 仆从管理 =====================

    /** 检查是否有足够栏位召唤指定消耗的仆从 */
    public boolean canSummon(Player player, int slotCost) {
        return getMaxServantSize(player) - getServantUsedSlots() >= slotCost;
    }

    /** 召唤仆从（延迟添加到 Servant 分组） */
    public boolean summonServant(Player player, Servant servant) {
        if (canSummon(player, servant.getSlotCost())) {
            add(Type.Servant, servant);
            return true;
        }
        return false;
    }

    /** 获取玩家仆从最大栏位数（来自属性） */
    public int getMaxServantSize(Player player) {
        AttributeInstance attr = player.getAttribute(AttributeRegister.ServantMaxCount);
        return attr != null ? (int) attr.getValue() : 0;
    }

    /** 通过 UUID 在所有分组中查找并标记实体为待移除 */
    public void remove(UUID uuid) {
        for (Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner : groups.values()) {
            for (List<AttachmentEntity> list : inner.values()) {
                for (AttachmentEntity e : list) {
                    if (e.getUuid().equals(uuid)) {
                        e.setRemove();
                        changed = true;
                        return;
                    }
                }
            }
        }
    }

    // ===================== 通用移除 =====================

    /**
     * 每tick调用：处理溢出、tick实体、清理标记、添加队列、同步网络
     */
    public void update(Player player) {
        // 检查仆从栏位溢出，标记多余仆从
        if (!player.level().isClientSide()) {
            List<Servant> servants = getServants();
            if (!servants.isEmpty() && !canSummon(player, 0)) {
                servants.getFirst().setRemove();
            }

            // 清理所有分组中标记移除的实体
            for (Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner : groups.values()) {
                Iterator<Map.Entry<AttachmentEntityType<?>, List<AttachmentEntity>>> typeIt = inner.entrySet().iterator();
                while (typeIt.hasNext()) {
                    List<AttachmentEntity> list = typeIt.next().getValue();
                    list.removeIf(entity -> {
                        if (entity.isRemove()) {
                            entity.onRemove();
                            changed = true;
                            return true;
                        }
                        return false;
                    });
                    if (list.isEmpty()) {
                        typeIt.remove();
                    }
                }
            }

            // 将待添加队列合并到主分组
            if (!pendingAdd.isEmpty()) {
                for (Map.Entry<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> typeEntry : pendingAdd.entrySet()) {
                    Type type = typeEntry.getKey();
                    Map<AttachmentEntityType<?>, List<AttachmentEntity>> targetInner = groups.computeIfAbsent(type, k -> new HashMap<>());
                    for (Map.Entry<AttachmentEntityType<?>, List<AttachmentEntity>> entityEntry : typeEntry.getValue()
                            .entrySet()) {
                        targetInner.computeIfAbsent(entityEntry.getKey(), k -> new ArrayList<>())
                                .addAll(entityEntry.getValue());
                    }
                }
                pendingAdd.clear();
                changed = true;
            }
        }

        // tick所有未标记移除的实体
        for (Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner : groups.values()) {
            for (List<AttachmentEntity> list : inner.values()) {
                for (AttachmentEntity entity : list) {
                    if (!entity.isRemove()) {
                        entity.setOwner(player);
                        entity.tick();
                    }
                }
            }
        }

        // 同步数据到客户端
        if (!player.level().isClientSide() && (!groups.isEmpty() || changed)) {
            changed = false;
            player.syncData(AttachmentRegister.EntityData);
        }
    }

    public Map<Type, Map<AttachmentEntityType<?>, List<AttachmentEntity>>> getGroups() {
        return groups;
    }

    // ===================== Tick 更新 =====================

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
                for (AttachmentEntity e : list) existing.put(e.getUuid(), e);
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
                    list.add(entity);
                }
            }
        }

        return data;
    }

    public enum Type {
        Servant,
        Projectile,
        ExtraServant
    }
}