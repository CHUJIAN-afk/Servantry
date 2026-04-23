package first.servantry.common.attachment;

import first.servantry.api.servant.Servant;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 索敌缓存数据附件。
 * <p>
 * 每tick更新玩家周围的生物缓存，避免重复计算距离和可见性。
 * 不需要序列化和网络同步，仅用于服务端性能优化。
 * </p>
 */
public class TargetCacheData {

    /**
     * 缓存的目标条目，包含预计算的距离和可见性信息。
     */
    public record CachedTarget(LivingEntity entity, double distanceToPlayer, boolean hasLineOfSight) {
    }

    /** 缓存的目标列表，按距离玩家从近到远排序 */
    private final List<CachedTarget> cachedTargets = new ArrayList<>();

    /** 缓存的最大搜索半径 */
    private static final double CACHE_RADIUS = 32;

    /**
     * 更新缓存。每tick调用一次。
     *
     * @param player 玩家
     */
    public void update(Player player) {
        cachedTargets.clear();
        // 获取玩家周围的所有生物
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(CACHE_RADIUS), entity -> entity.isAlive() && entity != player);
        Vec3 playerEyePos = player.getEyePosition();
        for (LivingEntity entity : entities) {
            double distance = entity.distanceToSqr(player);
            boolean hasLineOfSight = checkLineOfSight(player, playerEyePos, entity);
            cachedTargets.add(new CachedTarget(entity, distance, hasLineOfSight));
        }
        // 按距离排序，便于后续筛选
        cachedTargets.sort(Comparator.comparingDouble(t -> t.distanceToPlayer));
    }

    /**
     * 检查玩家与目标之间是否有视线。
     */
    private boolean checkLineOfSight(Player player, Vec3 playerEyePos, LivingEntity target) {
        Vec3 targetEyePos = target.getEyePosition();
        ClipContext context = new ClipContext(
                playerEyePos, targetEyePos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        return player.level().clip(context).getType() == HitResult.Type.MISS;
    }

    /**
     * 获取满足条件的目标列表。
     * <p>
     * 使用缓存数据避免重复计算距离和可见性。
     * </p>
     *
     * @param servant           仆从（用于计算仆从与目标的距离）
     * @param maxDistToServant  仆从与目标的最大距离平方
     * @param maxDistToPlayer   玩家与目标的最大距离平方
     * @param requireLineOfSight 是否要求玩家与目标之间无方块阻挡
     * @return 满足条件的目标列表（可变）
     */
    public List<LivingEntity> getTargets(Servant servant, double maxDistToServant, double maxDistToPlayer, boolean requireLineOfSight) {
        List<LivingEntity> result = new ArrayList<>();
        Vec3 servantPos = servant.getPos();

        for (CachedTarget cached : cachedTargets) {
            // 距离检查：由于列表已按距离排序，可以提前终止
            if (cached.distanceToPlayer > maxDistToPlayer) {
                break;
            }

            // 可见性检查
            if (requireLineOfSight && !cached.hasLineOfSight) {
                continue;
            }

            // 仆从与目标的距离检查
            double distToServant = cached.entity.distanceToSqr(servantPos);
            if (distToServant <= maxDistToServant) {
                result.add(cached.entity);
            }
        }

        return result;
    }

    /**
     * 获取满足条件的目标列表，并按与指定位置的距离排序。
     *
     * @param servant           仆从
     * @param maxDistToServant  仆从与目标的最大距离平方
     * @param maxDistToPlayer   玩家与目标的最大距离平方
     * @param requireLineOfSight 是否要求玩家与目标之间无方块阻挡
     * @param sortPosition      排序参考位置（通常为仆从位置）
     * @return 满足条件且已排序的目标列表
     */
    public List<LivingEntity> getTargetsSorted(Servant servant, double maxDistToServant, double maxDistToPlayer, boolean requireLineOfSight, Vec3 sortPosition) {
        List<LivingEntity> result = new ArrayList<>();

        for (CachedTarget cached : cachedTargets) {
            if (cached.distanceToPlayer > maxDistToPlayer) {
                break;
            }

            if (requireLineOfSight && !cached.hasLineOfSight) {
                continue;
            }

            double distToServant = cached.entity.distanceToSqr(sortPosition);
            if (distToServant <= maxDistToServant) {
                result.add(cached.entity);
            }
        }

        // 按与指定位置的距离排序
        result.sort(Comparator.comparingDouble(e -> e.distanceToSqr(sortPosition)));

        return result;
    }

    /**
     * 获取缓存的原始列表（只读）。
     */
    public List<CachedTarget> getCachedTargets() {
        return cachedTargets;
    }

}
