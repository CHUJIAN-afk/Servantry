package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.common.attachment.LevelProjectileData;
import first.servantry.common.attachment.ServantData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AttachmentRegister {

	private static final DeferredRegister<AttachmentType<?>> Register =
			DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Servantry.MODID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<ServantData>> ServantData =
			Register.register("servant_data", () -> AttachmentType.builder(ServantData::new)
					.sync(new ServantData())
					.build()
			);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<LevelProjectileData>> LevelProjectileData =
			Register.register("level_projectile_data", () -> AttachmentType.builder(LevelProjectileData::new)
					.sync(new LevelProjectileData())
					.build()
			);

	public static void register(IEventBus eventbus) {
		Register.register(eventbus);
	}

}
