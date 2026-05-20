package first.servantry.common.event;

import com.mojang.datafixers.util.Either;
import first.servantry.Servantry;
import first.servantry.api.client.render.AttachmentEntityRenderDispatcher;
import first.servantry.api.client.renderType.TrailShaders;
import first.servantry.client.attachmentEntityRenderer.projectile.LaserProjectileRenderer;
import first.servantry.client.attachmentEntityRenderer.projectile.SharkDragonProjectileRenderer;
import first.servantry.client.attachmentEntityRenderer.projectile.StardustProjectileRenderer;
import first.servantry.client.attachmentEntityRenderer.servant.*;
import first.servantry.client.tooltip.ScabbardTooltipComponent;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.particle.provider.GenericParticleProvider;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.DataComponentRegister;
import first.servantry.register.ItemRegister;
import first.servantry.register.ParticleRegister;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.io.IOException;
import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

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


        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.StardustProjectile.get(), new StardustProjectileRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.LaserProjectile.get(), new LaserProjectileRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.SharkDragonProjectile.get(), new SharkDragonProjectileRenderer());
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
        net.minecraft.world.item.ItemStack itemStack = event.getItemStack();
        if (itemStack.is(ItemRegister.InfiniteScabbard.get())) {
            ScabbardContainer scabbard = itemStack.getOrDefault(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
            if (!scabbard.isEmpty()) {
                List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();
                tooltipElements.add(1, Either.right(new ScabbardTooltipComponent(scabbard.itemStack())));
            }
        }
    }

}