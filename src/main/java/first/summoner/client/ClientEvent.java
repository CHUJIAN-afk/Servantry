package first.summoner.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import first.summoner.Summoner;
import first.summoner.client.render.MoonPortalEffect;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void shaders(RegisterShadersEvent event) throws IOException {
        MoonPortalEffect.close();
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("moon_portal"), DefaultVertexFormat.POSITION), MoonPortalEffect::setShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("moon_portal_beam"), DefaultVertexFormat.POSITION), MoonPortalEffect::setBeamShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("moon_portal_resolve"), DefaultVertexFormat.POSITION), MoonPortalEffect::setResolveShader);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderPortals(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            MoonPortalEffect.render();
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            MoonPortalEffect.close();
        }
    }

    @SubscribeEvent
    public static void handler(ClientTickEvent.Post event) {
        MusicManager.handler(event);
    }
}
