package first.servantry.api.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.servant.ITrailRenderer;
import first.servantry.api.servant.ITrailRenderer.TrailNode;
import first.servantry.api.servant.PathNode;
import first.servantry.common.projectile.StardustLaser;
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

    // 控制粗细的消散率
    default float getTrailFadeOut(float progress) {
        return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);
    }

    @Override
    default void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, AdvancedProjectile projectile, PathNode visualRenderNode, List<TrailNode> smoothNodes) {
        if (smoothNodes.size() < 2) return;

        // 【新增】获取消散进度（如果适用）
        float deathProgress = 0.0f;
        if (projectile instanceof StardustLaser laser) {
            deathProgress = laser.getDeathProgress();
        }

        VertexConsumer consumer = bufferSource.getBuffer(ITrailRenderer.TrailRenderType.getTrail());
        Matrix4f pose = poseStack.last().pose();
        int res = getTrailResolution();
        float maxRadius = getTrailMaxRadius();
        Vec3 renderPos = visualRenderNode.pos();

        for (int i = 0; i < smoothNodes.size() - 1; i++) {
            TrailNode curr = smoothNodes.get(i);
            TrailNode prev = smoothNodes.get(i + 1);

            float currBaseProgress = (float) i / (smoothNodes.size() - 1);
            float prevBaseProgress = (float) (i + 1) / (smoothNodes.size() - 1);

            // 【核心计算】加上消散进度，使得消散时尾部（progress最先达到1.0）率先隐藏
            float currEffectiveProgress = Math.min(1.0f, currBaseProgress + deathProgress);
            float prevEffectiveProgress = Math.min(1.0f, prevBaseProgress + deathProgress);

            float currFade = getTrailFadeOut(currEffectiveProgress);
            float prevFade = getTrailFadeOut(prevEffectiveProgress);

            float currRadius = maxRadius * currFade;
            float prevRadius = maxRadius * prevFade;

            int currColor = getTrailColorRGB(currBaseProgress); // 颜色可以保留原进度以防断层
            int prevColor = getTrailColorRGB(prevBaseProgress);

            // 【增加常态的末端透明度增加效果】：使用 3.0 更高的次幂让尾端透明得更快
            float currAlphaFade = (float) Math.pow(Math.max(0.0f, 1.0f - currEffectiveProgress), 3.0);
            float prevAlphaFade = (float) Math.pow(Math.max(0.0f, 1.0f - prevEffectiveProgress), 3.0);

            int cA = Math.round(currAlphaFade * 200);
            int pA = Math.round(prevAlphaFade * 200);

            // 如果已经完全透明则跳过顶点渲染优化性能
            if (cA <= 0 && pA <= 0) continue;

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

        if (!smoothNodes.isEmpty()) {
            // 【修改】头部圆球的透明度也会随着消散逐渐降为 0
            float headAlphaFade = (float) Math.pow(Math.max(0.0f, 1.0f - deathProgress), 3.0);
            if (headAlphaFade > 0.01f) {
                Vec3 headPos = smoothNodes.getFirst().pos.subtract(renderPos);
                // 头部半径在消散时同步变小
                float radius = getTrailMaxRadius() * getTrailFadeOut(deathProgress);
                int colorRGB = getTrailColorRGB(0f);
                int r = (colorRGB >> 16) & 0xFF;
                int g = (colorRGB >> 8) & 0xFF;
                int b = colorRGB & 0xFF;
                int colorVal = FastColor.ARGB32.color(Math.round(headAlphaFade * 200), r, g, b);

                int rings = 8;
                int sectors = 8;
                for (int i = 0; i < rings; i++) {
                    float phi1 = (float) (Math.PI * (float) i / rings);
                    float phi2 = (float) (Math.PI * (float) (i + 1) / rings);
                    for (int j = 0; j < sectors; j++) {
                        float theta1 = (float) (2.0 * Math.PI * (float) j / sectors);
                        float theta2 = (float) (2.0 * Math.PI * (float) (j + 1) / sectors);

                        Vector3f v1 = getSphereVertex(headPos, radius, theta1, phi1);
                        Vector3f v2 = getSphereVertex(headPos, radius, theta2, phi1);
                        Vector3f v3 = getSphereVertex(headPos, radius, theta2, phi2);
                        Vector3f v4 = getSphereVertex(headPos, radius, theta1, phi2);

                        consumer.addVertex(pose, v1.x(), v1.y(), v1.z()).setColor(colorVal).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                        consumer.addVertex(pose, v2.x(), v2.y(), v2.z()).setColor(colorVal).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                        consumer.addVertex(pose, v3.x(), v3.y(), v3.z()).setColor(colorVal).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                        consumer.addVertex(pose, v4.x(), v4.y(), v4.z()).setColor(colorVal).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                    }
                }
            }
        }
    }

    private Vector3f getSphereVertex(Vec3 center, float radius, float theta, float phi) {
        float x = (float) (center.x + radius * Math.sin(phi) * Math.cos(theta));
        float y = (float) (center.y + radius * Math.cos(phi));
        float z = (float) (center.z + radius * Math.sin(phi) * Math.sin(theta));
        return new Vector3f(x, y, z);
    }
}