package first.servantry.common.event;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.Registries;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(Servantry.rl("servant/enchanted_throwing_knives")));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
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

    private static final Map<Item, List<MutableComponent>> Cache = new HashMap<>();

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        List<Component> toolTip = event.getToolTip();

        if (itemStack.getItem() instanceof IServantWeapon<?> iServantWeapon && player != null) {
            ServantType<?> type = iServantWeapon.getType();
            ResourceLocation location = Registries.SERVANT_TYPES.getKey(type);

            if (location != null) {
                String key = "servant." + location.getNamespace() + "." + location.getPath();
                ServantData data = player.getData(AttachmentRegister.ServantData);
                float damage = iServantWeapon.getDamage();
                float knockback = iServantWeapon.getKnockback();

                // 1. 伤害 (例如: "9 召唤伤害")
                if (damage > 0) {
                    String damageStr = damage == (long) damage ? String.format("%d", (long) damage) : String.valueOf(damage);
                    toolTip.add(Component.literal(damageStr).withStyle(ChatFormatting.BLUE)
                            .append(Component.translatable("item.servantry.tooltip.damage").withStyle(ChatFormatting.GRAY)));
                }

                // 2. 击退 (例如: "0.5 击退力")
                String kbStr = knockback == (long) knockback ? String.format("%d", (long) knockback) : String.valueOf(knockback);
                toolTip.add(Component.literal(kbStr).withStyle(ChatFormatting.BLUE)
                        .append(Component.translatable("item.servantry.tooltip.knockback").withStyle(ChatFormatting.GRAY)));


                // 3. 召唤宣言 (例如: "召唤 泰拉棱镜 为你而战")
                toolTip.add(Component.translatable("item.servantry.tooltip.summon",
                        Component.translatable(key).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));

                // 4. 栏位消耗 (例如: "仆从栏位: 3 / 5")
                toolTip.add(Component.translatable("item.servantry.tooltip.slots",
                        Component.literal(String.valueOf(data.getServants().size())).withStyle(ChatFormatting.BLUE),
                        Component.literal(String.valueOf(data.getMaxSize(player))).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));

                // 5. 移除操作提示 (深灰色，避免喧宾夺主)
                toolTip.add(Component.translatable("item.servantry.tooltip.remove_all").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        Item item = itemStack.getItem();
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        if (registryName.getNamespace().equals(Servantry.MODID)) {
            List<MutableComponent> cachedLore = Cache.computeIfAbsent(item, k -> {
                List<MutableComponent> lines = new ArrayList<>();
                String baseKey = "item." + Servantry.MODID + "." + registryName.getPath() + ".tooltip.";
                int index = 1;
                while (I18n.exists(baseKey + index)) {
                    lines.add(Component.translatable(baseKey + index));
                    index++;
                }
                return lines;
            });
            if (!cachedLore.isEmpty()) {
                for (MutableComponent component : cachedLore) {
                    toolTip.add(component.withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }
}