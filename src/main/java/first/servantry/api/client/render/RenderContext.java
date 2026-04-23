package first.servantry.api.client.render;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntity;

/**
 * 渲染上下文，封装附件实体渲染所需的所有参数和配置。
 * <p>
 * 渲染上下文采用链式配置模式，通过链式调用方法设置参数。
 * 子类渲染器只需在 {@code createContext()} 方法中返回配置好的上下文实例，
 * 即可获得完整的拖尾和本体渲染效果。
 * </p>
 *
 * <h3>配置分类</h3>
 * <ul>
 *   <li><b>拖尾类型</b>：无拖尾、圆锥拖尾、丝带拖尾</li>
 *   <li><b>拖尾参数</b>：计时器、历史长度、插值分段、颜色、淡出曲线等</li>
 *   <li><b>本体参数</b>：缩放比例、旋转偏移、视觉节点插值</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建圆锥拖尾配置
 * RenderContext<MyEntity> context = RenderContext.<MyEntity>cone(timer, 0xFF0000, 0.2f)
 *     .trailHistoryLength(6)
 *     .trailResolution(12)
 *     .modelScale(0.5f);
 *
 * // 创建丝带拖尾配置
 * RenderContext<MyEntity> context = RenderContext.<MyEntity>ribbon(timer, 0x88CCFF)
 *     .trailColorFunction((e, progress, time) -> computeColor(progress))
 *     .modelRotationOffset(0, 90, 0);
 * }</pre>
 *
 * @param <T> 附件实体类型
 * @see AttachmentEntity
 * @see IAttachmentEntityRenderer
 */
public class RenderContext<T extends AttachmentEntity> {

    // ===================== 拖尾类型枚举 =====================

    /**
     * 拖尾渲染类型。
     * <ul>
     *   <li>{@link #NONE} - 无拖尾，仅渲染实体本体</li>
     *   <li>{@link #CONE} - 圆锥拖尾，末端细前端粗，适合球状或圆形实体</li>
     *   <li>{@link #RIBBON} - 丝带拖尾，菱形截面四向展开，适合剑状或扁平实体</li>
     * </ul>
     */
    public enum TrailType {
        /** 无拖尾效果 */
        NONE,
        /** 圆锥拖尾：每个节点绘制正多边形截面，半径随进度递减形成锥形 */
        CONE,
        /** 丝带拖尾：每个节点绘制菱形截面，四个方向的面片形成十字交叉效果 */
        RIBBON
    }

    // ===================== 拖尾类型配置 =====================

    /**
     * 拖尾渲染类型，默认 {@link TrailType#NONE}。
     * <p>
     * 设置为 NONE 时，即使 trailTimer > 0 也不会渲染拖尾。
     * </p>
     */
    public TrailType trailType = TrailType.NONE;

    // ===================== 拖尾基础参数 =====================

    /**
     * 拖尾计时器值。
     * <p>
     * 控制拖尾的显示时长。当值 > 0 时才会渲染拖尾。
     * 通常与实体内部的 trailTimer 字段同步，实现攻击时显示拖尾、静止时隐藏的效果。
     * </p>
     */
    public int trailTimer = 0;

    /**
     * 历史节点数量，默认 4。
     * <p>
     * 从历史记录中取出的节点数量，影响拖尾长度。
     * 数值越大，拖尾越长，但渲染开销也越大。
     * 建议范围：3-8。
     * </p>
     */
    public int trailHistoryLength = 4;

    /**
     * 每节点插值分段数，默认 4。
     * <p>
     * Catmull-Rom 样条插值时，每两个原始节点之间插入的分段数。
     * 数值越大，曲线越平滑，但顶点数也越多。
     * 建议范围：2-6。
     * </p>
     */
    public int trailSegmentsPerNode = 4;

    /**
     * 拖尾起始索引，默认 0。
     * <p>
     * 从历史节点的哪个位置开始渲染。用于实现拖尾动态缩短效果。
     * 例如，当 trailTimer 从 10 递减到 0 时，可设置 trailStartIndex = 10 - trailTimer，
     * 使拖尾从尾部开始逐渐消失。
     * </p>
     */
    public int trailStartIndex = 0;

    // ===================== 圆锥拖尾专属参数 =====================

    /**
     * 圆锥拖尾最大半径，默认 0.2。
     * <p>
     * 拖尾头部（进度 0）的截面半径。尾部半径由淡出函数计算。
     * 建议根据实体大小调整，通常为实体半径的 0.5-1.5 倍。
     * </p>
     */
    public float trailMaxRadius = 0.2f;

