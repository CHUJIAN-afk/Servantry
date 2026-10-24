package first.summoner.register;


import first.summoner.Summoner;
import first.summoner.client.entityRenerer.AstrageldonRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Summoner.MODID)
public class SummonerEntityRenderRegister {

    @SubscribeEvent
    public static void handler(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SummonerEntityRegister.ASTRAGELDON.get(), AstrageldonRenderer::new);
    }
}
