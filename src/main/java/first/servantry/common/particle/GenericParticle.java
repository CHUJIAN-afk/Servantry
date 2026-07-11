package first.servantry.common.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
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

        // 颜色插值 → ARGB int（供 10 参数 fast path 使用）
        float progress = (this.age + partialTick) / (float) this.lifetime;
        int centerColor = packColor(Mth.lerp(progress, this.startR, this.endR), Mth.lerp(progress, this.startG, this.endG), Mth.lerp(progress, this.startB, this.endB), this.alpha);
        int edgeColor = packColor(Mth.lerp(progress, this.startEdgeR, this.endEdgeR), Mth.lerp(progress, this.startEdgeG, this.endEdgeG), Mth.lerp(progress, this.startEdgeB, this.endEdgeB), this.alpha);

        // 纹理坐标
        float u0 = this.sprite.getU0();
        float u1 = this.sprite.getU1();
        float v0 = this.sprite.getV0();
        float v1 = this.sprite.getV1();

        int light = getLightColor(partialTick);
        int overlay = OverlayTexture.NO_OVERLAY;

        // 每个色块大小相同（scale x scale），形成十字形
        // 中心色块
        renderQuad(buffer, x, y, z, quaternion, -scale, -scale, scale, scale, u0, u1, v0, v1, centerColor, light, overlay);

        // 四个边缘色块，每个偏移 2*scale
        // 上
        renderQuad(buffer, x, y, z, quaternion, -scale, scale, scale, scale * 3, u0, u1, v0, v1, edgeColor, light, overlay);
        // 下
        renderQuad(buffer, x, y, z, quaternion, -scale, -scale * 3, scale, -scale, u0, u1, v0, v1, edgeColor, light, overlay);
        // 左
        renderQuad(buffer, x, y, z, quaternion, -scale * 3, -scale, -scale, scale, u0, u1, v0, v1, edgeColor, light, overlay);
        // 右
        renderQuad(buffer, x, y, z, quaternion, scale, -scale, scale * 3, scale, u0, u1, v0, v1, edgeColor, light, overlay);
    }

    /** 将 float 颜色分量打包为 ARGB int */
    private static int packColor(float r, float g, float b, float a) {
        return ((int) (Mth.clamp(a, 0, 1) * 255) << 24) | ((int) (Mth.clamp(r, 0, 1) * 255) << 16) | ((int) (Mth.clamp(g, 0, 1) * 255) << 8) | (int) (Mth.clamp(b, 0, 1) * 255);
    }

    /**
     * 渲染单个 quad：保留 quaternion 旋转保证正确朝向，用 10 参数 fast path 写顶点。
     * <p>
     * 自定义 {@link #GENERIC_PARTICLE_TYPE} 使用 NEW_ENTITY 格式，触发 BufferBuilder fastFormat 走 fast path，
     * 单次 addVertex 完成全部元素写入，跳过逐元素 beginElement 校验。
     * </p>
     */
    private void renderQuad(VertexConsumer buffer, float cx, float cy, float cz, Quaternionf quaternion, float minX, float minY, float maxX, float maxY, float u0, float u1, float v0, float v1, int color, int light, int overlay) {
        // 复用静态 Vector3f 避免 per-quad 分配，旋转后直接写顶点
        Vector3f v = TMP_VEC;

        v.set(minX, minY, 0.0F).rotate(quaternion);
        buffer.addVertex(cx + v.x, cy + v.y, cz + v.z, color, u0, v0, overlay, light, 0.0F, 0.0F, 1.0F);
        v.set(maxX, minY, 0.0F).rotate(quaternion);
        buffer.addVertex(cx + v.x, cy + v.y, cz + v.z, color, u1, v0, overlay, light, 0.0F, 0.0F, 1.0F);
        v.set(maxX, maxY, 0.0F).rotate(quaternion);
        buffer.addVertex(cx + v.x, cy + v.y, cz + v.z, color, u1, v1, overlay, light, 0.0F, 0.0F, 1.0F);
        v.set(minX, maxY, 0.0F).rotate(quaternion);
        buffer.addVertex(cx + v.x, cy + v.y, cz + v.z, color, u0, v1, overlay, light, 0.0F, 0.0F, 1.0F);
    }

    /** per-quad 旋转复用的临时 Vector3f（单线程渲染，安全复用） */
    private static final Vector3f TMP_VEC = new Vector3f();

    /**
     * 自定义粒子渲染类型：NEW_ENTITY 格式 + entity translucent shader，使 BufferBuilder 走 fastFormat fast path。
     * <p>
     * 原版 {@link ParticleRenderType#PARTICLE_SHEET_TRANSLUCENT} 使用 PARTICLE 格式（无 overlay/normal），
     * BufferBuilder 对其 fastFormat=false，10 参数 addVertex 走慢路径。改用 NEW_ENTITY 后：
     * <ul>
     *   <li>fastFormat=true → 10 参数 addVertex 一次 beginVertex + 连续 memPut，跳过所有 beginElement</li>
     *   <li>需匹配 rendertypeEntityTranslucent shader（支持 POSITION_COLOR_TEX_OVERLAY_LIGHT_NORMAL）</li>
     *   <li>纹理仍用粒子 atlas（Sampler0）</li>
     * </ul>
     * </p>
     */
    public static final ParticleRenderType GENERIC_PARTICLE_TYPE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, @NotNull TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        }

        @Override
        public String toString() {
            return "GENERIC_PARTICLE";
        }
    };

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return GENERIC_PARTICLE_TYPE;
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }
}