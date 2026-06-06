package first.servantry.api.client.render.renderConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.PathNode;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拖尾渲染配置基类。
 * <p>
 * 使用模板方法模式，将渲染逻辑封装在配置类中，子类实现具体渲染。
 * </p>
 *
 * @param <T>    实体类型
 * @param <SELF> 配置类自身类型（用于链式调用）
 */
public abstract class TrailConfig<T extends AttachmentEntity, SELF extends TrailConfig<T, SELF>> {

    // ===================== 基础参数 =====================

    /**
     * 拖尾计时器值，>0 时显示拖尾
     */
    public int timer = 0;

    /**
     * 圆形顶点缓存
     */
    protected static final Map<Integer, float[]> COS_CACHE = new HashMap<>();
    protected static final Map<Integer, float[]> SIN_CACHE = new HashMap<>();
    /** 历史节点数量，默认 4 */
    public int historyLength = 4;

    // ===================== 颜色配置 =====================
    /** 每节点插值分段数，默认 8 */
    public int segmentsPerNode = 8;
    /** 拖尾起始索引，默认 0 */
    public int startIndex = 0;
    /** 基础颜色 RGB */
    public int colorRGB = 0xFF0000;

    // ===================== 链式配置方法 =====================

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    public SELF timer(int timer) {
        this.timer = timer;
        return self();
    }

    public SELF historyLength(int length) {
        this.historyLength = length;
        return self();
    }

    public SELF segmentsPerNode(int segments) {
        this.segmentsPerNode = segments;
        return self();
    }

    public SELF startIndex(int index) {
        this.startIndex = index;
        return self();
    }

    public SELF colorRGB(int color) {
        this.colorRGB = color;
        return self();
    }

    public SELF colorFunction(RenderContext.ColorFunction<T> function) {
        this.colorFunction = function;
        return self();
    }

    public SELF fadeOut(RenderContext.FadeFunction function) {
        this.fadeOut = function;
        return self();
    }

    // ===================== 抽象渲染方法 =====================
    /** 颜色函数 */
    public RenderContext.ColorFunction<T> colorFunction = (entity, progress, timeShift) -> colorRGB;

