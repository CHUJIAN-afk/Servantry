package first.servantry.register;

import first.lyra.common.creativeTab.CreativeTabDispatcher;
import first.servantry.Servantry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


@EventBusSubscriber(modid = Servantry.MODID)
public class ServantryCreativeTabRegister {

    public static final DeferredRegister<CreativeModeTab> Register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Servantry.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> Tab = Register.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("modid.servantry"))
            .icon(() -> ServantryServantWeaponRegister.TerraPrism.get().getDefaultInstance())
            .build());

    private static boolean tabRegistered = false;

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
        // 接入 Lyra 创造分类:物品按 Section 特征标签自动归类,横幅渲染由 Lyra 的 mixin 处理
        CreativeTabDispatcher.registerSection(ServantryServantWeaponRegister.SERVANT_WEAPON);
        CreativeTabDispatcher.registerSection(ServantryArmorRegister.ARMOR);
        CreativeTabDispatcher.registerSection(ServantryCurioRegister.ACCESSORY);
        CreativeTabDispatcher.registerSection(ServantryItemRegister.MATERIAL);
        CreativeTabDispatcher.registerSection(ServantryItemRegister.BLOCK);
    }

    /** Tab 注册完成后接入 Lyra 调度器(此处 Tab.value() 已可用)。 */
    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (!tabRegistered) {
            CreativeTabDispatcher.registerTab(Tab);
            tabRegistered = true;
        }
    }
}
