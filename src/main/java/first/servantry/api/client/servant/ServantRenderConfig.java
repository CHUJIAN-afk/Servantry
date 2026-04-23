package first.servantry.api.client.servant;

import first.servantry.api.PathNode;
import first.servantry.api.servant.Servant;

/**
 * 仆从渲染配置，封装所有渲染参数。
 * <p>
 * 实现类只需重写 {@link AbstractServantRenderer#createConfig(Servant)} 方法返回配置即可获得完整的渲染效果。
 * 配置包含：
 * <ul>
 *   <li>拖尾类型（无、圆锥、丝带）</li>
 *   <li>拖尾计时器、颜色、半径、淡出曲线等</li>
 *   <li>本体渲染参数（缩放、旋转偏移等）</li>
 * </ul>
 * </p>
 *
 * @param <T> 仆从类型
 */
public class ServantRenderConfig<T extends Servant> {

    // ===================== 拖尾类型 =====================

    /** 拖尾类型枚举 */
    public enum TrailType {
        NONE,       // 无拖尾
        CONE,       // 圆锥拖尾（末端细、前端粗）
        RIBBON      // 丝带拖尾（菱形截面）
    }

    /** 拖尾类型，默认无拖尾 */
    public TrailType trailType = TrailType.NONE;

    // ===================== 拖尾基础参数 =====================

    /** 拖尾计时器值，>0 时显示拖尾 */
    public int trailTimer = 0;

    /** 历史节点数量，默认4 */
    public int trailHistoryLength = 4;

    /** 每节点插值分段数，默认4 */
    public int trailSegmentsPerNode = 4;

    /** 拖尾起始索引（用于动态缩短），默认0 */
    public int trailStartIndex = 0;

    // ===================== 圆锥拖尾参数 =====================

    /** 圆锥最大半径，默认0.2 */
    public float trailMaxRadius = 0.2f;

    /** 圆锥截面边数，默认6 */
    public int trailResolution = 6;

    // ===================== 颜色参数 =====================

    /** 基础颜色RGB，默认红色 */
    public int trailColorRGB = 0xFF0000;

    /** 颜色随进度变化函数，progress: 0=头部, 1=尾部 */
    public ColorFunction<T> trailColorFunction = (servant, progress, timeShift) -> trailColorRGB;

    // ===================== 淡出参数 =====================

    /** 淡出函数，返回0~1的缩放因子 */
    public FadeFunction trailFadeOut = progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);

    // ===================== 丝带拖尾参数 =====================

    /** 丝带剑尖透明度增强，默认1.0 */
    public AlphaBoostFunction<T> trailTipAlphaBoost = (servant, progress) -> 1.0f;

    /** 丝带剑尖亮度增强，默认1.0 */
    public BrightnessBoostFunction<T> trailTipBrightnessBoost = (servant, progress) -> 1.0f;

    // ===================== 本体渲染参数 =====================

    /** 本体缩放，默认1.0 */
    public float modelScale = 1.0f;

    /** 本体旋转偏移（欧拉角，度），默认无偏移 */
    public float modelYawOffset = 0f;
    public float modelPitchOffset = 0f;
    public float modelRollOffset = 0f;

    /** 视觉节点插值函数，默认返回原始节点 */
    public VisualNodeFunction<T> visualNodeFunction = (servant, partialTick, rawNode) -> rawNode;

    // ===================== 函数接口 =====================

    @FunctionalInterface
    public interface ColorFunction<T extends Servant> {
        int getColor(T servant, float progress, float timeShift);
    }

    @FunctionalInterface
    public interface FadeFunction {
        float getFade(float progress);
    }

    @FunctionalInterface
    public interface AlphaBoostFunction<T extends Servant> {
        float getBoost(T servant, float progress);
    }

    @FunctionalInterface
    public interface BrightnessBoostFunction<T extends Servant> {
        float getBoost(T servant, float progress);
    }

    @FunctionalInterface
    public interface VisualNodeFunction<T extends Servant> {
        PathNode getVisualNode(T servant, float partialTick, PathNode rawNode);
    }

    // ===================== 静态工厂方法 =====================

    /**
     * 创建默认配置（无拖尾）。
     */
    public static <T extends Servant> ServantRenderConfig<T> none() {
        return new ServantRenderConfig<>();
    }

    /**
     * 创建圆锥拖尾配置。
     *
     * @param timer    拖尾计时器值
     * @param colorRGB 颜色
     * @param radius   最大半径
     * @return 配置实例
     */
    public static <T extends Servant> ServantRenderConfig<T> cone(int timer, int colorRGB, float radius) {
        ServantRenderConfig<T> config = new ServantRenderConfig<>();
        config.trailType = TrailType.CONE;
        config.trailTimer = timer;
        config.trailColorRGB = colorRGB;
        config.trailMaxRadius = radius;
        return config;
    }

    /**
     * 创建丝带拖尾配置。
     *
     * @param timer    拖尾计时器值
     * @param colorRGB 颜色
     * @return 配置实例
     */
    public static <T extends Servant> ServantRenderConfig<T> ribbon(int timer, int colorRGB) {
        ServantRenderConfig<T> config = new ServantRenderConfig<>();
        config.trailType = TrailType.RIBBON;
        config.trailTimer = timer;
        config.trailColorRGB = colorRGB;
        return config;
    }

    // ===================== 链式配置方法 =====================

    public ServantRenderConfig<T> trailType(TrailType type) {
        this.trailType = type;
        return this;
    }

    public ServantRenderConfig<T> trailTimer(int timer) {
        this.trailTimer = timer;
        return this;
    }

    public ServantRenderConfig<T> trailHistoryLength(int length) {
        this.trailHistoryLength = length;
        return this;
    }

    public ServantRenderConfig<T> trailSegmentsPerNode(int segments) {
        this.trailSegmentsPerNode = segments;
        return this;
    }

    public ServantRenderConfig<T> trailStartIndex(int index) {
        this.trailStartIndex = index;
        return this;
    }

    public ServantRenderConfig<T> trailMaxRadius(float radius) {
        this.trailMaxRadius = radius;
        return this;
    }

    public ServantRenderConfig<T> trailResolution(int resolution) {
        this.trailResolution = resolution;
        return this;
    }

    public ServantRenderConfig<T> trailColorRGB(int color) {
        this.trailColorRGB = color;
        return this;
    }

    public ServantRenderConfig<T> trailColorFunction(ColorFunction<T> function) {
        this.trailColorFunction = function;
        return this;
    }

    public ServantRenderConfig<T> trailFadeOut(FadeFunction function) {
        this.trailFadeOut = function;
        return this;
    }

    public ServantRenderConfig<T> trailTipAlphaBoost(AlphaBoostFunction<T> function) {
        this.trailTipAlphaBoost = function;
        return this;
    }

    public ServantRenderConfig<T> trailTipBrightnessBoost(BrightnessBoostFunction<T> function) {
        this.trailTipBrightnessBoost = function;
        return this;
    }

    public ServantRenderConfig<T> modelScale(float scale) {
        this.modelScale = scale;
        return this;
    }

    public ServantRenderConfig<T> modelRotationOffset(float yaw, float pitch, float roll) {
        this.modelYawOffset = yaw;
        this.modelPitchOffset = pitch;
        this.modelRollOffset = roll;
        return this;
    }

    public ServantRenderConfig<T> visualNodeFunction(VisualNodeFunction<T> function) {
        this.visualNodeFunction = function;
        return this;
    }
}
