package first.servantry.api.client.render;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntity;
import net.minecraft.client.renderer.RenderType;

/**
 * 渲染上下文，封装附件实体渲染所需的所有参数和配置。
 * <p>
 * 渲染上下文采用链式配置模式，通过链式调用方法设置参数。
 * 子类渲染器只需在 {@code createContext()} 方法中返回配置好的上下文实例，
 * 即可获得完整的拖尾和本体渲染效果。
 * </p>
 *
 * <h2>配置分类</h2>
 * <ul>
 *   <li><b>拖尾类型</b>：无拖尾、圆锥拖尾、丝带拖尾</li>
 *   <li><b>拖尾参数</b>：计时器、历史长度、插值分段、颜色、淡出曲线等</li>
 *   <li><b>本体参数</b>：缩放比例、旋转偏移、视觉节点插值</li>
 * </ul>
 *
 * <h2>拖尾类型选择指南</h2>
 * <pre>{@code
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                        拖尾类型选择决策树                            │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │                                                                     │
 * │   实体形状是什么？                                                   │
 * │   ├── 球状/圆形/无明显方向 ──→ 圆锥拖尾 (CONE)                       │
 * │   │   例如：能量球、魔法弹、火焰球                                    │
 * │   │                                                                 │
 * │   ├── 剑状/扁平/有明显方向 ──→ 丝带拖尾 (RIBBON)                     │
 * │   │   例如：剑刃、刀锋、棱镜                                         │
 * │   │                                                                 │
 * │   └── 不需要拖尾 ──→ 无拖尾 (NONE)                                   │
 * │                                                                     │
 * └─────────────────────────────────────────────────────────────────────┘
 * }</pre>
 *
 * <h2>轨迹效果示例</h2>
 *
 * <h3>圆锥拖尾效果</h3>
 * <pre>{@code
 *     侧视图：                    俯视图：
 *
 *         ╭──────╮                  ☆ ── 正多边形截面
 *        ╱        ╲                 ╲
 *       ╱          ╲                 ╲
 *      │            │                 ☆
 *      │            │                ╱
 *       ╲          ╲                ╱
 *        ╲          ╲              ☆
 *         ╰──────────╯
 *
 *     头部(大) → 尾部(小)         半径随进度递减
 * }</pre>
 *
 * <h3>丝带拖尾效果</h3>
 * <pre>{@code
 *     侧视图（单个三角形截面）：       俯视图（轨迹展开）：
 *
 *            * 尖端                      * ── 尖端（朝前）
 *           /|                          /|
 *          / |                         / |
 *         /  |                        /  |
 *        *---+---* 基部              *---+---* ── 基部
 *       left right                  left   right
 *
 *     参数说明：
 *     - ribbonWidth: 三角形的高（尖端到基部的距离）
 *     - ribbonDiamondSize: 基部宽度（left 到 right 的距离）
 *
 *     轨迹效果：
 *     ═════════════════════════════════════════════════════════
 *     头部 ═══════════════════════════════════════════════ 尾部
 *     (大)                                              (小/淡出)
 *     ═════════════════════════════════════════════════════════
 * }</pre>
 *
 * <h2>完整使用示例</h2>
 *
 * <h3>示例1：能量球（圆锥拖尾）</h3>
 * <pre>{@code
 * @Override
 * protected RenderContext<EnergyBall> createContext(EnergyBall entity) {
 *     return RenderContext.<EnergyBall>cone(entity.getTrailTimer(), 0xFF6600, 0.3f)
 *         .trailHistoryLength(6)           // 6个历史节点
 *         .trailSegmentsPerNode(4)         // 每节点4段插值
 *         .trailResolution(8)              // 8边形截面
 *         .trailShaderType(ShaderType.ADDITIVE)  // 加法混合发光
 *         .trailColorFunction((e, progress, time) -> {
 *             // 从橙色渐变到黄色
 *             int r = 255;
 *             int g = (int) (102 + progress * 153);  // 102 → 255
 *             int b = 0;
 *             return (r << 16) | (g << 8) | b;
 *         })
 *         .trailFadeOut(p -> (1 - p) * (1 - p))  // 二次淡出
 *         .modelScale(0.8f);
 * }
 * }</pre>
 *
 * <h3>示例2：剑刃（丝带拖尾）</h3>
 * <pre>{@code
 * @Override
 * protected RenderContext<SwordEntity> createContext(SwordEntity entity) {
 *     return RenderContext.<SwordEntity>ribbon(entity.getTrailTimer(), 0x88CCFF)
 *         .trailHistoryLength(8)           // 更长的轨迹
 *         .ribbonWidth(0.5f)               // 三角形高度
 *         .ribbonDiamondSize(0.25f)        // 基部宽度
 *         .trailShaderType(ShaderType.UNLIT)  // 光影兼容
 *         .trailColorFunction((e, progress, time) -> {
 *             // 从亮蓝渐变到暗蓝
 *             float brightness = 1.0f - progress * 0.6f;
 *             int r = (int) (0x88 * brightness);
 *             int g = (int) (0xCC * brightness);
 *             int b = (int) (0xFF * brightness);
 *             return (r << 16) | (g << 8) | b;
 *         })
 *         .trailTipAlphaBoost((e, p) -> p < 0.2f ? 2.0f : 1.0f)  // 尖端更亮
 *         .trailTipBrightnessBoost((e, p) -> p < 0.15f ? 1.3f : 1.0f)
 *         .modelRotationOffset(0, 90, 0)   // 调整模型朝向
 *         .modelScale(1.0f);
 * }
 * }</pre>
 *
 * <h3>示例3：动态缩短的拖尾</h3>
 * <pre>{@code
 * // 当 trailTimer 从 10 递减到 0 时，拖尾从尾部逐渐消失
 * @Override
 * protected RenderContext<MyEntity> createContext(MyEntity entity) {
 *     int timer = entity.getTrailTimer();  // 假设从 10 递减
 *     return RenderContext.<MyEntity>cone(timer, 0xFF0000, 0.2f)
 *         .trailHistoryLength(10)           // 最大历史长度
 *         .trailStartIndex(10 - timer)      // 动态起始索引
 *         // 当 timer=10: startIndex=0, 显示完整拖尾
 *         // 当 timer=5:  startIndex=5, 显示后半段
 *         // 当 timer=0:  startIndex=10, 不显示
 *         ;
 * }
 * }</pre>
 *
 * <h2>参数调优建议</h2>
 * <table border="1">
 *   <tr><th>参数</th><th>范围</th><th>效果</th></tr>
 *   <tr><td>trailHistoryLength</td><td>3-12</td><td>越大轨迹越长，但开销越大</td></tr>
 *   <tr><td>trailSegmentsPerNode</td><td>2-8</td><td>越大曲线越平滑，顶点越多</td></tr>
 *   <tr><td>trailResolution (圆锥)</td><td>4-16</td><td>截面边数，越多越圆</td></tr>
 *   <tr><td>trailMaxRadius (圆锥)</td><td>0.1-1.0</td><td>头部最大半径</td></tr>
 *   <tr><td>ribbonWidth (丝带)</td><td>0.05-1.0</td><td>三角形高度（尖锐程度）</td></tr>
 *   <tr><td>ribbonDiamondSize (丝带)</td><td>0.1-1.0</td><td>三角形宽度</td></tr>
 * </table>
 *
 * @param <T> 附件实体类型
 * @see AttachmentEntity
 * @see IAttachmentEntityRenderer
 * @see AbstractAttachmentEntityRenderer
 */
