package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.attachment.WhipData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AttachmentRegister {

	private static final DeferredRegister<AttachmentType<?>> Register =
			DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Servantry.MODID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<WhipData>> WhipData =
			Register.register("whip_data", () -> AttachmentType.builder(WhipData::new)
					.sync(new WhipData())
					.build()
			);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<ServantData>> ServantData =
			Register.register("servant_data", () -> AttachmentType.builder(ServantData::new)
					.sync(new ServantData())
					.build()
			);

	public static void register(IEventBus eventbus) {
		Register.register(eventbus);
	}

}
