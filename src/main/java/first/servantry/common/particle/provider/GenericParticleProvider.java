package first.servantry.common.particle.provider;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class GenericParticleProvider implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet sprite;
    private final ParticleFactory factory;

    public GenericParticleProvider(SpriteSet sprite, ParticleFactory factory) {
        this.sprite = sprite;
        this.factory = factory;
    }

    /**
     * 返回与{@code registerSpriteSet}兼容的注册函数。
     */
    public static ParticleEngine.SpriteParticleRegistration<SimpleParticleType> registration(ParticleFactory factory) {
        return sprite -> new GenericParticleProvider(sprite, factory);
    }

    @Override
    public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        return factory.create(level, x, y, z, vx, vy, vz, sprite);
    }

    @FunctionalInterface
    public interface ParticleFactory {
        Particle create(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprite);
    }

}
