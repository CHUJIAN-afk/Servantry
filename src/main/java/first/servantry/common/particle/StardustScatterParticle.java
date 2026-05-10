package first.servantry.common.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class StardustScatterParticle extends TextureSheetParticle {

    private final SpriteSet spriteSet;
    private final float baseScale;

    // 平滑缩放：存储上一帧的缩放值用于渲染插值
    private float prevScale;
    private float currentScale;

    public StardustScatterParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = spriteSet;

        // 接收外部传入的爆发初速度
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        // 【核心物理】：极高的空气阻力，让它猛地喷出后瞬间悬停
        this.friction = 0.75F;

        // 基础大小与寿命
        this.quadSize = 0.1F + this.random.nextFloat() * 0.05F;
        this.baseScale = this.quadSize;
        this.prevScale = this.baseScale;
        this.currentScale = this.baseScale;
        this.lifetime = 5 + this.random.nextInt(25); // 寿命较短，符合爆发感

        // 【星尘调色】：青蓝色调，带一点随机偏差增加层次感
        this.rCol = 0.2F + this.random.nextFloat() * 0.2F; // 0.2 ~ 0.4
        this.gCol = 0.8F + this.random.nextFloat() * 0.2F; // 0.8 ~ 1.0
        this.bCol = 1.0F;

        // 初始化贴图
        this.setSpriteFromAge(spriteSet);
    }

    public static StardustScatterParticle create(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        return new StardustScatterParticle(level, x, y, z, vx, vy, vz, spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteSet);
        // 保存上一帧的缩放值
        this.prevScale = this.currentScale;
        // 计算当前帧的目标缩放值
        float lifeProgress = (float) this.age / (float) this.lifetime;
        this.currentScale = this.baseScale * (1.0F - (float) Math.pow(lifeProgress, 2.0));
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera renderInfo, float partialTick) {
        // 在渲染时进行平滑插值
        this.quadSize = Mth.lerp(partialTick, this.prevScale, this.currentScale);
        super.render(buffer, renderInfo, partialTick);
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