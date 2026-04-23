package first.servantry.api.projectile;

/**
 * 射弹状态枚举。
 * <p>
 * 定义射弹的生命周期状态：
 * <ul>
 *   <li>{@link #FLYING} - 飞行状态，追踪目标、渲染拖尾</li>
 *   <li>{@link #ATTACHED} - 黏贴状态，附着在目标身上</li>
 *   <li>{@link #DEAD} - 死亡状态，等待被移除</li>
 * </ul>
 * </p>
 */
public enum ProjectileState {
    /** 飞行状态：追踪目标、渲染拖尾 */
    FLYING,
    /** 黏贴状态：附着在目标身上 */
    ATTACHED,
    /** 死亡状态：等待被移除 */
    DEAD
}
