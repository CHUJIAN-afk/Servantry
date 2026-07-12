package first.servantry.api.common.attachment;

import first.servantry.network.BatchedParticlesPayload;
import first.servantry.register.ServantryAttachmentRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class BatchedParticlesData {

    public static void handler(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!level.isClientSide()) {
            BatchedParticlesData data = level.getData(ServantryAttachmentRegister.BatchedParticles);
            if (data.size() > 0) {
                PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new BatchedParticlesPayload(data.drain()));
            }
        }
    }

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