public class RenderContext<T extends AttachmentEntity> {

    // ===================== 拖尾类型枚举 =====================

    /**
     * 拖尾渲染类型。
     * <p>
     * 选择指南：
     * </p>
     * <pre>{@code
     * ┌─────────────┬────────────────────────────────────────────┐
     * │ 类型        │ 适用场景                                     │
     * ├─────────────┼────────────────────────────────────────────┤
     * │ NONE        │ 不需要拖尾效果                               │
     * │ CONE        │ 球状、圆形、能量弹、魔法球                    │
     * │ RIBBON      │ 剑状、刀锋、扁平物体、有明显方向性的实体       │
     * └─────────────┴────────────────────────────────────────────┘
     * }</pre>
     */
    public enum TrailType {
        /** 无拖尾效果，仅渲染实体本体 */
        NONE,
        /**
         * 圆锥拖尾：每个节点绘制正多边形截面，半径随进度递减形成锥形。
         * <pre>{@code
         *     效果示意：
         *         ╭──╮
         *        ╱    ╲
         *       │      │
         *        ╲    ╱
         *         ╰──╯
         *     头→尾
         * }</pre>
         */
        CONE,
        /**
         * 丝带拖尾：每个节点绘制三角形截面，尖端朝前。
         * <pre>{@code
         *     效果示意：
         *            *
         *           /|\
         *          / | \
         *         *--+--*
         *        左 尖端 右
         *     尖端指向运动方向
         * }</pre>
         */
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

