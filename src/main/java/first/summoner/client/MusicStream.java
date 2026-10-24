package first.summoner.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

final class MusicStream implements AudioStream {

    private final ByteBuffer data;
    private final long decoder;
    private final AudioFormat format;
    private final int length;
    private final Supplier<Loop> loop;
    private int position;
    private boolean closed;

    MusicStream(ResourceLocation path, double seconds, Supplier<Loop> loop) throws IOException {
        byte[] bytes;
        try (var input = Minecraft.getInstance().getResourceManager().open(path)) {
            bytes = input.readAllBytes();
        }
        data = BufferUtils.createByteBuffer(bytes.length).put(bytes).flip();
        try (var stack = MemoryStack.stackPush()) {
            var error = stack.mallocInt(1);
            decoder = STBVorbis.stb_vorbis_open_memory(data, error, null);
            if (decoder == 0) {
                throw new IOException("Cannot open music " + path + ": " + error.get(0));
            }
            var info = STBVorbis.stb_vorbis_get_info(decoder, STBVorbisInfo.malloc(stack));
            format = new AudioFormat(info.sample_rate(), 16, info.channels(), true, false);
        }
        length = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
        this.loop = loop;
        position = (int) Math.min(seconds * format.getSampleRate(), length);
        STBVorbis.stb_vorbis_seek(decoder, position);
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public synchronized ByteBuffer read(int size) {
        ByteBuffer result = BufferUtils.createByteBuffer(size);
        if (!closed) {
            int channels = format.getChannels();
            var samples = result.asShortBuffer();
            Loop range = loop.get();
            int start = range == null ? 0 : (int) Math.min(range.start * format.getSampleRate(), length);
            int end = range == null ? length : (int) Math.min(range.end * format.getSampleRate(), length);
            boolean repeat = range != null && start < end;
            while (samples.remaining() >= channels) {
                if (repeat && position >= end) {
                    STBVorbis.stb_vorbis_seek(decoder, start);
                    position = start;
                }
                int count = Math.min(samples.remaining() / channels, (repeat ? end : length) - position);
                if (count <= 0) {
                    break;
                }
                samples.limit(samples.position() + count * channels);
                int decoded = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, samples);
                if (decoded == 0) {
                    break;
                }
                samples.position(samples.position() + decoded * channels);
                samples.limit(samples.capacity());
                position += decoded;
            }
            result.limit(samples.position() * Short.BYTES);
        }
        return !closed && result.hasRemaining() ? result : null;
    }

    @Override
    public synchronized void close() {
        if (!closed) {
            STBVorbis.stb_vorbis_close(decoder);
            closed = true;
        }
    }

    record Loop(double start, double end) {

        Loop {
            if (!Double.isFinite(start) || !Double.isFinite(end) || start < 0 || end <= start) {
                throw new IllegalArgumentException("Music loop requires 0 <= start < end");
            }
        }
    }
}
