package first.servantry.api.common.attachment;

import first.servantry.network.BatchedParticlesPayload;
import net.minecraft.core.particles.ParticleOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量粒子累积附件（挂载于 {@link net.minecraft.world.level.Level}）。
 * <p>
 * 服务端在单 tick 内多次调用 {@link first.servantry.utils.ParticleHelper#emit} 时，
 * 将每条粒子记录累积到此附件，避免每个粒子单独发送网络包。
 * tick 末由 {@link first.servantry.common.event.Event#tick} 一次性取出并打包为
 * {@link BatchedParticlesPayload} 下发，随后清空。
 * </p>
 * <p>
 * 仅服务端使用：客户端 {@code emit} 直接调用 {@link net.minecraft.world.level.Level#addParticle} 生成粒子，
 * 不经过此附件。
 * </p>
 */
public class BatchedParticlesData {

    private final List<BatchedParticlesPayload.Entry> entries = new ArrayList<>();

    /** 累积一条粒子记录 */
    public void add(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {
        entries.add(new BatchedParticlesPayload.Entry(options, x, y, z, vx, vy, vz));
    }

    /** 取出并清空当前累积的所有粒子记录 */
    public List<BatchedParticlesPayload.Entry> drain() {
        List<BatchedParticlesPayload.Entry> snapshot = new ArrayList<>(entries);
        entries.clear();
        return snapshot;
    }

    /** 当前累积粒子数 */
    public int size() {
        return entries.size();
    }
}
