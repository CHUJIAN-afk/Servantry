package first.servantry.api.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.servant.ITrailRenderer;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.ITrailRenderer.TrailNode;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public interface IProjectileConeTrail extends IProjectileTrail {

    default float getTrailMaxRadius() { return 0.2f; }
    default int getTrailResolution() { return 6; }
    default int getTrailColorRGB(float progress) { return 0xFFFFFF; }
    
    default float getTrailFadeOut(float progress) {
        return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);
    }

    @Override
    default void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, AdvancedProjectile projectile, PathNode visualRenderNode, List<TrailNode> smoothNodes) {
        if (smoothNodes.size() < 2) return;

        VertexConsumer consumer = bufferSource.getBuffer(ITrailRenderer.TrailRenderType.getTrail());
        Matrix4f pose = poseStack.last().pose();
        int res = getTrailResolution();
        float maxRadius = getTrailMaxRadius();
        Vec3 renderPos = visualRenderNode.pos();

        for (int i = 0; i < smoothNodes.size() - 1; i++) {
            TrailNode curr = smoothNodes.get(i);
            TrailNode prev = smoothNodes.get(i + 1);

            float currProgress = (float) i / (smoothNodes.size() - 1);
            float prevProgress = (float) (i + 1) / (smoothNodes.size() - 1);

            float currFade = getTrailFadeOut(currProgress);
            float prevFade = getTrailFadeOut(prevProgress);

            float currRadius = maxRadius * currFade;
            float prevRadius = maxRadius * prevFade;

            int currColor = getTrailColorRGB(currProgress);
            int prevColor = getTrailColorRGB(prevProgress);

            int cA = Math.round(currFade * 200);
            int pA = Math.round(prevFade * 200);

            int cr = (currColor >> 16) & 0xFF, cg = (currColor >> 8) & 0xFF, cb = currColor & 0xFF;
            int pr = (prevColor >> 16) & 0xFF, pg = (prevColor >> 8) & 0xFF, pb = prevColor & 0xFF;

            int cColorVal = FastColor.ARGB32.color(cA, cr, cg, cb);
            int pColorVal = FastColor.ARGB32.color(pA, pr, pg, pb);

            Vec3 cRel = curr.pos.subtract(renderPos);
            Vec3 pRel = prev.pos.subtract(renderPos);

            for (int j = 0; j < res; j++) {
                float angle1 = (float) j / res * (float) Math.PI * 2f;
                float angle2 = (float) (j + 1) / res * (float) Math.PI * 2f;

                Vector3f cV1 = new Vector3f((float)Math.cos(angle1)*currRadius, (float)Math.sin(angle1)*currRadius, 0).rotate(curr.rot);
                Vector3f cV2 = new Vector3f((float)Math.cos(angle2)*currRadius, (float)Math.sin(angle2)*currRadius, 0).rotate(curr.rot);
                Vector3f pV1 = new Vector3f((float)Math.cos(angle1)*prevRadius, (float)Math.sin(angle1)*prevRadius, 0).rotate(prev.rot);
                Vector3f pV2 = new Vector3f((float)Math.cos(angle2)*prevRadius, (float)Math.sin(angle2)*prevRadius, 0).rotate(prev.rot);

                consumer.addVertex(pose, (float)cRel.x + cV1.x(), (float)cRel.y + cV1.y(), (float)cRel.z + cV1.z()).setColor(cColorVal).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float)cRel.x + cV2.x(), (float)cRel.y + cV2.y(), (float)cRel.z + cV2.z()).setColor(cColorVal).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float)pRel.x + pV2.x(), (float)pRel.y + pV2.y(), (float)pRel.z + pV2.z()).setColor(pColorVal).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float)pRel.x + pV1.x(), (float)pRel.y + pV1.y(), (float)pRel.z + pV1.z()).setColor(pColorVal).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            }
        }
    }
}