package first.servantry.common.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 通用粒子 - 自定义渲染中心色块和边缘色块。
 */
public class GenericParticle extends TextureSheetParticle {

    private final SpriteSet spriteSet;
    private final float baseScale;
    private final float spinSpeed;

    // 中心颜色插值
    private final float startR, startG, startB;
    private final float endR, endG, endB;

    // 边缘颜色插值
    private final float startEdgeR, startEdgeG, startEdgeB;
    private final float endEdgeR, endEdgeG, endEdgeB;

    // 平滑插值
    private float prevScale;
    private float currentScale;

    public GenericParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet, GenericParticleOptions options) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = spriteSet;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.friction = options.friction();
        this.gravity = 0.0F;

        // 使用配置中的大小
        this.quadSize = options.scale();
        this.baseScale = this.quadSize;
        this.prevScale = this.baseScale;
        this.currentScale = this.baseScale;

        this.lifetime = options.lifetime();

        // 中心颜色
        int rgb = options.color();
        this.rCol = ((rgb >> 16) & 0xFF) / 255.0F;
        this.gCol = ((rgb >> 8) & 0xFF) / 255.0F;
        this.bCol = (rgb & 0xFF) / 255.0F;
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;

        // 中心最终颜色
        this.endR = ((options.endColor() >> 16) & 0xFF) / 255.0F;
        this.endG = ((options.endColor() >> 8) & 0xFF) / 255.0F;
        this.endB = (options.endColor() & 0xFF) / 255.0F;

        // 边缘颜色
        this.startEdgeR = ((options.edgeColor() >> 16) & 0xFF) / 255.0F;
        this.startEdgeG = ((options.edgeColor() >> 8) & 0xFF) / 255.0F;
        this.startEdgeB = (options.edgeColor() & 0xFF) / 255.0F;

        // 边缘最终颜色
        this.endEdgeR = ((options.endEdgeColor() >> 16) & 0xFF) / 255.0F;
        this.endEdgeG = ((options.endEdgeColor() >> 8) & 0xFF) / 255.0F;
        this.endEdgeB = (options.endEdgeColor() & 0xFF) / 255.0F;

        this.alpha = 1.0F;

        // 旋转
        this.spinSpeed = options.spinSpeed();
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;

        this.setSpriteFromAge(spriteSet);
    }

    public static GenericParticle createWithOptions(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet, GenericParticleOptions options) {
        return new GenericParticle(level, x, y, z, vx, vy, vz, spriteSet, options);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteSet);

        this.prevScale = this.currentScale;
        this.oRoll = this.roll;

        // 插值缩小
        float progress = (float) this.age / (float) this.lifetime;
        this.currentScale = this.baseScale * (1.0F - progress * progress);

        // 更新旋转
        this.roll += this.spinSpeed * (1 - progress);
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x);
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y);
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z);

        // 使用父类的 billboard 模式设置旋转
        Quaternionf quaternion = new Quaternionf();
        this.getFacingCameraMode().setRotation(quaternion, camera, partialTick);
        if (this.roll != 0.0F) {
            quaternion.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
        }

        // 平滑插值缩放
        float scale = Mth.lerp(partialTick, this.prevScale, this.currentScale);

        // 颜色插值
        float progress = (this.age + partialTick) / (float) this.lifetime;
        float centerR = Mth.lerp(progress, this.startR, this.endR);
        float centerG = Mth.lerp(progress, this.startG, this.endG);
        float centerB = Mth.lerp(progress, this.startB, this.endB);
        float edgeR = Mth.lerp(progress, this.startEdgeR, this.endEdgeR);
        float edgeG = Mth.lerp(progress, this.startEdgeG, this.endEdgeG);
        float edgeB = Mth.lerp(progress, this.startEdgeB, this.endEdgeB);

        // 纹理坐标
        float u0 = this.sprite.getU0();
        float u1 = this.sprite.getU1();
        float v0 = this.sprite.getV0();
        float v1 = this.sprite.getV1();

        int light = getLightColor(partialTick);

        // 每个色块大小相同（scale x scale），形成十字形
        // 中心色块
        renderQuad(buffer, x, y, z, quaternion, -scale, -scale, scale, scale, u0, u1, v0, v1, centerR, centerG, centerB, light);

        // 四个边缘色块，每个偏移 2*scale
        // 上
        renderQuad(buffer, x, y, z, quaternion, -scale, scale, scale, scale * 3, u0, u1, v0, v1, edgeR, edgeG, edgeB, light);
        // 下
        renderQuad(buffer, x, y, z, quaternion, -scale, -scale * 3, scale, -scale, u0, u1, v0, v1, edgeR, edgeG, edgeB, light);
        // 左
        renderQuad(buffer, x, y, z, quaternion, -scale * 3, -scale, -scale, scale, u0, u1, v0, v1, edgeR, edgeG, edgeB, light);
        // 右
        renderQuad(buffer, x, y, z, quaternion, scale, -scale, scale * 3, scale, u0, u1, v0, v1, edgeR, edgeG, edgeB, light);
    }

    private void renderQuad(VertexConsumer buffer, float cx, float cy, float cz, Quaternionf quaternion,
                            float minX, float minY, float maxX, float maxY,
                            float u0, float u1, float v0, float v1,
                            float r, float g, float b, int light) {
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(minX, minY, 0.0F),
                new Vector3f(maxX, minY, 0.0F),
                new Vector3f(maxX, maxY, 0.0F),
                new Vector3f(minX, maxY, 0.0F)
        };

        for (Vector3f vertex : vertices) {
            vertex.rotate(quaternion);
        }

        buffer.addVertex(cx + vertices[0].x(), cy + vertices[0].y(), cz + vertices[0].z())
                .setColor(r, g, b, this.alpha)
                .setUv(u0, v0)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(cx + vertices[1].x(), cy + vertices[1].y(), cz + vertices[1].z())
                .setColor(r, g, b, this.alpha)
                .setUv(u1, v0)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(cx + vertices[2].x(), cy + vertices[2].y(), cz + vertices[2].z())
                .setColor(r, g, b, this.alpha)
                .setUv(u1, v1)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(cx + vertices[3].x(), cy + vertices[3].y(), cz + vertices[3].z())
                .setColor(r, g, b, this.alpha)
                .setUv(u0, v1)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }
}