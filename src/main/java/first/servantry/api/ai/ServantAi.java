package first.servantry.api.ai;

import net.minecraft.world.entity.LivingEntity;

/**
 * 统一的 AI 容器接口：同时被遗留 {@link ActionController} 与新版
 * {@link first.servantry.api.ai.fsm.StateMachine} 实现，使 Servant 基类
 * 无需关心具体 AI 架构即可完成网络同步与每刻推进。
 */
public interface ServantAi {
    String getCurrentId();

    void setClientState(String id);

    void tick(LivingEntity target);
}
