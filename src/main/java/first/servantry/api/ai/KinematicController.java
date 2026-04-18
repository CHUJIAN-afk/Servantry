package first.servantry.api.ai;

import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * 仆从动力学辅助控制器
 * 适用于不需要固定轨道，而是通过【受力/加速度】来控制的游荡型、自由飞行型仆从。
 */
public class KinematicController {
    private Vec3 velocity = Vec3.ZERO;

    // 施加加速度（推力、后坐力等）
    public void applyForce(Vec3 force) {
        this.velocity = this.velocity.add(force);
    }

    public void setVelocity(Vec3 vel) {
        this.velocity = vel;
    }

    public Vec3 getVelocity() {
        return this.velocity;
    }

    /**
     * 根据当前速度移动实体，并应用摩擦力衰减
     * @param servant  要控制的仆从
     * @param friction 摩擦力（例如 0.85f，越小减速越快）
     * @param maxSpeed 最大速度限制
     */
    public void tickMove(Servant servant, float friction, float maxSpeed) {
        if (this.velocity.lengthSqr() > maxSpeed * maxSpeed) {
            this.velocity = this.velocity.normalize().scale(maxSpeed);
        }

        Vec3 currentPos = servant.getPos();
        Vec3 nextPos = currentPos.add(this.velocity);

        // 利用 Servant 原有的单节点更新机制完成顺滑移动，欧拉角保持原样
        PathNode nextNode = new PathNode("", nextPos, servant.getYaw(), servant.getPitch(), servant.getRoll());
        servant.setPath(Collections.singletonList(nextNode));

        // 摩擦力衰减
        this.velocity = this.velocity.scale(friction);
    }

}