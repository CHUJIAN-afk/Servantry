package first.servantry.common.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class StardustScatterParticle extends TextureSheetParticle {

    private final SpriteSet spriteSet;
    private final float baseScale;

    protected StardustScatterParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
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
        this.lifetime = 5 + this.random.nextInt(25); // 寿命较短，符合爆发感

        // 【星尘调色】：青蓝色调，带一点随机偏差增加层次感
        this.rCol = 0.2F + this.random.nextFloat() * 0.2F; // 0.2 ~ 0.4
        this.gCol = 0.8F + this.random.nextFloat() * 0.2F; // 0.8 ~ 1.0
        this.bCol = 1.0F;

        // 初始化贴图
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteSet);
        // 【视觉动画】：随时间流逝逐渐平滑缩小，而不是突然消失
        float lifeProgress = (float) this.age / (float) this.lifetime;
        this.quadSize = this.baseScale * (1.0F - (float) Math.pow(lifeProgress, 2.0));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    // 粒子提供者，用于绑定贴图和实例化
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new StardustScatterParticle(level, x, y, z, vx, vy, vz, this.sprite);
        }

    }

}