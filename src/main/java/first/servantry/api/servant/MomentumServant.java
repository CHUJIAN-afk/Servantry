package first.servantry.api.servant;

import first.servantry.api.PathNode;
import first.servantry.api.PlannedPath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * 基于动量物理的仆从抽象基类，提供视角和动量调度器简化子类控制。
 * <p>
 * 子类只需通过调度器方法控制仆从行为，无需关心底层物理实现：
 * <ul>
 *   <li>{@link #lookAt(Vec3)} / {@link #lookAt(Vec3)} - 控制朝向</li>
 *   <li>{@link #moveToward(Vec3, double)} / {@link #applyImpulse(Vec3)} - 控制移动</li>
 *   <li>{@link #teleportTo(Vec3)} - 瞬移</li>
 * </ul>
 * </p>
 *
 * <h3>架构设计</h3>
 * <ul>
 *   <li><b>视角调度器</b>：管理朝向的平滑过渡，支持朝向目标、朝向方向等</li>
 *   <li><b>动量调度器</b>：管理速度、阻力、力的施加，支持惯性运动</li>
 * </ul>
 *
 * <h3>子类示例</h3>
 * <pre>{@code
 * public class MyServant extends MomentumServant {
 *     @Override
 *     public void tickBehavior() {
 *         LivingEntity target = getTarget();
 *         if (target != null) {
 *             // 朝向目标
 *             lookAt(target.getEyePosition());
 *             // 向目标移动
 *             moveToward(target.position(), 0.1);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see Servant
 */
public abstract class MomentumServant extends Servant {

    // ===================== 动量调度器 =====================

    /** 当前速度向量 */
    private Vec3 velocity = Vec3.ZERO;

    /** 阻力系数 [0, 1]，每tick速度衰减比例 */
    private float drag = 0.95f;

    // ===================== 视角调度器 =====================

    /** 期望偏航角（度） */
    private float desiredYaw;

    /** 期望俯仰角（度） */
    private float desiredPitch;

    /** 期望滚转角（度） */
    private float desiredRoll;

    /** 转向速度（度/tick），控制朝向平滑过渡 */
    private float rotationSpeed = 1f;

    // ===================== 构造方法 =====================

    /**
     * 构造动量仆从，初始化默认状态。
     */
    public MomentumServant() {
        super();
    }

    // ===================== Servant 实现 =====================

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            // 应用物理更新
            if (!isExecutingPath()) {
                tickPhysics();
            }
            tickOrientation();
        }
        super.tick();
    }

    @Override
    public boolean isExecutingPath() {
        return super.isExecutingPath() && currentPlannedPath.getIdentifier() != "physics";
    }

    // ===================== 物理更新（私有实现） =====================

    /**
     * 动量物理更新：应用阻力，更新位置。
     */
    private void tickPhysics() {
        // 应用阻力衰减
        velocity = velocity.scale(drag);

        // 根据速度更新位置
        Vec3 newPos = getPos().add(velocity);

        setPath(new PlannedPath("physics", Collections.singletonList(new PathNode(newPos, desiredYaw, desiredPitch, desiredRoll))));
    }

    /**
     * 视角更新：平滑过渡到期望朝向。
     */
    protected void tickOrientation() {
        float deltaYaw = Mth.wrapDegrees(desiredYaw - getYaw());
        float deltaPitch = Mth.wrapDegrees(desiredPitch - getPitch());
        float deltaRoll = Mth.wrapDegrees(desiredRoll - getRoll());

        desiredYaw = getYaw() + Mth.clamp(deltaYaw, -rotationSpeed, rotationSpeed);
        desiredPitch = getPitch() + Mth.clamp(deltaPitch, -rotationSpeed, rotationSpeed);
        desiredRoll = getRoll() + Mth.clamp(deltaRoll, -rotationSpeed, rotationSpeed);
    }

    // ===================== 视角调度器方法 =====================

    public void lookAtPos(Vec3 targetPos) {
        lookAtDirection(targetPos.subtract(getPos()).normalize());
    }

    public void lookAtDirection(Vec3 direction) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float targetPitch = (float) Math.toDegrees(Math.asin(-direction.y));
        setDesiredRotation(targetYaw, targetPitch, getRoll());
    }

    /**
     * 设置期望朝向角度。
     * <p>
     * 朝向将在后续tick中平滑过渡到目标值。
     * </p>
     *
     * @param yaw   期望偏航角（度）
     * @param pitch 期望俯仰角（度）
     * @param roll  期望滚转角（度）
     */
    public void setDesiredRotation(float yaw, float pitch, float roll) {
        this.desiredYaw = yaw;
        this.desiredPitch = pitch;
        this.desiredRoll = roll;
    }

    // ===================== 动量调度器方法 =====================

    /**
     * 施加力（累加到速度）。
     * <p>
     * 力会在后续tick中持续影响速度（受阻力衰减）。
     * 适合持续加速场景。
     * </p>
     *
     * @param force 力向量
     */
    public void applyForce(Vec3 force) {
        velocity = velocity.add(force);
    }

    /**
     * 设置速度向量。
     *
     * @param velocity 新速度
     */
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    /**
     * 瞬移到目标位置。
     * <p>
     * 清零速度并立即移动到目标位置。
     * </p>
     *
     * @param targetPos 目标位置
     */
    public void teleportTo(Vec3 targetPos) {
        velocity = Vec3.ZERO;
        setPath(Collections.singletonList(new PathNode(targetPos, desiredYaw, desiredPitch, desiredRoll)));
    }

    // ===================== 访问器 =====================

    /** @return 当前速度 */
    public Vec3 getVelocity() { return velocity; }

    /**
     * 设置阻力系数。
     *
     * @param drag 阻力系数 [0, 1]
     */
    public void setDrag(float drag) { this.drag = Mth.clamp(drag, 0.0f, 1.0f); }

    /**
     * 设置转向速度。
     *
     * @param rotationSpeed 转向速度（度/tick）
     */
    public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }

    /** @return 期望偏航角 */
    public float getDesiredYaw() { return desiredYaw; }

    /** @return 期望俯仰角 */
    public float getDesiredPitch() { return desiredPitch; }

    /** @return 期望滚转角 */
    public float getDesiredRoll() { return desiredRoll; }
}
