package first.servantry.api.client.dynamicLight;

import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.PathNode;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface DynamicLightRenderer<T extends AttachmentEntity> {

    int getLuminance();

    default @NotNull Map<Vec3, Integer> getDynamicLight(T entity, RenderContext<T> context, PathNode visualNode) {
        return Map.of(visualNode.pos(), getLuminance());
    }
}
