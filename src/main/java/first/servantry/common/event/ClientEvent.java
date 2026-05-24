package first.servantry.common.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import first.servantry.Servantry;
import first.servantry.api.client.render.AttachmentEntityRenderDispatcher;
import first.servantry.api.client.renderType.TrailShaders;
import first.servantry.client.attachmentEntityRenderer.projectile.LaserProjectileRenderer;
import first.servantry.client.attachmentEntityRenderer.projectile.SharkDragonProjectileRenderer;
import first.servantry.client.attachmentEntityRenderer.projectile.ShatteredStellarCoreProjectileRenderer;
import first.servantry.client.attachmentEntityRenderer.projectile.StardustProjectileRenderer;
import first.servantry.client.attachmentEntityRenderer.servant.*;
import first.servantry.client.renderType.OreScoutHighlightRenderType;
import first.servantry.client.tooltip.ScabbardTooltipComponent;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.particle.provider.GenericParticleProvider;
import first.servantry.common.servant.OreScout;
import first.servantry.register.*;
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

import java.io.IOException;
import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderOreScoutHighlights(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel clientLevel = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || clientLevel == null || player == null) {
            return;
        }
        OreScout scout = player.getData(AttachmentRegister.EntityData).getEntities().stream()
                .filter(attachmentEntity -> attachmentEntity instanceof OreScout)
                .map(attachmentEntity -> (OreScout) attachmentEntity)
                .filter(oreScout -> !oreScout.getHighlightedOres().isEmpty())
                .findFirst()
                .orElse(null);
        if (scout != null) {
            MultiBufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            VertexConsumer innerConsumer = bufferSource.getBuffer(OreScoutHighlightRenderType.line());
            Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
            PoseStack poseStack = event.getPoseStack();
            for (BlockPos pos : scout.getHighlightedOres()) {
                poseStack.pushPose();
                poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
                LevelRenderer.renderLineBox(poseStack, innerConsumer, 0, 0, 0, 1, 1, 1, 0.72F, 0.96F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegister.Generic.get(), GenericParticleProvider::new);
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.TerraPrism.get(), new TerraprismRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.EnchantedThrowingKnives.get(), new EnchantedThrowingKnivesRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.StardustCell.get(), new StardustCellRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.StardustDragon.get(), new StardustDragonRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.Twins.get(), new TwinsRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.Sharknado.get(), new SharknadoRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.InfiniteShadow.get(), new InfiniteShadowRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.DeadlySphere.get(), new DeadlySphereRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.EtherealStellarCore.get(), new EyeOfEternalNightRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.OreScout.get(), new OreScoutServantRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.ScavengerFairy.get(), new ScavengerFairyRenderer());

        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.StardustProjectile.get(), new StardustProjectileRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.LaserProjectile.get(), new LaserProjectileRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.SharkDragonProjectile.get(), new SharkDragonProjectileRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.EternalNightLaserProjectile.get(), new ShatteredStellarCoreProjectileRenderer());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        TrailShaders.register(event);
    }

    @SubscribeEvent
    public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ScabbardTooltipComponent.class, tooltipComponent -> tooltipComponent);
    }

    @SubscribeEvent
    public static void renderSoulItemTooltipHandler(RenderTooltipEvent.GatherComponents event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.is(ItemRegister.InfiniteScabbard.get())) {
            ScabbardContainer scabbard = itemStack.getOrDefault(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
            if (!scabbard.isEmpty()) {
                List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();
                tooltipElements.add(1, Either.right(new ScabbardTooltipComponent(scabbard.itemStack())));
            }
        }
    }
}
