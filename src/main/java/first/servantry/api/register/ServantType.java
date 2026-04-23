package first.servantry.api.register;

import first.servantry.api.entity.EntityType;
import first.servantry.api.servant.Servant;

import java.util.function.Supplier;

/**
 * 仆从类型注册记录。
 * <p>
 * 用于注册不同类型的仆从，包含工厂方法以创建仆从实例。
 * 继承自 {@link EntityType} 以支持统一的附件实体架构。
 * </p>
 *
 * @param <T> 仆从类型
 */
public class ServantType<T extends Servant> extends EntityType<T> {

    public ServantType(Supplier<T> factory) {
        super(factory);
    }
}
