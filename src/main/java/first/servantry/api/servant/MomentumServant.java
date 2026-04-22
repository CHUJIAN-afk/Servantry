package first.servantry.api.servant;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * 基于动量物理的仆从抽象基类，继承自 {@link Servant}。
 * <p>
 * 与传统依靠固定路径节点移动的仆从不同，动量仆从使用加速度、速度和阻力模拟惯性运动。
 * 当没有计划路径（{@link #getCurrentPath()} 为 null）时，仆从会根据当前速度向量和加速度自由移动；
 * 当存在计划路径时，可切换回传统的路径跟随模式（由子类决定具体行为）。
 * </p>
 * <p>
 * 核心物理参数：
 * <ul>
 *   <li><b>速度向量 {@link #velocity}</b>：当前移动的速度（米/秒），每 tick 更新；</li>
 *   <li><b>阻力系数 {@link #drag}</b>：每 tick 速度的衰减比例（0~1），模拟空气/摩擦力；</li>
 *   <li><b>最大速度 {@link #maxSpeed}</b>：速度向量的长度上限，防止无限加速。</li>
 * </ul>
 * </p>
 * <p>
 * 朝向系统与运动分离，允许仆从在移动的同时独立转向。
 * 子类需实现 {@link #computeAcceleration()} 来提供加速度逻辑。
 * </p>
 */
public abstract class MomentumServant extends Servant {

    // ===================== 物理状态字段 =====================

    /** 当前速度向量（米/秒） */
    protected Vec3 velocity = Vec3.ZERO;


    /** 每 tick 速度的衰减系数，范围 [0, 1]，值越小阻力越大 */
    protected float drag = 0.95f;

    /** 最大速度标量上限（米/秒），防止速度无限增长 */
    protected float maxSpeed = 0.5f;

    /** 期望的朝向角度（偏航、俯仰、滚转），独立于运动方向 */
    protected float desiredYaw;
    protected float desiredPitch;
    protected float desiredRoll;

    /** 朝向插值速度（度/ tick），控制转向平滑程度 */
    protected float rotationSpeed = 10.0f;

    // ===================== 构造器 =====================

    public MomentumServant() {
        super();
        // 初始化期望朝向为当前实际朝向
        this.desiredYaw = getYaw();
        this.desiredPitch = getPitch();
        this.desiredRoll = getRoll();
    }


    // ===================== 核心物理更新 =====================

    /**
     * 动量仆从的 tick 更新逻辑。
     * <p>
     * 首先调用父类 tick 处理通用逻辑（AI、路径等）。
     * 若当前没有正在执行的路径，则进入自由动量模式：
     * <ol>
     *   <li>调用 {@link #computeAcceleration()} 计算当前 tick 的加速度；</li>
     *   <li>将加速度累加到速度向量上；</li>
     *   <li>应用阻力衰减速度；</li>
     *   <li>限制速度不超过最大速度；</li>
     *   <li>根据速度向量更新位置，生成新的当前节点；</li>
     *   <li>独立更新朝向（朝向平滑转向期望角度）。</li>
     * </ol>
     * 若正在执行路径，则维持父类的路径跟随行为（不修改位置）。
     * </p>
     */
    @Override
    public void tick() {
        super.tick(); // 处理 Goal 系统与历史节点
        velocity = velocity.scale(drag);
        double speed = velocity.length();
        if (speed > maxSpeed) velocity = velocity.scale(maxSpeed / speed);

        if (velocity.lengthSqr() > 1e-8) {
            Vec3 newPos = getPos().add(velocity);
            updateOrientation();
            // 通过 setPath 更新当前节点（避免直接访问私有字段）
            setPath(Collections.singletonList(new PathNode(newPos, getYaw(), getPitch(), getRoll())));
        } else {
            updateOrientation();
            setPath(Collections.singletonList(new PathNode(getPos(), getYaw(), getPitch(), getRoll())));
        }
    }

    /**
     * 更新仆从的朝向角度，使其平滑趋向期望角度。
     * <p>
     * 默认实现使用线性插值（旋转速度乘以 tick 时长）平滑过渡。
     * 子类可重写以实现更复杂的转向逻辑（如基于角速度的物理模拟）。
     * </p>
     */
    protected void updateOrientation() {
        float deltaYaw = Mth.wrapDegrees(desiredYaw - getYaw());
        float deltaPitch = Mth.wrapDegrees(desiredPitch - getPitch());
        float deltaRoll = Mth.wrapDegrees(desiredRoll - getRoll());

        float newYaw = getYaw() + Mth.clamp(deltaYaw, -rotationSpeed, rotationSpeed);
        float newPitch = getPitch() + Mth.clamp(deltaPitch, -rotationSpeed, rotationSpeed);
        float newRoll = getRoll() + Mth.clamp(deltaRoll, -rotationSpeed, rotationSpeed);

        // 直接设置期望角度，实际应用在下次 setPath 时
        this.desiredYaw = newYaw;
        this.desiredPitch = newPitch;
        this.desiredRoll = newRoll;
    }

    // ===================== 朝向控制 API =====================

    /**
     * 设置期望的朝向角度（度）。
     * <p>
     * 实际朝向将在后续 tick 中通过 {@link #updateOrientation()} 平滑逼近。
     * </p>
     *
     * @param yaw   期望偏航角
     * @param pitch 期望俯仰角
     * @param roll  期望滚转角
     */
    public void setDesiredRotation(float yaw, float pitch, float roll) {
        this.desiredYaw = yaw;
        this.desiredPitch = pitch;
        this.desiredRoll = roll;
    }

    /**
     * 直接设置当前朝向角度（瞬间转向）。
     * <p>
     * 此操作会立即更新仆从的朝向，通常用于初始化或传送后重置。
     * </p>
     *
     * @param yaw   偏航角
     * @param pitch 俯仰角
     * @param roll  滚转角
     */
    public void setRotation(float yaw, float pitch, float roll) {
        this.desiredYaw = yaw;
        this.desiredPitch = pitch;
        this.desiredRoll = roll;
        // 立即应用
        this.currentPathNode = new PathNode(getPos(), yaw, pitch, roll);
    }

    // ===================== 物理参数访问器 =====================

    /** @return 当前速度向量（只读） */
    public Vec3 getVelocity() {
        return velocity;
    }

    /** 设置速度向量（慎用，会覆盖物理模拟） */
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    /** @return 阻力系数 */
    public float getDrag() {
        return drag;
    }

    /** 设置阻力系数 */
    public void setDrag(float drag) {
        this.drag = Mth.clamp(drag, 0.0f, 1.0f);
    }

    /** @return 最大速度标量 */
    public float getMaxSpeed() {
        return maxSpeed;
    }

    /** 设置最大速度 */
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /** @return 期望偏航角 */
    public float getDesiredYaw() {
        return desiredYaw;
    }

    /** @return 期望俯仰角 */
    public float getDesiredPitch() {
        return desiredPitch;
    }

    /** @return 期望滚转角 */
    public float getDesiredRoll() {
        return desiredRoll;
    }

    /** @return 朝向旋转速度（度/tick） */
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    /** 设置朝向旋转速度 */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    // ===================== 辅助方法 =====================

    /**
     * 施加一个瞬间冲量，直接修改速度向量。
     * <p>
     * 可用于模拟击退、跳跃等突发受力。
     * </p>
     *
     * @param impulse 冲量向量（米/秒）
     */
    public void applyImpulse(Vec3 impulse) {
        this.velocity = this.velocity.add(impulse);
        // 可选：限制瞬时速度不超过最大速度
        double speed = velocity.length();
        if (speed > maxSpeed) {
            velocity = velocity.scale(maxSpeed / speed);
        }
    }

    /**
     * 施加一个持续的力，等价于在加速度上累加。
     * <p>
     * 该力将在下一次 {@link #computeAcceleration()} 时生效，通常子类会在计算加速度时考虑外力。
     * 这里提供一个便捷方法，子类可选择重写以支持外部累加。
     * </p>
     *
     * @param force 力向量（米/秒²）
     */
    public void applyForce(Vec3 force) {
        this.velocity = this.velocity.add(force);
    }

}