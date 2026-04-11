package first.servantry.api;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID)
public class ServantManager {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ServantData data = player.getData(AttachmentRegister.ServantData);
        List<Servant> servants = data.getServants();
        while (!servants.isEmpty() && data.getMaxSize(player) < servants.size()) {
            servants.removeFirst();
        }
        if (!servants.isEmpty()) {
            for (Servant servant : servants) {
                servant.setOwner(player);
                servant.tick();
            }
        }
        if (!player.level().isClientSide()) {
            player.syncData(AttachmentRegister.ServantData);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel clientLevel = minecraft.level;
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES && clientLevel != null) {
            PoseStack poseStack = event.getPoseStack();
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            MultiBufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            for (Player player : clientLevel.players()) {
                ServantData data = player.getData(AttachmentRegister.ServantData);
                for (Servant servant : data.getServants()) {
                    servant.setOwner(player);
                    servant.renderInternal(partialTick, poseStack, bufferSource);
                }
            }
        }
    }

}