package first.servantry.api.client.render.renderConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.PathNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 水滴拖尾配置（圆锥 + 头部半球）。
 * <p>
 * 适用于需要圆润头部的拖尾效果。圆锥主体复用 {@link ConeTrailConfig}，
 * 本类仅追加头部半球。
 * </p>
 * <pre>{@code
 * 效果示意：
 *       ╭─╮  ← 半球顶部
 *      ╱   ╲
 *     │     │
 *      ╲   ╱
 *       ╰─╯  ← 圆锥尾部
 * 头→尾
 * }</pre>
 *
 * @param <T> 实体类型
 */
public class DropletTrailConfig<T extends AttachmentEntity> extends ConeTrailConfig<T> {

    @Override
    public void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode, RenderType renderType) {
        RenderSetup<T> setup = beginRender(entity, poseStack, bufferSource, partialTick, visualNode, renderType);
        if (setup == null) {
            return;
        }
        // 圆锥主体（复用父类）
        renderConeBody(setup);
        // 头部半球
        renderHeadHemisphere(setup);
    }

    /**
     * 渲染头部半球：在首个平滑节点处画半圆封顶。
     */
    protected void renderHeadHemisphere(RenderSetup<T> setup) {
        VertexConsumer consumer = setup.consumer;
        Matrix4f pose = setup.pose;
        T entity = setup.entity;

        float[] cosArr = getCosArray(resolution);
        float[] sinArr = getSinArray(resolution);

        InterpolatedNode headNode = setup.smoothNodes.getFirst();
        float headFade = fadeOut.getFade(0);
        // 头部半径必须与圆锥第一段（progress=0）的头部半径用同一公式，
        // 否则半球赤道与圆锥截面半径不一致，接缝处错位重合。
        float headRadius = maxRadius * (minRadiusRatio + (1 - minRadiusRatio) * headFade);
        int headColor = colorFunction.getColor(entity, 0, setup.partialTick);
        // 旧版 alpha = round(headFade * 200)
        int headARGB = packColor(headColor, headFade * (200f / 255f));

        Vec3 headRel = headNode.pos().subtract(setup.renderPos);
        int hemisphereSegments = Math.max(2, resolution / 2);

        for (int lat = 0; lat < hemisphereSegments; lat++) {
            float latAngle1 = (float) (Math.PI / 2 * lat / hemisphereSegments);
            float latAngle2 = (float) (Math.PI / 2 * (lat + 1) / hemisphereSegments);
            float r1 = (float) Math.cos(latAngle1) * headRadius;
            float r2 = (float) Math.cos(latAngle2) * headRadius;
            float h1 = (float) Math.sin(latAngle1) * headRadius;
            float h2 = (float) Math.sin(latAngle2) * headRadius;

            for (int lon = 0; lon < resolution; lon++) {
                float cos1 = cosArr[lon], sin1 = sinArr[lon];
                float cos2 = cosArr[lon + 1], sin2 = sinArr[lon + 1];

                Vector3f v1 = new Vector3f(cos1 * r1, sin1 * r1, h1).rotate(headNode.rot());
                Vector3f v2 = new Vector3f(cos2 * r1, sin2 * r1, h1).rotate(headNode.rot());
                Vector3f v3 = new Vector3f(cos2 * r2, sin2 * r2, h2).rotate(headNode.rot());
                Vector3f v4 = new Vector3f(cos1 * r2, sin1 * r2, h2).rotate(headNode.rot());

                emitQuad(consumer, pose,
                        (float) headRel.x + v1.x, (float) headRel.y + v1.y, (float) headRel.z + v1.z, headARGB,
                        (float) headRel.x + v2.x, (float) headRel.y + v2.y, (float) headRel.z + v2.z, headARGB,
                        (float) headRel.x + v3.x, (float) headRel.y + v3.y, (float) headRel.z + v3.z, headARGB,
                        (float) headRel.x + v4.x, (float) headRel.y + v4.y, (float) headRel.z + v4.z, headARGB);
            }
        }
    }
}
