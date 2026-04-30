package first.servantry.api.core;

import first.servantry.api.entity.AttachmentEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class AttachmentEntityDispatcher {

    private static AttachmentEntityDispatcher dispatcher = null;

    private final Map<UUID, List<AttachmentEntity>> tick = new HashMap<>();
    private final List<Runnable> pendingAdds = new ArrayList<>();

    private AttachmentEntityDispatcher() {
    }

    public static AttachmentEntityDispatcher getInstance() {
        if (dispatcher == null) {
            dispatcher = new AttachmentEntityDispatcher();
        }
        return dispatcher;
    }

    public void add(ServerPlayer player, AttachmentEntity attachmentEntity) {
        pendingAdds.add(() -> tick.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(attachmentEntity));
    }

    public void update(MinecraftServer server) {
        // 处理待添加实体
        pendingAdds.forEach(Runnable::run);
        pendingAdds.clear();
        // 执行 tick
        for (Map.Entry<UUID, List<AttachmentEntity>> entry : tick.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            List<AttachmentEntity> entities = entry.getValue();
            if (!entities.isEmpty()) {
                if (player != null) {
                    for (AttachmentEntity entity : entities) {
                        entity.setOwner(player);
                        entity.tick();
                    }
                } else {
                    entities.forEach(AttachmentEntity::setRemove);
                }
            }
        }
        // 移除标记的实体
        tick.values().forEach(list -> list.removeIf(AttachmentEntity::isRemove));
    }

}
