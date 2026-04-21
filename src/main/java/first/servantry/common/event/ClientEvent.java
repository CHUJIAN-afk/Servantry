package first.servantry.common.event;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.client.ServantRenderDispatcher;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.projectile.AdvancedProjectile;
import first.servantry.api.projectile.IProjectileCollider;
import first.servantry.api.projectile.IProjectileConeTrail;
import first.servantry.api.register.Registries;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.LevelProjectileData;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.particle.StardustScatterParticle;
import first.servantry.common.renderer.TerraprismRenderer;
import first.servantry.register.*;
import first.servantry.utils.ArmorSetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.*;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent<T extends Servant> {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegister.StardustScatter.get(), StardustScatterParticle.Provider::new);
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        ServantRenderDispatcher.register(ServantRegister.TerraPrism.get(), new TerraprismRenderer());
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
                ServantRenderDispatcher.render(player, poseStack, bufferSource, partialTick);
            }
            LevelProjectileData data = clientLevel.getData(AttachmentRegister.LevelProjectileData);
            List<AdvancedProjectile> projectiles = data.getProjectiles();
            for (AdvancedProjectile projectile : projectiles) {
                LinkedList<PathNode> historyNodes = projectile.getHistoryNodes();
                PathNode current = historyNodes.getFirst();
                PathNode last = historyNodes.size() > 1 ? historyNodes.get(1) : current;
                PathNode renderNode = last.lerp(current, partialTick);
                Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                poseStack.pushPose();
                poseStack.translate(renderNode.pos().x - cameraPos.x, renderNode.pos().y - cameraPos.y, renderNode.pos().z - cameraPos.z);
                int packedLight = LevelRenderer.getLightColor(clientLevel, BlockPos.containing(renderNode.pos()));
                projectile.render(poseStack, bufferSource, partialTick, packedLight, renderNode);
                if (projectile instanceof IProjectileConeTrail iProjectileConeTrail) {
                    iProjectileConeTrail.processTrailRender(poseStack, bufferSource, partialTick, projectile, renderNode);
                }
                if (projectile instanceof IProjectileCollider iProjectileCollider) {
                    if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                        iProjectileCollider.renderDebugHitbox(poseStack, bufferSource, renderNode.yaw(), renderNode.pitch(), renderNode.roll());
                    }
                }
                poseStack.popPose();
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
                    AttributeInstance attribute = player.getAttribute(AttributeRegister.ServantDamage);
                    damage = attribute != null ? (float) (damage * attribute.getValue()) : damage;
                    String damageStr = String.format("%.1f", damage);
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
                toolTip.add(Component.translatable("item.servantry.tooltip.remove_all").withStyle(ChatFormatting.GRAY));
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
                if (player != null) {
                    toolTip.add(Component.empty());
                }
                for (MutableComponent component : cachedLore) {
                    toolTip.add(component.withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
        Holder<ArmorMaterial> hallowedArmorMaterial = ArmorMaterialRegister.HallowedArmorMaterial;
        if (itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().equals(hallowedArmorMaterial)) {
            ArmorSetUtil.addSetBonusTooltip(player, hallowedArmorMaterial, toolTip);
        }
    }
}