    /**
     * 拖尾着色器类型，默认 {@link ShaderType#STANDARD}。
     * <p>
     * 控制拖尾使用的着色器和混合模式，影响光影模组兼容性。
     * </p>
     * <pre>{@code
     * ┌───────────┬─────────────────────────────────────────────┐
     * │ 类型      │ 特点                                        │
     * ├───────────┼─────────────────────────────────────────────┤
     * │ STANDARD  │ 标准透明，适合大多数情况                     │
     * │ UNLIT     │ 无光照透明，光影兼容性更好，适合发光物体      │
     * │ ADDITIVE  │ 加法混合，颜色叠加，适合火焰、能量效果        │
     * └───────────┴─────────────────────────────────────────────┘
     * }</pre>
     */
    public ShaderType trailShaderType = ShaderType.STANDARD;

    /**
     * 拖尾着色器类型枚举。
     */
    public enum ShaderType {
        /** 标准透明着色器，使用标准透明度混合 */
        STANDARD,
        /** 无光照透明着色器，禁用深度写入，光影兼容性更好 */
        UNLIT,
        /** 加法混合着色器，颜色叠加效果，适合发光拖尾 */
        ADDITIVE
    }

    // ===================== 拖尾基础参数 =====================

    /**
     * 拖尾计时器值。
     * <p>
     * 控制拖尾的显示时长。当值 > 0 时才会渲染拖尾。
     * 通常与实体内部的 trailTimer 字段同步，实现攻击时显示拖尾、静止时隐藏的效果。
     * </p>
     * <pre>{@code
     * // 典型用法：
     * // 在实体 tick() 中：
     * if (attacking) {
     *     trailTimer = 15;  // 攻击时设置拖尾时长
     * } else {
     *     trailTimer--;     // 非攻击时递减
     * }
     * }</pre>
     */
    public int trailTimer = 0;

    /**
     * 历史节点数量，默认 4。
     * <p>
     * 从历史记录中取出的节点数量，直接影响拖尾的视觉长度。
     * </p>
     * <pre>{@code
     * 历史节点数量与轨迹长度关系：
     *
     * historyLength=4:    *--*--*--*--*    (短轨迹)
     * historyLength=8:    *--*--*--*--*--*--*--*--*    (长轨迹)
     *
     * 注意：
     * - 数值越大，拖尾越长，但渲染开销也越大
     * - 建议根据实体移动速度调整：快速移动用较大值，慢速用较小值
     * - 建议范围：3-12
     * }</pre>
     */
    public int trailHistoryLength = 4;

    /**
     * 每节点插值分段数，默认 4。
     * <p>
     * Catmull-Rom 样条插值时，每两个原始节点之间插入的分段数。
     * </p>
     * <pre>{@code
     * 插值效果对比：
     *
     * segmentsPerNode=2:  *---*---*---*    (折线感明显)
     * segmentsPerNode=4:  *-*-*-*-*-*-*    (较平滑)
     * segmentsPerNode=8:  ~~~~~~~~~~~~     (非常平滑)
     *
     * 建议范围：2-8
     * }</pre>
     */
    public int trailSegmentsPerNode = 4;

    /**
     * 拖尾起始索引，默认 0。
     * <p>
     * 从历史节点的哪个位置开始渲染。用于实现拖尾动态缩短效果。
     * </p>
     * <pre>{@code
     * 动态缩短示例：
     *
     * 假设 historyLength=10, timer 从 10 递减到 0
     *
     * timer=10, startIndex=0:
     *   ═════════════════════════════════════
     *   |完整轨迹显示|
     *
     * timer=5, startIndex=5:
     *   ═════════════════════════════════════
     *           |后半段显示|
     *
     * timer=0, startIndex=10:
     *   ═════════════════════════════════════
     *                                   (不显示)
     *
     * 实现代码：
     * .trailStartIndex(maxTimer - currentTimer)
     * }</pre>
     */
    public int trailStartIndex = 0;

