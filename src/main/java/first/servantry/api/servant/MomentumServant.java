package first.servantry.api.servant;

import first.servantry.api.PathNode;
import first.servantry.api.PlannedPath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于动量物理的仆从抽象基类。
 */
public abstract class MomentumServant extends Servant {

    // ===================== 动量状态 =====================

    private Vec3 velocity = Vec3.ZERO;
    private float drag = 0.95f;

    // ===================== 视角状态 =====================

    private float desiredYaw;
    private float desiredPitch;
    private float desiredRoll;
    private float rotationSpeed = 1f;

    public MomentumServant() {
        super();
    }

    // ===================== 生命周期 =====================

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
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

    // ===================== 物理更新 =====================

    private void tickPhysics() {
        velocity = velocity.scale(drag);
        Vec3 newPos = getPos().add(velocity);
        setPath(new PlannedPath("physics", Collections.singletonList(new PathNode(newPos, desiredYaw, desiredPitch, desiredRoll))));
    }

    private void tickOrientation() {
        float deltaYaw = Mth.wrapDegrees(desiredYaw - getYaw());
        float deltaPitch = Mth.wrapDegrees(desiredPitch - getPitch());
        float deltaRoll = Mth.wrapDegrees(desiredRoll - getRoll());

        desiredYaw = getYaw() + Mth.clamp(deltaYaw, -rotationSpeed, rotationSpeed);
        desiredPitch = getPitch() + Mth.clamp(deltaPitch, -rotationSpeed, rotationSpeed);
        desiredRoll = getRoll() + Mth.clamp(deltaRoll, -rotationSpeed, rotationSpeed);
    }

    // ===================== 视角控制 =====================

    /** 看向指定位置 */
    public void lookAtPos(Vec3 targetPos) {
        lookAtDirection(targetPos.subtract(getPos()).normalize());
    }

    /** 看向指定方向 */
    public void lookAtDirection(Vec3 direction) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float targetPitch = (float) Math.toDegrees(Math.asin(-direction.y));
        setDesiredRotation(targetYaw, targetPitch, getRoll());
    }

    /** 设置期望朝向 */
    public void setDesiredRotation(float yaw, float pitch, float roll) {
        this.desiredYaw = yaw;
        this.desiredPitch = pitch;
        this.desiredRoll = roll;
    }

    // ===================== 动量控制 =====================

    /** 施加力（累加到速度） */
    public void applyForce(Vec3 force) {
        velocity = velocity.add(force);
    }

    /** 设置速度 */
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    /** 瞬移到目标位置 */
    public void teleportTo(Vec3 targetPos) {
        velocity = Vec3.ZERO;
        Vec3 start = getPos();
        float yaw = getYaw(), pitch = getPitch(), roll = getRoll();
        List<PathNode> path = new ArrayList<>();
        int tick = 4;
        for (int i = 1; i <= tick; i++) {
            float t = (float) i / tick;
            Vec3 pos = start.lerp(targetPos, t);
            path.add(new PathNode(pos, yaw, pitch, roll));
        }
        setPath(path);
    }

    // ===================== 访问器 =====================

    public Vec3 getVelocity() { return velocity; }
    public void setDrag(float drag) { this.drag = Mth.clamp(drag, 0.0f, 1.0f); }
    public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }
    public float getDesiredYaw() { return desiredYaw; }
    public float getDesiredPitch() { return desiredPitch; }
    public float getDesiredRoll() { return desiredRoll; }
}
