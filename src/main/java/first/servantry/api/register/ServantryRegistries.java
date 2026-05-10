package first.servantry.api.register;

import first.servantry.Servantry;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 自定义注册表管理类。
 * <p>
 * 包含附件实体类型的自定义注册表。
 * </p>
 */
@EventBusSubscriber(modid = Servantry.MODID)
public class ServantryRegistries {

    /**
     * 附件实体类型注册表键
     */
    private static final ResourceKey<Registry<AttachmentEntityType<? extends AttachmentEntity>>> ATTACHMENT_ENTITY_TYPE_KEY = ResourceKey.createRegistryKey(Servantry.rl("attachment_entity_types"));

    /**
     * 附件实体类型注册表
     */
    public static final Registry<AttachmentEntityType<? extends AttachmentEntity>> ATTACHMENT_ENTITY_TYPES = new RegistryBuilder<>(ATTACHMENT_ENTITY_TYPE_KEY).sync(true).create();

    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        event.register(ATTACHMENT_ENTITY_TYPES);
    }

}
