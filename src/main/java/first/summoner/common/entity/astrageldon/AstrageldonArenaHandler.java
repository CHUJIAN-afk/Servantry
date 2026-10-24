package first.summoner.common.entity.astrageldon;

import first.summoner.Summoner;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Summoner.MODID)
public final class AstrageldonArenaHandler {

    private static final ResourceLocation FLIGHT = Summoner.rl("astrageldon_arena_flight");

    private AstrageldonArenaHandler() {
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (var level : event.getServer().getAllLevels()) {
            var arenas = level.getEntities(SummonerEntityRegister.ASTRAGELDON.get(), Astrageldon::hasArena);
            for (ServerPlayer player : level.players()) {
                Astrageldon arena = player.isAlive() && !player.isSpectator()
                        ? AstrageldonArena.nearest(arenas, player.getBoundingBox().getCenter(), AstrageldonArena.ENTRY_RANGE, 1) : null;
                var flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
                boolean member = flight.hasModifier(FLIGHT);
                if (arena != null) {
                    double outside = player.getBoundingBox().getCenter().distanceTo(arena.getArenaCenter()) - arena.getArenaBoundaryRadius();
                    if (outside > 0 && (!member || outside > arena.getArenaSyncMargin())) {
                        if (member && player.hurt(AstrageldonCombat.source(arena, arena), 16.4f)) {
                            AstrageldonCombat.interstellarCorruption(player);
                        }
                        Vec3 position = AstrageldonArena.entryPosition(player, arena);
                        player.stopRiding();
                        player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
                        player.setDeltaMovement(Vec3.ZERO);
                        player.hurtMarked = true;
                    }
                    if (!member) {
                        flight.addTransientModifier(new AttributeModifier(FLIGHT, 1, AttributeModifier.Operation.ADD_VALUE));
                        player.getAbilities().flying = true;
                        player.onUpdateAbilities();
                    }
                    player.resetFallDistance();
                } else if (member) {
                    flight.removeModifier(FLIGHT);
                    if (!player.mayFly()) {
                        player.getAbilities().flying = false;
                    }
                    player.onUpdateAbilities();
                }
            }
        }
    }
}
