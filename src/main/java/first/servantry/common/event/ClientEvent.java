package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.client.render.AttachmentEntityRenderDispatcher;
import first.servantry.api.client.renderType.TrailShaders;
import first.servantry.client.attachmentEntityRenderer.*;
import first.servantry.common.particle.provider.GenericParticleProvider;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.ParticleRegister;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegister.Generic.get(), GenericParticleProvider.registration());
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.TerraPrism.get(), new TerraprismRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.EnchantedThrowingKnives.get(), new EnchantedThrowingKnivesRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.StardustCell.get(), new StardustCellRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.StardustDragon.get(), new StardustDragonRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.Twins.get(), new TwinsRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.StardustProjectile.get(), new StardustProjectileRenderer());
        AttachmentEntityRenderDispatcher.register(AttachmentEntityRegister.LaserProjectile.get(), new LaserProjectileRenderer());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            TrailShaders.register(event);
        } catch (Exception e) {
            Servantry.LOGGER.error("Failed to register trail shaders", e);
        }
    }

}