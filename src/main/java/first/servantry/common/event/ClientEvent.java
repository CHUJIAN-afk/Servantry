package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.client.render.EntityRenderDispatcher;
import first.servantry.client.renderer.projectile.StardustProjectileConeRenderer;
import first.servantry.client.renderer.servant.EnchantedThrowingKnivesRendererServant;
import first.servantry.client.renderer.servant.StardustCellRendererServant;
import first.servantry.client.renderer.servant.StardustDragonRenderer;
import first.servantry.client.renderer.servant.TerraprismRendererServant;
import first.servantry.common.particle.StardustScatterParticle;
import first.servantry.register.ParticleRegister;
import first.servantry.register.ProjectileRegister;
import first.servantry.register.ServantRegister;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegister.StardustScatter.get(), StardustScatterParticle.Provider::new);
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderDispatcher.register(ServantRegister.TerraPrism.get(), new TerraprismRendererServant());
        EntityRenderDispatcher.register(ServantRegister.StardustCell.get(), new StardustCellRendererServant());
        EntityRenderDispatcher.register(ServantRegister.EnchantedThrowingKnives.get(), new EnchantedThrowingKnivesRendererServant());
        EntityRenderDispatcher.register(ServantRegister.StardustDragon.get(), new StardustDragonRenderer());
        EntityRenderDispatcher.register(ProjectileRegister.StardustProjectile.get(), new StardustProjectileConeRenderer());
    }

}