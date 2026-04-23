package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.common.attachment.InvincibleData;
import first.servantry.common.attachment.ProjectileData;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.attachment.TargetCacheData;
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

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<InvincibleData>> InvincibleData = Register.register("invincible_data", () -> AttachmentType.builder(InvincibleData::new).build());

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<ProjectileData>> ProjectileData =
			Register.register("projectile_data", () -> AttachmentType.builder(ProjectileData::new)
					.sync(new ProjectileData())
					.build()
			);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<TargetCacheData>> TargetCacheData =
			Register.register("target_cache_data", () -> AttachmentType.builder(TargetCacheData::new).build());

	public static void register(IEventBus eventbus) {
		Register.register(eventbus);
	}

}
