package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.common.entity.Astrageldon;
import first.summoner.common.entity.astrageldon.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SummonerEntityRegister {

    private static final DeferredRegister<EntityType<?>> Register = DeferredRegister.create(Registries.ENTITY_TYPE, Summoner.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Astrageldon>> ASTRAGELDON =
            Register.register("astrageldon", location -> EntityType.Builder.of(Astrageldon::new, MobCategory.MONSTER)
                    .sized(174F / 32, 118F / 32)
                    .fireImmune()
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath())
            );

    public static final DeferredHolder<EntityType<?>, EntityType<AstraBlade>> ASTRA_BLADE =
            Register.register("astra_blade", location -> EntityType.Builder.of(AstraBlade::new, MobCategory.MISC)
                    .sized(20F / 32, 58F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<AstraRock>> ASTRA_ROCK =
            Register.register("astra_rock", location -> EntityType.Builder.of(AstraRock::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<AstrageldonStar>> ASTRAGELDON_STAR =
            Register.register("astrageldon_star", location -> EntityType.Builder.of(AstrageldonStar::new, MobCategory.MISC)
                    .sized(42F / 32, 42F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<AstrageldonLaser>> ASTRAGELDON_LASER =
            Register.register("astrageldon_laser", location -> EntityType.Builder.of(AstrageldonLaser::new, MobCategory.MISC)
                    .sized(12F / 32, 12F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<NovaSlime>> NOVA_SLIME =
            Register.register("nova_slime", location -> EntityType.Builder.of(NovaSlime::new, MobCategory.MONSTER)
                    .sized(64F / 32, 40F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<NebulaResidue>> NEBULA_RESIDUE =
            Register.register("nebula_residue", location -> EntityType.Builder.of(NebulaResidue::new, MobCategory.MISC)
                    .sized(34F / 32, 34F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<AstrageldonStar2>> ASTRAGELDON_STAR_2 =
            Register.register("astrageldon_star_2", location -> EntityType.Builder.of(AstrageldonStar2::new, MobCategory.MISC)
                    .sized(36F / 32, 36F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<NovaSlimer>> NOVA_SLIMER =
            Register.register("nova_slimer", location -> EntityType.Builder.of(NovaSlimer::new, MobCategory.MONSTER)
                    .sized(52F / 32, 44F / 32)
                    .fireImmune()
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<SlimerSpear>> SLIMER_SPEAR =
            Register.register("slimer_spear", location -> EntityType.Builder.of(SlimerSpear::new, MobCategory.MISC)
                    .sized(48F / 32, 48F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<AstrageldonAfterimage>> ASTRAGELDON_AFTERIMAGE =
            Register.register("astrageldon_afterimage", location -> EntityType.Builder.of(AstrageldonAfterimage::new, MobCategory.MISC)
                    .sized(170F / 32, 170F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<AstrageldonStar3>> ASTRAGELDON_STAR_3 =
            Register.register("astrageldon_star_3", location -> EntityType.Builder.of(AstrageldonStar3::new, MobCategory.MISC)
                    .sized(42F / 32, 42F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static final DeferredHolder<EntityType<?>, EntityType<StarLaser>> STAR_LASER =
            Register.register("star_laser", location -> EntityType.Builder.of(StarLaser::new, MobCategory.MISC)
                    .sized(8F / 32, 8F / 32)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath()));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
