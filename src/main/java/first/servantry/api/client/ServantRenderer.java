package first.servantry.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class ServantRenderer<T extends Servant> {

    public abstract void render(T servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode);

}