    /**
     * 圆锥截面正多边形边数，默认 6。
     * <p>
     * 每个节点处绘制的正多边形边数。边数越多，截面越接近圆形。
     * 建议范围：4-16。边数过多会显著增加顶点数。
     * </p>
     */
    public int trailResolution = 6;

    // ===================== 颜色配置 =====================

    /**
     * 基础颜色 RGB，默认红色 (0xFF0000)。
     * <p>
     * 当未设置 trailColorFunction 时使用此颜色。
     * 格式：0xRRGGBB，不包含 Alpha 通道。
     * </p>
     */
    public int trailColorRGB = 0xFF0000;

    /**
     * 颜色随进度变化函数。
     * <p>
     * 参数说明：
     * <ul>
     *   <li>entity - 当前渲染的实体实例</li>
     *   <li>progress - 进度值，0 表示拖尾头部（靠近实体），1 表示尾部</li>
     *   <li>timeShift - 时间偏移量，用于实现动态颜色变化效果</li>
     * </ul>
     * 返回值：RGB 颜色值 (0xRRGGBB)。
     * </p>
     */
    public ColorFunction<T> trailColorFunction = (entity, progress, timeShift) -> trailColorRGB;

    // ===================== 淡出配置 =====================

    /**
     * 淡出函数，控制拖尾从头部到尾部的透明度和半径变化。
     * <p>
     * 参数 progress：0 表示头部，1 表示尾部。
     * 返回值：缩放因子 [0, 1]，1 表示完全不透明，0 表示完全透明。
     * </p>
     * <p>
     * 默认实现：{@code (1 - progress)^1.5}，产生平滑的淡出效果。
     * 其他常用选项：
     * <ul>
     *   <li>线性淡出：{@code progress -> 1 - progress}</li>
     *   <li>二次淡出：{@code progress -> (1 - progress)^2}</li>
     *   <li>先快后慢：{@code progress -> sqrt(1 - progress)}</li>
     * </ul>
     * </p>
     */
    public FadeFunction trailFadeOut = progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);

    // ===================== 丝带拖尾专属参数 =====================

    /**
     * 丝带尖端透明度增强函数。
     * <p>
     * 用于增强丝带拖尾尖端的可见度，使其更加突出。
     * 返回值：透明度乘数，1.0 表示不增强，>1 表示增强。
     * </p>
     * <p>
     * 典型用法：在 progress < 0.3 时返回较高的值，使尖端更亮。
     * </p>
     */
    public AlphaBoostFunction<T> trailTipAlphaBoost = (entity, progress) -> 1.0f;

    /**
     * 丝带尖端亮度增强函数。
     * <p>
     * 用于增强丝带拖尾尖端的颜色亮度。
     * 返回值：亮度乘数，1.0 表示不增强，>1 表示增强。
     * </p>
     * <p>
     * 注意：过高的亮度可能导致颜色溢出（超过 255），会被自动截断。
     * </p>
     */
    public BrightnessBoostFunction<T> trailTipBrightnessBoost = (entity, progress) -> 1.0f;

    // ===================== 本体渲染参数 =====================

    /**
     * 本体模型缩放比例，默认 1.0。
     * <p>
     * 在渲染实体本体前应用的整体缩放。
     * 建议根据物品模型大小调整，使渲染效果与实体碰撞箱匹配。
     * </p>
     */
    public float modelScale = 1.0f;

    /**
     * 本体模型 Yaw 轴旋转偏移（度），默认 0。
     * <p>
     * 在应用实体自身旋转后，额外添加的偏移角度。
     * 用于调整模型朝向，例如使剑尖朝前。
     * </p>
     */
    public float modelYawOffset = 0f;

    /**
     * 本体模型 Pitch 轴旋转偏移（度），默认 0。
     */
    public float modelPitchOffset = 0f;

    /**
     * 本体模型 Roll 轴旋转偏移（度），默认 0。
     */
    public float modelRollOffset = 0f;

    /**
     * 视觉节点插值函数。
     * <p>
     * 用于在渲染时对实体的位置和朝向进行额外处理。
     * 典型用途：
     * <ul>
     *   <li>平滑过渡：在攻击状态和待机状态之间平滑插值</li>
     *   <li>自定义旋转：覆盖实体的默认旋转计算</li>
     * </ul>
     * </p>
     * <p>
     * 默认实现：直接返回原始节点，不进行任何处理。
     * </p>
     */
    public VisualNodeFunction<T> visualNodeFunction = (entity, partialTick, rawNode) -> rawNode;

    // ===================== 函数式接口定义 =====================

    /**
     * 颜色计算函数。
     *
     * @param <T> 实体类型
     */
    @FunctionalInterface
    public interface ColorFunction<T extends AttachmentEntity> {
        /**
         * 计算指定进度处的颜色。
         *
         * @param entity    当前渲染的实体
         * @param progress  进度值 [0, 1]，0=头部，1=尾部
         * @param timeShift 时间偏移量，用于动态颜色效果
         * @return RGB 颜色值 (0xRRGGBB)
         */
        int getColor(T entity, float progress, float timeShift);
    }

    /**
     * 淡出函数。
     */
    @FunctionalInterface
    public interface FadeFunction {
        /**
         * 计算指定进度处的淡出系数。
         *
         * @param progress 进度值 [0, 1]，0=头部，1=尾部
         * @return 淡出系数 [0, 1]，用于缩放半径和透明度
         */
        float getFade(float progress);
    }

    /**
     * 透明度增强函数。
     *
     * @param <T> 实体类型
     */
    @FunctionalInterface
    public interface AlphaBoostFunction<T extends AttachmentEntity> {
        /**
         * 计算指定进度处的透明度增强系数。
         *
         * @param entity   当前渲染的实体
         * @param progress 进度值 [0, 1]
         * @return 透明度乘数，1.0 表示不增强
         */
        float getBoost(T entity, float progress);
    }

    /**
     * 亮度增强函数。
     *
     * @param <T> 实体类型
     */
    @FunctionalInterface
    public interface BrightnessBoostFunction<T extends AttachmentEntity> {
        /**
         * 计算指定进度处的亮度增强系数。
         *
         * @param entity   当前渲染的实体
         * @param progress 进度值 [0, 1]
         * @return 亮度乘数，1.0 表示不增强
         */
        float getBoost(T entity, float progress);
    }

    /**
     * 视觉节点计算函数。
     *
     * @param <T> 实体类型
     */
    @FunctionalInterface
    public interface VisualNodeFunction<T extends AttachmentEntity> {
        /**
         * 计算渲染时使用的视觉节点。
         *
         * @param entity     当前渲染的实体
         * @param partialTick 部分刻时间 [0, 1)，用于帧间插值
         * @param rawNode    原始渲染节点（由渲染调度器计算）
         * @return 处理后的视觉节点
         */
        PathNode getVisualNode(T entity, float partialTick, PathNode rawNode);
    }

    // ===================== 静态工厂方法 =====================

    /**
     * 创建无拖尾的默认渲染上下文。
     * <p>
     * 仅渲染实体本体，不渲染拖尾效果。
     * </p>
     *
     * @param <T> 实体类型
     * @return 新建的渲染上下文
     */
    public static <T extends AttachmentEntity> RenderContext<T> none() {
        return new RenderContext<>();
    }

    /**
     * 创建圆锥拖尾渲染上下文。
     * <p>
     * 圆锥拖尾特点：
     * <ul>
     *   <li>每个节点绘制正多边形截面</li>
     *   <li>截面半径从头部到尾部递减，形成锥形</li>
     *   <li>适合球状、圆形或无明显方向性的实体</li>
     * </ul>
     * </p>
     *
     * @param timer    拖尾计时器值，>0 时显示拖尾
     * @param colorRGB 基础颜色 (0xRRGGBB)
     * @param radius   拖尾头部最大半径
     * @param <T>      实体类型
     * @return 新建的渲染上下文
     */
    public static <T extends AttachmentEntity> RenderContext<T> cone(int timer, int colorRGB, float radius) {
        RenderContext<T> context = new RenderContext<>();
        context.trailType = TrailType.CONE;
        context.trailTimer = timer;
        context.trailColorRGB = colorRGB;
        context.trailMaxRadius = radius;
        return context;
    }

    /**
     * 创建丝带拖尾渲染上下文。
     * <p>
     * 丝带拖尾特点：
     * <ul>
     *   <li>每个节点绘制菱形截面，尖端朝前</li>
     *   <li>四个方向的面片形成十字交叉效果</li>
     *   <li>适合剑状、扁平或有明显方向性的实体</li>
     * </ul>
     * </p>
     *
     * @param timer    拖尾计时器值，>0 时显示拖尾
     * @param colorRGB 基础颜色 (0xRRGGBB)
     * @param <T>      实体类型
     * @return 新建的渲染上下文
     */
    public static <T extends AttachmentEntity> RenderContext<T> ribbon(int timer, int colorRGB) {
        RenderContext<T> context = new RenderContext<>();
        context.trailType = TrailType.RIBBON;
        context.trailTimer = timer;
        context.trailColorRGB = colorRGB;
        return context;
    }

    // ===================== 链式配置方法 =====================

    /**
     * 设置拖尾类型。
     *
     * @param type 拖尾类型
     * @return this，用于链式调用
     */
    public RenderContext<T> trailType(TrailType type) {
        this.trailType = type;
        return this;
    }

    /**
     * 设置拖尾计时器值。
     *
     * @param timer 计时器值
     * @return this，用于链式调用
     */
    public RenderContext<T> trailTimer(int timer) {
        this.trailTimer = timer;
        return this;
    }

    /**
     * 设置历史节点数量。
     *
     * @param length 节点数量
     * @return this，用于链式调用
     */
    public RenderContext<T> trailHistoryLength(int length) {
        this.trailHistoryLength = length;
        return this;
    }

    /**
     * 设置每节点插值分段数。
     *
     * @param segments 分段数
     * @return this，用于链式调用
     */
    public RenderContext<T> trailSegmentsPerNode(int segments) {
        this.trailSegmentsPerNode = segments;
        return this;
    }

    /**
     * 设置拖尾起始索引。
     *
     * @param index 起始索引
     * @return this，用于链式调用
     */
    public RenderContext<T> trailStartIndex(int index) {
        this.trailStartIndex = index;
        return this;
    }

    /**
     * 设置圆锥拖尾最大半径。
     *
     * @param radius 半径值
     * @return this，用于链式调用
     */
    public RenderContext<T> trailMaxRadius(float radius) {
        this.trailMaxRadius = radius;
        return this;
    }

    /**
     * 设置圆锥截面边数。
     *
     * @param resolution 边数
     * @return this，用于链式调用
     */
    public RenderContext<T> trailResolution(int resolution) {
        this.trailResolution = resolution;
        return this;
    }

    /**
     * 设置基础颜色。
     *
     * @param color RGB 颜色值 (0xRRGGBB)
     * @return this，用于链式调用
     */
    public RenderContext<T> trailColorRGB(int color) {
        this.trailColorRGB = color;
        return this;
    }

    /**
     * 设置颜色函数。
     *
     * @param function 颜色计算函数
     * @return this，用于链式调用
     */
    public RenderContext<T> trailColorFunction(ColorFunction<T> function) {
        this.trailColorFunction = function;
        return this;
    }

    /**
     * 设置淡出函数。
     *
     * @param function 淡出计算函数
     * @return this，用于链式调用
     */
    public RenderContext<T> trailFadeOut(FadeFunction function) {
        this.trailFadeOut = function;
        return this;
    }

    /**
     * 设置丝带尖端透明度增强函数。
     *
     * @param function 透明度增强函数
     * @return this，用于链式调用
     */
    public RenderContext<T> trailTipAlphaBoost(AlphaBoostFunction<T> function) {
        this.trailTipAlphaBoost = function;
        return this;
    }

    /**
     * 设置丝带尖端亮度增强函数。
     *
     * @param function 亮度增强函数
     * @return this，用于链式调用
     */
    public RenderContext<T> trailTipBrightnessBoost(BrightnessBoostFunction<T> function) {
        this.trailTipBrightnessBoost = function;
        return this;
    }

    /**
     * 设置本体模型缩放比例。
     *
     * @param scale 缩放比例
     * @return this，用于链式调用
     */
    public RenderContext<T> modelScale(float scale) {
        this.modelScale = scale;
        return this;
    }

    /**
     * 设置本体模型旋转偏移。
     *
     * @param yaw   Yaw 轴偏移（度）
     * @param pitch Pitch 轴偏移（度）
     * @param roll  Roll 轴偏移（度）
     * @return this，用于链式调用
     */
    public RenderContext<T> modelRotationOffset(float yaw, float pitch, float roll) {
        this.modelYawOffset = yaw;
        this.modelPitchOffset = pitch;
        this.modelRollOffset = roll;
        return this;
    }

    /**
     * 设置视觉节点插值函数。
     *
     * @param function 视觉节点计算函数
     * @return this，用于链式调用
     */
    public RenderContext<T> visualNodeFunction(VisualNodeFunction<T> function) {
        this.visualNodeFunction = function;
        return this;
    }
}
