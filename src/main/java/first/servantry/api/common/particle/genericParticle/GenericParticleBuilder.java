package first.servantry.api.common.particle.genericParticle;

import java.util.Random;

/**
 * 通用粒子配置构建器，支持链式调用配置颜色、寿命、旋转、阻力、大小等参数。
 */
public class GenericParticleBuilder {

    private final Random random = new Random();
    private int color = 0xFFFFFF;
    private int endColor = 0xFFFFFF;
    private int edgeColor = 0xFFFFFF;
    private int endEdgeColor = 0xFFFFFF;
    private float colorRandomR = 0.0F;
    private float colorRandomG = 0.0F;
    private float colorRandomB = 0.0F;
    private int lifetime = 0;
    private int lifetimeRandom = 0;
    private float spinSpeed = 0.0F;
    private float spinRandom = 0.0F;
    private float friction = 1F;
    private float scale = 1F;
    private float scaleOffset = 0.0F;

    private GenericParticleBuilder() {

    }

    public static GenericParticleBuilder create() {
        return new GenericParticleBuilder();
    }

    /**
     * 设置粒子颜色（中心色块）
     */
    public GenericParticleBuilder color(int rgb) {
        this.color = rgb;
        this.endColor = rgb;
        return this;
    }

    /** 设置粒子颜色（中心色块，RGB分量） */
    public GenericParticleBuilder color(int r, int g, int b) {
        this.color = (r << 16) | (g << 8) | b;
        this.endColor = color;
        return this;
    }

    /** 设置粒子最终颜色（中心色块） */
    public GenericParticleBuilder endColor(int rgb) {
        this.endColor = rgb;
        return this;
    }

    /** 设置粒子最终颜色（中心色块，RGB分量） */
    public GenericParticleBuilder endColor(int r, int g, int b) {
        this.endColor = (r << 16) | (g << 8) | b;
        return this;
    }

    /**
     * 设置边缘颜色（四个边缘色块同色）
     */
    public GenericParticleBuilder edgeColor(int rgb) {
        this.edgeColor = rgb;
        this.endEdgeColor = rgb;
        return this;
    }

    /**
     * 设置边缘颜色（四个边缘色块同色，RGB分量）
     */
    public GenericParticleBuilder edgeColor(int r, int g, int b) {
        this.edgeColor = (r << 16) | (g << 8) | b;
        this.endEdgeColor = edgeColor;
        return this;
    }

    /**
     * 设置边缘最终颜色
     */
    public GenericParticleBuilder endEdgeColor(int rgb) {
        this.endEdgeColor = rgb;
        return this;
    }

    /**
     * 设置边缘最终颜色（RGB分量）
     */
    public GenericParticleBuilder endEdgeColor(int r, int g, int b) {
        this.endEdgeColor = (r << 16) | (g << 8) | b;
        return this;
    }

    /** 设置粒子寿命 */
    public GenericParticleBuilder lifetime(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    /** 设置寿命随机偏差范围 */
    public GenericParticleBuilder lifetimeRandom(int range) {
        this.lifetimeRandom = range;
        return this;
    }

    /** 设置旋转速度（弧度/tick） */
    public GenericParticleBuilder spin(float spinSpeed) {
        this.spinSpeed = spinSpeed;
        return this;
    }

    /** 设置旋转随机范围 */
    public GenericParticleBuilder spinRandom(float range) {
        this.spinRandom = range;
        return this;
    }

    /** 设置粒子阻力 */
    public GenericParticleBuilder friction(float friction) {
        this.friction = friction;
        return this;
    }

    /**
     * 设置颜色随机偏差（RGB分量，范围0.0-1.0）
     */
    public GenericParticleBuilder colorRandom(float random) {
        this.colorRandomR = random;
        this.colorRandomG = random;
        this.colorRandomB = random;
        return this;
    }

    /** 设置颜色随机偏差（RGB分量，范围0.0-1.0） */
    public GenericParticleBuilder colorRandom(float r, float g, float b) {
        this.colorRandomR = r;
        this.colorRandomG = g;
        this.colorRandomB = b;
        return this;
    }

    /**
     * 设置粒子大小
     */
    public GenericParticleBuilder scale(float scale) {
        this.scale = scale;
        return this;
    }

    /**
     * 设置粒子大小随机偏差
     */
    public GenericParticleBuilder scaleRandom(float range) {
        this.scaleOffset = range;
        return this;
    }

    /**
     * 构建粒子选项
     */
    public GenericParticleOptions build() {
        int finalColor = applyColorRandom(color);
        int finalEdgeColor = applyColorRandom(edgeColor);
        int finalLifetime = lifetime + (lifetimeRandom > 0 ? random.nextInt(lifetimeRandom) : 0);
        float finalSpin = spinSpeed + (spinRandom > 0 ? random.nextFloat() * spinRandom * 2 - spinRandom : 0);
        float finalScale = scale + (scaleOffset > 0 ? random.nextFloat() * scaleOffset : 0);
        return new GenericParticleOptions(finalColor, endColor, finalEdgeColor, endEdgeColor, finalLifetime, finalSpin, friction, finalScale, scaleOffset);
    }

    private int applyColorRandom(int baseColor) {
        float r = ((baseColor >> 16) & 0xFF) / 255.0F + (colorRandomR > 0 ? random.nextFloat() * colorRandomR : 0);
        float g = ((baseColor >> 8) & 0xFF) / 255.0F + (colorRandomG > 0 ? random.nextFloat() * colorRandomG : 0);
        float b = (baseColor & 0xFF) / 255.0F + (colorRandomB > 0 ? random.nextFloat() * colorRandomB : 0);
        return ((int) (Math.min(r, 1.0F) * 255) << 16) | ((int) (Math.min(g, 1.0F) * 255) << 8) | (int) (Math.min(b, 1.0F) * 255);
    }
}
