package first.servantry.api.core;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.armorSet.ArmorSet;
import first.servantry.api.client.render.AttachmentEntityRenderDispatcher;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.AttributeRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel clientLevel = minecraft.level;
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS && clientLevel != null) {
            PoseStack poseStack = event.getPoseStack();
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            MultiBufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            for (Player player : clientLevel.players()) {
                AttachmentEntityRenderDispatcher.render(player, poseStack, bufferSource, partialTick);
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
            AttachmentEntityType<?> type = iServantWeapon.getType();
            ResourceLocation location = ServantryRegistries.ATTACHMENT_ENTITY_TYPES.getKey(type);

            if (location != null) {
                String key = "servant." + location.getNamespace() + "." + location.getPath();
                EntityData data = player.getData(AttachmentRegister.EntityData);
                float damage = iServantWeapon.getDamage();
                float knockback = iServantWeapon.getKnockback();

                // 1. 伤害 (例如: "9 召唤伤害")
                if (damage > 0) {
                    AttributeInstance attribute = player.getAttribute(AttributeRegister.ServantDamage);
                    damage = attribute != null ? (float) (damage * attribute.getValue()) : damage;
                    String damageStr = String.format("%.1f ", damage);
                    toolTip.add(Component.literal(damageStr).withStyle(ChatFormatting.BLUE)
                            .append(Component.translatable("item.servantry.tooltip.damage").withStyle(ChatFormatting.GRAY)));
                }

                // 2. 击退 (例如: "0.5 击退力")
                if (knockback > 0) {
                    AttributeInstance attribute = player.getAttribute(AttributeRegister.ServantKnockback);
                    knockback = attribute != null ? (float) (knockback * attribute.getValue()) : knockback;
                    String kbStr = String.format("%.1f ", knockback);
                    toolTip.add(Component.literal(kbStr).withStyle(ChatFormatting.BLUE)
                            .append(Component.translatable("item.servantry.tooltip.knockback").withStyle(ChatFormatting.GRAY)));
                }

                // 3. 召唤宣言 (例如: "召唤 泰拉棱镜 为你而战")
                toolTip.add(Component.translatable("item.servantry.tooltip.summon",
                        Component.translatable(key).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));

                // 4. 栏位消耗 (例如: "仆从栏位: 3 / 5")
                toolTip.add(Component.translatable("item.servantry.tooltip.slots",
                        Component.literal(String.valueOf(data.getUsedSlots())).withStyle(ChatFormatting.BLUE),
                        Component.literal(String.valueOf(data.getMaxServantSize(player))).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));

                // 5. 移除操作提示 (深灰色，避免喧宾夺主)
                toolTip.add(Component.translatable("item.servantry.tooltip.remove_all").withStyle(ChatFormatting.GRAY));
            }
        }

        Item item = itemStack.getItem();
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        if (registryName.getNamespace().equals(Servantry.MODID)) {
            List<MutableComponent> cachedLore = Cache.computeIfAbsent(item, k -> {
                List<MutableComponent> lines = new ArrayList<>();
                String baseKey = "item" + "." + Servantry.MODID + "." + registryName.getPath() + "." + "tooltip" + ".";
                int index = 1;
                while (I18n.exists(baseKey + index)) {
                    lines.add(Component.translatable(baseKey + index));
                    index++;
                }
                return lines;
            });
            if (!cachedLore.isEmpty()) {
                if (player != null) {
                    toolTip.add(Component.empty());
                }
                for (MutableComponent component : cachedLore) {
                    toolTip.add(component.withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void armorSetTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        Player player = event.getEntity();
        List<Component> toolTip = event.getToolTip();
        List<Item> armors = new ArrayList<>();
        if (player != null) {
            Iterable<ItemStack> armorSlots = player.getArmorSlots();
            for (ItemStack armorSlot : armorSlots) {
                armors.add(armorSlot.getItem());
            }
        }
        List<ArmorSet> armorSets = getArmorSets(item);
        for (ArmorSet armorSet : armorSets) {
            ResourceLocation id = armorSet.id();
            List<MutableComponent> lines = new ArrayList<>();
            lines.add(Component.empty());
            List<DeferredItem<Item>> items = armorSet.items();
            MutableComponent set = Component.empty();
            boolean full = player != null && armorSet.full(player);
            ChatFormatting descColor = full ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
            for (DeferredItem<Item> itemDeferredItem : items) {
                if (items.getFirst() == itemDeferredItem) {
                    set.append(Component.literal("[ ").withStyle(descColor));
                }
                Item piece = itemDeferredItem.get();
                ChatFormatting format = armors.contains(piece) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
                set.append(piece.getDescription().copy().withStyle(format)).append(Component.literal(" "));
                if (items.getLast() == itemDeferredItem) {
                    set.append(Component.literal("] ").withStyle(descColor));
                }
            }
            set.append(Component.translatable("item.servantry.tooltip.set_bonus_title").withStyle(descColor));
            lines.add(set);
            Collection<Map.Entry<Holder<Attribute>, AttributeModifier>> entries = armorSet.modifiers().entries();
            descColor = full ? ChatFormatting.BLUE : ChatFormatting.DARK_GRAY;
            for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : entries) {
                Attribute attr = entry.getKey().value();
                AttributeModifier modifier = entry.getValue();
                lines.add(attr.toComponent(modifier, TooltipFlag.NORMAL).withStyle(descColor));
            }
            String baseKey = Servantry.MODID + "." + id.getNamespace() + "." + id.getPath() + "." + "set" + ".";
            int index = 1;
            while (I18n.exists(baseKey + index)) {
                lines.add(Component.translatable(baseKey + index).withStyle(descColor));
                index++;
            }
            toolTip.addAll(lines);
        }
    }

    private static @NotNull List<ArmorSet> getArmorSets(Item item) {
        List<ArmorSet> list = ServantryRegistries.ARMOR_SETS.stream().toList();
        List<ArmorSet> target = new ArrayList<>();
        for (ArmorSet armorSet : list) {
            List<DeferredItem<Item>> items = armorSet.items();
            for (DeferredItem<Item> itemDeferredItem : items) {
                if (item == itemDeferredItem.get()) {
                    target.add(armorSet);
                    break;
                }
            }
        }
        return target;
    }
}