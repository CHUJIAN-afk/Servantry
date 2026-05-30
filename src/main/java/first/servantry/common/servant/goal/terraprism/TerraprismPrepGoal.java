package first.servantry.common.servant.goal.terraprism;

import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Terraprism;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

public class TerraprismPrepGoal extends ServantGoal<Terraprism> {

    private PathNode prep;

    public TerraprismPrepGoal(Terraprism servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget()) && !servant.attacking && servant.getOwner().getRandom().nextDouble() < 0.1;
    }

    @Override
    public boolean canContinueToUse() {
        return servant.isTarget(servant.getTarget()) && !servant.attacking;
    }

    @Override
    public void start() {
        prep = new PathNode(servant.getPos().add(0, 2, 0), servant.getYaw(), servant.getPitch(), servant.getRoll());
    }

    @Override
    public void tick() {
        Vec3 toTarget = servant.getTarget().getBoundingBox().getCenter().subtract(servant.getPos());
        Vec3 bladeNormal = toTarget.cross(new Vec3(0, 1, 0)).normalize();
        PathNode prepNode = servant.getEulerNode(servant.getPos(), toTarget, bladeNormal);
        prep = new PathNode(prep.pos(), prepNode.yaw(), prepNode.pitch(), prepNode.roll());
        servant.setPath(Collections.singletonList(servant.getCurrentPathNode().lerp(prep, 0.25f)));
        // 检测是否到达准备位置
        if (servant.getPos().distanceToSqr(prep.pos()) < 0.05) {
            servant.attacking = true;
        }
    }
}
