package first.summoner.common.minion.minion.goal.terraprism;

import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.minion.Terraprism;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

public class TerraprismPrepGoal extends MinionGoal<Terraprism> {

    private PathNode prep;

    public TerraprismPrepGoal(Terraprism minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isTarget(minion.getTarget()) && !minion.attacking && minion.getRandom().nextDouble() < 0.1;
    }

    @Override
    public boolean canContinueToUse() {
        return minion.isTarget(minion.getTarget()) && !minion.attacking;
    }

    @Override
    public void start() {
        prep = new PathNode(minion.getPos().add(0, 2, 0), minion.getYaw(), minion.getPitch(), minion.getRoll());
    }

    @Override
    public void tick() {
        Vec3 toTarget = minion.getTarget().getBoundingBox().getCenter().subtract(minion.getPos());
        Vec3 bladeNormal = toTarget.cross(new Vec3(0, 1, 0)).normalize();
        PathNode prepNode = minion.getEulerNode(minion.getPos(), toTarget, bladeNormal);
        prep = new PathNode(prep.pos(), prepNode.yaw(), prepNode.pitch(), prepNode.roll());
        minion.setPath(Collections.singletonList(minion.getCurrentPathNode().lerp(prep, 0.25f)));
        // 检测是否到达准备位置
        if (minion.getPos().distanceToSqr(prep.pos()) < 0.05) {
            minion.attacking = true;
        }
    }
}
