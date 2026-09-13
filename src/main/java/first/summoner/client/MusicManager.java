package first.summoner.client;

import first.summoner.mixin.SoundEngineAccessor;
import first.summoner.mixin.SoundManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Map;

public class MusicManager {

    private static final Map<SoundEvent, MusicInstance> MusicSoundData = new HashMap<>();

    public static void play(Holder<SoundEvent> soundEvent) {
        MusicInstance musicInstance = MusicSoundData.computeIfAbsent(soundEvent.value(), MusicInstance::new);
        musicInstance.time = 10;
    }

    public static void stop(Holder<SoundEvent> soundEvent) {
        MusicInstance musicInstance = MusicSoundData.get(soundEvent.value());
        if (musicInstance != null) {
            musicInstance.time = 0;
        }
    }

    public static void handler(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            SoundManager soundManager = minecraft.getSoundManager();
            if (!MusicSoundData.isEmpty()) {
                SoundEngine soundEngine = ((SoundManagerAccessor) soundManager).getSoundEngine();
                SoundEngineAccessor engineAccessor = (SoundEngineAccessor) soundEngine;
                engineAccessor.getInstanceBySource().forEach((soundSource, instance) -> {
                    if (soundSource == SoundSource.MUSIC && !(instance instanceof MusicInstance)) {
                        soundEngine.stop(instance);
                    }
                });
            }
            MusicSoundData.values().removeIf(musicInstance -> {
                if (musicInstance.time <= 0) {
                    soundManager.stop(musicInstance);
                    return true;
                } else {
                    if (!soundManager.isActive(musicInstance)) {
                        soundManager.play(musicInstance);
                    }
                }
                musicInstance.time--;
                return false;
            });
        } else {
            MusicSoundData.clear();
        }
    }

    private static final class MusicInstance extends AbstractSoundInstance {

        private int time = 0;

        private MusicInstance(SoundEvent sound) {
            super(sound, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.relative = true;
            this.volume = 1.0F;
        }
    }
}
