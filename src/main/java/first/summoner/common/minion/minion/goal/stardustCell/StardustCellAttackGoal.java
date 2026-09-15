package first.summoner.common.minion.minion.goal.stardustCell;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.PlannedPath;
import first.summoner.common.minion.minion.StardustCell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 星尘细胞攻击Goal。
 * <p>
 * 追踪目标并在射击冷却结束时发射星细胞射弹。
 * </p>
 */
public class StardustCellAttackGoal extends AttachmentEntityGoal<StardustCell> {

    public StardustCellAttackGoal(StardustCell minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        double distance = minion.getPos().distanceTo(targetPos);
        minion.addVelocity(targetPos.subtract(minion.getPos()).normalize().scale(0.025));
        if (minion.getVelocity().length() > 1) {
            minion.setVelocity(minion.getVelocity().normalize());
        }
        if (distance < 16) {
            if (minion.getShootCooldown() <= 0) {
                minion.shootAtTarget(target);
                minion.setShootCooldown(12 + minion.getRandom().nextInt(4));
            }
        } else if (distance < 24) {
            if (!minion.isExecutingPath()) {
                PathNode start = minion.getCurrentPathNode();
                PathNode end = start.modifyPos(targetPos);
                List<PathNode> pathNodeList = new ArrayList<>();
                PlannedPath plannedPath = new PlannedPath(pathNodeList);
                for (int i = 0; i < 4; i++) {
                    pathNodeList.add(start.lerp(end, (float) i / 3));
                }
                plannedPath.run(0, () -> {
                    minion.setPhysics(false);
                    minion.setVelocity(Vec3.ZERO);
                    minion.setTrailTimer(4);
                });
                plannedPath.run(3, () -> {
                    minion.setPhysics(true);
                    minion.setVelocity(Vec3.ZERO.offsetRandom(minion.getRandom(), 1));
                    if (minion.getTarget() != null) {
                        minion.shootAtTarget(target);
                    }
                });
                minion.setPlannedPath(plannedPath);
            }
        }
    }
}
