package first.servantry.api.client.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.servant.Servant;
import net.minecraft.client.renderer.MultiBufferSource;

@FunctionalInterface
public interface IServantRenderer<T extends Servant> {

    void render(T servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode);

}