package first.servantry.api.register;

import first.servantry.Servantry;
import first.servantry.api.armorSet.ArmorSet;
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
 * 包含附件实体类型和盔甲套装的自定义注册表。
 * 秘银砧配方已迁移至原版配方系统，不再使用自定义注册表。
 * </p>
 */
@EventBusSubscriber(modid = Servantry.MODID)
public class ServantryRegistries {

    private static final ResourceKey<Registry<AttachmentEntityType<? extends AttachmentEntity>>> ATTACHMENT_ENTITY_TYPE_KEY = ResourceKey.createRegistryKey(Servantry.rl("attachment_entity_types"));

    public static final Registry<AttachmentEntityType<? extends AttachmentEntity>> ATTACHMENT_ENTITY_TYPES = new RegistryBuilder<>(ATTACHMENT_ENTITY_TYPE_KEY).sync(true).create();

    private static final ResourceKey<Registry<ArmorSet>> ARMOR_SET_KEY = ResourceKey.createRegistryKey(Servantry.rl("armor_set"));

    public static final Registry<ArmorSet> ARMOR_SETS = new RegistryBuilder<>(ARMOR_SET_KEY).sync(true).create();

    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        event.register(ATTACHMENT_ENTITY_TYPES);
        event.register(ARMOR_SETS);
    }

}