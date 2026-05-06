package first.servantry.api.common.attachment;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.projectile.ProjectileState;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.register.ServantType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.api.servant.Servant;
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
import java.util.stream.Collectors;

/**
 * 统一的附件实体数据附件。
 * <p>
 * 存储玩家所有的附件实体（仆从和射弹），提供统一的管理和同步功能。
 * </p>
 */
public class EntityData implements AttachmentSyncHandler<EntityData> {

    /** 所有附件实体列表 */
    private final List<AttachmentEntity> entities = new ArrayList<>();

    /** 待添加实体队列（延迟处理，避免并发修改） */
    private final List<AttachmentEntity> pendingAdd = new ArrayList<>();

    /** 待移除实体队列（延迟处理，避免并发修改） */
    private final List<AttachmentEntity> pendingRemove = new ArrayList<>();

    /** 数据变更标记 */
    private boolean changed = false;

    /** 是否正在 tick 中 */
    private boolean ticking = false;

    // ===================== 实体管理 =====================

    /**
     * 获取所有附件实体。
     *
     * @return 实体列表（只读）
     */
    public List<AttachmentEntity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * 获取所有仆从。
     *
     * @return 仆从列表
     */
    public List<Servant> getServants() {
        return entities.stream()
                .filter(e -> e instanceof Servant)
                .map(e -> (Servant) e)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有射弹。
     *
     * @return 射弹列表
     */
    public List<Projectile> getProjectiles() {
        return entities.stream()
                .filter(e -> e instanceof Projectile)
                .map(e -> (Projectile) e)
                .collect(Collectors.toList());
    }

    // ===================== 仆从管理 =====================

    /**
     * 获取当前已占用的仆从栏位数。
     *
     * @return 已占用栏位数
     */
    public int getUsedSlots() {
        return entities.stream()
                .filter(e -> e instanceof Servant)
                .mapToInt(e -> ((Servant) e).getSlotCost())
                .sum();
    }

    /**
     * 召唤仆从。
     *
     * @param player  玩家
     * @param servant 仆从实例
     * @return 是否成功
     */
    public boolean summonServant(Player player, Servant servant) {
        int maxSlots = getMaxServantSize(player);
        int usedSlots = getUsedSlots();
        if (maxSlots >= usedSlots + servant.getSlotCost()) {
            if (ticking) {
                pendingAdd.add(servant);
            } else {
                entities.add(servant);
            }
            changed = true;
            return true;
        }
        return false;
    }

    /**
     * 移除指定类型的仆从。
     *
     * @param type 仆从类型
     */
    public void removeServant(ServantType<?> type) {
        List<Servant> targets = new ArrayList<>();
        for (AttachmentEntity entity : entities) {
            if (entity instanceof Servant servant && servant.getType() == type) {
                targets.add(servant);
            }
        }
        if (!targets.isEmpty()) {
            changed = true;
            for (Servant target : targets) {
                if (ticking) {
                    pendingRemove.add(target);
                } else {
                    entities.remove(target);
                }
            }
        }
    }

    /**
     * 获取仆从最大数量。
     *
     * @param player 玩家
     * @return 最大数量
     */
    public int getMaxServantSize(Player player) {
        AttributeInstance attribute = player.getAttribute(AttributeRegister.ServantMaxCount);
        if (attribute != null) {
            return (int) attribute.getValue();
        }
        return 0;
    }

    /**
     * 获取仆从在同类中的顺序。
     *
     * @param target 目标仆从
     * @return 顺序索引
     */
    public int getOrder(Servant target) {
        int order = 0;
        for (AttachmentEntity entity : entities) {
            if (entity instanceof Servant s) {
                if (s == target) return order;
                if (s.getType().equals(target.getType())) order++;
            }
        }
        return -1;
    }

    /**
     * 获取同类仆从数量。
     *
     * @param target 目标仆从
     * @return 数量
     */
    public int getSameSize(Servant target) {
        int count = 0;
        for (AttachmentEntity entity : entities) {
            if (entity instanceof Servant s && s.getType().equals(target.getType())) {
                count++;
            }
        }
        return count;
    }

    // ===================== 射弹管理 =====================

    /**
     * 添加射弹。
     *
     * @param projectile 射弹实例
     */
    public void addProjectile(Projectile projectile) {
        if (ticking) {
            pendingAdd.add(projectile);
        } else {
            entities.add(projectile);
        }
        changed = true;
    }

    /**
     * 移除射弹。
     *
     * @param projectile 射弹实例
     */
    public void removeProjectile(Projectile projectile) {
        if (ticking) {
            pendingRemove.add(projectile);
        } else {
            entities.remove(projectile);
        }
        changed = true;
    }

    /**
     * 通过UUID移除实体。
     *
     * @param uuid 实体UUID
     */
    public void remove(UUID uuid) {
        AttachmentEntity target = null;
        for (AttachmentEntity entity : entities) {
            if (entity.getUuid().equals(uuid)) {
                target = entity;
                break;
            }
        }
        if (target != null) {
            if (ticking) {
                pendingRemove.add(target);
            } else {
                entities.remove(target);
            }
            changed = true;
        }
    }

    /**
     * 清理所有标记为移除的射弹。
     */
    public void cleanupMarked() {
        boolean removed = false;
        Iterator<AttachmentEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            AttachmentEntity entity = iterator.next();
            if (entity instanceof Projectile p && p.isMarkedForRemoval()) {
                if (ticking) {
                    pendingRemove.add(entity);
                } else {
                    iterator.remove();
                }
                removed = true;
            }
        }
        if (removed) {
            changed = true;
        }
    }

