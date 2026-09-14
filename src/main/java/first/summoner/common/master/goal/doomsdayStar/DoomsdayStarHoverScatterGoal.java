package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.entity.PathNode;
import first.summoner.common.master.DoomsdayStar;
import first.summoner.common.projectile.MeltdownStar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DoomsdayStarHoverScatterGoal extends DoomsdayStarGoal {

    private static final int CHARGE_DURATION = 40;
    private static final int ORB_FORM_TICK = 20;
    private static final int SHOT_COUNT = 3;

    private final List<MeltdownStar> orbitingStars = new ArrayList<>(108);
    private int chargeTicks;
    private int shotsFired;
    private boolean finished;

    public DoomsdayStarHoverScatterGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(goalState, doomsdayStar);
    }

    @Override
    public void start() {
        chargeTicks = 0;
        shotsFired = 0;
        finished = false;
        orbitingStars.clear();
        doomsdayStar.setGravity(0.0F);
        doomsdayStar.setDrag(1.0F);
        doomsdayStar.setVelocity(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (doomsdayStar.getLevel().isClientSide() || finished) {
            return;
        }

        LivingEntity target = doomsdayStar.getTarget();
        if (target == null) {
            doomsdayStar.setVelocity(Vec3.ZERO);
            return;
        }

        PathNode startNode = doomsdayStar.getCurrentPathNode();
        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() + 5.0D, 0.0D);
        Vec3 direction = targetPos.subtract(startNode.pos());
        float yaw = startNode.yaw();
        float pitch = startNode.pitch();
        if (direction.lengthSqr() > 1.0E-7D) {
            direction = direction.normalize();
            yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        }

        float interpolation = chargeTicks < ORB_FORM_TICK ? 0.2F : 0.2F * (CHARGE_DURATION - chargeTicks) / ORB_FORM_TICK;
        PathNode targetNode = new PathNode(targetPos, yaw, pitch, startNode.roll());
        doomsdayStar.setPath(List.of(startNode.lerp(targetNode, interpolation)));
        chargeTicks++;

        if (chargeTicks == ORB_FORM_TICK) {
            MeltdownStar.fire(doomsdayStar, orbitingStars);
        }

        if (chargeTicks >= CHARGE_DURATION) {
            for (MeltdownStar star : orbitingStars) {
                star.launch();
            }
            orbitingStars.clear();
            shotsFired++;
            chargeTicks = 0;
            if (shotsFired >= SHOT_COUNT) {
                finished = true;
                doomsdayStar.setGoalState(DoomsdayStar.GoalState.SLAM);
            }
        }
    }

    @Override
    public void stop() {
        if (!finished) {
            for (MeltdownStar star : orbitingStars) {
                star.launch();
            }
            orbitingStars.clear();
            finished = true;
        }
        doomsdayStar.setVelocity(Vec3.ZERO);
    }
}
