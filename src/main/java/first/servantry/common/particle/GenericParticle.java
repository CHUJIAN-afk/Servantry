package first.servantry.common.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/**
 * 通用粒子，使用 minecraft:glow 贴图。
 * <p>
 * 特性：
 * <ul>
 *   <li>可配置阻力</li>
 *   <li>平滑插值缩小效果</li>
 *   <li>平滑插值旋转</li>
 *   <li>根据速度自动旋转贴图</li>
 *   <li>服务端可配置颜色、最终颜色、寿命、旋转速度、阻力</li>
 * </ul>
 * </p>
 */
public class GenericParticle extends TextureSheetParticle {

    private final SpriteSet spriteSet;
    private final float baseScale;
    private float spinSpeed;

    // 颜色插值
    private float startR, startG, startB;
    private float endR, endG, endB;

    // 平滑插值
    private float prevScale;
    private float currentScale;

    public GenericParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = spriteSet;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.friction = 0.75F;
        this.gravity = 0.0F;

        this.quadSize = 0.1F + this.random.nextFloat() * 0.05F;
        this.baseScale = this.quadSize;
        this.prevScale = this.baseScale;
        this.currentScale = this.baseScale;

        this.lifetime = 10 + this.random.nextInt(15);

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.startR = this.startG = this.startB = 1.0F;
        this.endR = this.endG = this.endB = 1.0F;
        this.alpha = 0.9F;

        // 根据速度计算旋转速度，初始旋转随机
        double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
        this.spinSpeed = (float) speed * 0.5F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;

        //this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    public GenericParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet, GenericParticleOptions options) {
        this(level, x, y, z, vx, vy, vz, spriteSet);

        // 应用服务端配置
        setColor(options.color());
        this.lifetime = options.lifetime();
        this.spinSpeed = options.spinSpeed();
        this.friction = options.friction();

        // 最终颜色
        this.endR = ((options.endColor() >> 16) & 0xFF) / 255.0F;
        this.endG = ((options.endColor() >> 8) & 0xFF) / 255.0F;
        this.endB = (options.endColor() & 0xFF) / 255.0F;
    }

    public static GenericParticle create(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        return new GenericParticle(level, x, y, z, vx, vy, vz, spriteSet);
    }

    public static GenericParticle createWithOptions(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet, GenericParticleOptions options) {
        return new GenericParticle(level, x, y, z, vx, vy, vz, spriteSet, options);
    }

    /**
     * 设置粒子颜色（整数RGB）。
     */
    public GenericParticle setColor(int rgb) {
        this.rCol = ((rgb >> 16) & 0xFF) / 255.0F;
        this.gCol = ((rgb >> 8) & 0xFF) / 255.0F;
        this.bCol = (rgb & 0xFF) / 255.0F;
        // 设置起始颜色用于插值
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteSet);

        // 保存上一帧的值用于插值
        this.prevScale = this.currentScale;
        this.oRoll = this.roll;

        // 插值缩小
        float progress = (float) this.age / (float) this.lifetime;
        this.currentScale = this.baseScale * (1.0F - progress * progress);

        // 更新旋转
        this.roll += this.spinSpeed * (1 - progress);
    }

    @Override
    public void render(@NotNull com.mojang.blaze3d.vertex.VertexConsumer buffer, @NotNull net.minecraft.client.Camera camera, float partialTick) {
        // 平滑插值缩放
        this.quadSize = Mth.lerp(partialTick, this.prevScale, this.currentScale);

        // 颜色插值
        float progress = (this.age + partialTick) / (float) this.lifetime;
        this.rCol = Mth.lerp(progress, this.startR, this.endR);
        this.gCol = Mth.lerp(progress, this.startG, this.endG);
        this.bCol = Mth.lerp(progress, this.startB, this.endB);

        super.render(buffer, camera, partialTick);
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