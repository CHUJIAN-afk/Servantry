package first.servantry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import first.servantry.Servantry;
import first.servantry.api.ServantryHelper;
import first.servantry.api.client.render.AttachmentEntityRenderDispatcher;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.client.attachmentEntityRenderer.projectile.*;
import first.servantry.client.attachmentEntityRenderer.projectile.ChlorophyteCrystalRenderer;
import first.servantry.client.attachmentEntityRenderer.servant.*;
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
import net.neoforged.neoforge.client.event.*;

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
                    .get(EntityData.Type.Servant, ServantryAttachmentEntityRegister.OreScout.get());
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
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.TerraPrism.get(), new TerraprismRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.EnchantedThrowingKnives.get(), new EnchantedThrowingKnivesRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.StardustCell.get(), new StardustCellRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.StardustDragon.get(), new StardustDragonRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.Twins.get(), new TwinsRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.Sharknado.get(), new SharknadoRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.InfiniteShadow.get(), new InfiniteShadowRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.DeadlySphere.get(), new DeadlySphereRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.EtherealStellarCore.get(), new EtherealStellarCoreRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.OreScout.get(), new OreScoutServantRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.ScavengerFairy.get(), new ScavengerFairyRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.ChlorophyteCrystal.get(), new first.servantry.client.attachmentEntityRenderer.servant.ChlorophyteCrystalRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.VoidEater.get(), new VoidEaterRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.Ballista.get(), new BallistaRenderer());

        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.StardustProjectile.get(), new MiniStardustRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.LaserProjectile.get(), new LaserRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.DemonFlameProjectile.get(), new DemonFlameRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.SharkDragonProjectile.get(), new SharkDragonRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.EternalNightLaserProjectile.get(), new ShatteredStellarCoreRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.ChlorophyteCrystalProjectile.get(), new ChlorophyteCrystalRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.ZenithProjectile.get(), new ZenithRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.CustomLaserProjectile.get(), new CustomLaserRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.RainbowCrystalProjectile.get(), new RainbowCrystalRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.GodFlameProjectile.get(), new GodFlameRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.BlitzBall.get(), new BlitzBallRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.Rain.get(), new RainRenderer());
        AttachmentEntityRenderDispatcher.register(ServantryAttachmentEntityRegister.DestructionBullet.get(), new DestructionBulletRenderer());
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