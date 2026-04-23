package first.servantry.api.servant;

import first.servantry.api.PathNode;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 基于动量物理的仆从抽象基类，继承自 {@link Servant}。
 * <p>
 * 特性：
 * <ul>
 *   <li>维护速度向量，支持惯性运动</li>
 *   <li>视角平滑转向，可独立于运动方向</li>
 *   <li>提供瞬移、冲量等便捷方法</li>
 * </ul>
 * </p>
 */
public abstract class MomentumServant extends Servant {

    // ===================== 物理状态 =====================

    /** 当前速度向量 */
    protected Vec3 velocity = Vec3.ZERO;

    /** 阻力系数（每tick速度衰减） */
    protected float drag = 0.95f;

    /** 最大速度 */
    protected float maxSpeed = 0.5f;

    // ===================== 朝向状态 =====================

    /** 期望偏航角 */
    protected float desiredYaw;

    /** 期望俯仰角 */
    protected float desiredPitch;

    /** 期望滚转角 */
    protected float desiredRoll;

    /** 转向速度（度/tick） */
    protected float rotationSpeed = 10.0f;

    // ===================== 构造器 =====================

    public MomentumServant() {
        super();
    }

    // ===================== Tick更新 =====================

    @Override
    public void tick() {
        super.tick();

        // 应用阻力
        velocity = velocity.scale(drag);
        double speed = velocity.length();
        if (speed > maxSpeed) {
            velocity = velocity.scale(maxSpeed / speed);
        }

        // 更新朝向
        updateOrientation();

        // 应用速度到位置
        if (velocity.lengthSqr() > 1e-8) {
            Vec3 newPos = getPos().add(velocity);
            currentPathNode = new PathNode(newPos, desiredYaw, desiredPitch, desiredRoll);
        } else {
            currentPathNode = new PathNode(getPos(), desiredYaw, desiredPitch, desiredRoll);
        }
    }

    /**
     * 平滑更新朝向角度。
     */
    protected void updateOrientation() {
        float deltaYaw = Mth.wrapDegrees(desiredYaw - getYaw());
        float deltaPitch = Mth.wrapDegrees(desiredPitch - getPitch());
        float deltaRoll = Mth.wrapDegrees(desiredRoll - getRoll());

        desiredYaw = getYaw() + Mth.clamp(deltaYaw, -rotationSpeed, rotationSpeed);
        desiredPitch = getPitch() + Mth.clamp(deltaPitch, -rotationSpeed, rotationSpeed);
        desiredRoll = getRoll() + Mth.clamp(deltaRoll, -rotationSpeed, rotationSpeed);
    }

    // ===================== 朝向控制 =====================

    /**
     * 设置期望朝向角度。
     */
    public void setDesiredRotation(float yaw, float pitch, float roll) {
        this.desiredYaw = yaw;
        this.desiredPitch = pitch;
        this.desiredRoll = roll;
    }

    /**
     * 立即设置朝向（无过渡）。
     */
    public void setRotation(float yaw, float pitch, float roll) {
        this.desiredYaw = yaw;
        this.desiredPitch = pitch;
        this.desiredRoll = roll;
        currentPathNode = new PathNode(getPos(), yaw, pitch, roll);
    }

    // ===================== 物理控制 =====================

    /**
     * 施加冲量。
     */
    public void applyImpulse(Vec3 impulse) {
        velocity = velocity.add(impulse);
        double speed = velocity.length();
        if (speed > maxSpeed) {
            velocity = velocity.scale(maxSpeed / speed);
        }
    }

    /**
     * 施加力（累加到速度）。
     */
    public void applyForce(Vec3 force) {
        velocity = velocity.add(force);
    }

    /**
     * 瞬移到目标位置。
     */
    public void teleportTo(Vec3 targetPos) {
        velocity = Vec3.ZERO;
        currentPathNode = new PathNode(targetPos, currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
    }

    // ===================== 访问器 =====================

    public Vec3 getVelocity() { return velocity; }
    public void setVelocity(Vec3 velocity) { this.velocity = velocity; }

    public float getDrag() { return drag; }
    public void setDrag(float drag) { this.drag = Mth.clamp(drag, 0.0f, 1.0f); }

    public float getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(float maxSpeed) { this.maxSpeed = maxSpeed; }

    public float getRotationSpeed() { return rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }

    public float getDesiredYaw() { return desiredYaw; }
    public float getDesiredPitch() { return desiredPitch; }
    public float getDesiredRoll() { return desiredRoll; }
}
