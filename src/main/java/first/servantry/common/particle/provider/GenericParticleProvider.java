package first.servantry.common.particle.provider;

import first.servantry.common.particle.GenericParticle;
import first.servantry.common.particle.GenericParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.NotNull;

public class GenericParticleProvider implements ParticleProvider<GenericParticleOptions> {

    private final SpriteSet sprite;

    public GenericParticleProvider(SpriteSet sprite) {
        this.sprite = sprite;
    }

    public static ParticleEngine.SpriteParticleRegistration<GenericParticleOptions> registration() {
        return GenericParticleProvider::new;
    }

    @Override
    public Particle createParticle(@NotNull GenericParticleOptions options, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        return GenericParticle.createWithOptions(level, x, y, z, vx, vy, vz, sprite, options);
    }
}