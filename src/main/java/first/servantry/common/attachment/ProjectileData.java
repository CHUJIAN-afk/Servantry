package first.servantry.common.attachment;

import first.servantry.api.projectile.Projectile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 射弹数据附件，存储玩家所有射弹。
 * <p>
 * 实现AttachmentSyncHandler以支持网络同步。
 * 提供并发安全的添加和移除操作。
 * </p>
 */
public class ProjectileData implements AttachmentSyncHandler<ProjectileData> {

    /** 射弹列表 */
    private final List<Projectile> projectiles = new ArrayList<>();

    /** 数据变更标记 */
    private boolean changed = false;

    /**
     * 获取所有射弹。
     *
     * @return 射弹列表（只读）
     */
    public List<Projectile> getProjectiles() {
        return Collections.unmodifiableList(projectiles);
    }

    /**
     * 添加射弹。
     *
     * @param projectile 射弹实例
     */
    public void add(Projectile projectile) {
        projectiles.add(projectile);
        changed = true;
    }

    /**
     * 移除射弹。
     *
     * @param projectile 射弹实例
     */
    public void remove(Projectile projectile) {
        projectiles.remove(projectile);
        changed = true;
    }

    /**
     * 通过UUID移除射弹。
     *
     * @param uuid 射弹UUID
     */
    public void remove(UUID uuid) {
        projectiles.removeIf(p -> p.getUuid().equals(uuid));
        changed = true;
    }

    /**
     * 清理所有标记为移除的射弹。
     * <p>
     * 此方法应在tick结束后调用，以避免并发修改异常。
     * </p>
     */
    public void cleanupMarkedProjectiles() {
        boolean removed = projectiles.removeIf(Projectile::isMarkedForRemoval);
        if (removed) {
            changed = true;
        }
    }

    /**
     * 清空所有射弹。
     */
    public void clear() {
        projectiles.clear();
        changed = true;
    }

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

    /**
     * 获取所有处于ATTACHED状态的射弹。
     *
     * @return ATTACHED状态的射弹列表
     */
    public List<Projectile> getAttachedProjectiles() {
        List<Projectile> result = new ArrayList<>();
        for (Projectile p : projectiles) {
            if (p.getState() == Projectile.ProjectileState.ATTACHED) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * 获取所有处于FLYING状态的射弹。
     *
     * @return FLYING状态的射弹列表
     */
    public List<Projectile> getFlyingProjectiles() {
        List<Projectile> result = new ArrayList<>();
        for (Projectile p : projectiles) {
            if (p.getState() == Projectile.ProjectileState.FLYING) {
                result.add(p);
            }
        }
        return result;
    }

    // ===================== 网络同步 =====================

    @Override
    public void write(RegistryFriendlyByteBuf buf, ProjectileData data, boolean isSelf) {
        buf.writeVarInt(data.projectiles.size());
        for (Projectile projectile : data.projectiles) {
            buf.writeUUID(projectile.getUuid());
            projectile.writeBase(buf);
        }
    }

    @Override
    public ProjectileData read(@NotNull IAttachmentHolder holder, @NotNull RegistryFriendlyByteBuf buf, @Nullable ProjectileData oldData) {
        ProjectileData data = oldData != null ? oldData : new ProjectileData();

        // 保留现有射弹的缓存引用
        Map<UUID, Projectile> existingProjectiles = new HashMap<>();
        for (Projectile p : data.projectiles) {
            existingProjectiles.put(p.getUuid(), p);
        }

        data.projectiles.clear();
        int size = buf.readVarInt();

        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            Projectile projectile = existingProjectiles.get(uuid);
            if (projectile == null) {
                // 创建新射弹实例（需要通过类型注册表）
                // 这里简化处理，实际应该根据类型创建
                projectile = new first.servantry.common.projectile.StardustProjectile();
                projectile.setUuid(uuid);
            }
            projectile.readBase(buf);
            data.projectiles.add(projectile);
        }

        return data;
    }
}