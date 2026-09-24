package first.summoner.common.attachment;

import first.lyra.client.render.RenderUtil;
import first.lyra.common.attachment.SummonMarkTracker;
import first.lyra.register.LyraAttachmentRegister;
import first.summoner.Summoner;
import first.summoner.common.item.GildedWandItem;
import first.summoner.network.GildedWandPacket;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class GildedWandData {

    public LivingEntity target = null;
    public final int BenchmarkFps = 165;
    public int fps = 165;
    public int power = 0;

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player) {
            GildedWandData data = player.getData(SummonerAttachmentRegister.GildedWandData);
            data.fps = minecraft.getFps();
            if (!(player.getUseItem().getItem() instanceof GildedWandItem) || !player.isUsingItem()) {
                data.target = null;
                return;
            }

            Vec3 startPos = player.getEyePosition();
            Vec3 lookAngle = player.getLookAngle();
            int searchRange = 32;
            double fovAngle = 30;
            double distanceWeight = 0.2;

            LivingEntity best = null;
            double bestScore = Double.MAX_VALUE;
            for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(searchRange))) {
                if (living.isAlive() && living != player) {
                    // 穿墙过滤：允许穿墙时跳过该判断
                    if (living.hasLineOfSight(player)) {
                        Vec3 targetPoint = living.getEyePosition();
                        Vec3 toEntity = targetPoint.subtract(startPos);
                        double distance = toEntity.length();
                        if (distance <= searchRange) {
                            // 目标相对视线的夹角（度），后方目标角度超过 90 度自动被剔除
                            double angle = Math.toDegrees(Math.acos(toEntity.dot(lookAngle) / distance));
                            if (angle <= fovAngle) {
                                // 角度、距离分别归一化到 [0,1]
                                double normalizedAngle = angle / fovAngle;
                                double normalizedDistance = distance / searchRange;

                                // 综合分数：按权重合成角度与距离，越小越优先
                                double score = (1.0 - distanceWeight) * normalizedAngle + distanceWeight * normalizedDistance;
                                if (score < bestScore) {
                                    bestScore = score;
                                    best = living;
                                }
                            }
                        }
                    }
                }
            }
            data.target = best;
            if (best != null) {
                PacketDistributor.sendToServer(new GildedWandPacket(best.getId(), player.getUsedItemHand()));
            }
        }
    }

    @SubscribeEvent
    public static void aimbot(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) {
            GildedWandData data = player.getData(SummonerAttachmentRegister.GildedWandData);
            LivingEntity target = data.target;
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            if (target != null) {
                Vec3 startPos = player.getEyePosition(partialTick);
                Vec3 targetPos = target.getPosition(partialTick).add(0, target.getBoundingBox().getYsize() / 2, 0);
                Vec3 direction = targetPos.subtract(startPos).normalize();
                float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
                float pitch = (float) Math.toDegrees(Math.asin(-direction.y));
                // fps 补偿：以 165fps 为基准，帧率越低单次插值进度越大，保证不同帧率下转向速度一致
                float aimSpeed = Math.min(0.1f * data.BenchmarkFps / Math.max(data.fps, 1), 1);
                player.setYRot(Mth.rotLerp(aimSpeed, player.getYRot(), yaw));
                player.setXRot(Mth.rotLerp(aimSpeed, player.getXRot(), pitch));
            }
            SummonMarkTracker markData = player.getData(LyraAttachmentRegister.SummonMarkData);
            LivingEntity markTarget = markData.getTarget();
            if (markTarget != null) {
                Vec3 targetPos = markTarget.getPosition(partialTick).add(0, markTarget.getBoundingBox().getYsize(), 0);
                MultiBufferSource bufferSource = minecraft.renderBuffers().bufferSource();
                RenderUtil.renderImage(Summoner.rl("textures/summon_mark.png"), targetPos, 0.25f, 0.25f, bufferSource, true, FastColor.ARGB32.color((int) (0.75 * 255), 255, 255, 255));
            }
        }
    }
}
