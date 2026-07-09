package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AttachmentRegister {

    private static final DeferredRegister<AttachmentType<?>> Register =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Servantry.MODID);

    /** 统一的实体数据附件 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EntityData>> EntityData =
            Register.register("entity_data", () -> AttachmentType.builder(EntityData::new)
                    .sync(new EntityData())
                    .build()
            );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<InvincibleData>> InvincibleData =
            Register.register("invincible_data", () -> AttachmentType.builder(InvincibleData::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HealthData>> HealthData =
            Register.register("health_data", () -> AttachmentType.builder(HealthData::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TargetCache>> TargetCache =
            Register.register("target_cache", () -> AttachmentType.builder(TargetCache::new).build());

    /** Level 级批量粒子累积附件（仅服务端使用，不同步） */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BatchedParticlesData>> BatchedParticles =
            Register.register("batched_particles", () -> AttachmentType.builder(BatchedParticlesData::new).build());

    /** Level 级伤害数字累积附件（服务端累积，客户端接收渲染，不同步） */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DamageInfoData>> DamageInfoData =
            Register.register("damage_info_data", () -> AttachmentType.builder(DamageInfoData::new).build());

    public static void register(IEventBus eventbus) {
        Register.register(eventbus);
    }

}
