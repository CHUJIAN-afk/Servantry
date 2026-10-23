package first.summoner.client.attachmentEntityRenderer.minion;

import com.lowdragmc.lowdraglib2.math.HDRColor;
import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import com.lowdragmc.photon.client.gameobject.emitter.data.MaterialSetting;
import com.lowdragmc.photon.client.gameobject.emitter.data.RendererSetting;
import com.lowdragmc.photon.client.gameobject.emitter.data.material.BlendMode;
import com.lowdragmc.photon.client.gameobject.emitter.data.material.TextureMaterial;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.FXCompositeMode;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.ParticleQueueRenderType;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.PhotonFXRenderPass;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.RenderPassPipeline;
import com.lowdragmc.photon.client.gameobject.particle.IParticle;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import first.summoner.Summoner;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class TerraprismTrailPass extends PhotonFXRenderPass {

    private static final TerraprismTrailPass PASS = new TerraprismTrailPass(false);
    private static final TerraprismTrailPass HIGHLIGHT = new TerraprismTrailPass(true);
    private static final RandomSource RANDOM = RandomSource.create();
    private static Carrier carrier;

    private TerraprismTrailPass(boolean highlight) {
        super(createRenderer(highlight), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }

    private static RendererSetting.Runtime createRenderer(boolean highlight) {
        TextureMaterial texture = new TextureMaterial(Summoner.rl("textures/particle/terraprism_trail.png"));
        texture.setDiscardThreshold(0.001f);
        if (highlight) {
            texture.setHdr(HDRColor.white().withIntensity(3f));
            texture.setHdrMode(TextureMaterial.HDRMode.MULTIPLICATIVE);
        }
        MaterialSetting material = new MaterialSetting(texture).setCull(false).setDepthTest(true).setDepthMask(false);
        material.setBlend((highlight ? BlendMode.Preset.ADDITIVE : BlendMode.Preset.ALPHA).create());
        RendererSetting setting = new RendererSetting();
        setting.setMaterials(List.of(material));
        setting.setVertexSortingMode(RendererSetting.SortMode.DISTANCE);
        setting.setCompositeMode(FXCompositeMode.LATE);
        setting.setOrderInLayer(highlight ? 1 : 0);
        return new RendererSetting.Runtime(setting) {
        };
    }

    public static void beginFrame() {
        if (carrier != null && !IrisCompat.isShadowPass()) {
            carrier.ribbons.clear();
        }
    }

    public static void submit(List<Vertex> vertices, List<Vertex> highlight) {
        if (carrier == null) {
            Minecraft minecraft = Minecraft.getInstance();
            carrier = new Carrier(minecraft.level);
            minecraft.particleEngine.add(carrier);
        }
        carrier.ribbons.add(new Ribbon(vertices, false));
        carrier.ribbons.add(new Ribbon(highlight, true));
    }

    public static void close() {
        if (carrier != null) {
            carrier.ribbons.clear();
            carrier.remove();
            carrier = null;
        }
    }

    @Override
    protected void renderQueue(VertexConsumer buffer, Collection<IParticle> particles, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        for (IParticle particle : particles) {
            for (Vertex vertex : ((Ribbon) particle).vertices) {
                Vec3 pos = vertex.pos.subtract(cameraPos);
                buffer.addVertex((float) pos.x, (float) pos.y, (float) pos.z)
                        .setColor(vertex.color).setUv(vertex.u, vertex.v)
                        .setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
            }
        }
    }

    public record Vertex(Vec3 pos, int color, float u, float v) {
    }

    private static class Carrier extends Particle {

        private final List<IParticle> ribbons = new ArrayList<>();

        private Carrier(ClientLevel level) {
            super(level, 0, 0, 0);
        }

        @Override
        public void tick() {
        }

        @Override
        public void render(VertexConsumer buffer, Camera camera, float partialTick) {
            if (!IrisCompat.isShadowPass() && buffer instanceof RenderPassPipeline pipeline && !ribbons.isEmpty()) {
                pipeline.setupRenderingState(camera, partialTick);
                for (IParticle ribbon : ribbons) {
                    pipeline.pipeQueue(ribbon.getRenderType(), List.of(ribbon));
                }
                ribbons.clear();
            }
        }

        @Override
        public @NotNull ParticleRenderType getRenderType() {
            return ParticleQueueRenderType.TRANSLUCENT_QUEUE;
        }

        @Override
        public @NotNull AABB getRenderBoundingBox(float partialTick) {
            return AABB.INFINITE;
        }
    }

    private record Ribbon(List<Vertex> vertices, boolean highlight) implements IParticle {

        @Override
        public PhotonFXRenderPass getRenderType() {
            return highlight ? HIGHLIGHT : PASS;
        }

        @Override
        public RandomSource getRandomSource() {
            return RANDOM;
        }

        @Override
        public boolean isRemoved() {
            return false;
        }

        @Override
        public float getT() {
            return 0f;
        }

        @Override
        public float getT(float partialTick) {
            return 0f;
        }

        @Override
        public float getMemRandom(Object key) {
            return RandomSource.create(key.hashCode()).nextFloat();
        }

        @Override
        public float getMemRandom(Object key, Function<RandomSource, Float> function) {
            return function.apply(RandomSource.create(key.hashCode()));
        }

        @Override
        public void updateTick(float delta) {
        }
    }
}
