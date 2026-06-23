package first.servantry.api.common.attachment;

import first.servantry.register.AttributeRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 目标缓存，存储玩家周围的实体列表。
 * <p>
 * 每tick更新一次，所有仆从共享同一份缓存，避免重复查询。
 * 存储为玩家附件，仅服务端使用。
 * </p>
 */
public class TargetCache {

    /** 缓存的实体列表 */
    private final List<LivingEntity> entities = new ArrayList<>();

    /**
     * 更新缓存。每tick调用一次。
     *
     * @param player 玩家
     */
    public void update(Player player) {
        entities.clear();
        double value = 1;
        AttributeInstance instance = player.getAttribute(AttributeRegister.ServantSearchRange);
        if (instance != null) {
            value = instance.getValue();
        }
        double distance = 32 * value;
        List<LivingEntity> result = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(distance)).stream()
                .filter(living -> living.isAlive() && living != player && living.distanceToSqr(player) <= (distance * distance))
                .toList();
        entities.addAll(result);
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