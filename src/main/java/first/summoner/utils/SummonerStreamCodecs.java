package first.summoner.utils;

import first.lyra.common.attachmentEntity.Ellipse;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.utils.LyraStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface SummonerStreamCodecs extends LyraStreamCodecs {
    StreamCodec<RegistryFriendlyByteBuf, Ellipse> ELLIPSE = StreamCodec.composite(LyraStreamCodecs.VEC_3, Ellipse::getPointA, LyraStreamCodecs.VEC_3, Ellipse::getPointB, LyraStreamCodecs.VEC_3, Ellipse::getPlaneNormal, LyraStreamCodecs.FLOAT, Ellipse::getCurvature, Ellipse::new);

    StreamCodec<RegistryFriendlyByteBuf, List<PathNode>> PATH_NODE_LIST = StreamCodec.of((buf, list) -> {
        buf.writeVarInt(list.size());
        for (PathNode pathNode : list) {
            SummonerStreamCodecs.PATH_NODE.encode(buf, pathNode);
        }
    }, buf -> {
        int size = buf.readVarInt();
        List<PathNode> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(SummonerStreamCodecs.PATH_NODE.decode(buf));
        }
        return ids;
    });

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
