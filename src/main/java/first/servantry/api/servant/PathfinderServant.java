package first.servantry.api.servant;

import first.servantry.api.entity.IBlockCollision;
import net.minecraft.world.phys.Vec3;

public abstract class PathfinderServant extends MomentumServant implements IBlockCollision<PathfinderServant> {


    @Override
    public void onBlockCollision(CollisionContext context) {
        if (context.bottomSupported()) {
            Vec3 velocity = getVelocity();
            setVelocity(new Vec3(velocity.x(), 0, velocity.z()));
        }
    }
}
