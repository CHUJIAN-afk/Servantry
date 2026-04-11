package first.servantry.api;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record PathNode(String feature, Vec3 pos, float yaw, float pitch, float roll) {

    public static final PathNode Empty = new PathNode("", new Vec3(0, 0, 0), 0, 0, 0);

    public PathNode(Vec3 pos, float yaw, float pitch, float roll) {
        this("", pos, yaw, pitch, roll);
    }

    public PathNode withFeature(String feature) {
        return new PathNode(feature, pos(), yaw(), pitch(), roll());
    }

    public PathNode lerp(PathNode to, float partialTick) {
        return new PathNode(
                to.feature(),
                this.pos().lerp(to.pos(), partialTick),
                Mth.rotLerp(partialTick, this.yaw(), to.yaw()),
                Mth.rotLerp(partialTick, this.pitch(), to.pitch()),
                Mth.rotLerp(partialTick, this.roll(), to.roll())
        );
    }

}