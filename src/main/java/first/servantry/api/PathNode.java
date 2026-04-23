package first.servantry.api;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record PathNode(Vec3 pos, float yaw, float pitch, float roll) {

    public static final PathNode Empty = new PathNode(Vec3.ZERO, 0, 0, 0);

    public PathNode lerp(PathNode to, float partialTick) {
        return new PathNode(
                this.pos().lerp(to.pos(), partialTick),
                Mth.rotLerp(partialTick, this.yaw(), to.yaw()),
                Mth.rotLerp(partialTick, this.pitch(), to.pitch()),
                Mth.rotLerp(partialTick, this.roll(), to.roll())
        );
    }

}