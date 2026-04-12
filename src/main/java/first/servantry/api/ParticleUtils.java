package first.servantry.api;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ParticleUtils {

    public static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 逐渐生成两点之间的直线粒子
     * @param level      世界
     * @param start      起点坐标
     * @param end        终点坐标
     * @param particle   粒子类型
     * @param density    粒子密度（每格距离的粒子数）
     * @param jitter     位置随机偏移量（0=完全直线）
     * @param speed      粒子速度
     * @param steps      扩散步数
     * @param interval   每步之间的间隔时间（毫秒）
     */
    public static void spawnMovingParticleLine(ServerLevel level,
                                         Vec3 start,
                                         Vec3 end,
                                         ParticleOptions particle,
                                         double density,
                                         float jitter,
                                         double speed,
                                         int steps,
                                         long interval) {
        for (int step = 1; step <= steps; step++) {
            Vec3 currentEnd = start.lerp(end, step / (double) steps);
            executorService.schedule(() -> spawnParticleLine(level, start, currentEnd, particle, density, jitter, speed), step * interval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 生成两点之间的直线粒子
     * @param level      世界
     * @param start      起点坐标
     * @param end        终点坐标
     * @param particle   粒子类型
     * @param density    粒子密度（每格距离的粒子数）
     * @param jitter     位置随机偏移量（0=完全直线）
     * @param speed      粒子速度
     */
    public static void spawnParticleLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, double density, float jitter, double speed) {
        double distance = start.distanceTo(end);
        int particles = (int) (distance * density);
        for (int i = 0; i <= particles; i++) {
            double ratio = i / (double) particles;
            Vec3 pos = start.lerp(end, ratio);
            if (jitter > 0) {
                Random random = getRandom();
                pos = pos.add(
                        (random.nextDouble() - 0.5) * jitter,
                        (random.nextDouble() - 0.5) * jitter,
                        (random.nextDouble() - 0.5) * jitter
                );
            }
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0, 0, 0, speed);
        }
    }

    public static void spawnParticleLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, double density, float jitter) {
        spawnParticleLine(level, start, end, particle, density, jitter, 0);
    }

    /**
     * 生成逐渐向外扩散的水平粒子群（圆形环绕 + 圆内随机粒子）
     * @param level          世界
     * @param centerX        圆心X
     * @param centerY        圆心Y
     * @param centerZ        圆心Z
     * @param particle       粒子类型
     * @param maxRadius      最大半径
     * @param totalParticles 每步的粒子数（圆形+内部）
     * @param innerRatio     内部粒子占比
     * @param speed          粒子速度
     * @param steps          扩散步数
     * @param interval       每步之间的间隔时间（毫秒）
     */
    public static void spawnExpandingParticleCircle(ServerLevel level,
                                                    double centerX,
                                                    double centerY,
                                                    double centerZ,
                                                    ParticleOptions particle,
                                                    float maxRadius,
                                                    int totalParticles,
                                                    float innerRatio,
                                                    float speed,
                                                    int steps,
                                                    long interval) {
        for (int step = 0; step < steps; step++) {
            float currentRadius = step * (maxRadius / steps);
            executorService.schedule(() -> spawnParticleCircle(level, centerX, centerY, centerZ, particle, currentRadius, totalParticles, innerRatio, speed), step * interval, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * 生成水平粒子群（圆形环绕 + 圆内随机粒子）
     *
     * @param level          世界
     * @param centerX        圆心X
     * @param centerY        圆心Y（高度）
     * @param centerZ        圆心Z
     * @param particle       粒子类型
     * @param maxRadius      最大半径
     * @param totalParticles 总粒子数（圆形+内部）
     * @param innerRatio     内部粒子占比（0.2 = 20%粒子在内部）
     * @param speed          粒子速度
     */
    public static void spawnParticleCircle(ServerLevel level,
                                           double centerX,
                                           double centerY,
                                           double centerZ,
                                           ParticleOptions particle,
                                           float maxRadius,
                                           int totalParticles,
                                           float innerRatio,
                                           float speed) {

        // 圆形环绕粒子（80%）
        int surfaceParticles = (int) (totalParticles * (1 - innerRatio));
        Random random = getRandom();
        for (int i = 0; i < surfaceParticles; i++) {
            // 随机圆形坐标（均匀分布）
            double theta = random.nextDouble() * 2 * Math.PI; // 水平角
            double r = maxRadius * (0.8 + 0.2 * random.nextDouble()); // 随机半径波动

            double x = centerX + r * Math.cos(theta);
            double z = centerZ + r * Math.sin(theta);

            level.sendParticles(particle, x, centerY, z, 1, 0.1, 0, 0.1, 0.02);
        }

        // 圆内随机粒子（20%）
        int innerParticles = totalParticles - surfaceParticles;
        for (int i = 0; i < innerParticles; i++) {
            // 随机圆内坐标（均匀分布）
            double r = maxRadius * Math.sqrt(random.nextDouble()); // 平方根保证均匀
            double theta = random.nextDouble() * 2 * Math.PI;

            double x = centerX + r * Math.cos(theta);
            double z = centerZ + r * Math.sin(theta);

            level.sendParticles(particle, x, centerY, z, 1, 0, 0, 0, speed);
        }
    }

    public static void spawnParticleSphere(ServerLevel level, Vec3 vec3, ParticleOptions particle, float maxRadius, int totalParticles, float innerRatio) {
        spawnParticleSphere(level, vec3.x(), vec3.y(), vec3.z(), particle, maxRadius, totalParticles, innerRatio, 0);
    }

    public static void spawnParticleSphere(ServerLevel level, Vec3 vec3, ParticleOptions particle, float maxRadius, int totalParticles, float innerRatio, float speed) {
        spawnParticleSphere(level, vec3.x(), vec3.y(), vec3.z(), particle, maxRadius, totalParticles, innerRatio, speed);
    }

    public static void spawnParticleSphere(ServerLevel level, double centerX, double centerY, double centerZ, ParticleOptions particle, float maxRadius, int totalParticles, float innerRatio) {
        spawnParticleSphere(level, centerX, centerY, centerZ, particle, maxRadius, totalParticles, innerRatio, 0);
    }

    /**
     * 生成3D球形粒子群（球面环绕 + 球体内随机粒子）
     *
     * @param level          世界
     * @param centerX        球心X
     * @param centerY        球心Y
     * @param centerZ        球心Z
     * @param particle       粒子类型
     * @param maxRadius      最大半径
     * @param totalParticles 总粒子数（球面+内部）
     * @param innerRatio     内部粒子占比（0.2 = 20%粒子在内部）
     */
    public static void spawnParticleSphere(ServerLevel level,
                                           double centerX,
                                           double centerY,
                                           double centerZ,
                                           ParticleOptions particle,
                                           float maxRadius,
                                           int totalParticles,
                                           float innerRatio,
                                           float speed) {

        // 球面环绕粒子（80%）
        int surfaceParticles = (int) (totalParticles * (1 - innerRatio));
        Random random = getRandom();
        for (int i = 0; i < surfaceParticles; i++) {
            // 随机球面坐标（均匀分布）
            double theta = random.nextDouble() * 2 * Math.PI; // 水平角
            double phi = Math.acos(2 * random.nextDouble() - 1); // 俯仰角
            double r = maxRadius * (0.8 + 0.2 * random.nextDouble()); // 随机半径波动

            double x = centerX + r * Math.sin(phi) * Math.cos(theta);
            double y = centerY + r * Math.sin(phi) * Math.sin(theta);
            double z = centerZ + r * Math.cos(phi);

            level.sendParticles(particle, x, y, z, 1, 0.1, 0.1, 0.1, 0.02);
        }

        // 球体内随机粒子（20%）
        int innerParticles = totalParticles - surfaceParticles;
        for (int i = 0; i < innerParticles; i++) {
            // 随机球体内坐标（均匀分布）
            double r = maxRadius * Math.pow(random.nextDouble(), 1 / 3.0); // 立方根保证均匀
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * random.nextDouble() - 1);

            double x = centerX + r * Math.sin(phi) * Math.cos(theta);
            double y = centerY + r * Math.sin(phi) * Math.sin(theta);
            double z = centerZ + r * Math.cos(phi);

            level.sendParticles(particle, x, y, z, 1, 0, 0, 0, speed);
        }
    }

}

