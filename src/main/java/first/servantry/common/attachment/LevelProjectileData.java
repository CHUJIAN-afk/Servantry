package first.servantry.common.attachment;

import first.servantry.api.projectile.AdvancedProjectile;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.register.Registries;
import first.servantry.api.servant.PathNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LevelProjectileData implements AttachmentSyncHandler<LevelProjectileData> {

    private final List<AdvancedProjectile> projectiles = new ArrayList<>();
    private boolean change = false;

    public void addProjectile(AdvancedProjectile projectile) {
        projectiles.add(projectile);
        change = true;
    }

    public List<AdvancedProjectile> getProjectiles() {
        return projectiles;
    }

    public void tickProjectiles() {
        boolean removedAny = projectiles.removeIf(AdvancedProjectile::isRemoved);
        for (AdvancedProjectile proj : projectiles) {
            proj.tick();
        }
        if (removedAny || !projectiles.isEmpty()) {
            change = true;
        }
    }

    public boolean isChange() { return change; }
    public void setChange(boolean change) { this.change = change; }

    @Override
    public void write(RegistryFriendlyByteBuf buf, LevelProjectileData data, boolean isSelf) {
        buf.writeVarInt(data.projectiles.size());
        for (AdvancedProjectile proj : data.projectiles) {
            ResourceLocation location = Registries.PROJECTILE_TYPES.getKey(proj.getType());
            assert location != null;
            buf.writeResourceLocation(location);
            buf.writeUUID(proj.getUuid());
            proj.writeSyncData(buf);
        }
    }

    @Override
    public LevelProjectileData read(@NotNull IAttachmentHolder holder, @NotNull RegistryFriendlyByteBuf buf, @Nullable LevelProjectileData oldData) {
        LevelProjectileData data = oldData != null ? oldData : new LevelProjectileData();
        Map<UUID, AdvancedProjectile> existing = new HashMap<>();
        for (AdvancedProjectile p : data.getProjectiles()) {
            existing.put(p.getUuid(), p);
        }
        
        data.getProjectiles().clear();
        int size = buf.readVarInt();
        Level level = (Level) holder;

        for (int i = 0; i < size; i++) {
            ResourceLocation typeId = buf.readResourceLocation();
            UUID uuid = buf.readUUID();
            ProjectileType<?> type = Registries.PROJECTILE_TYPES.get(typeId);
            assert type != null;
            
            AdvancedProjectile proj = existing.get(uuid);
            if (proj == null) {
                proj = type.factory().create(level, null, new PathNode(Vec3.ZERO, 0, 0, 0));
                proj.setUuid(uuid);
            }
            
            proj.readSyncData(buf);
            data.getProjectiles().add(proj);
        }
        return data;
    }
}