package first.servantry.api;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static first.servantry.api.ParticleUtils.getRandom;

public class ServantryUtil {

    /**
     * 播放音效
     *
     * @param level       维度
     * @param center      音效中心位置
     * @param soundEvent  音效事件
     * @param soundSource 音效源
     */
    public static void playSound(Level level, Vec3 center, SoundEvent soundEvent, SoundSource soundSource) {
        level.playSound(null, center.x(), center.y(), center.z(), soundEvent, soundSource, 1.0f, getRandom().nextFloat(0.4f, 0.8f));
    }

}
