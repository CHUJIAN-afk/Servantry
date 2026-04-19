package first.servantry.api.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.OBB;
import first.servantry.api.servant.PathNode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface IProjectileCollider {

    boolean onHitEntity(AdvancedProjectile projectile, LivingEntity hitResult);

    AABB getHitbox();

    /**
     * 碰撞采样的节点数量。
     * 默认为 2（即当前刻与上一刻）。
     */
    default int getCollisionSampleNodes() {
        return 2;
    }

    default void processCollision(AdvancedProjectile projectile) {
        if (projectile.isRemoved()) return;

        Player owner = projectile.getOwner();
        if (owner == null) return;

        LinkedList<PathNode> historyNodes = projectile.getHistoryNodes();
        int sampleNodes = getCollisionSampleNodes();
        if (historyNodes.size() < 2) return;

        // 高级实体碰撞逻辑 (OBB平滑扫描)
        AABB localBox = getHitbox();
        if (localBox == null) return;
        double dx = localBox.getXsize();
        double dy = localBox.getYsize();
        double dz = localBox.getZsize();
        double minDim = Math.min(Math.min(dx, dy), dz);

        if (minDim > 0 && sampleNodes >= 2) {
            int segments = Math.min(sampleNodes - 1, historyNodes.size() - 1);
            List<OBB> sweepOBBs = new ArrayList<>();
            AABB broadAABB = null;
            Vec3 boxCenterOffset = localBox.getCenter();
            Vec3 boxSize = new Vec3(dx, dy, dz);

            // 倒序遍历（从老到新），保证超高速跨刻飞行时的命中时序正确
            for (int s = segments - 1; s >= 0; s--) {
                PathNode current = historyNodes.get(s);
                PathNode prev = historyNodes.get(s + 1);
                PathNode older = historyNodes.size() > s + 2 ? historyNodes.get(s + 2) : prev;

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

            if (broadAABB != null) {
                // 使用宽泛 AABB 初步筛出可能命中的实体
                List<LivingEntity> potentialTargets = projectile.getLevel().getEntitiesOfClass(LivingEntity.class, broadAABB);
                boolean hitRegistered = false;

                // 按照生成的时序，逐个切片检测 OBB 相交，实现精准防穿透判断
                for (OBB obb : sweepOBBs) {
                    for (LivingEntity target : potentialTargets) {
                        if (obb.intersects(target.getBoundingBox())) {
                            if (onHitEntity(projectile, target)) {
                                projectile.discard();
                                hitRegistered = true;
                                break;
                            }
                        }
                    }
                    if (hitRegistered) break;
                }
            }
        }
    }

    default void renderDebugHitbox(PoseStack poseStack, MultiBufferSource bufferSource, float yaw, float pitch, float roll) {
        if (getHitbox() == null) return;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.translate(0, 0, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, consumer, this.getHitbox(), 1.0F, 0.0F, 0.0F, 1.0F);
        poseStack.popPose();
    }
}