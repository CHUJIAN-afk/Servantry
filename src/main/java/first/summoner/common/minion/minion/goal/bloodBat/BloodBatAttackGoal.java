package first.summoner.common.minion.minion.goal.bloodBat;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.Ellipse;
import first.summoner.common.minion.minion.BloodBat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BloodBatAttackGoal extends AttachmentEntityGoal<BloodBat> {

    public BloodBatAttackGoal(BloodBat minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void start() {
        minion.progress = 0;
        minion.hasDamaged = false;
        minion.hitTargets.clear();
        Vec3 idlePos = minion.getInterpolatedIdleState(1).pos();
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        minion.normal = Ellipse.randomPlaneNormal(minion.getRandom(), targetPos, idlePos);
    }

    @Override
    public void stop() {
        minion.progress = 0;
        minion.hasDamaged = false;
        minion.hitTargets.clear();
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        if (minion.isTargetChange() && !minion.hasDamaged()) {
            // 未造成伤害前目标切换：直接回归待机
            minion.prep = false;
            return;
        }
        // 椭圆轨迹推进：从待机位置（起点）飞向目标（中点）再折返
        minion.progress += 1f / 22f;
        if (minion.progress >= 1) {
            minion.prep = false;
            return;
        }
        // 以待机位置与锁定目标位置为支点（跨帧追踪：每 tick 用实时位置重建）
        Vec3 idlePos = minion.getInterpolatedIdleState(1).pos();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        minion.setCurrentPathNode(minion.getEllipseNode(minion.progress, idlePos, targetPos));
    }
}