    /**
     * 获取所有处于ATTACHED状态的射弹。
     *
     * @return ATTACHED状态的射弹列表
     */
    public List<Projectile> getAttachedProjectiles() {
        return entities.stream()
                .filter(e -> e instanceof Projectile p && p.getState() == ProjectileState.ATTACHED)
                .map(e -> (Projectile) e)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有处于FLYING状态的射弹。
     *
     * @return FLYING状态的射弹列表
     */
    public List<Projectile> getFlyingProjectiles() {
        return entities.stream()
                .filter(e -> e instanceof Projectile p && p.getState() == ProjectileState.FLYING)
                .map(e -> (Projectile) e)
                .collect(Collectors.toList());
    }

    // ===================== Tick 更新 =====================

    /**
     * 更新所有实体。
     *
     * @param player 所有者玩家
     */
    public void tickAll(Player player) {
        ticking = true;
        try {
            for (AttachmentEntity entity : entities) {
                entity.setOwner(player);
                entity.tick();
            }
        } finally {
            ticking = false;
        }

        // 处理待移除的实体
        if (!pendingRemove.isEmpty()) {
            entities.removeAll(pendingRemove);
            pendingRemove.clear();
            changed = true;
        }

        // 处理待添加的实体
        if (!pendingAdd.isEmpty()) {
            entities.addAll(pendingAdd);
            pendingAdd.clear();
            changed = true;
        }

        // 清理标记移除的射弹
        cleanupMarked();
    }

    // ===================== 变更标记 =====================

    /**
     * 检查数据是否变更。
     *
     * @return 是否变更
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * 设置数据变更标记。
     *
     * @param changed 是否变更
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    // ===================== 网络同步 =====================

    @Override
    public void write(RegistryFriendlyByteBuf buf, EntityData data, boolean isSelf) {
        buf.writeVarInt(data.entities.size());
        for (AttachmentEntity entity : data.entities) {
            // 写入实体类型标记（0=仆从, 1=射弹）
            boolean isServant = entity instanceof Servant;
            buf.writeBoolean(isServant);

            if (isServant) {
                Servant servant = (Servant) entity;
                ResourceLocation location = ServantryRegistries.SERVANT_TYPES.getKey(servant.getType());
                assert location != null;
                buf.writeResourceLocation(location);
            } else {
                Projectile projectile = (Projectile) entity;
                ResourceLocation location = ServantryRegistries.PROJECTILE_TYPES.getKey(projectile.getProjectileType());
                assert location != null;
                buf.writeResourceLocation(location);
            }

            buf.writeUUID(entity.getUuid());
            entity.writeBase(buf);
        }
    }

    @Override
    public EntityData read(@NotNull IAttachmentHolder holder, @NotNull RegistryFriendlyByteBuf buf, @Nullable EntityData oldData) {
        EntityData data = oldData != null ? oldData : new EntityData();

        // 保留现有实体的缓存引用
        Map<UUID, AttachmentEntity> existingEntities = new HashMap<>();
        for (AttachmentEntity e : data.entities) {
            existingEntities.put(e.getUuid(), e);
        }

        data.entities.clear();
        int size = buf.readVarInt();

        for (int i = 0; i < size; i++) {
            boolean isServant = buf.readBoolean();
            ResourceLocation typeId = buf.readResourceLocation();
            UUID uuid = buf.readUUID();

            AttachmentEntity entity = existingEntities.get(uuid);
            if (entity == null) {
                if (isServant) {
                    ServantType<?> type = ServantryRegistries.SERVANT_TYPES.get(typeId);
                    assert type != null;
                    entity = type.factory().get();
                } else {
                    ProjectileType<?> type = ServantryRegistries.PROJECTILE_TYPES.get(typeId);
                    assert type != null;
                    entity = type.factory().get();
                }
                entity.setUuid(uuid);
            }
            entity.readBase(buf);
            data.entities.add(entity);
        }

        return data;
    }
}
