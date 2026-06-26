package first.servantry.common.servant.goal.stardustDragon;

import first.servantry.api.entity.PathNode;
import first.servantry.api.entity.PlannedPath;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 星尘龙跟随目标（非头部体节）。
 * <p>
 * 体节沿前一体节的历史轨迹跟随，不抄近道。
 * 每tick沿前一体节的historyNodes消耗segmentDistance弧长，
 * 到达目标位置后朝向前一体节的姿态。
 * </p>
 */
public class StardustDragonFollowGoal extends ServantGoal<StardustDragon> {

    public StardustDragonFollowGoal(StardustDragon servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return !servant.isHead();
    }

    @Override
    public void tick() {
        StardustDragon preceding = servant.getPrecedingSegment();
        if (preceding == null) return;

        ArrayList<PathNode> history = preceding.getHistoryNodes();
        if (history.size() < 2) return;

        double trailCursor = servant.getSegmentDistance();

        double accumulated = 0;
        Vec3 targetPos = servant.getPos();

        for (int i = 0; i < history.size() - 1; i++) {
            PathNode curr = history.get(i);
            PathNode next = history.get(i + 1);
            double segLen = curr.pos().distanceTo(next.pos());

            if (accumulated + segLen >= trailCursor) {
                float t = segLen > 0.001 ? (float) ((trailCursor - accumulated) / segLen) : 0;
                targetPos = curr.pos().lerp(next.pos(), Mth.clamp(t, 0, 1));
                break;
            }
            accumulated += segLen;

            if (i == history.size() - 2) {
                targetPos = next.pos();
            }
        }
        // 朝向：从自身位置指向前一体节位置
        Vec3 toPreceding = preceding.getPos().subtract(targetPos);
        Vec3 direction = toPreceding.lengthSqr() > 0.0001 ? toPreceding.normalize() : servant.getCurrentVelocity();
        Vec3 segNormal = servant.getCurrentNormal().lerp(preceding.getCurrentNormal(), 0.5f);
        PathNode orientation = servant.getEulerNode(targetPos, direction, segNormal);

        servant.setPath(new PlannedPath("physics", Collections.singletonList(orientation)));
        servant.setDesiredRotation(orientation.yaw(), orientation.pitch(), orientation.roll());
    }
}
