package first.summoner.register;

import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AttachmentEntityRenderDispatcher;
import first.lyra.client.render.IAttachmentEntityRenderer;
import first.lyra.client.render.RenderUtil;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.summoner.Summoner;
import first.summoner.client.attachmentEntityRenderer.SimpleRenderer;
import first.summoner.client.attachmentEntityRenderer.minion.*;
import first.summoner.client.attachmentEntityRenderer.projectile.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 附件实体渲染器注册类（仅客户端加载）。
 * <p>
 * 渲染器引用客户端类（LocalPlayer 等），必须与实体类型注册分离：
 * 若由 {@link SummonerAttachmentEntityRegister} 直接引用，
 * 服务端加载该类时会被 RuntimeDistCleaner 拦截
 * （Attempted to load class ... for invalid dist DEDICATED_SERVER）导致模组崩溃。
 * 本类通过 {@code @EventBusSubscriber(value = Dist.CLIENT)} 保证只在客户端加载执行。
 */
@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class SummonerAttachmentEntityRenderRegister {

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        // ===================== 仆从 =====================
        register(SummonerAttachmentEntityRegister.INFINITE_SHADOW, InfiniteShadowRenderer::new);
        register(SummonerAttachmentEntityRegister.TERRA_PRISM, TerraprismRenderer::new);
        register(SummonerAttachmentEntityRegister.STARDUST_CELL, StardustCellRenderer::new);
        register(SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES, EnchantedThrowingKnivesRenderer::new);
        register(SummonerAttachmentEntityRegister.STARDUST_DRAGON, StardustDragonRenderer::new);
        register(SummonerAttachmentEntityRegister.TWINS, TwinsRenderer::new);
        register(SummonerAttachmentEntityRegister.SHARKNADO, SharknadoRenderer::new);
        register(SummonerAttachmentEntityRegister.DEADLY_SPHERE, null);
        register(SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE, EtherealStellarCoreRenderer::new);
        register(SummonerAttachmentEntityRegister.ORE_SCOUT, null);
        register(SummonerAttachmentEntityRegister.SCAVENGER_FAIRY, null);
        register(SummonerAttachmentEntityRegister.CHLOROPHYTE_CRYSTAL, ChlorophyteCrystalRenderer::new);
        register(SummonerAttachmentEntityRegister.MOON_PORTAL, null);
        register(SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL, null);
        register(SummonerAttachmentEntityRegister.BALLISTA, null);
        register(SummonerAttachmentEntityRegister.SUPER_PEASHOOTER, null);
        register(SummonerAttachmentEntityRegister.VOID_EATER, null);
        register(SummonerAttachmentEntityRegister.CLOUD, null);
        register(SummonerAttachmentEntityRegister.PULSE_TURRET, null);
        register(SummonerAttachmentEntityRegister.HORNET, null);
        register(SummonerAttachmentEntityRegister.IMP, null);
        register(SummonerAttachmentEntityRegister.UFO, null);
        register(SummonerAttachmentEntityRegister.CANNON, null);
        register(SummonerAttachmentEntityRegister.NECRO_SPIRIT, null);
        register(SummonerAttachmentEntityRegister.LASER_MINIGUN, LaserMinigunRenderer::new);
        register(SummonerAttachmentEntityRegister.LIGHTNING_AURA, () -> simpleRenderer(null, 0, 8));
        register(SummonerAttachmentEntityRegister.BARNACLE, null);
        register(SummonerAttachmentEntityRegister.EYE_LASER_TURRET, null);
        register(SummonerAttachmentEntityRegister.HYDRA, null);
        register(SummonerAttachmentEntityRegister.INFERNO, null);
        register(SummonerAttachmentEntityRegister.BLOOD_BAT, BloodBatRenderer::new);
        register(SummonerAttachmentEntityRegister.RUIN_RELIC, null);

        // ===================== 射弹 =====================
        register(SummonerAttachmentEntityRegister.MINI_STARDUST_CELL, MiniStardustRenderer::new);
        register(SummonerAttachmentEntityRegister.LASER, LaserRenderer::new);
        register(SummonerAttachmentEntityRegister.DEMON_FLAME, () -> simpleRenderer(null, 0, 8));
        register(SummonerAttachmentEntityRegister.SHARK_DRAGON, SharkDragonRenderer::new);
        register(SummonerAttachmentEntityRegister.SHATTERED_STELLAR_CORE, ShatteredStellarCoreRenderer::new);
        register(SummonerAttachmentEntityRegister.MINI_CHLOROPHYTE_CRYSTAL, MiniChlorophyteCrystalRenderer::new);
        register(SummonerAttachmentEntityRegister.ZENITH, ZenithRenderer::new);
        register(SummonerAttachmentEntityRegister.CUSTOM_LASER, CustomLaserRenderer::new);
        register(SummonerAttachmentEntityRegister.MINI_RAINBOW_CRYSTAL, RainbowCrystalRenderer::new);
        register(SummonerAttachmentEntityRegister.CROSSBOW_BOLT, null);
        register(SummonerAttachmentEntityRegister.GOD_FLAME, GodFlameRenderer::new);
        register(SummonerAttachmentEntityRegister.BLITZ_BALL, BlitzBallRenderer::new);
        register(SummonerAttachmentEntityRegister.RAIN, RainRenderer::new);
        register(SummonerAttachmentEntityRegister.DESTRUCTION_BULLET, DestructionBulletRenderer::new);
        register(SummonerAttachmentEntityRegister.HORNET_STINGER, null);
        register(SummonerAttachmentEntityRegister.IMP_FIREBALL, () -> simpleRenderer(Summoner.rl("textures/item/entity/imp_fireball.png"), 0.25f, 8));
        register(SummonerAttachmentEntityRegister.CORN, null);
        register(SummonerAttachmentEntityRegister.MINI_NECRO_SPIRIT, null);
        register(SummonerAttachmentEntityRegister.ELECTRIC_LASER, ElectricLaserRenderer::new);
        register(SummonerAttachmentEntityRegister.METEOR, () -> simpleRenderer(Summoner.rl("textures/item/entity/meteor.png"), 1, 8));
        register(SummonerAttachmentEntityRegister.PETAL, () -> simpleRenderer(Summoner.rl("textures/item/entity/petal.png"), 0.5f, 0));
        register(SummonerAttachmentEntityRegister.POISON_SALIVA, PoisonSalivaRenderer::new);
        register(SummonerAttachmentEntityRegister.FROST_BOLT, null);
        register(SummonerAttachmentEntityRegister.EXPLOSION_FLAME_BALL, null);
        register(SummonerAttachmentEntityRegister.FORBIDDEN_ORB, null);
    }

    private static <T extends AttachmentEntity> void register(DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> type, @Nullable Supplier<IAttachmentEntityRenderer<T>> renderer) {
        AttachmentEntityRenderDispatcher.register(type.get(), renderer != null ? renderer.get() : new SimpleRenderer<>(null));
    }

    private static <T extends AttachmentEntity> SimpleRenderer<T> simpleRenderer(@Nullable ResourceLocation texture, float size, int light) {
        return new SimpleRenderer<>((entity, poseStack, bufferSource, visualNode, context, partialTick, alpha) -> {
            if (texture != null && size > 0) {
                RenderUtil.renderImage(texture, visualNode.pos(), size, size, bufferSource, false, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255));
            }
            if (light > 0) {
                DynamicLightDispatcher.addLightSources(visualNode.pos(), light);
            }
        });
    }
}
