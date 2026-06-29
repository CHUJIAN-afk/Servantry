package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.renderType.TrailRenderType;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.CustomLaserProjectile;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class CustomLaserProjectileRenderer extends AbstractAttachmentEntityRenderer<CustomLaserProjectile> {

    @Override
    protected RenderContext<CustomLaserProjectile> createContext(CustomLaserProjectile laser) {
        return RenderContext.<CustomLaserProjectile>builder()
                .model(new ModelConfig<CustomLaserProjectile>()
                               .alphaDistanceFactor(0)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void renderEntity(CustomLaserProjectile entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<CustomLaserProjectile> config) {
        int color = entity.getColor();
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        VertexConsumer consumer = bufferSource.getBuffer(TrailRenderType.getTrail());
        Matrix4f pose = poseStack.last().pose();
        // 内核光柱
        renderBeamQuads(pose, consumer, length, width, ARGB32.color((int) (255 * entity.getAlpha()), color), 0);
        // 外层光晕，更宽更透明
        renderBeamQuads(pose, consumer, length, width * 1.5f, ARGB32.color((int) (128 * entity.getAlpha()), color), 22.5F);
    }

    private void renderBeamQuads(Matrix4f pose, VertexConsumer consumer, float length, float radius, int color, float rotation) {
        float rad = (float) Math.toRadians(rotation);
        // 四个面，每面绕旋转角偏移 90 度
        for (int face = 0; face < 4; face++) {
            float angle = rad + face * (float) (Math.PI / 2);
            float nx = (float) Math.cos(angle);
            float ny = (float) Math.sin(angle);
            // 面的两个边缘顶点偏移
            float ex1 = nx * radius, ey1 = ny * radius;
            float ex2 = -ny * radius, ey2 = nx * radius;
            // z=0 端（近端）
            emitVertex(consumer, pose, ex1, ey1, 0, color, 1, 0, nx, ny, 0);
            emitVertex(consumer, pose, ex2, ey2, 0, color, 0, 0, nx, ny, 0);
            // z=-length 端（远端）
            emitVertex(consumer, pose, ex2, ey2, -length, color, 0, 1, nx, ny, 0);
            emitVertex(consumer, pose, ex1, ey1, -length, color, 1, 1, nx, ny, 0);
        }
    }

    private void emitVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, int color, float u, float v, float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(nx, ny, nz);
    }
}
