package first.servantry.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    /**
     * 使用四元数球面线性插值(slerp)进行旋转插值。
     * <p>
     * 相比欧拉角线性插值，四元数slerp能正确处理roll接近±180°的情况，
     * 避免最短路径插值导致的振荡。
     * </p>
     */
    public PathNode slerp(PathNode to, float partialTick) {
        Quaternionf q1 = new Quaternionf();
        q1.rotationYXZ(
                (float) Math.toRadians(-yaw),
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(roll)
        );
        Quaternionf q2 = new Quaternionf();
        q2.rotationYXZ(
                (float) Math.toRadians(-to.yaw),
                (float) Math.toRadians(to.pitch),
                (float) Math.toRadians(to.roll)
        );
        Quaternionf result = q1.slerp(q2, partialTick, new Quaternionf());
        Vec3 pos1 = this.pos().lerp(to.pos(), partialTick);
        Vector3f ypr = new Vector3f();
        result.getEulerAnglesYXZ(ypr);
        return new PathNode(
                pos1,
                (float) Math.toDegrees(-ypr.x()),
                (float) Math.toDegrees(ypr.y()),
                (float) Math.toDegrees(ypr.z())
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PathNode(Vec3 pos1, float yaw1, float pitch1, float roll1)) {
            return pos1.equals(this.pos()) && yaw1 == this.yaw() && pitch1 == this.pitch() && roll1 == this.roll();
        }
        return false;
    }
}