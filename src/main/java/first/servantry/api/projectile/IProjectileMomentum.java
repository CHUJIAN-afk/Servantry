package first.servantry.api.projectile;

import first.servantry.api.servant.PathNode;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

public interface IProjectileMomentum {

    Vec3 getVelocity();
    void setVelocity(Vec3 velocity);

    default void applyForce(Vec3 force) {
        setVelocity(getVelocity().add(force));
    }

    /**
     * 核心逻辑：推算下一帧的物理坐标与欧拉角，并送入未来队列
     */
    default void processMomentum(AdvancedProjectile projectile, float friction, float maxSpeed) {
        Vec3 vel = getVelocity();
        if (vel.lengthSqr() > maxSpeed * maxSpeed) {
            vel = vel.normalize().scale(maxSpeed);
        }

        Vec3 nextPos = projectile.getPos().add(vel);

        float yaw = projectile.getYaw();
        float pitch = projectile.getPitch();
        
        // 自动根据速度向量推导朝向
        if (vel.lengthSqr() > 1e-5) {
            Vec3 dir = vel.normalize();
            yaw = (float) (Math.atan2(-dir.x, dir.z) * (180D / Math.PI));
            double horiz = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
            pitch = (float) (Math.atan2(-dir.y, horiz) * (180D / Math.PI));
        }

        projectile.setPath(Collections.singletonList(
                new PathNode("", nextPos, yaw, pitch, projectile.getRoll())
        ));

        setVelocity(vel.scale(friction));
    }
}