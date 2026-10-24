package first.summoner.client;

import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public final class AstrageldonMusic {

    private static Holder<SoundEvent> current;

    private AstrageldonMusic() {
    }

    public static void tick() {
        var minecraft = Minecraft.getInstance();
        if (!minecraft.isPaused()) {
            Holder<SoundEvent> next = null;
            if (minecraft.level != null && minecraft.player != null && minecraft.player.isAlive()) {
                double nearest = 200 * 200;
                for (var entity : minecraft.level.entitiesForRendering()) {
                    if (entity instanceof Astrageldon boss && boss.isAlive()) {
                        double distance = boss.distanceToSqr(minecraft.player);
                        if (distance < nearest) {
                            nearest = distance;
                            next = boss.usesSecondPhaseMusic() ? SummonerSoundRegister.DoomsdayStarPhase2 : SummonerSoundRegister.DoomsdayStarPhase1;
                        }
                    }
                }
            }
            if (next != current) {
                if (current != null) {
                    MusicManager.stop(current, 10);
                }
                if (next != null) {
                    MusicManager.play(next);
                    MusicManager.setVolume(next, 0);
                    MusicManager.fadeVolume(next, 1, 10);
                }
                current = next;
            }
            if (current != null) {
                MusicManager.play(current);
            }
        }
    }

    public static void clear() {
        current = null;
    }
}
