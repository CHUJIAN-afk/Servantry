package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.entity.AttachmentEntityDamageSource;
import first.lyra.common.entity.PathNode;
import first.lyra.register.LyraDamageRegister;
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
    private static final int SPHERE_POINT_COUNT = 36;
    private static final int SCATTER_COUNT = 3;
    private static final double SCATTER_ANGLE = Math.toRadians(5.0D);

    private final List<MeltdownStar> orbitingStars = new ArrayList<>(SPHERE_POINT_COUNT * SCATTER_COUNT);
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
            pitch = (float) Math.toDegrees(Math.asin(-direction.y));
        }

        float interpolation = chargeTicks < ORB_FORM_TICK
                ? 0.2F
                : 0.2F * (CHARGE_DURATION - chargeTicks) / ORB_FORM_TICK;
        PathNode targetNode = new PathNode(targetPos, yaw, pitch, startNode.roll());
        doomsdayStar.setPath(List.of(startNode.lerp(targetNode, interpolation)));
        chargeTicks++;

        if (chargeTicks == ORB_FORM_TICK) {
            for (int i = 0; i < SPHERE_POINT_COUNT; i++) {
                double y = 1.0D - 2.0D * (i + 0.5D) / SPHERE_POINT_COUNT;
                double horizontalRadius = Math.sqrt(1.0D - y * y);
                double phi = i * Math.PI * (3.0D - Math.sqrt(5.0D));
                Vec3 sphereDirection = new Vec3(
                        Math.cos(phi) * horizontalRadius,
                        y,
                        Math.sin(phi) * horizontalRadius
                ).normalize();
                Vec3 reference = Math.abs(sphereDirection.y) < 0.999D ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 tangentA = sphereDirection.cross(reference).normalize();
                Vec3 tangentB = sphereDirection.cross(tangentA).normalize();

                for (int j = 0; j < SCATTER_COUNT; j++) {
                    double angle = Math.TAU * j / SCATTER_COUNT;
                    Vec3 tangent = tangentA.scale(Math.cos(angle)).add(tangentB.scale(Math.sin(angle)));
                    Vec3 launchDirection = sphereDirection.scale(Math.cos(SCATTER_ANGLE))
                            .add(tangent.scale(Math.sin(SCATTER_ANGLE)))
                            .normalize();
                    MeltdownStar star = new MeltdownStar();
                    star.setDamage(doomsdayStar.getAttackDamage());
                    star.setDamageSourceSupplier((level, player) -> new AttachmentEntityDamageSource(
                            LyraDamageRegister.getDamageTypeHolder(LyraDamageRegister.Summon, level),
                            doomsdayStar.getHurtEntity(),
                            doomsdayStar.getHurtEntity(),
                            star.getPos(),
                            star
                    ));
                    star.orbit(doomsdayStar, launchDirection);
                    star.join(doomsdayStar.getLevel());
                    orbitingStars.add(star);
                }
            }
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
