package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.entity.PathNode;
import first.summoner.common.master.DoomsdayStar;
import first.summoner.common.projectile.MeltdownStar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DoomsdayStarScatterSlamGoal extends DoomsdayStarGoal {

    private static final int SHOT_COUNT = 3;

    private final List<MeltdownStar> orbitingStars = new ArrayList<>(108);
    private int shotsFired;

    public DoomsdayStarScatterSlamGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(goalState, doomsdayStar);
    }

    @Override
    public void start() {
        shotsFired = 0;
        orbitingStars.clear();
        doomsdayStar.setGravity(-0.08F);
        doomsdayStar.setDrag(0.98F);
        doomsdayStar.setVelocity(Vec3.ZERO);
        doomsdayStar.setFallLaserIndicatorRender(true);
    }

    @Override
    public void tick() {
        if (shotsFired >= SHOT_COUNT) {
            return;
        }
        if (!orbitingStars.isEmpty()) {
            if (doomsdayStar.getVelocity().y() < 0.0D) {
                doomsdayStar.setVelocity(doomsdayStar.getVelocity().scale(0.9));
                if (doomsdayStar.getVelocity().y() < -0.2D) {
                    doomsdayStar.applyForce(new Vec3(0.0D, -0.1D, 0.0D));
                    for (MeltdownStar star : orbitingStars) {
                        star.launch();
                    }
                    orbitingStars.clear();
                    shotsFired++;
                    if (shotsFired >= SHOT_COUNT) {
                        doomsdayStar.setGoalState(DoomsdayStar.GoalState.ORBIT);
                    }
                }
            }
            return;
        }

        LivingEntity target = doomsdayStar.getTarget();
        if (target != null && doomsdayStar.onGround()) {
            PathNode pathNode = doomsdayStar.getCurrentPathNode();
            Vec3 targetDirection = target.getBoundingBox().getCenter().subtract(pathNode.pos());
            float targetYaw = (float) Math.toDegrees(Math.atan2(-targetDirection.x, targetDirection.z));
            doomsdayStar.setCurrentPathNode(new PathNode(pathNode.pos(), targetYaw, 0.0F, 0.0F));

            MeltdownStar.fire(doomsdayStar, orbitingStars);
            Vec3 position = target.position().add(target.position().subtract(doomsdayStar.getPos()).normalize());
            doomsdayStar.jump(new Vec3(position.x(), Math.max(doomsdayStar.getPos().y(), position.y()) + 2.0D + shotsFired * 0.5D, position.z()));
        }
    }

    @Override
    public void stop() {
        for (MeltdownStar star : orbitingStars) {
            star.launch();
        }
        orbitingStars.clear();
        doomsdayStar.setFallLaserIndicatorRender(false);
    }
}
