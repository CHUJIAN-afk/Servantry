package first.summoner.register;


import first.summoner.Summoner;
import first.summoner.client.entityRenerer.AstrageldonRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Summoner.MODID)
public class SummonerEntityRenderRegister {

    @SubscribeEvent
    public static void handler(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SummonerEntityRegister.ASTRAGELDON.get(), AstrageldonRenderer::new);
        event.registerEntityRenderer(SummonerEntityRegister.ASTRA_BLADE.get(), NoopRenderer::new);
        event.registerEntityRenderer(SummonerEntityRegister.ASTRA_ROCK.get(), NoopRenderer::new);
        event.registerEntityRenderer(SummonerEntityRegister.ASTRAGELDON_STAR.get(), NoopRenderer::new);
        event.registerEntityRenderer(SummonerEntityRegister.ASTRAGELDON_LASER.get(), NoopRenderer::new);
        event.registerEntityRenderer(SummonerEntityRegister.NOVA_SLIME.get(), NoopRenderer::new);
    }
}
