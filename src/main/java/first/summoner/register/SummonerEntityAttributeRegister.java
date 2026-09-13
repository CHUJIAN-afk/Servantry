package first.summoner.register;

import first.summoner.Summoner;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Summoner.MODID)
public final class SummonerEntityAttributeRegister {

    @SubscribeEvent
    public static void register(EntityAttributeCreationEvent event) {
        event.put(SummonerEntityRegister.DOOMSDAY_STAR.get(), Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .build());
    }
}
