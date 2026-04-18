package first.servantry.api.servant;

import net.minecraft.world.phys.Vec3;
import java.util.Collections;

public interface IMomentumControlled {

    Vec3 getVelocity();

    void setVelocity(Vec3 velocity);

    // 施加加速度（推力、后坐力等）
    default void applyForce(Vec3 force) {
        setVelocity(getVelocity().add(force));
    }

    // 执行物理运动，计算摩擦力与极速，并生成下一刻的路径节点
    default void tickMomentum(Servant servant, float friction, float maxSpeed) {
        Vec3 vel = getVelocity();
        if (vel.lengthSqr() > maxSpeed * maxSpeed) {
            vel = vel.normalize().scale(maxSpeed);
        }

        Vec3 nextPos = servant.getPos().add(vel);

        // 自动利用 Servant 的路径系统执行位移
        servant.setPath(Collections.singletonList(new PathNode("", nextPos, servant.getYaw(), servant.getPitch(), servant.getRoll())));
        // 摩擦力衰减
        setVelocity(vel.scale(friction));
    }

}