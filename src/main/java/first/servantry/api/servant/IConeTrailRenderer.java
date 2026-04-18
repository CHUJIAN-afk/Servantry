package first.servantry.api.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * 圆锥形轨迹渲染接口，末端细，前端粗，自带干涉淡出效果
 */
public interface IConeTrailRenderer extends ITrailRenderer {

    /**
     * 获取轨迹在当前位置的最大半径
     */
    default float getTrailMaxRadius() {
        return 0.2f;
    }

    /**
     * 获取干涉淡出效果，根据轨迹进度 (0.0=当前位置, 1.0=末端) 计算透明度和半径缩放
     */
    default float getTrailFadeOut(float progress) {
        // 使用一个带有曲线平滑的淡出，末端迅速变细并消散
        return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);
    }

    /**
     * 圆锥的切面多边形边数，越高越圆滑，但性能消耗越大
     */
    default int getTrailResolution() {
        return 6;
    }

    /**
     * 轨迹核心颜色 (RGB)
     */
    default int getTrailColorRGB(float progress) {
        return 0xFF0000; // 默认红色
    }

    @Override
    default void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode visualRenderNode, List<TrailNode> smoothNodes) {
        VertexConsumer consumer = bufferSource.getBuffer(TrailRenderType.getTrail());
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

            int cA = Math.round(currFade * 200); // 最大透明度限制
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

                // 生成受当前节点旋转影响的横截面顶点
                Vector3f cV1 = new Vector3f((float)Math.cos(angle1)*currRadius, (float)Math.sin(angle1)*currRadius, 0).rotate(curr.rot);
                Vector3f cV2 = new Vector3f((float)Math.cos(angle2)*currRadius, (float)Math.sin(angle2)*currRadius, 0).rotate(curr.rot);
                Vector3f pV1 = new Vector3f((float)Math.cos(angle1)*prevRadius, (float)Math.sin(angle1)*prevRadius, 0).rotate(prev.rot);
                Vector3f pV2 = new Vector3f((float)Math.cos(angle2)*prevRadius, (float)Math.sin(angle2)*prevRadius, 0).rotate(prev.rot);

                // 绘制四边形 (Quad) 闭合管状轨迹
                consumer.addVertex(pose, (float)cRel.x + cV1.x(), (float)cRel.y + cV1.y(), (float)cRel.z + cV1.z()).setColor(cColorVal).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float)cRel.x + cV2.x(), (float)cRel.y + cV2.y(), (float)cRel.z + cV2.z()).setColor(cColorVal).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float)pRel.x + pV2.x(), (float)pRel.y + pV2.y(), (float)pRel.z + pV2.z()).setColor(pColorVal).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
                consumer.addVertex(pose, (float)pRel.x + pV1.x(), (float)pRel.y + pV1.y(), (float)pRel.z + pV1.z()).setColor(pColorVal).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            }
        }
        if (!smoothNodes.isEmpty()) {
            Vec3 headPos = smoothNodes.getFirst().pos.subtract(renderPos);
            float radius = getTrailMaxRadius();
            int colorRGB = getTrailColorRGB(0f);
            int r = (colorRGB >> 16) & 0xFF;
            int g = (colorRGB >> 8) & 0xFF;
            int b = colorRGB & 0xFF;
            int colorVal = FastColor.ARGB32.color(200, r, g, b);

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

    private Vector3f getSphereVertex(Vec3 center, float radius, float theta, float phi) {
        float x = (float) (center.x + radius * Math.sin(phi) * Math.cos(theta));
        float y = (float) (center.y + radius * Math.cos(phi));
        float z = (float) (center.z + radius * Math.sin(phi) * Math.sin(theta));
        return new Vector3f(x, y, z);
    }

}