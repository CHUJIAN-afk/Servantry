package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.master.DoomsdayStar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DoomsdayStarJumpGoal extends DoomsdayStarGoal {

    private int index = 0;
    private int cooldown = 12;

    public DoomsdayStarJumpGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(goalState, doomsdayStar);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && doomsdayStar.getTarget() != null;
    }

    @Override
    public void start() {
        index = 0;
        cooldown = 12;
        doomsdayStar.setFallLaserIndicatorRender(true);
    }

    @Override
    public void tick() {
        LivingEntity target = doomsdayStar.getTarget();
        if (target != null) {
            PathNode pathNode = doomsdayStar.getCurrentPathNode();
            Vec3 direction = target.getBoundingBox().getCenter().subtract(pathNode.pos());
            float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            doomsdayStar.setCurrentPathNode(new PathNode(pathNode.pos(), targetYaw, 0, 0));
            if (doomsdayStar.onGround()) {
                if (index > 4) {
                    doomsdayStar.setGoalState(DoomsdayStar.GoalState.HOVER_SCATTER);
                    return;
                }
                if (--cooldown <= 0) {
                    Vec3 position = target.position().add(target.position().subtract(doomsdayStar.getPos()).normalize());
                    doomsdayStar.jump(new Vec3(position.x(), Math.max(doomsdayStar.getPos().y(), position.y()) + 2 + index * 0.5f, position.z()));
                    cooldown = index < 2 ? 12 : 3;
                    index++;
                    if (index == 4) {
                        doomsdayStar.fireDoomsdayStarGel();
                    }
                }
            } else {
                Vec3 velocity = doomsdayStar.getVelocity();
                if (velocity.y() < 0) {
                    doomsdayStar.setVelocity(velocity.scale(0.9));
                    if (velocity.y() < -0.2) {
                        doomsdayStar.setVelocity(velocity.add(0, -index, 0));
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        doomsdayStar.setFallLaserIndicatorRender(false);
    }
}
