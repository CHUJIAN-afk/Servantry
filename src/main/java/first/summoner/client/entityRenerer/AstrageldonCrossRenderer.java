package first.summoner.client.entityRenerer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.lyra.client.render.EntityPathRender;
import first.summoner.common.entity.astrageldon.AstrageldonProjectile;
import first.summoner.common.entity.astrageldon.AstrageldonStar3;
import first.summoner.common.entity.astrageldon.StarLaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AstrageldonCrossRenderer<T extends AstrageldonProjectile> extends EntityRenderer<T> {

    public AstrageldonCrossRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull T entity, float yaw, float partialTick, @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int light) {
        poses.pushPose();
        EntityPathRender.translate(entity, poses, partialTick);
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        Vec3 center = new Vec3(0, entity.getBbHeight() / 2, 0);
        if (entity instanceof AstrageldonStar3 star) {
            for (Vec3 axis : star.getAxes()) {
                line(lines, poses.last(), center.subtract(axis.scale(45)), center.add(axis.scale(45)), star.getCharge(partialTick) * 0.65f);
            }
        }
        if (entity instanceof StarLaser laser) {
            float frame = laser.getFrame(partialTick);
            float alpha = Math.max(0, (155 - frame * 4) / 255);
            Vec3 origin = entity.getBoundingBox().getCenter();
            for (int i = 0; i < 120; i++) {
                Vec3 from = laser.getPoint(i, frame).subtract(origin).add(center);
                Vec3 to = laser.getPoint(i + 1, frame).subtract(origin).add(center);
                line(lines, poses.last(), from, to, alpha);
            }
        }
        poses.popPose();
    }

    private static void line(VertexConsumer lines, PoseStack.Pose pose, Vec3 from, Vec3 to, float alpha) {
        Vec3 normal = to.subtract(from).normalize();
        lines.addVertex(pose.pose(), (float) from.x, (float) from.y, (float) from.z).setColor(1, 0.86f, 0.31f, alpha)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        lines.addVertex(pose.pose(), (float) to.x, (float) to.y, (float) to.z).setColor(1, 0.86f, 0.31f, alpha)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    @Override
    public boolean shouldRender(@NotNull T entity, @NotNull Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
