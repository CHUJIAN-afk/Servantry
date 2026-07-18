package first.servantry.utils;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 弹道学实用类 — 根据起终点、仰角、阻力、重力计算初速度向量，确保精准命中。
 * <p>
 * 基于 Projectile 物理模型：每tick更新 {@code velocity = velocity.scale(drag).add(0, gravity, 0); newPos = pos + velocity}
 * </p>
 *
 * <h3>位置闭式解（drag ≠ 1）</h3>
 * <pre>
 * A(t) = (1 - drag^t) / (1 - drag)
 * x(t) = vx0 * A(t)
 * y(t) = vy0 * A(t) + gravity / (1-drag) * [t - A(t)]
 * </pre>
 */
public class Ballistics {

    private static final int MAX_ITERATIONS = 50;
    private static final double EPSILON = 1e-6;

    /**
     * 根据起终点、仰角、阻力、重力，计算初速度向量，使抛物线精准命中目标。
     * <p>
     * 给定仰角θ，用二分法求解初速度大小s，使弹道在飞行时间T后恰好到达target。
     * </p>
     *
     * @param start            发射位置
     * @param target           目标位置
     * @param elevationAngleDeg 仰角（度），必须 > 0 且 < 90
     * @param drag             阻力系数 [0,1]，1=无阻力
     * @param gravity          重力加速度（负值向下，如 -0.05）
     * @return 初速度向量，若不可达则返回 null
     */
    public static Vec3 solveVelocity(Vec3 start, Vec3 target, float elevationAngleDeg, float drag, float gravity) {
        if (elevationAngleDeg <= 0 || elevationAngleDeg >= 90) {
            throw new IllegalArgumentException("Elevation angle must be between 0 and 90 degrees (exclusive)");
        }

        double dx = target.x - start.x;
        double dz = target.z - start.z;
        double D = Math.sqrt(dx * dx + dz * dz); // 水平距离
        double H = target.y - start.y;            // 垂直位移

        if (D < EPSILON) {
            // 目标在正上方/正下方，直接垂直发射
            double vy = solveVerticalSpeed(H, drag, gravity);
            return new Vec3(0, vy, 0);
        }

        // 水平方向单位向量
        Vec3 hDir = new Vec3(dx / D, 0, dz / D);
        double theta = Math.toRadians(elevationAngleDeg);
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        if (Math.abs(drag - 1.0) < EPSILON) {
            // 无阻力情况：闭式解
            return solveNoDrag(D, H, hDir, theta, cosTheta, sinTheta, gravity);
        }

        // 有阻力情况：二分法求解初速度大小 s
        // 垂直方程: H = s*sin(θ)*A + g/(1-d)*(T - A)
        // 其中 A = D/(s*cos(θ)), T = ln(1 - A*(1-d))/ln(d)
        // f(s) = s*sin(θ)*A + g/(1-d)*(T - A) - H = 0

        // 寻找二分法上下界
        double sLow = D * 0.01; // 很小的速度
        double sHigh = D * 2.0; // 足够大的速度

        // 确保上下界函数值异号
        double fLow = evaluateVerticalEquation(sLow, D, H, cosTheta, sinTheta, drag, gravity);
        double fHigh = evaluateVerticalEquation(sHigh, D, H, cosTheta, sinTheta, drag, gravity);

        // 如果上界还不够，扩大搜索范围
        int expandCount = 0;
        while (fLow * fHigh > 0 && expandCount < 20) {
            sHigh *= 2.0;
            fHigh = evaluateVerticalEquation(sHigh, D, H, cosTheta, sinTheta, drag, gravity);
            expandCount++;
        }

        if (fLow * fHigh > 0) {
            return null; // 不可达
        }

        // 二分法
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double sMid = (sLow + sHigh) / 2.0;
            double fMid = evaluateVerticalEquation(sMid, D, H, cosTheta, sinTheta, drag, gravity);

            if (Math.abs(fMid) < EPSILON) {
                // 收敛，构造速度向量
                double vh = sMid * cosTheta;
                double vv = sMid * sinTheta;
                return hDir.scale(vh).add(0, vv, 0);
            }

            if (fLow * fMid < 0) {
                sHigh = sMid;
                fHigh = fMid;
            } else {
                sLow = sMid;
                fLow = fMid;
            }
        }

