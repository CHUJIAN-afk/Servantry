package first.servantry.common.servant;

import first.servantry.api.register.ServantType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.register.ServantRegister;

/**
 * 双子魔眼 - 由Retinazer和Spazmatism组成的双瞳仆从。
 * <p>
 * 单个仆从实体，通过AI控制表现出两种特性：
 * <ul>
 *   <li>Retinazer（激光眼）- 红色，发射激光攻击</li>
 *   <li>Spazmatism（咒焰眼）- 绿色，喷射咒焰攻击</li>
 * </ul>
 * </p>
 */
public class Twins extends MomentumServant {

    public Twins() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        // TODO: 待后续实现AI目标
    }

    @Override
    public float getDamage() {
        return 2.4f;
    }

    @Override
    public float getKnockback() {
        return 0.1f;
    }

    @Override
    public ServantType<? extends MomentumServant> getType() {
        return ServantRegister.Twins.get();
    }

}