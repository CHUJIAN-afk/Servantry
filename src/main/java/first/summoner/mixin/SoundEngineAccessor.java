package first.summoner.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.sounds.SoundEngine.class)
public interface SoundEngineAccessor {
    @Accessor
    Multimap<SoundSource, SoundInstance> getInstanceBySource();
}
