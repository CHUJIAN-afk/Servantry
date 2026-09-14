package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.master.DoomsdayStar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DoomsdayStarSlamGoal extends DoomsdayStarGoal {

    private static final int JUMP_COUNT = 5;
    private static final int INTERPOLATION_TICKS = 40;
    private static final int GROUND = 0;
    private static final int AIRBORNE = 1;
    private static final int ASCENDING = 2;
    private static final int INTERPOLATING = 3;
    private static final int SLAMMING = 4;
    private static final int FINISHED = 5;

    private int phase;
    private int index;
    private int ticks;

    public DoomsdayStarSlamGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(goalState, doomsdayStar);
    }

    @Override
    public void start() {
        phase = GROUND;
        index = 0;
        ticks = 0;
        doomsdayStar.setGravity(-0.08F);
        doomsdayStar.setDrag(0.98F);
        doomsdayStar.setVelocity(Vec3.ZERO);
        doomsdayStar.setFallLaserIndicatorRender(true);
    }

    @Override
    public void tick() {
        if (doomsdayStar.getLevel().isClientSide() || phase == FINISHED) {
            return;
        }

        if (phase == SLAMMING) {
            doomsdayStar.addVelocity(new Vec3(0.0D, -0.1D, 0.0D));
            if (doomsdayStar.onGround()) {
                doomsdayStar.setVelocity(Vec3.ZERO);
                phase = FINISHED;
                doomsdayStar.setGoalState(DoomsdayStar.GoalState.SCATTER_SLAM);
            }
            return;
        }

        LivingEntity target = doomsdayStar.getTarget();
        if (target == null) {
            return;
        }

        if (phase == INTERPOLATING) {
            Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() + 256.0D, 0.0D);
            if (ticks >= INTERPOLATION_TICKS) {
                phase = SLAMMING;
                return;
            }

            PathNode currentNode = doomsdayStar.getCurrentPathNode();
            PathNode targetNode = new PathNode(targetPos, currentNode.yaw(), currentNode.pitch(), currentNode.roll());
            doomsdayStar.setPath(List.of(currentNode.lerp(targetNode, 1.0F / (INTERPOLATION_TICKS - ticks))));
            ticks++;
            return;
        }

        if (phase == ASCENDING) {
            if (doomsdayStar.getVelocity().y() <= 0.0D) {
                phase = INTERPOLATING;
                ticks = 0;
                doomsdayStar.setVelocity(Vec3.ZERO);
            }
            return;
        }

        if (phase == GROUND) {
            if (doomsdayStar.onGround()) {
                Vec3 pos = doomsdayStar.getPos();
                doomsdayStar.jump(new Vec3(pos.x, pos.y + (index + 1) * 5.0D, pos.z));
                doomsdayStar.setOnGround(false);
                phase = AIRBORNE;
            }
            return;
        }

        Vec3 velocity = doomsdayStar.getVelocity();
        if (doomsdayStar.onGround()) {
            index++;
            doomsdayStar.fireDoomsdayStarGel();
            if (index >= 3) {
                Vec3 pos = doomsdayStar.getPos();
                doomsdayStar.jump(new Vec3(pos.x, pos.y + 25.0D, pos.z));
                doomsdayStar.setOnGround(false);
                phase = ASCENDING;
            } else {
                phase = GROUND;
            }
        } else if (velocity.y() < 0.0D) {
            doomsdayStar.setVelocity(velocity.add(0.0D, -index * 2, 0.0D));
        }
    }

    @Override
    public void stop() {
        doomsdayStar.setFallLaserIndicatorRender(false);
    }
}
