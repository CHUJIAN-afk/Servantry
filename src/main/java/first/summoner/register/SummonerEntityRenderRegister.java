package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.client.entityRenderer.DoomsdayStarRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public final class SummonerEntityRenderRegister {

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SummonerEntityRegister.DOOMSDAY_STAR.get(), DoomsdayStarRenderer::new);
    }
}
