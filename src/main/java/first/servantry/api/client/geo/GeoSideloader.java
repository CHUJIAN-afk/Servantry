package first.servantry.api.client.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MolangQueries;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Geo 外挂渲染器（单例），独立于 GeckoLib 的 Entity/Item/BlockEntity 渲染体系。
 * <p>
 * 设计原则与 {@code GeoEntityRenderer} 一致：渲染器是单例，状态在每帧渲染后重置。
 * 通过向 {@link DummyGeoAnimatable} 注入伪 tick 值来精确控制动画进度，
 * 而非依赖游戏实体的真实时间流逝。
 * <p>
 * 使用方式：
 * <pre>{@code
 * // 1. 获取单例（按 ResourceLocation 缓存）
 * GeoSideloader sideloader = GeoSideloader.getGeoSideloader(
 *     ResourceLocation.fromNamespaceAndPath("servantry", "test_boss"));
 *
 * // 2. 每帧设置动画与进度后渲染
 * sideloader.setAnimation("attack_1", 0.5f);
 * sideloader.render(poseStack, bufferSource, partialTick, packedLight);
 * }</pre>
 */
public class GeoSideloader implements GeoRenderer<DummyGeoAnimatable> {

    /**
     * 按 ResourceLocation 缓存的 Sideloader 单例池
     */
    private static final Map<ResourceLocation, GeoSideloader> CACHE = new HashMap<>();
    /**
     * 当前使用的 Geo 模型定义
     */
    private final GeoAttachmentModel geoModel;
    /**
     * 空 Animatable 壳，承载动画控制器与注入 tick
     */
    private final DummyGeoAnimatable dummyAnimatable;
    /**
     * 当前帧要播放的动画名，渲染后自动重置为 null
     */
    private String currentAnimationName;
    /**
     * 当前帧动画进度 [0,1]，渲染后自动重置为 0
     */
    private float progress = 0f;

    private GeoSideloader(GeoAttachmentModel geoModel) {
        this.geoModel = geoModel;
        this.dummyAnimatable = new DummyGeoAnimatable();
    }

    /**
     * 获取或创建指定资源位置的 Sideloader 单例。
     *
     * @param location 模型资源定位，命名空间+路径对应 geo/texture/animation 文件
     * @return 与该 location 绑定的 Sideloader 实例
     */
    public static GeoSideloader getGeoSideloader(ResourceLocation location) {
        return CACHE.computeIfAbsent(location, k -> new GeoSideloader(new GeoAttachmentModel(location)));
    }

    // ===================== 外挂 API =====================

    /**
     * 设置当前帧要播放的动画及进度。
     * 必须在 {@link #render} 之前调用，每帧结束后状态自动重置。
     *
     * @param animationName animation.json 中定义的动画名称
     * @param progress      动画进度，0=起始 1=结束（循环动画会自动循环）
     */
    public void setAnimation(String animationName, float progress) {
        this.currentAnimationName = animationName;
        this.progress = progress;
    }

    /**
     * 执行一帧渲染，流程：
     * <ol>
     *   <li>将动画名注入 Controller，将进度注入 DummyAnimatable 的 tick</li>
     *   <li>构建 AnimationState 并驱动模型动画计算</li>
     *   <li>使用 GeoRenderer 默认流程绘制模型</li>
     *   <li>清理 Molang 上下文，重置本帧状态</li>
     * </ol>
     */
    @SuppressWarnings("all")
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        DummyGeoAnimatable animatable = getAnimatable();
        ResourceLocation texture = getTextureLocation(animatable);
        RenderType renderType = getRenderType(animatable, texture, bufferSource, partialTick);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        poseStack.pushPose();

        // 注入动画名与进度
        double tickValue = progress;
        if (currentAnimationName != null) {
            RawAnimation rawAnimation = RawAnimation.begin().then(currentAnimationName, Animation.LoopType.LOOP);
            animatable.getController().setAnimation(rawAnimation);
        }
        animatable.setInjectedTick(tickValue);

        // 驱动动画计算
        AnimationState<DummyGeoAnimatable> animationState = new AnimationState<>(animatable, 0, 0, partialTick, false);
        animationState.setData(DataTickets.TICK, tickValue);
        GeoModel<DummyGeoAnimatable> model = getGeoModel();
        model.handleAnimations(animatable, 0, animationState, partialTick);

        // 绘制模型
        BakedGeoModel bakedModel = getGeoModel().getBakedModel(getGeoModel().getModelResource(getAnimatable(), this));
        GeoRenderer.super.actuallyRender(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, false, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        // 清理
        MolangQueries.clearActor();
        this.currentAnimationName = null;
        this.progress = 0;
    }

    // ===================== GeoRenderer 接口实现 =====================

    /**
     * 禁用默认 actuallyRender，渲染逻辑由 {@link #render} 自行管理
     */
    @Override
    public void actuallyRender(PoseStack poseStack, DummyGeoAnimatable animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
    }

    @Override
    public GeoModel<DummyGeoAnimatable> getGeoModel() {
        return this.geoModel;
    }

    @Override
    public DummyGeoAnimatable getAnimatable() {
        return this.dummyAnimatable;
    }

    @Override
    public @NotNull RenderType getRenderType(DummyGeoAnimatable animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void fireCompileRenderLayersEvent() {
    }

    @Override
    public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return true;
    }

    @Override
    public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
    }

    @Override
    public void updateAnimatedTextureFrame(DummyGeoAnimatable animatable) {
    }
}
