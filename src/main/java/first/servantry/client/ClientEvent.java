package first.servantry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import first.servantry.Servantry;
import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.client.renderType.OreScoutHighlightRenderType;
import first.servantry.client.screen.MithrilAnvilGui;
import first.servantry.client.tooltip.ScabbardTooltipComponent;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.servant.OreScout;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantryDataComponentRegister;
import first.servantry.register.ServantryMenuRegister;
import first.servantry.register.ServantryServantWeaponRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderOreScoutHighlights(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel clientLevel = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS && clientLevel != null && player != null) {
            List<OreScout> scouts = ServantryHelper.get(player)
                    .getEntityData()
                    .get(EntityData.Type.Servant, ServantryAttachmentEntityRegister.ORE_SCOUT.get());
            if (!scouts.isEmpty()) {
                OreScout scout = scouts.getFirst();
                MultiBufferSource bufferSource = minecraft.renderBuffers()
                        .bufferSource();
                VertexConsumer innerConsumer = bufferSource.getBuffer(OreScoutHighlightRenderType.line());
                Vec3 cameraPos = minecraft.gameRenderer.getMainCamera()
                        .getPosition();
                PoseStack poseStack = event.getPoseStack();
                for (BlockPos pos : scout.getHighlightedOres()) {
                    poseStack.pushPose();
                    poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
                    LevelRenderer.renderLineBox(poseStack, innerConsumer, 0, 0, 0, 1, 1, 1, 0.72F, 0.96F, 1.0F, 1.0F);
                    poseStack.popPose();
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ServantryMenuRegister.MITHRIL_ANVIL.get(), MithrilAnvilGui.MithrilAnvilScreen::new);
    }

    @SubscribeEvent
    public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ScabbardTooltipComponent.class, tooltipComponent -> tooltipComponent);
    }

    @SubscribeEvent
    public static void renderSoulItemTooltipHandler(RenderTooltipEvent.GatherComponents event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.is(ServantryServantWeaponRegister.InfiniteScabbard.get())) {
            ScabbardContainer scabbard = itemStack.getOrDefault(ServantryDataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
            if (!scabbard.isEmpty()) {
                List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();
                tooltipElements.add(1, Either.right(new ScabbardTooltipComponent(scabbard.itemStack())));
            }
        }
    }
}