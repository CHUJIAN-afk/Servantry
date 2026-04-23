package first.servantry.common.event;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.client.ProjectileRenderDispatcher;
import first.servantry.api.client.ServantRenderDispatcher;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.api.register.ServantType;
import first.servantry.client.renderer.projectile.StardustProjectileConeRenderer;
import first.servantry.client.renderer.servant.EnchantedThrowingKnivesRendererServant;
import first.servantry.client.renderer.servant.StardustCellRendererServant;
import first.servantry.client.renderer.servant.TerraprismRendererServant;
import first.servantry.api.common.attachment.ServantData;
import first.servantry.common.particle.StardustScatterParticle;
import first.servantry.register.*;
import first.servantry.utils.ArmorSetUtil;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegister.StardustScatter.get(), StardustScatterParticle.Provider::new);
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        ServantRenderDispatcher.register(ServantRegister.TerraPrism.get(), new TerraprismRendererServant());
        ServantRenderDispatcher.register(ServantRegister.StardustCell.get(), new StardustCellRendererServant());
        ServantRenderDispatcher.register(ServantRegister.EnchantedThrowingKnives.get(), new EnchantedThrowingKnivesRendererServant());
        ProjectileRenderDispatcher.register(ProjectileRegister.StardustProjectile.get(), new StardustProjectileConeRenderer());
    }

}