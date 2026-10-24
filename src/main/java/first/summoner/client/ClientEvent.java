package first.summoner.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import first.summoner.Summoner;
import first.summoner.client.attachmentEntityRenderer.minion.TerraprismTrailPass;
import first.summoner.client.render.AstrageldonArenaEffect;
import first.summoner.client.render.FlameEffect;
import first.summoner.client.render.MoonPortalEffect;
import first.summoner.client.render.RainbowCrystalEffect;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void shaders(RegisterShadersEvent event) throws IOException {
        TerraprismTrailPass.close();
        MoonPortalEffect.close();
        FlameEffect.close();
        RainbowCrystalEffect.close();
        AstrageldonArenaEffect.close();
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("astrageldon_arena"), DefaultVertexFormat.POSITION), AstrageldonArenaEffect::setShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("astrageldon_arena_resolve"), DefaultVertexFormat.POSITION), AstrageldonArenaEffect::setResolveShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("moon_portal"), DefaultVertexFormat.POSITION), MoonPortalEffect::setShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("moon_portal_beam"), DefaultVertexFormat.POSITION), MoonPortalEffect::setBeamShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("moon_portal_resolve"), DefaultVertexFormat.POSITION), MoonPortalEffect::setResolveShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("flame"), DefaultVertexFormat.POSITION), FlameEffect::setShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("flame_color"), DefaultVertexFormat.POSITION), FlameEffect::setColorShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("flame_composite"), DefaultVertexFormat.POSITION), FlameEffect::setCompositeShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("rainbow_crystal"), DefaultVertexFormat.POSITION), RainbowCrystalEffect::setCrystalShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("rainbow_crystal_projectile"), DefaultVertexFormat.POSITION), RainbowCrystalEffect::setProjectileShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("rainbow_crystal_beam"), DefaultVertexFormat.POSITION), RainbowCrystalEffect::setBeamShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("rainbow_crystal_spark"), DefaultVertexFormat.POSITION), RainbowCrystalEffect::setSparkShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("rainbow_crystal_glow"), DefaultVertexFormat.POSITION), RainbowCrystalEffect::setGlowShader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Summoner.rl("rainbow_crystal_resolve"), DefaultVertexFormat.POSITION), RainbowCrystalEffect::setResolveShader);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void trails(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            TerraprismTrailPass.beginFrame();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderPortals(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            AstrageldonArenaEffect.render();
            MoonPortalEffect.render();
            FlameEffect.render();
            RainbowCrystalEffect.render();
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            AstrageldonArenaClient.clear();
            AstrageldonMusic.clear();
            MusicManager.clear();
            TerraprismTrailPass.close();
            MoonPortalEffect.close();
            FlameEffect.close();
            RainbowCrystalEffect.close();
            AstrageldonArenaEffect.close();
        }
    }

    @SubscribeEvent
    public static void soundEngine(SoundEngineLoadEvent event) {
        AstrageldonMusic.clear();
        MusicManager.clear();
    }

    @SubscribeEvent
    public static void arenaTick(ClientTickEvent.Pre event) {
        AstrageldonArenaClient.beginTick();
    }

    @SubscribeEvent
    public static void handler(ClientTickEvent.Post event) {
        AstrageldonMusic.tick();
        MusicManager.handler(event);
    }
}
