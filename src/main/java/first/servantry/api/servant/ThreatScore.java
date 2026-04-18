package first.servantry.api.servant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.WeakHashMap;

public record ThreatScore(Player player, Servant servant, LivingEntity target) {

    private static final WeakHashMap<Player, WeakHashMap<LivingEntity, TargetCache>> CACHE = new WeakHashMap<>();

    private record TargetCache(int tick, double distSqr, boolean hasLoS) {}

    /**
     * 综合评估目标的威胁分数（分数越高，目标优先级越高）
     *
     * @param absoluteWarningDist 绝对警戒距离（低于此值直接拉满仇恨，跳过后续所有计算）
     * @param playerDistWeight    玩家与目标距离的得分权重
     * @param playerLoSWeight     玩家与目标无阻挡时的固定加分
     * @param servantDistWeight   仆从与目标距离的得分权重
     * @param servantLoSWeight    仆从与目标无阻挡时的固定加分
     * @return 最终威胁分数
     */
    public double evaluate(double absoluteWarningDist,
                           double playerDistWeight, double playerLoSWeight,
                           double servantDistWeight, double servantLoSWeight) {

        // === 1. 玩家维度 (带同 Tick 缓存机制) ===
        int currentTick = player.tickCount;
        WeakHashMap<LivingEntity, TargetCache> playerMap = CACHE.computeIfAbsent(player, k -> new WeakHashMap<>());
        TargetCache cache = playerMap.get(target);

        if (cache == null || cache.tick() != currentTick) {
            double pDistSqr = player.distanceToSqr(target);

            // 核心短路：如果目标贴脸，直接拉满仇恨
            if (pDistSqr <= absoluteWarningDist * absoluteWarningDist) {
                return Double.MAX_VALUE;
            }

            boolean pHasLoS = player.hasLineOfSight(target);
            cache = new TargetCache(currentTick, pDistSqr, pHasLoS);
            playerMap.put(target, cache);
        }

        // === 2. 仆从维度 (基于坐标和底层射线的硬核计算) ===
        // 假设 Servant 有 getPos() 方法返回 Vec3。如果你的方法名不同，请自行替换。
        Vec3 servantPos = servant.getPos();
        Vec3 targetPos = target.position();
        Vec3 targetEyePos = target.getEyePosition();

        // 2.1 计算仆从与目标的距离平方
        double sDistSqr = servantPos.distanceToSqr(targetPos);

        // 2.2 仆从视线检测：从仆从位置向目标眼睛位置发射射线，只检测方块遮挡
        ClipContext context = new ClipContext(servantPos, targetEyePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target);
        BlockHitResult hitResult = player.level().clip(context);

        // 如果射线没有命中任何方块 (MISS)，说明视线无阻挡
        boolean sHasLoS = hitResult.getType() == HitResult.Type.MISS;

        // === 3. 权重计算 ===
        // 算法：使用 100 / (距离 + 1) 将距离反转为得分。距离越近，基础得分越高
        double pDistScore = 100.0 / (Math.sqrt(cache.distSqr()) + 1.0);
        double sDistScore = 100.0 / (Math.sqrt(sDistSqr) + 1.0);

        return (pDistScore * playerDistWeight) + (cache.hasLoS() ? playerLoSWeight : 0.0) + (sDistScore * servantDistWeight) + (sHasLoS ? servantLoSWeight : 0.0);
    }
}