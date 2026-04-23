package first.servantry.api.entity;

import java.util.function.Supplier;

/**
 * 附件实体类型注册记录。
 * <p>
 * 用于注册不同类型的附件实体，包含工厂方法以创建实体实例。
 * </p>
 *
 * @param <T> 实体类型
 */
public class EntityType<T extends AttachmentEntity> {

    private final Supplier<T> factory;

    public EntityType(Supplier<T> factory) {
        this.factory = factory;
    }

    public Supplier<T> factory() {
        return factory;
    }
}
