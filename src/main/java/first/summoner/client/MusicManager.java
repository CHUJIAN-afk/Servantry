package first.summoner.client;

import first.summoner.mixin.SoundEngineAccessor;
import first.summoner.mixin.SoundManagerAccessor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class MusicManager {

    private static final Map<SoundEvent, MusicInstance> MUSIC = new HashMap<>();

    public static void play(Holder<SoundEvent> sound) {
        instance(sound).time = 10;
    }

    public static void stop(Holder<SoundEvent> sound) {
        stop(sound, 0);
    }

    public static void stop(Holder<SoundEvent> sound, int fadeTicks) {
        MusicInstance music = MUSIC.get(sound.value());
        if (music != null) {
            music.time = fadeTicks;
            music.fade(0, fadeTicks);
        }
    }

    public static void setVolume(Holder<SoundEvent> sound, float volume) {
        instance(sound).fade(volume, 0);
    }

    public static void fadeVolume(Holder<SoundEvent> sound, float volume, int ticks) {
        instance(sound).fade(volume, ticks);
    }

    public static void setPosition(Holder<SoundEvent> sound, double seconds) {
        MusicInstance music = instance(sound);
        MusicInstance replacement = new MusicInstance(sound.value());
        replacement.position = Math.max(0, seconds);
        replacement.time = music.time;
        replacement.fade(music.getVolume(), 0);
        replacement.targetVolume = music.targetVolume;
        replacement.fadeTicks = music.fadeTicks;
        replacement.loop = music.loop;
        release(music);
        MUSIC.put(sound.value(), replacement);
    }

    public static void setLoop(Holder<SoundEvent> sound, double startSeconds, double endSeconds) {
        instance(sound).loop = new MusicStream.Loop(startSeconds, endSeconds);
    }

    public static void clearLoop(Holder<SoundEvent> sound) {
        instance(sound).loop = null;
    }

    private static MusicInstance instance(Holder<SoundEvent> sound) {
        return MUSIC.computeIfAbsent(sound.value(), MusicInstance::new);
    }

    private static void release(MusicInstance music) {
        Minecraft.getInstance().getSoundManager().stop(music);
        music.close();
    }

    public static void clear() {
        MUSIC.values().forEach(MusicManager::release);
        MUSIC.clear();
    }

    public static void handler(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            if (!minecraft.isPaused()) {
                var soundManager = minecraft.getSoundManager();
                if (!MUSIC.isEmpty()) {
                    var engine = ((SoundManagerAccessor) soundManager).getSoundEngine();
                    ((SoundEngineAccessor) engine).getInstanceBySource().forEach((source, sound) -> {
                        if (source == SoundSource.MUSIC && !(sound instanceof MusicInstance)) {
                            engine.stop(sound);
                        }
                    });
                }
                MUSIC.values().removeIf(music -> {
                    boolean expired = music.time <= 0;
                    if (expired) {
                        release(music);
                    } else {
                        if (!music.started && minecraft.options.getSoundSourceVolume(SoundSource.MUSIC) > 0
                                && minecraft.options.getSoundSourceVolume(SoundSource.MASTER) > 0) {
                            soundManager.play(music);
                            music.started = true;
                        }
                        music.time--;
                    }
                    return expired;
                });
            }
        } else {
            clear();
        }
    }

    private static final class MusicInstance extends AbstractTickableSoundInstance {

        private int time;
        private boolean started;
        private double position;
        private float targetVolume = 1;
        private int fadeTicks;
        private volatile MusicStream.Loop loop;
        private CompletableFuture<MusicStream> stream;

        private MusicInstance(SoundEvent sound) {
            super(sound, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            attenuation = Attenuation.NONE;
            relative = true;
            volume = 1;
        }

        private void fade(float target, int ticks) {
            targetVolume = Mth.clamp(target, 0, 1);
            fadeTicks = Math.max(0, ticks);
            if (fadeTicks == 0) {
                volume = targetVolume;
            }
        }

        @Override
        public void tick() {
            if (fadeTicks > 0) {
                volume += (targetVolume - volume) / fadeTicks;
                fadeTicks--;
            }
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public @NotNull CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary buffers, @NotNull Sound sound, boolean looping) {
            stream = CompletableFuture.supplyAsync(() -> {
                try {
                    return new MusicStream(sound.getPath(), position, () -> loop);
                } catch (IOException exception) {
                    throw new CompletionException(exception);
                }
            }, Util.nonCriticalIoPool());
            return stream.thenApply(value -> value);
        }

        private void close() {
            stop();
            if (stream != null) {
                stream.thenAccept(MusicStream::close);
            }
        }
    }
}
