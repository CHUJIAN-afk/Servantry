package first.summoner.common.particle.zenithParticle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.NotNull;

public class ZenithParticleProvider implements ParticleProvider<ZenithParticleOptions> {

    private final SpriteSet sprite;

    public ZenithParticleProvider(SpriteSet sprite) {
        this.sprite = sprite;
    }

    @Override
    public Particle createParticle(@NotNull ZenithParticleOptions options, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        return new ZenithParticle(level, x, y, z, vx, vy, vz, this.sprite, options);
    }
}