    // ===================== 圆锥拖尾专属参数 =====================

    /**
     * 圆锥拖尾最大半径，默认 0.2。
     * <p>
     * 拖尾头部（进度 0）的截面半径。尾部半径由淡出函数计算。
     * </p>
     * <pre>{@code
     * 半径效果：
     *
     * radius=0.1:    ╭╮    (细小)
     *                ││
     *                ╰╯
     *
     * radius=0.3:    ╭──╮  (粗大)
     *               │    │
     *                ╰──╯
     *
     * 建议根据实体大小调整，通常为实体半径的 0.5-1.5 倍
     * }</pre>
     */
    public float trailMaxRadius = 0.2f;

    /**
     * 圆锥截面正多边形边数，默认 6。
     * <p>
     * 每个节点处绘制的正多边形边数。边数越多，截面越接近圆形。
     * </p>
     * <pre>{@code
     * 边数效果：
     *
     * resolution=4:   ◇    (菱形)
     * resolution=6:   ⬡    (六边形)
     * resolution=8:   ⯃    (八边形，接近圆)
     *
     * 建议范围：4-16。边数过多会显著增加顶点数。
     * }</pre>
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
     *   <li>timeShift - 时间偏移量，用于实现动态颜色变化效果（如彩虹渐变）</li>
     * </ul>
     * 返回值：RGB 颜色值 (0xRRGGBB)。
     * </p>
     * <pre>{@code
     * // 示例1：从头到尾渐变（红→黄）
     * .trailColorFunction((e, progress, time) -> {
     *     int r = 255;
     *     int g = (int) (progress * 255);
     *     int b = 0;
     *     return (r << 16) | (g << 8) | b;
     * })
     *
     * // 示例2：彩虹渐变（使用 timeShift）
     * .trailColorFunction((e, progress, time) -> {
     *     float hue = (time + progress * 0.3f) % 1.0f;
     *     return Mth.hsvToRgb(hue, 0.8f, 1.0f);
     * })
     *
     * // 示例3：固定颜色
     * .trailColorFunction((e, progress, time) -> 0x00FF00)
     * }</pre>
     */
    public ColorFunction<T> trailColorFunction = (entity, progress, timeShift) -> trailColorRGB;

    // ===================== 淡出配置 =====================

    /**
     * 淡出函数，控制拖尾从头部到尾部的透明度和半径变化。
     * <p>
     * 参数 progress：0 表示头部，1 表示尾部。
     * 返回值：缩放因子 [0, 1]，1 表示完全不透明，0 表示完全透明。
     * </p>
     * <pre>{@code
     * 常用淡出曲线对比：
     *
     * progress:  0.0   0.25   0.5   0.75   1.0
     *
     * 线性:      1.0 ─────────────────────── 0.0
     *            (1 - progress)
     *
     * 二次:      1.0 ────────╮
     *                        ╰───────────── 0.0
     *            ((1 - progress)²)
     *
     * 1.5次:     1.0 ──────╮
     *                      ╰────────────── 0.0
     *            ((1 - progress)^1.5)  [默认]
     *
     * 先快后慢:  1.0 ─╮
     *                 ╰────────────────── 0.0
     *            (sqrt(1 - progress))
     *
     * 代码示例：
     * .trailFadeOut(p -> (1 - p) * (1 - p))           // 二次淡出
     * .trailFadeOut(p -> (float) Math.sqrt(1 - p))    // 先快后慢
     * .trailFadeOut(p -> 1 - p)                       // 线性淡出
     * }</pre>
     */
    public FadeFunction trailFadeOut = progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);

    // ===================== 丝带拖尾专属参数 =====================

    /**
     * 丝带宽度（三角形的高），默认 0.15。
     * <p>
     * 控制丝带三角形从尖端到基部的距离（Z方向）。
     * </p>
     * <pre>{@code
     * 三角形截面结构：
     *
     *            * 尖端
     *           /|\
     *          / | \  ribbonWidth (高)
     *         /  |  \
     *        *---+---*
     *       左  基部  右
     *
     * width=0.1:    *      (短小尖锐)
     *              /|
     *             *-*
     *
     * width=0.5:    *      (长而尖锐)
     *              /|
     *             / |
     *            *--*
     *
     * 建议范围：0.05-1.0
     * }</pre>
     */
    public float ribbonWidth = 0.15f;

