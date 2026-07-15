package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ModelRenderer {

    private ModelRenderer(){

    }

    public static void renderModel(ModelResourceLocation modelLocation, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        if (true){
            BakedModel model = minecraft.getModelManager().getModel(modelLocation);
            minecraft.getItemRenderer().renderModelLists(
                    model,
                    ItemStack.EMPTY,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource.getBuffer(Sheets.translucentItemSheet())
            );
            return;
        }
        BakedModel model = minecraft.getModelManager().getModel(modelLocation);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        RandomSource randomSource = RandomSource.create();
        List<BakedQuad> quads = model.getQuads(null, null, randomSource, null, null);
        PoseStack.Pose pose = poseStack.last();
        quads.forEach(bakedQuad -> consumer.putBulkData(pose, bakedQuad, 1, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true));
    }
}
