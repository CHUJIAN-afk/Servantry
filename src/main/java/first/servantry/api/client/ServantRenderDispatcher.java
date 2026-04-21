package first.servantry.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.ICollide;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServantRenderDispatcher {

    private static final Map<ServantType<?>, ServantRenderer<?>> renderers = new HashMap<>();

    public static void render(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        List<Servant> servants = player.getData(AttachmentRegister.ServantData).getServants();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        for (Servant servant : servants) {
            if (getRenderer(servant) instanceof ServantRenderer<Servant> renderer) {
                servant.setOwner(player);
                poseStack.pushPose();
                PathNode renderNode = servant.getRenderNode(partialTick);
                poseStack.translate(renderNode.pos().x() - cameraPos.x(), renderNode.pos().y() - cameraPos.y(), renderNode.pos().z() - cameraPos.z());
                int packedLight = LevelRenderer.getLightColor(((LocalPlayer) player).clientLevel, BlockPos.containing(renderNode.pos().x(), renderNode.pos().y(), renderNode.pos().z()));
                renderer.render(servant, poseStack, bufferSource, partialTick, packedLight, renderNode);
                if (renderer instanceof IRibbonTrailRenderer ribbonTrailRenderer) {
                    ribbonTrailRenderer.processRibbonTrailRender(poseStack, bufferSource, partialTick, servant, renderNode);
                }
                if (servant instanceof IConeTrailRenderer coneTrailRenderer) {
                    coneTrailRenderer.processConeTrailRender(poseStack, bufferSource, partialTick, servant, renderNode);
                }
                if (servant instanceof ICollide iCollide) {
                    if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                        poseStack.pushPose();
                        poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));
                        poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
                        poseStack.translate(0, 0, 0.5);
                        poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.roll()));
                        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
                        LevelRenderer.renderLineBox(poseStack, consumer, iCollide.getHitbox(), 1.0F, 0.0F, 0.0F, 1.0F);
                        poseStack.popPose();
                    }
                }
                poseStack.popPose();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Servant> ServantRenderer<T> getRenderer(T servant) {
        ServantType<T> type = (ServantType<T>) servant.getType();
        return (ServantRenderer<T>) renderers.get(type);
    }

    public static <T extends Servant> void register(ServantType<T> type, ServantRenderer<T> renderer) {
        if (!renderers.containsKey(type)) {
            renderers.put(type, renderer);
        }
    }

}
