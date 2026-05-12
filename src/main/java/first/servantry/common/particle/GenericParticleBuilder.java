package first.servantry.common.particle;

import java.util.Random;

/**
 * 通用粒子配置构建器，支持链式调用配置颜色、寿命、旋转、阻力等参数。
 */
public class GenericParticleBuilder {

    private final Random random = new Random();
    private int color = 0xFFFFFF;
    private int endColor = 0xFFFFFF;
    private int lifetime = 15;
    private int lifetimeRandom = 0;
    private float spinSpeed = 0.0F;
    private float spinRandom = 0.0F;
    private float friction = 0.75F;
    private float colorRandomR = 0.0F;
    private float colorRandomG = 0.0F;
    private float colorRandomB = 0.0F;

    public static GenericParticleBuilder create() {
        return new GenericParticleBuilder();
    }

    /**
     * 设置粒子颜色（RGB整数）。
     */
    public GenericParticleBuilder color(int rgb) {
        this.color = rgb;
        this.endColor = color;
        return this;
    }

    /**
     * 设置粒子颜色（RGB分量，范围0-255）。
     */
    public GenericParticleBuilder color(int r, int g, int b) {
        this.color = (r << 16) | (g << 8) | b;
        this.endColor = color;
        return this;
    }

    /**
     * 设置粒子最终颜色（RGB整数），颜色会从初始颜色插值到最终颜色。
     */
    public GenericParticleBuilder endColor(int rgb) {
        this.endColor = rgb;
        return this;
    }

    /**
     * 设置粒子最终颜色（RGB分量，范围0-255）。
     */
    public GenericParticleBuilder endColor(int r, int g, int b) {
        this.endColor = (r << 16) | (g << 8) | b;
        return this;
    }

    /**
     * 设置粒子寿命。
     */
    public GenericParticleBuilder lifetime(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    /**
     * 设置寿命随机偏差范围。
     */
    public GenericParticleBuilder lifetimeRandom(int range) {
        this.lifetimeRandom = range;
        return this;
    }

    /**
     * 设置旋转速度（弧度/tick）。
     */
    public GenericParticleBuilder spin(float spinSpeed) {
        this.spinSpeed = spinSpeed;
        return this;
    }

    /**
     * 设置旋转随机范围（在基础旋转速度上添加随机值）。
     */
    public GenericParticleBuilder spinRandom(float range) {
        this.spinRandom = range;
        return this;
    }

    /**
     * 设置粒子阻力。
     */
    public GenericParticleBuilder friction(float friction) {
        this.friction = friction;
        return this;
    }

    /**
     * 设置颜色随机偏差（RGB分量，范围0.0-1.0），每个粒子会在基础颜色上添加随机偏差。
     */
    public GenericParticleBuilder colorRandom(float r, float g, float b) {
        this.colorRandomR = r;
        this.colorRandomG = g;
        this.colorRandomB = b;
        return this;
    }

    /**
     * 构建粒子选项，应用随机偏差。
     */
    public GenericParticleOptions build() {
        int finalColor = applyColorRandom();
        int finalLifetime = lifetime + (lifetimeRandom > 0 ? random.nextInt(lifetime, lifetimeRandom) : 0);
        float finalSpin = spinSpeed + (spinRandom > 0 ? random.nextFloat() * spinRandom * 2 - spinRandom : 0);
        return new GenericParticleOptions(finalColor, endColor, finalLifetime, finalSpin, friction);
    }

    private int applyColorRandom() {
        float r = ((color >> 16) & 0xFF) / 255.0F + (colorRandomR > 0 ? random.nextFloat() * colorRandomR : 0);
        float g = ((color >> 8) & 0xFF) / 255.0F + (colorRandomG > 0 ? random.nextFloat() * colorRandomG : 0);
        float b = (color & 0xFF) / 255.0F + (colorRandomB > 0 ? random.nextFloat() * colorRandomB : 0);
        return ((int) (Math.min(r, 1.0F) * 255) << 16) | ((int) (Math.min(g, 1.0F) * 255) << 8) | (int) (Math.min(b, 1.0F) * 255);
    }
}
