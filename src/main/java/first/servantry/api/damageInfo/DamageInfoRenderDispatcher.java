package first.servantry.api.damageInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.client.render.AlphaBufferSource;
import first.servantry.api.common.attachment.DamageInfoData;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 伤害数字渲染调度器。
 * <p>
 * 数据来源为客户端 Level 附件 {@link DamageInfoData}，
 * 由 {@link DamageInfoData#tick()} 驱动生命周期衰减。
 * </p>
 */
public class DamageInfoRenderDispatcher {

    public static void render(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        DamageInfoData data = level.getData(AttachmentRegister.DamageInfoData);
        List<DamageInfo> infos = data.getActiveInfos();
        if (infos.isEmpty()) return;

        AlphaBufferSource alphaSource = new AlphaBufferSource(bufferSource);
        Vec3 camPos = camera.getPosition();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();

        // 单次 getBuffer：所有 DamageInfo 共享同一个 VertexConsumer，避免重复切换
        VertexConsumer consumer = alphaSource.getBuffer(RenderType.entityTranslucent(DamageInfo.getTexture()));

        for (DamageInfo info : infos) {
            Vec3 renderPos = info.getRenderPos(partialTick);

            // 预计算渲染参数
            float scale = info.getRenderScale(partialTick);
            float alpha = info.getRenderAlpha(partialTick);
            float roll = info.getRenderRoll(partialTick);
            int color = info.getRenderColor(partialTick);

            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            alphaSource.setAlpha(alpha);

            // 居中偏移
            float offsetX = -info.getTotalWidth(scale) / 2f;

            poseStack.pushPose();
            // 平移到渲染位置（相对于相机）
            poseStack.translate(renderPos.x() - camPos.x(), renderPos.y() - camPos.y(), renderPos.z() - camPos.z());
            // 面向相机
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.XN.rotationDegrees(180));
            poseStack.mulPose(Axis.ZN.rotationDegrees(roll));
            // 居中偏移
            poseStack.translate(offsetX, 0, 0);

            Matrix4f matrix = poseStack.last().pose();
            info.renderQuads(matrix, consumer, scale, r, g, b, alpha);

            poseStack.popPose();
        }
    }
}
