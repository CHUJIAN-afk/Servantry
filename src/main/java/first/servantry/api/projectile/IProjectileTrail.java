package first.servantry.api.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.ITrailRenderer.TrailNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public interface IProjectileTrail {

    default int getTrailHistoryLength() { return 4; }
    default int getTrailSegmentsPerNode() { return 4; }

    void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, AdvancedProjectile projectile, PathNode visualRenderNode, List<TrailNode> smoothNodes);

    /**
     * 核心逻辑：曲线平滑插值
     */
    default void processTrailRender(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, AdvancedProjectile projectile, PathNode renderNode) {
        LinkedList<PathNode> history = projectile.getHistoryNodes();
        int actualLength = Math.min(history.size(), getTrailHistoryLength());
        if (actualLength < 3) return;

        PathNode[] renderNodesArray = new PathNode[actualLength];
        Iterator<PathNode> iterator = history.iterator();
        for (int i = 0; i < actualLength; i++) {
            renderNodesArray[i] = iterator.next();
        }
        renderNodesArray[0] = renderNode;

        int segments = getTrailSegmentsPerNode();
        List<TrailNode> smoothNodes = new ArrayList<>((actualLength - 1) * segments + 1);
        Quaternionf tempQ = new Quaternionf();

        for (int i = 0; i < actualLength - 1; i++) {
            PathNode p0 = renderNodesArray[Math.max(i - 1, 0)];
            PathNode p1 = renderNodesArray[i];
            PathNode p2 = renderNodesArray[i + 1];
            PathNode p3 = renderNodesArray[Math.min(i + 2, actualLength - 1)];

            Quaternionf q1 = new Quaternionf().rotateY((float) Math.toRadians(-p1.yaw())).rotateX((float) Math.toRadians(p1.pitch())).rotateZ((float) Math.toRadians(p1.roll()));
            Quaternionf q2 = new Quaternionf().rotateY((float) Math.toRadians(-p2.yaw())).rotateX((float) Math.toRadians(p2.pitch())).rotateZ((float) Math.toRadians(p2.roll()));

            for (int j = 0; j < segments; j++) {
                float t = (float) j / segments;
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
                tempQ.set(q1).slerp(q2, t);
                smoothNodes.add(new TrailNode(pos, new Quaternionf(tempQ)));
            }
        }
        
        poseStack.pushPose();
        // 抵消实体本身的坐标位移，完全依赖 smoothNodes 的世界坐标相对值绘制
        Vec3 offset = renderNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        drawTrailVertices(poseStack, bufferSource, partialTick, projectile, renderNode, smoothNodes);

        poseStack.popPose();
    }
}