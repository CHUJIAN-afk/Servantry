package first.servantry.api.damageInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.common.attachment.DamageInfoData;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

/**
 * 伤害数字渲染调度器。
 * <p>
 * 按贴图分组获取 VertexConsumer，逐条委托 {@link DamageInfo#render} 完成渲染。
 * </p>
 */
public class DamageInfoRenderDispatcher {

    public static void render(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        DamageInfoData data = level.getData(AttachmentRegister.DamageInfoData);
        Map<ResourceLocation, List<DamageInfo>> infos = data.getActiveInfos();
        if (!infos.isEmpty()) {
            Vec3 camPos = camera.getPosition();
            EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
            for (Map.Entry<ResourceLocation, List<DamageInfo>> group : infos.entrySet()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(group.getKey()));
                for (DamageInfo info : group.getValue()) {
                    info.render(poseStack, consumer, bufferSource, camPos, dispatcher, partialTick);
                }
            }
        }
    }
}