    /**
     * 丝带棱形大小（三角形底边长度），默认 0.3。
     * <p>
     * 控制丝带三角形底边的宽度（左右方向）。
     * </p>
     * <pre>{@code
     * 三角形截面结构：
     *
     *            *
     *           /|\
     *          / | \
     *         *--+--*
     *         ←─→
     *    ribbonDiamondSize (底边长度)
     *
     * diamondSize=0.1:    *    (窄)
     *                    /|\
     *                   *-*-*
     *
     * diamondSize=0.5:    *    (宽)
     *                    /|\
     *                   / | \
     *                  *--+--*
     *
     * 建议范围：0.1-1.0
     * }</pre>
     */
    public float ribbonDiamondSize = 0.3f;

    /**
     * 丝带尖端透明度增强函数。
     * <p>
     * 用于增强丝带拖尾尖端的可见度，使其更加突出。
     * 返回值：透明度乘数，1.0 表示不增强，>1 表示增强。
     * </p>
     * <pre>{@code
     * // 示例：尖端前30%增强2倍透明度
     * .trailTipAlphaBoost((e, progress) -> {
     *     if (progress < 0.3f) {
     *         return 2.0f;  // 尖端区域增强
     *     }
     *     return 1.0f;      // 其他区域不增强
     * })
     *
     * // 渐变增强
     * .trailTipAlphaBoost((e, progress) -> {
     *     if (progress < 0.2f) {
     *         return Mth.lerp(progress / 0.2f, 3.0f, 1.0f);
     *     }
     *     return 1.0f;
     * })
     * }</pre>
     */
    public AlphaBoostFunction<T> trailTipAlphaBoost = (entity, progress) -> 1.0f;

    /**
     * 丝带尖端亮度增强函数。
     * <p>
     * 用于增强丝带拖尾尖端的颜色亮度。
     * 返回值：亮度乘数，1.0 表示不增强，>1 表示增强。
     * </p>
     * <pre>{@code
     * // 示例：尖端前25%增强1.5倍亮度
     * .trailTipBrightnessBoost((e, progress) -> {
     *     if (progress < 0.25f) {
     *         return 1.5f;
     *     }
     *     return 1.0f;
     * })
     * }</pre>
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
     * </p>
     * <pre>{@code
     * 典型用途：
     *
     * 1. 攻击/待机状态平滑过渡：
     *    .visualNodeFunction((entity, partialTick, rawNode) -> {
     *        float blend = entity.getBlendFactor(partialTick);
     *        PathNode idleNode = entity.getIdleNode(partialTick);
     *        return rawNode.lerp(idleNode, blend);
     *    })
     *
     * 2. 自定义旋转：
     *    .visualNodeFunction((entity, partialTick, rawNode) -> {
     *        return new PathNode(rawNode.pos(), rawNode.yaw() + 45, rawNode.pitch(), rawNode.roll());
     *    })
     * }</pre>
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
     * </p>
     * <pre>{@code
     *     侧视图：
     *         ╭──╮
     *        ╱    ╲
     *       │      │
     *        ╲    ╱
     *         ╰──╯
     *     头→尾
     *
     * 适合：球状、圆形、能量弹、魔法球
     * }</pre>
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
     * </p>
     * <pre>{@code
     *     三角形截面：
     *            * 尖端（朝前）
     *           /|\
     *          / | \
     *         *--+--*
     *        左  基部  右
     *
     * 适合：剑状、刀锋、扁平物体、有明显方向性的实体
     * }</pre>
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
     * 设置拖尾着色器类型。
     *
     * @param type 着色器类型
     * @return this，用于链式调用
     */
    public RenderContext<T> trailShaderType(ShaderType type) {
        this.trailShaderType = type;
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
     * 设置丝带宽度（三角形高度）。
     *
     * @param width 宽度值
     * @return this，用于链式调用
     */
    public RenderContext<T> ribbonWidth(float width) {
        this.ribbonWidth = width;
        return this;
    }

    /**
     * 设置丝带棱形大小（三角形底边长度）。
     *
     * @param size 棱形大小值
     * @return this，用于链式调用
     */
    public RenderContext<T> ribbonDiamondSize(float size) {
        this.ribbonDiamondSize = size;
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
