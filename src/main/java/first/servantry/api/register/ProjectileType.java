package first.servantry.api.register;

import first.servantry.api.projectile.Projectile;

import java.util.function.Supplier;

/**
 * 射弹类型注册记录。
 * <p>
 * 用于注册不同类型的射弹，包含工厂方法以创建射弹实例。
 * </p>
 *
 * @param <T> 射弹类型
 */
public record ProjectileType<T extends Projectile>(Supplier<T> factory) {
}