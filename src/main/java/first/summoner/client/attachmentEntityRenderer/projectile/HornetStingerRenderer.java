package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.lyra.client.render.*;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.projectile.HornetStinger;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * 黄蜂毒刺渲染器。
 * <p>
 * 不使用实体渲染器、也不使用旧的静态 JSON 模型：直接以两片 0 厚度面片（十字薄片）自绘，
 * 几何与 UV 对齐 Confluence 的 {@code HornetStingerProjectileModel}（{@code texOffs(0, 10)} +
 * {@code addBox(0, 4, -7, 0, 6, 10)} 与 {@code texOffs(0, 0)} + {@code addBox(-3, 7, -7, 6, 0, 10)}，
 * 贴图 32x32；1 像素 = 1/16 方块）：
 * </p>
 * <ul>
 *     <li>竖直薄片：x=0 平面，y∈[4,10]，z∈[-7,3]；取 texOffs(0,10) 盒子的 -X 面，UV = u∈[0,10]，v∈[20,26]</li>
 *     <li>水平薄片：y=7 平面，x∈[-3,3]，z∈[-7,3]；取 texOffs(0,0) 盒子的 +Y 面，UV = u∈[10,16]，v∈[0,10]</li>
 * </ul>
 * <p>
 * 两片均为双面渲染（正面 + 反面，反面逆序且法线取反）。
 * </p>
 */
public class HornetStingerRenderer extends AbstractAttachmentEntityRenderer<HornetStinger> {

    /** 毒刺贴图（32x32，路径含扩展名，直接对应 {@code assets/summoner/lyra_model/json/projectile/hornet_stinger/stinger.png}）。 */
    private static final ResourceLocation TEXTURE = Summoner.rl("lyra_model/json/projectile/hornet_stinger/stinger.png");
    /** 模型单位：1 像素 = 1/16 方块。 */
    private static final float PX = 1.0F / 16.0F;
    /** 贴图单位：1 像素。 */
    private static final float TEXEL = 1.0F / 32.0F;

    /** 竖直薄片顶点：{x, y, z, u, v}，按 +X 法线逆时针排列。 */
    private static final float[][] VERTICAL_SHEET = {
            {0.0F, 4.0F * PX, 3.0F * PX, 10.0F * TEXEL, 20.0F * TEXEL},
            {0.0F, 4.0F * PX, -7.0F * PX, 0.0F, 20.0F * TEXEL},
            {0.0F, 10.0F * PX, -7.0F * PX, 0.0F, 26.0F * TEXEL},
            {0.0F, 10.0F * PX, 3.0F * PX, 10.0F * TEXEL, 26.0F * TEXEL}
    };

    /** 水平薄片顶点：{x, y, z, u, v}，按 +Y 法线逆时针排列。 */
    private static final float[][] HORIZONTAL_SHEET = {
            {-3.0F * PX, 7.0F * PX, -7.0F * PX, 10.0F * TEXEL, 0.0F},
            {-3.0F * PX, 7.0F * PX, 3.0F * PX, 10.0F * TEXEL, 10.0F * TEXEL},
            {3.0F * PX, 7.0F * PX, 3.0F * PX, 16.0F * TEXEL, 10.0F * TEXEL},
            {3.0F * PX, 7.0F * PX, -7.0F * PX, 16.0F * TEXEL, 0.0F}
    };

    @Override
    protected RenderContext<HornetStinger> createContext(HornetStinger stinger, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(stinger, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.6F)
                               .translateOffset(0, -0.45F, 0.3F)
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5F));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer consumer = bufferSource.getBuffer(LyraRenderTypes.texture(TEXTURE, false));
        Matrix4f pose = poseStack.last().pose();
        int color = context.color.argbInt();
        // 竖直薄片（法线 ±X）
        emitDoubleSidedQuad(consumer, pose, VERTICAL_SHEET, color, 1.0F, 0.0F, 0.0F);
        // 水平薄片（法线 ±Y）
        emitDoubleSidedQuad(consumer, pose, HORIZONTAL_SHEET, color, 0.0F, 1.0F, 0.0F);
    }

    /** 提交一片双面四边形：正面按给定法线顺序，反面逆序输出并取反法线。 */
    private static void emitDoubleSidedQuad(VertexConsumer consumer, Matrix4f pose, float[][] sheet, int color, float normalX, float normalY, float normalZ) {
        for (float[] vertex : sheet) {
            emitVertex(consumer, pose, vertex, color, normalX, normalY, normalZ);
        }
        for (int i = sheet.length - 1; i >= 0; i--) {
            emitVertex(consumer, pose, sheet[i], color, -normalX, -normalY, -normalZ);
        }
    }

    /** 提交单个顶点（NEW_ENTITY 格式，全亮）。 */
    private static void emitVertex(VertexConsumer consumer, Matrix4f pose, float[] vertex, int color, float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, vertex[0], vertex[1], vertex[2])
                .setColor(color)
                .setUv(vertex[3], vertex[4])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(RenderUtil.FULL_LIGHT)
                .setNormal(normalX, normalY, normalZ);
    }
}
