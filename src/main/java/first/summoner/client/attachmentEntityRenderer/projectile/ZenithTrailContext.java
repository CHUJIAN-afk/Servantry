package first.summoner.client.attachmentEntityRenderer.projectile;

import first.lyra.client.render.trail.RibbonTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.Zenith;

import java.util.ArrayList;
import java.util.List;

public class ZenithTrailContext extends RibbonTrailContext<Zenith> {

    @Override
    protected List<InterpolatedNode> buildSmoothNodes(Zenith entity, PathNode visualNode, float partialTick) {
        List<InterpolatedNode> result = new ArrayList<>();
        float tick = entity.getTickCount() + partialTick;
        int count = 60;
        if (tick <= 4) {
            count = (int) (tick * 15);
        }
        if (tick >= 7) {
            count = (int) ((11 - tick) * 15);
        }
        if (count > 0) {
            for (int i = 0; i < count && tick > 0; i++) {
                PathNode node = entity.getRenderNodeFromTickCount(tick);
                result.add(new InterpolatedNode(node.pos(), node.toQuaternion()));
                tick -= 0.05f;
            }
        }
        return result;
    }
}
