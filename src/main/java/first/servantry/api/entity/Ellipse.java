package first.servantry.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 椭圆曲线工具类。
 * <p>
 * 通过长轴两端点、面法向量和曲率定义椭圆，提供位置、切线、法线等查询。
 * progress=0 和 1 在 pointB 端，progress=0.5 在 pointA 端，形成闭合曲线。
 * </p>
 */
public class Ellipse {

    private final Vec3 pointA;
    private final Vec3 pointB;
    private final Vec3 planeNormal;
    private final float curvature;

    private final Vec3 center;
    private final Vec3 major;
    private final Vec3 minor;
    private final Vec3 majorDir;
    private final Vec3 minorDir;

    public Ellipse(Vec3 pointA, Vec3 pointB, Vec3 planeNormal, float curvature) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.planeNormal = planeNormal;
        this.curvature = curvature;

        this.major = pointB.subtract(pointA).scale(0.5);
        this.center = pointA.add(major);
        this.majorDir = major.normalize();

        Vec3 mDir = planeNormal.cross(majorDir).normalize();
        if (mDir.lengthSqr() < 1e-5) mDir = majorDir.cross(new Vec3(0, 1, 0)).normalize();
        this.minorDir = mDir;
        this.minor = minorDir.scale(major.length() * curvature);
    }

    /**
     * 生成垂直于长轴的随机椭圆面法向量。
     */
    public static @NotNull Vec3 randomPlaneNormal(RandomSource random, Vec3 pointA, Vec3 pointB) {
        Vec3 majorDir = pointB.subtract(pointA).normalize();
        Vec3 randVec = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize();
        Vec3 planeNormal = randVec.subtract(majorDir.scale(randVec.dot(majorDir))).normalize();
        if (planeNormal.lengthSqr() < 1e-5) planeNormal = majorDir.cross(new Vec3(0, 1, 0)).normalize();
        if (planeNormal.lengthSqr() < 1e-5) planeNormal = new Vec3(1, 0, 0);
        return planeNormal;
    }

    /**
     * 椭圆上 progress 对应的位置
     */
    public Vec3 getPoint(float progress) {
        float biasedT = progress - 0.08f * Mth.sin(progress * Mth.TWO_PI);
        float theta = biasedT * Mth.TWO_PI;
        return center.add(major.scale(Math.cos(theta))).add(minor.scale(Math.sin(theta)));
    }

    /**
     * 椭圆上 progress 对应的切线方向
     */
    public Vec3 getTangent(float progress) {
        float biasedT = progress - 0.08f * Mth.sin(progress * Mth.TWO_PI);
        float theta = biasedT * Mth.TWO_PI;
        return major.scale(-Math.sin(theta)).add(minor.scale(Math.cos(theta))).normalize();
    }

    /**
     * 椭圆中心
     */
    public Vec3 getCenter() {
        return center;
    }

    /**
     * 长轴方向（单位向量）
     */
    public Vec3 getMajorDir() {
        return majorDir;
    }

    /**
     * 短轴方向（单位向量）
     */
    public Vec3 getMinorDir() {
        return minorDir;
    }

    /**
     * 椭圆面法向量
     */
    public Vec3 getPlaneNormal() {
        return planeNormal;
    }

    /**
     * 曲率
     */
    public float getCurvature() {
        return curvature;
    }

    /**
     * 长轴端点A
     */
    public Vec3 getPointA() {
        return pointA;
    }

    /**
     * 长轴端点B
     */
    public Vec3 getPointB() {
        return pointB;
    }
}
