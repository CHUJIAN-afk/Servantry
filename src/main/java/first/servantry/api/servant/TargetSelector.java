package first.servantry.api.servant;

import first.servantry.register.AttachmentRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

/**
 * 高性能目标选择器，提供流式API进行目标筛选和评分。
 * <p>
 * 使用共享缓存机制，所有仆从共享同一份实体列表，避免重复查询。
 * 主线程计算，无并行开销。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * LivingEntity target = TargetSelector.create(servant)
 *     .maxDistance(64)
 *     .requireLineOfSight()
 *     .filter(servant::isTarget)
 *     .preferCloseTo(servant.getPos())
 *     .preferCurrentTarget(servant.getTarget())
 *     .find();
 * }</pre>
 * </p>
 */
public final class TargetSelector {

    private final Servant servant;
    private final Player owner;
    private final TargetCache cache;

    // 筛选条件
    private double maxDistSq = Double.MAX_VALUE;
    private boolean requireLOS = false;
    private Predicate<LivingEntity> customFilter = null;

    // 评分偏好
    private Vec3 preferPosition = null;
    private LivingEntity currentTarget = null;

    private TargetSelector(Servant servant, TargetCache cache) {
        this.servant = servant;
        this.owner = servant.getOwner();
        this.cache = cache;
    }

    /**
     * 创建目标选择器实例。
     *
     * @param servant 仆从实例
     * @return 目标选择器
     */
    public static TargetSelector create(Servant servant) {
        return new TargetSelector(servant, servant.getOwner().getData(AttachmentRegister.TargetCache));
    }

    /**
     * 设置最大搜索距离。
     *
     * @param dist 最大距离（格）
     * @return this
     */
    public TargetSelector maxDistance(double dist) {
        this.maxDistSq = dist * dist;
        return this;
    }

    /**
     * 要求目标与所有者之间有视线。
     *
     * @return this
     */
    public TargetSelector requireLineOfSight() {
        this.requireLOS = true;
        return this;
    }

    /**
     * 设置是否要求视线。
     *
     * @param require 是否要求
     * @return this
     */
    public TargetSelector requireLineOfSight(boolean require) {
        this.requireLOS = require;
        return this;
    }

    /**
     * 设置自定义筛选条件。
     *
     * @param filter 筛选谓词
     * @return this
     */
    public TargetSelector filter(Predicate<LivingEntity> filter) {
        this.customFilter = filter;
        return this;
    }

    /**
     * 设置优先靠近的位置。
     *
     * @param pos 参考位置
     * @return this
     */
    public TargetSelector preferCloseTo(Vec3 pos) {
        this.preferPosition = pos;
        return this;
    }

    /**
     * 设置当前目标的加分（用于保持目标一致性）。
     *
     * @param target 当前目标
     * @return this
     */
    public TargetSelector preferCurrentTarget(LivingEntity target) {
        this.currentTarget = target;
        return this;
    }

    /**
     * 执行搜索，返回最佳目标。
     *
     * @return 最佳目标，若无则返回null
     */
    public LivingEntity find() {
        if (cache == null || cache.isEmpty()) return null;

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        Vec3 servantPos = preferPosition != null ? preferPosition : servant.getPos();
        double servantX = servantPos.x;
        double servantY = servantPos.y;
        double servantZ = servantPos.z;

        for (LivingEntity entity : cache.getEntities()) {
            // 基础筛选
            if (entity == owner || !entity.isAlive()) continue;

            // 距离筛选
            double dx = entity.getX() - servantX;
            double dy = entity.getY() - servantY;
            double dz = entity.getZ() - servantZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxDistSq) continue;

            // 视线筛选
            if (requireLOS && !owner.hasLineOfSight(entity)) continue;

            // 自定义筛选
            if (customFilter != null && !customFilter.test(entity)) continue;

            // 计算评分
            double score = distSq;

            // 玩家附近加分
            double ox = entity.getX() - owner.getX();
            double oy = entity.getY() - owner.getY();
            double oz = entity.getZ() - owner.getZ();
            if (ox * ox + oy * oy + oz * oz < 36.0) {
                score -= 10000.0;
            }

            // 当前目标加分
            if (entity == currentTarget) {
                score -= 1000.0;
            }

            // 哈希抖动
            score += ((entity.getId() * 31 + servant.hashCode() * 17) % 5) * 40;

            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    /**
     * 执行搜索，返回前N个最佳目标。
     *
     * @param count 数量
     * @return 排序后的目标列表
     */
    public List<LivingEntity> findTop(int count) {
        // 简化实现，按距离排序返回前N个
        return cache.getEntities().stream()
                .filter(e -> e != owner && e.isAlive())
                .filter(e -> preferPosition == null || e.distanceToSqr(preferPosition) <= maxDistSq)
                .filter(e -> !requireLOS || owner.hasLineOfSight(e))
                .filter(e -> customFilter == null || customFilter.test(e))
                .sorted((a, b) -> {
                    Vec3 pos = preferPosition != null ? preferPosition : servant.getPos();
                    return Double.compare(a.distanceToSqr(pos), b.distanceToSqr(pos));
                })
                .limit(count)
                .toList();
    }
}