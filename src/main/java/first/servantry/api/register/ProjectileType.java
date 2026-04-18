package first.servantry.api.register;

import first.servantry.api.projectile.AdvancedProjectile;
import first.servantry.api.servant.PathNode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record ProjectileType<T extends AdvancedProjectile>(ProjectileFactory<T> factory) {
    public interface ProjectileFactory<T extends AdvancedProjectile> {
        T create(Level level, Player owner, PathNode startNode);
    }
}