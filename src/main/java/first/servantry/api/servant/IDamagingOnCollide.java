package first.servantry.api.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.OBB;
import first.servantry.api.PathNode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 为仆从实现此接口让仆从拥有对目标的碰撞判定
 */
public interface IDamagingOnCollide {

    /**
     * 碰撞采样的节点数量。
     * 默认为 2（即当前刻与上一刻）。
     * 如果返回 3，则会拼接计算 [上上刻->上一刻] 和 [上一刻->当前刻] 两段轨迹的碰撞。
     */
    default int getCollisionSampleNodes() {
        return 2;
    }

    default void processCollision(Servant servant) {
        AABB localBox = getHitbox();
        double dx = localBox.getXsize();
        double dy = localBox.getYsize();
        double dz = localBox.getZsize();
        double minDim = Math.min(Math.min(dx, dy), dz);
        LinkedList<PathNode> historyNodes = servant.getHistoryNodes();
        int sampleNodes = getCollisionSampleNodes();

        if (minDim > 0 && historyNodes.size() >= 2 && sampleNodes >= 2) {
            // 计算需要遍历的历史线段数 (如取3个节点，则有2段轨迹)
            int segments = Math.min(sampleNodes - 1, historyNodes.size() - 1);

            List<OBB> sweepOBBs = new ArrayList<>();
            AABB broadAABB = null;
            Vec3 boxCenterOffset = localBox.getCenter();
            Vec3 boxSize = new Vec3(dx, dy, dz);

            // 遍历所需的各个历史线段，分别生成贝塞尔平滑切片
            for (int s = 0; s < segments; s++) {
                PathNode current = historyNodes.get(s);         // 线段终点
                PathNode prev = historyNodes.get(s + 1);        // 线段起点
                PathNode older = historyNodes.size() > s + 2 ? historyNodes.get(s + 2) : prev; // 动量参考点

                Vec3 P1 = current.pos();
                Vec3 P0 = prev.pos();
                Vec3 P_minus1 = older.pos();

                Vec3 V0 = P0.subtract(P_minus1);
                Vec3 C = P0.add(V0.scale(0.5));

                double stepDist = minDim * 0.5;
                double pathLength = P0.distanceTo(P1);
                int steps = Math.max(1, (int) Math.ceil(pathLength / stepDist));

                for (int i = 0; i <= steps; i++) {
                    float t = (float) i / steps;
                    double mt = 1.0 - t;

                    Vec3 pos = P0.scale(mt * mt)
                            .add(C.scale(2 * mt * t))
                            .add(P1.scale(t * t));

                    float yaw = Mth.rotLerp(t, prev.yaw(), current.yaw());
                    float pitch = Mth.rotLerp(t, prev.pitch(), current.pitch());
                    float roll = Mth.rotLerp(t, prev.roll(), current.roll());

                    Vec3 hitCenter = pos;
                    if (boxCenterOffset.lengthSqr() > 1e-5) {
                        hitCenter = hitCenter.add(boxCenterOffset.xRot((float) Math.toRadians(-pitch)).yRot((float) Math.toRadians(-yaw)));
                    }

                    OBB obb = new OBB(hitCenter, boxSize, yaw, pitch, roll);
                    sweepOBBs.add(obb);

                    AABB obbBox = obb.getBoundingBox();
                    broadAABB = (broadAABB == null) ? obbBox : broadAABB.minmax(obbBox);
                }
            }

            if (broadAABB == null) return;

            Player owner = servant.getOwner();
            List<LivingEntity> potentialTargets = owner.level().getEntitiesOfClass(LivingEntity.class, broadAABB);
            Set<LivingEntity> hitTargets = new HashSet<>();

            for (LivingEntity target : potentialTargets) {
                if (servant.isTarget(target)) {
                    AABB targetBox = target.getBoundingBox();
                    for (OBB obb : sweepOBBs) {
                        if (obb.intersects(targetBox)) {
                            hitTargets.add(target);
                            break;
                        }
                    }
                }
            }

            if (!hitTargets.isEmpty()) {
                collisionAttack(hitTargets);
            }
        }
    }

    default void renderDebugHitbox(PoseStack poseStack, MultiBufferSource bufferSource, float yaw, float pitch, float roll) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.translate(0, 0, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, consumer, this.getHitbox(), 1.0F, 0.0F, 0.0F, 1.0F);
        poseStack.popPose();
    }

    /**
     * 仆从碰撞箱大小与偏移，以当前位置为原点，会同时在客户端以红框渲染
     */
    AABB getHitbox();

    /**
     * 当仆从在历史轨迹中发生碰撞时触发。
     * @param hitTargets 撞到的目标
     */
    void collisionAttack(Set<LivingEntity> hitTargets);

}