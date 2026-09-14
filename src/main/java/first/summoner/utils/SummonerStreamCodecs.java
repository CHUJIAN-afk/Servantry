package first.summoner.utils;

import first.lyra.utils.LyraStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

public interface SummonerStreamCodecs extends LyraStreamCodecs {
    StreamCodec<RegistryFriendlyByteBuf, Set<Integer>> SET_INT = StreamCodec.of((buf, ids) -> {
        buf.writeVarInt(ids.size());
        for (Integer id : ids) {
            buf.writeVarInt(id);
        }
    }, buf -> {
        int size = buf.readVarInt();
        Set<Integer> ids = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readVarInt());
        }
        return ids;
    });
    StreamCodec<RegistryFriendlyByteBuf, AABB> AABB = StreamCodec.composite(LyraStreamCodecs.VEC_3, net.minecraft.world.phys.AABB::getMinPosition, LyraStreamCodecs.VEC_3, net.minecraft.world.phys.AABB::getMaxPosition, AABB::new);
}
