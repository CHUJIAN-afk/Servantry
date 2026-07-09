package first.servantry.api.common.attachment;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 目标缓存，存储玩家周围的实体列表。
 * <p>
 * 每tick更新一次，所有仆从共享同一份缓存，避免重复查询。
 * 存储为玩家附件，仅服务端使用。
 * </p>
 */
public class TargetCache {

    /**
     * 缓存的实体列表
     */
    private final List<LivingEntity> entities = new ArrayList<>();

    private final Map<Integer, Boolean> visibilityCache = new HashMap<>();

    private final Map<Integer, Float> distanceCache = new HashMap<>();

    public LivingEntity getNewTarget(Servant servant, List<LivingEntity> targets, float ownerWarningDistance, boolean selfCenter) {
        Player owner = servant.getOwner();
        LivingEntity currentTarget = servant.getTarget();
        LivingEntity newTarget = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity entity : targets) {
            double score = selfCenter ? getDistance(servant, entity) : getDistance(owner, entity);
            if (ownerWarningDistance > 0 && getDistance(owner, entity) < ownerWarningDistance) {
                score -= 10000.0;
            }
            if (entity == currentTarget) {
                score -= 1000.0;
            }
            score += ((entity.getId() * 31 + servant.hashCode() * 17) % 5) * 40;
            if (score < bestScore) {
                bestScore = score;
                newTarget = entity;
            }
        }
        return newTarget;
    }

    public float getDistance(Servant servant, LivingEntity living) {
        Integer key = servant.getUuid().hashCode() + living.getUUID().hashCode();
        return distanceCache.computeIfAbsent(key, k -> (float) servant.getPos().distanceTo(living.getBoundingBox().getCenter()));
    }

    public float getDistance(Player player, LivingEntity living) {
        Integer key = player.getUUID().hashCode() + living.getUUID().hashCode();
        return distanceCache.computeIfAbsent(key, k -> (float) player.getBoundingBox().getCenter().distanceTo(living.getBoundingBox().getCenter()));
    }

    public float getServantSearchRange(Player player, float distance) {
        AttributeInstance instance = player.getAttribute(AttributeRegister.ServantSearchRange);
        if (instance != null) {
            distance *= (float) instance.getValue();
        }
        return distance;
    }

    //此缓存不能被共享，极易卡顿
    public boolean isVisibility(Servant servant, LivingEntity living) {
        Integer key = servant.getUuid().hashCode() + living.getUUID().hashCode();
        return visibilityCache.computeIfAbsent(key, k -> {
            Level level = living.level();
            AABB box = living.getBoundingBox();
            ClipContext context = new ClipContext(servant.getPos(), box.getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, living);
            BlockHitResult clip = level.clip(context);
            return clip.getType() == HitResult.Type.MISS;
        });
    }

    public boolean isVisibility(Player player, LivingEntity living) {
        Integer key = player.getUUID().hashCode() + living.getUUID().hashCode();
        return visibilityCache.computeIfAbsent(key, k -> player.hasLineOfSight(living));
    }

    /**
     * 更新缓存。每tick调用一次。
     *
     * @param player 玩家
     */
    public void update(Player player) {
        entities.clear();
        visibilityCache.clear();
        distanceCache.clear();
        EntityData data = player.getData(AttachmentRegister.EntityData);
        if (data.isRunning()) {
            AABB box = player.getBoundingBox();
            Vec3 center = box.getCenter();
            Vec3 maxVec3 = data.getGroups()
                    .values()
                    .stream()
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .flatMap(Collection::stream)
                    .map(AttachmentEntity::getPos)
                    .max(Comparator.comparingDouble(center::distanceToSqr))
                    .orElse(null);
            double distance;
            if (maxVec3 != null) {
                distance = getServantSearchRange(player, Math.max(32, (float) maxVec3.distanceTo(center) + 4));
            } else {
                distance = getServantSearchRange(player, 32);
            }
            List<LivingEntity> result = player.level()
                    .getEntitiesOfClass(LivingEntity.class, box.inflate(distance))
                    .stream()
                    .filter(LivingEntity::isAlive)
                    .filter(living -> !player.equals(living))
                    .filter(living -> getDistance(player, living) <= distance)
                    .toList();
            entities.addAll(result);
        }
    }

    /**
     * 获取缓存的实体列表。
     *
     * @return 实体列表（只读）
     */
    public List<LivingEntity> getEntities() {
        return entities;
    }

    /**
     * 检查缓存是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return entities.isEmpty();
    }

    /**
     * 获取缓存大小。
     *
     * @return 实体数量
     */
    public int size() {
        return entities.size();
    }
}