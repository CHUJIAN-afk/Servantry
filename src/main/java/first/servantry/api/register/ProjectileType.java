package first.servantry.api.register;

import first.servantry.api.entity.EntityType;
import first.servantry.api.projectile.Projectile;

import java.util.function.Supplier;

/**
 * 射弹类型注册记录。
 * <p>
 * 用于注册不同类型的射弹，包含工厂方法以创建射弹实例。
 * 继承自 {@link EntityType} 以支持统一的附件实体架构。
 * </p>
 *
 * @param <T> 射弹类型
 */
public class ProjectileType<T extends Projectile> extends EntityType<T> {

    public ProjectileType(Supplier<T> factory) {
        super(factory);
    }

}