package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ModelRenderer {

    public static void renderModel(ModelResourceLocation modelLocation, PoseStack poseStack, MultiBufferSource bufferSource) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        Minecraft.getInstance().getItemRenderer().renderModelLists(
                model,
                ItemStack.EMPTY,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource.getBuffer(Sheets.translucentItemSheet())
        );
    }
}
