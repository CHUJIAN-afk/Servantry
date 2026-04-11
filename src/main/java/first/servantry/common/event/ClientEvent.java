package first.servantry.common.event;


import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.Registries;
import first.servantry.api.register.ServantType;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID)
public class ClientEvent {

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (event.getItemStack().getItem() instanceof IServantWeapon<?> iServantWeapon && player != null) {
            ServantType<?> type = iServantWeapon.getType();
            ResourceLocation location = Registries.ServantTypes.getKey(type);
            if (location != null) {
                List<Component> toolTip = event.getToolTip();
                String key = "servant." + location.getNamespace() + "." + location.getPath();
                ServantData data = player.getData(AttachmentRegister.ServantData);
                toolTip.add(Component.translatable("item.servantry.tooltip.1").withStyle(ChatFormatting.GRAY).append(Component.translatable(key).withStyle(ChatFormatting.BLUE)));
                toolTip.add(Component.translatable("item.servantry.tooltip.2").withStyle(ChatFormatting.GRAY).append(Component.literal(data.getServants().size() + "/" + data.getMaxSize(player)).withStyle(ChatFormatting.BLUE)));
            }
        }
    }



}
