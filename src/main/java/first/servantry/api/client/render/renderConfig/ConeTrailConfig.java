package first.servantry.api.client.render.renderConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.PathNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * 圆锥拖尾配置。
 * <p>
 * 适用于球状、圆形、能量弹、魔法球等实体。
 * </p>
 * <pre>{@code
 * 效果示意：
 *     ╭──╮
 *    ╱    ╲
 *   │      │
 *    ╲    ╱
 *     ╰──╯
 * 头→尾
 * }</pre>
 *
 * @param <T> 实体类型
 */
public class ConeTrailConfig<T extends AttachmentEntity> extends TrailConfig<T, ConeTrailConfig<T>> {

    /**
     * 拖尾头部最大半径
     */
    public float maxRadius = 0.2f;

    /** 最小半径比例，控制尾端不会完全缩成一点 */
    public float minRadiusRatio = 0.0f;

    /** 圆锥截面正多边形边数 */
    public int resolution = 6;

    public ConeTrailConfig<T> maxRadius(float radius) {
        this.maxRadius = radius;
        return this;
    }

    public ConeTrailConfig<T> minRadiusRatio(float ratio) {
        this.minRadiusRatio = ratio;
        return this;
    }

    public ConeTrailConfig<T> resolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    @Override
    public void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource,
                       float partialTick, PathNode visualNode, RenderType renderType) {
        List<InterpolatedNode> smoothNodes = buildSmoothNodes(entity, visualNode);
        if (smoothNodes.size() < 2) return;

        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Matrix4f pose = poseStack.last().pose();
        float[] cosArr = getCosArray(resolution);
        float[] sinArr = getSinArray(resolution);

        Player owner = entity.getOwner();
        float timeShift = owner != null ? (owner.tickCount + partialTick) * 0.015f : 0f;

        int nodeCount = smoothNodes.size();
        Vec3 renderPos = visualNode.pos();

        Vector3f currV1 = new Vector3f(), currV2 = new Vector3f();
        Vector3f prevV1 = new Vector3f(), prevV2 = new Vector3f();

        for (int i = 0; i < nodeCount - 1; i++) {
            InterpolatedNode curr = smoothNodes.get(i);
            InterpolatedNode prev = smoothNodes.get(i + 1);

            float currProgress = (float) i / (nodeCount - 1);
            float prevProgress = (float) (i + 1) / (nodeCount - 1);

            float currFade = fadeOut.getFade(currProgress);
            float prevFade = fadeOut.getFade(prevProgress);
            float currRadius = maxRadius * (minRadiusRatio + (1 - minRadiusRatio) * currFade);
            float prevRadius = maxRadius * (minRadiusRatio + (1 - minRadiusRatio) * prevFade);

            int currColor = colorFunction.getColor(entity, currProgress, timeShift);
            int prevColor = colorFunction.getColor(entity, prevProgress, timeShift);

            int currAlpha = Math.round(currFade * 200);
            int prevAlpha = Math.round(prevFade * 200);
            int currARGB = FastColor.ARGB32.color(currAlpha, (currColor >> 16) & 0xFF, (currColor >> 8) & 0xFF, currColor & 0xFF);
            int prevARGB = FastColor.ARGB32.color(prevAlpha, (prevColor >> 16) & 0xFF, (prevColor >> 8) & 0xFF, prevColor & 0xFF);

            Vec3 currRel = curr.pos().subtract(renderPos);
            Vec3 prevRel = prev.pos().subtract(renderPos);

            for (int j = 0; j < resolution; j++) {
                float cos1 = cosArr[j], sin1 = sinArr[j];
                float cos2 = cosArr[j + 1], sin2 = sinArr[j + 1];

                currV1.set(cos1 * currRadius, sin1 * currRadius, 0).rotate(curr.rot());
                currV2.set(cos2 * currRadius, sin2 * currRadius, 0).rotate(curr.rot());
                prevV1.set(cos1 * prevRadius, sin1 * prevRadius, 0).rotate(prev.rot());
                prevV2.set(cos2 * prevRadius, sin2 * prevRadius, 0).rotate(prev.rot());

                emitQuad(consumer, pose,
                        (float) currRel.x + currV1.x, (float) currRel.y + currV1.y, (float) currRel.z + currV1.z, currARGB,
                        (float) currRel.x + currV2.x, (float) currRel.y + currV2.y, (float) currRel.z + currV2.z, currARGB,
                        (float) prevRel.x + prevV2.x, (float) prevRel.y + prevV2.y, (float) prevRel.z + prevV2.z, prevARGB,
                        (float) prevRel.x + prevV1.x, (float) prevRel.y + prevV1.y, (float) prevRel.z + prevV1.z, prevARGB);
            }
        }
    }
}