    // ===================== 共享渲染工具 =====================
    /** 淡出函数 */
    public RenderContext.FadeFunction fadeOut = progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 1.5);

    protected static float[] getCosArray(int resolution) {
        return COS_CACHE.computeIfAbsent(resolution, r -> {
            float[] arr = new float[r + 1];
            float delta = (float) (2.0 * Math.PI / r);
            for (int i = 0; i <= r; i++) {
                arr[i] = (float) Math.cos(i * delta);
            }
            return arr;
        });
    }

    protected static float[] getSinArray(int resolution) {
        return SIN_CACHE.computeIfAbsent(resolution, r -> {
            float[] arr = new float[r + 1];
            float delta = (float) (2.0 * Math.PI / r);
            for (int i = 0; i <= r; i++) {
                arr[i] = (float) Math.sin(i * delta);
            }
            return arr;
        });
    }

    /**
     * 渲染拖尾。
     *
     * @param entity       实体
     * @param poseStack    姿态栈
     * @param bufferSource 缓冲源
     * @param partialTick  部分刻
     * @param visualNode   视觉节点
     * @param renderType   渲染类型
     */
    public abstract void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource,
                                float partialTick, PathNode visualNode, RenderType renderType);

    /**
     * 构建平滑节点列表。
     * <p>
     * 对末端节点（最旧历史节点）使用 partialTick 向前一个节点插值，
     * 使末端在帧间平滑移动，消除逻辑帧边界导致的末端突变。
     * 其余节点保持原始值不变。
     * </p>
     */
    protected List<InterpolatedNode> buildSmoothNodes(T entity, PathNode visualNode, float partialTick) {
        ArrayList<PathNode> history = new ArrayList<>(entity.getHistoryNodes());
        history.set(0, visualNode);
        int actualLength = Math.min(history.size(), historyLength);
        if (actualLength < 2) {
            return List.of();
        }
        PathNode[] nodes = new PathNode[actualLength];
        for (int i = 0; i < actualLength; i++) {
            nodes[i] = history.get(i).lerp(history.get(Math.max(0, i - 1)), partialTick);
        }
        int endIndex = nodes.length - 1;
        int startIdx = Math.max(0, Math.min(startIndex, endIndex - 1));

        List<InterpolatedNode> result = new ArrayList<>((endIndex - startIdx) * segmentsPerNode + 1);
        Quaternionf tempQuat = new Quaternionf();

        for (int i = startIdx; i < endIndex; i++) {
            PathNode p0 = nodes[Math.max(i - 1, startIdx)];
            PathNode p1 = nodes[i];
            PathNode p2 = nodes[i + 1];
            PathNode p3 = nodes[Math.min(i + 2, endIndex)];

            Quaternionf q1 = eulerToQuaternion(p1.yaw(), p1.pitch(), p1.roll());
            Quaternionf q2 = eulerToQuaternion(p2.yaw(), p2.pitch(), p2.roll());

            for (int j = 0; j < segmentsPerNode; j++) {
                float t = ((float) j / segmentsPerNode);
                result.add(catmullRomInterpolate(p0, p1, p2, p3, q1, q2, t, tempQuat));
            }
        }

        PathNode lastNode = nodes[endIndex];
        result.add(new InterpolatedNode(lastNode.pos(), eulerToQuaternion(lastNode.yaw(), lastNode.pitch(), lastNode.roll())));

        return result;
    }

    protected InterpolatedNode catmullRomInterpolate(PathNode p0, PathNode p1, PathNode p2, PathNode p3,
                                                     Quaternionf q1, Quaternionf q2, float t, Quaternionf tempQuat) {
        float t2 = t * t, t3 = t2 * t;
        float f0 = -0.5f * t3 + t2 - 0.5f * t;
        float f1 = 1.5f * t3 - 2.5f * t2 + 1.0f;
        float f2 = -1.5f * t3 + 2.0f * t2 + 0.5f * t;
        float f3 = 0.5f * t3 - 0.5f * t2;

        Vec3 pos = new Vec3(
                p0.pos().x * f0 + p1.pos().x * f1 + p2.pos().x * f2 + p3.pos().x * f3,
                p0.pos().y * f0 + p1.pos().y * f1 + p2.pos().y * f2 + p3.pos().y * f3,
                p0.pos().z * f0 + p1.pos().z * f1 + p2.pos().z * f2 + p3.pos().z * f3
        );

        tempQuat.set(q1).slerp(q2, t);
        return new InterpolatedNode(pos, new Quaternionf(tempQuat));
    }

    protected Quaternionf eulerToQuaternion(float yaw, float pitch, float roll) {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(-yaw))
                .rotateX((float) Math.toRadians(pitch))
                .rotateZ((float) Math.toRadians(roll));
    }

    protected void emitQuad(VertexConsumer consumer, Matrix4f pose,
                            float x1, float y1, float z1, int c1,
                            float x2, float y2, float z2, int c2,
                            float x3, float y3, float z3, int c3,
                            float x4, float y4, float z4, int c4) {
        consumer.addVertex(pose, x1, y1, z1).setColor(c1).setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        consumer.addVertex(pose, x2, y2, z2).setColor(c2).setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        consumer.addVertex(pose, x3, y3, z3).setColor(c3).setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        consumer.addVertex(pose, x4, y4, z4).setColor(c4).setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
    }

    /**
     * 插值节点记录
     */
    public record InterpolatedNode(Vec3 pos, Quaternionf rot) {

        public InterpolatedNode lerp(InterpolatedNode to, float lerp) {
            return new InterpolatedNode(pos().lerp(to.pos(), lerp), rot().slerp(to.rot(), lerp));
        }

    }
}
