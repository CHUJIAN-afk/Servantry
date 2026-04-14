package first.servantry.api.register;

import first.servantry.Servantry;
import first.servantry.api.servant.Servant;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;


@EventBusSubscriber(modid = Servantry.MODID)
public class Registries {

    private static final ResourceKey<Registry<ServantType<? extends Servant>>> SERVANT_TYPE_KEY = ResourceKey.createRegistryKey(Servantry.rl("servant_types"));
    public static final Registry<ServantType<? extends Servant>> SERVANT_TYPES = new RegistryBuilder<>(SERVANT_TYPE_KEY).sync(true).create();

    // 【新增】：标记类型的注册表
    private static final ResourceKey<Registry<MarkerType>> MARKER_TYPE_KEY = ResourceKey.createRegistryKey(Servantry.rl("marker_types"));
    public static final Registry<MarkerType> MARKER_TYPES = new RegistryBuilder<>(MARKER_TYPE_KEY).sync(true).create();

    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        event.register(SERVANT_TYPES);
        event.register(MARKER_TYPES);
    }

}