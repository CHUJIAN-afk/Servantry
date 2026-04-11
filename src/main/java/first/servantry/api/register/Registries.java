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

    public static final ResourceKey<Registry<ServantType<? extends Servant>>> ServantTypeKey = ResourceKey.createRegistryKey(Servantry.rl("servant_types"));

    public static final Registry<ServantType<? extends Servant>> ServantTypes = new RegistryBuilder<>(ServantTypeKey).sync(true).create();

    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        event.register(ServantTypes);
    }

}