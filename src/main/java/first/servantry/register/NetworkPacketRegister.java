package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.network.WhipAttackPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Servantry.MODID)
public class NetworkPacketRegister {

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(WhipAttackPacket.TYPE, WhipAttackPacket.STREAM_CODEC, WhipAttackPacket::handle);
  }

    public static void playToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

}
