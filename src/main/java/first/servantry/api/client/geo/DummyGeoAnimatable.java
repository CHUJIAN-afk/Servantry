package first.servantry.api.client.geo;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;

/**
 * {@link GeoAnimatable} 适配器，仅用于撑起 Geo 渲染管道。
 * <p>
 * 不代表任何游戏对象，纯粹是渲染框架的壳。
 * 动画进度由 {@link GeoSideloader} 每帧通过 {@link #setInjectedTick} 注入，
 * {@link #getTick} 返回注入值而非真实时间，从而精确控制动画帧位置。
 */
public class DummyGeoAnimatable implements GeoAnimatable {

    /**
     * 单例缓存，Sideloader 与 Controller 共享此实例
     */
    private final AnimatableInstanceCache cache;

    /**
     * 始终返回 CONTINUE 的空控制器，动画由 Sideloader 外部驱动
     */
    private final AnimationController<DummyGeoAnimatable> controller;

    /**
     * 由 Sideloader 注入的伪 tick 值，用于控制动画帧位置
     */
    private double injectedTick;

    DummyGeoAnimatable() {
        this.cache = new SingletonAnimatableInstanceCache(this);
        this.controller = new AnimationController<>(this, "sideloader_controller", 0, state -> PlayState.CONTINUE);
    }

    // ---- 内部 API（仅供 GeoSideloader 调用）----

    /**
     * 注入伪 tick 值，每帧由 Sideloader 在渲染前调用
     */
    void setInjectedTick(double tick) {
        this.injectedTick = tick;
    }

    /**
     * 获取动画控制器，用于外部设置当前播放的动画
     */
    AnimationController<DummyGeoAnimatable> getController() {
        return this.controller;
    }

    // ---- GeoAnimatable 接口实现 ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(this.controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * 返回注入的 tick 而非真实时间，使动画帧位置可由外部精确控制
     */
    @Override
    public double getTick(Object object) {
        return this.injectedTick;
    }
}