        // 返回最佳近似
        double sBest = (sLow + sHigh) / 2.0;
        double vh = sBest * cosTheta;
        double vv = sBest * sinTheta;
        return hDir.scale(vh).add(0, vv, 0);
    }

    /**
     * 计算垂直方程残差：f(s) = 预测垂直位移 - 实际垂直位移
     */
    private static double evaluateVerticalEquation(double s, double D, double H, double cosTheta, double sinTheta, double drag, double gravity) {
        double vh = s * cosTheta;
        double vv = s * sinTheta;

        // A = D / vh
        double A = D / vh;

        // 检查可达性：A*(1-drag) 必须 < 1
        double oneMinusDrag = 1.0 - drag;
        if (A * oneMinusDrag >= 1.0) {
            return Double.NEGATIVE_INFINITY; // 不可达
        }

        // T = ln(1 - A*(1-drag)) / ln(drag)
        double dragPowerT = 1.0 - A * oneMinusDrag;
        if (dragPowerT <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        double T = Math.log(dragPowerT) / Math.log(drag);

        if (T < 0) {
            return Double.NEGATIVE_INFINITY;
        }

        // 预测垂直位移: y = vv*A + gravity/(1-drag) * (T - A)
        double predictedH = vv * A + gravity / oneMinusDrag * (T - A);

        return predictedH - H;
    }

    /**
     * 无阻力情况（drag=1）的闭式解。
     * <p>
     * 水平: D = vh * T → T = D / vh = D / (s*cos(θ))
     * 垂直: H = vv * T + 0.5 * gravity * T²
     * 代入: H = s*sin(θ) * D/(s*cos(θ)) + 0.5*g*(D/(s*cos(θ)))²
     * = D*tan(θ) + g*D²/(2*s²*cos²(θ))
     * 解 s: s² = g*D² / (2*cos²(θ)*(H - D*tan(θ)))
     * </p>
     */
    private static Vec3 solveNoDrag(double D, double H, Vec3 hDir, double theta, double cosTheta, double sinTheta, double gravity) {
        double tanTheta = Math.tan(theta);
        double denominator = H - D * tanTheta;

        if (denominator * gravity >= 0) {
            // gravity向下(负)且denominator为正，或gravity向上且denominator为负 → 无解
            // 需要gravity和denominator异号
            if (Math.abs(denominator) < EPSILON) {
                // H = D*tan(θ)，即目标恰好在仰角方向上，需要无限大速度
                return null;
            }
            return null;
        }

        double sSquared = gravity * D * D / (2.0 * cosTheta * cosTheta * denominator);
        if (sSquared < 0) {
            return null;
        }

        double s = Math.sqrt(sSquared);
        double vh = s * cosTheta;
        double vv = s * sinTheta;
        return hDir.scale(vh).add(0, vv, 0);
    }

    /**
     * 目标在正上方/正下方时，求解垂直初速度。
     * <p>
     * y(t) = vy0 * A(t) + gravity/(1-drag) * (t - A(t)) = H
     * 用二分法求 vy0
     * </p>
     */
    private static double solveVerticalSpeed(double H, double drag, double gravity) {
        if (Math.abs(drag - 1.0) < EPSILON) {
            // 无阻力: H = vy0*t + 0.5*g*t², 取最短时间解
            if (Math.abs(gravity) < EPSILON) {
                return H > 0 ? 1.0 : -1.0; // 无重力，匀速
            }
            double disc = gravity * gravity + 2 * gravity * H; // 简化判别式
            // H = vy*t + g/2*t² → g/2*t² + vy*t - H = 0
            // vy = (H - g/2*t²)/t, 取 t = sqrt(2*H/|g|) 近似
            double t = Math.sqrt(Math.abs(2 * H / gravity));
            return (H - 0.5 * gravity * t * t) / t;
        }

        // 有阻力：二分法
        double vyLow = -10.0;
        double vyHigh = 10.0;

        // 扩大范围
        for (int i = 0; i < 20; i++) {
            double fLow = evaluateVerticalOnly(vyLow, H, drag, gravity);
            double fHigh = evaluateVerticalOnly(vyHigh, H, drag, gravity);
            if (fLow * fHigh < 0) break;
            vyLow -= 10.0;
            vyHigh += 10.0;
        }

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double vyMid = (vyLow + vyHigh) / 2.0;
            double fMid = evaluateVerticalOnly(vyMid, H, drag, gravity);
            if (Math.abs(fMid) < EPSILON) return vyMid;
            double fLow = evaluateVerticalOnly(vyLow, H, drag, gravity);
            if (fLow * fMid < 0) vyHigh = vyMid;
            else vyLow = vyMid;
        }
        return (vyLow + vyHigh) / 2.0;
    }

    private static double evaluateVerticalOnly(double vy0, double H, double drag, double gravity) {
        // 找到 y(t) = H 的时刻 t，然后验证
        // 简化：模拟飞行直到 y 开始下降
        double y = 0;
        double vy = vy0;
        for (int t = 0; t < 1000; t++) {
            if (Math.abs(y - H) < EPSILON) return 0;
            vy = vy * drag + gravity;
            y += vy;
            if (vy < 0 && y < H && H >= 0) {
                // 已过顶点且低于目标
                return y - H;
            }
        }
        return y - H;
    }

    /**
     * 模拟弹道轨迹（用于调试可视化）。
     *
     * @param start    起始位置
     * @param velocity 初速度
     * @param drag     阻力
     * @param gravity  重力
     * @param maxTicks 最大模拟tick数
     * @return 轨迹点列表
     */
    public static List<Vec3> simulateTrajectory(Vec3 start, Vec3 velocity, float drag, float gravity, int maxTicks) {
        List<Vec3> trajectory = new ArrayList<>();
        Vec3 pos = start;
        Vec3 vel = velocity;
        trajectory.add(pos);
        for (int t = 0; t < maxTicks; t++) {
            vel = vel.scale(drag).add(0, gravity, 0);
            pos = pos.add(vel);
            trajectory.add(pos);
            if (pos.y < -64) break; // 掉出世界
        }
        return trajectory;
    }
}
