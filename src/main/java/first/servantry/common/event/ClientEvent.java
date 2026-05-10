package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.client.render.EntityRenderDispatcher;
import first.servantry.api.client.renderType.TrailShaders;
import first.servantry.client.attachmentEntityRenderer.*;
import first.servantry.common.particle.StardustScatterParticle;
import first.servantry.register.ParticleRegister;
import first.servantry.register.ProjectileRegister;
import first.servantry.register.ServantRegister;
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
        event.registerSpriteSet(ParticleRegister.StardustScatter.get(), StardustScatterParticle.Provider::new);
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderDispatcher.register(ServantRegister.TerraPrism.get(), new TerraprismRenderer());
        EntityRenderDispatcher.register(ServantRegister.EnchantedThrowingKnives.get(), new EnchantedThrowingKnivesRenderer());
        EntityRenderDispatcher.register(ServantRegister.StardustCell.get(), new StardustCellRenderer());
        EntityRenderDispatcher.register(ServantRegister.StardustDragon.get(), new StardustDragonRenderer());
        EntityRenderDispatcher.register(ServantRegister.Twins.get(), new TwinsRenderer());
        EntityRenderDispatcher.register(ProjectileRegister.StardustProjectile.get(), new StardustProjectileRenderer());
        EntityRenderDispatcher.register(ProjectileRegister.LaserProjectile.get(), new LaserProjectileRenderer());
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