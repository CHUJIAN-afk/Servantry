package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.entity.AttachmentEntityDamageSource;
import first.lyra.register.LyraDamageRegister;
import first.summoner.common.master.DoomsdayStar;
import first.summoner.common.projectile.StellarDebris;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DoomsdayStarOrbitGoal extends DoomsdayStarGoal {

    private static final int DEBRIS_PER_RING = 18;
    private static final int RING_COUNT = 4;
    private static final int ORBIT_DURATION = 100;
    private static final double DEBRIS_RADIUS_X = 6.0D;
    private static final double DEBRIS_RADIUS_Y = 3.6D;
    private static final double[] DEBRIS_SPEEDS = {0.08D, -0.08D, 0.06D, -0.06D};
    private static final double AXIS_ROTATION_SPEED = 0.035D;
    private static final double TARGET_DISTANCE = 4.0D;
    private static final double TANGENT_SPEED = 0.4D;
    private static final double VELOCITY_DAMPING = 0.2D;
    private static final double RADIAL_CORRECTION = 0.08D;
    private static final double VERTICAL_CORRECTION = 0.08D;

    private final List<StellarDebris> debris = new ArrayList<>(DEBRIS_PER_RING * RING_COUNT);
    private double axisAngle;
    private int ticks;

    public DoomsdayStarOrbitGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(goalState, doomsdayStar);
    }

    @Override
    public void start() {
        axisAngle = 0.0D;
        ticks = 0;
        debris.clear();
        doomsdayStar.setBlockCollision(false);
        doomsdayStar.setGravity(0.0F);
        doomsdayStar.setDrag(0.98F);
        doomsdayStar.setVelocity(Vec3.ZERO);
        doomsdayStar.setFallLaserIndicatorRender(false);

        Vec3 firstAxisA = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 firstAxisB = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 secondAxisA = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 secondAxisB = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3[] ringAxisA = {firstAxisA, secondAxisA, firstAxisB, secondAxisB};
        Vec3[] ringAxisB = {firstAxisB, secondAxisB, firstAxisA, secondAxisA};
        for (int ring = 0; ring < RING_COUNT; ring++) {
            for (int i = 0; i < DEBRIS_PER_RING; i++) {
                double angle = Math.TAU * i / DEBRIS_PER_RING;
                StellarDebris piece = new StellarDebris();
                piece.setDamage(doomsdayStar.getAttackDamage());
                piece.setDamageSourceSupplier((level, player) -> new AttachmentEntityDamageSource(
                        LyraDamageRegister.getDamageTypeHolder(LyraDamageRegister.Summon, level),
                        doomsdayStar.getHurtEntity(),
                        doomsdayStar.getHurtEntity(),
                        piece.getPos(),
                        piece
                ));
                piece.orbit(
                        doomsdayStar,
                        DEBRIS_RADIUS_X,
                        DEBRIS_RADIUS_Y,
                        angle,
                        DEBRIS_SPEEDS[ring]
                );
                piece.setRingFrame(ringAxisA[ring], ringAxisB[ring]);
                piece.join(doomsdayStar.getLevel());
                debris.add(piece);
            }
        }
    }

    @Override
    public void tick() {
        if (doomsdayStar.getLevel().isClientSide()) {
            return;
        }

        ticks++;
        if (ticks > ORBIT_DURATION) {
            doomsdayStar.setGoalState(DoomsdayStar.GoalState.JUMP);
            return;
        }

        LivingEntity target = doomsdayStar.getTarget();
        if (target == null) {
            doomsdayStar.setVelocity(Vec3.ZERO);
            return;
        }

        axisAngle += AXIS_ROTATION_SPEED;
        double cos = Math.cos(axisAngle);
        double sin = Math.sin(axisAngle);
        Vec3 firstAxisA = new Vec3(cos, -sin, 0.0D);
        Vec3 firstAxisB = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 secondAxisA = new Vec3(sin, cos, 0.0D);
        Vec3 secondAxisB = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3[] ringAxisA = {firstAxisA, secondAxisA, firstAxisB, secondAxisB};
        Vec3[] ringAxisB = {firstAxisB, secondAxisB, firstAxisA, secondAxisA};
        for (int ring = 0; ring < RING_COUNT; ring++) {
            for (int i = 0; i < DEBRIS_PER_RING; i++) {
                debris.get(ring * DEBRIS_PER_RING + i).setRingFrame(ringAxisA[ring], ringAxisB[ring]);
            }
        }

        Vec3 playerCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 doomsdayStarCenter = doomsdayStar.getPos();
        Vec3 playerOffset = doomsdayStarCenter.subtract(playerCenter);
        double distance = playerOffset.length();
        if (distance <= 1.0E-7D) {
            doomsdayStar.setVelocity(Vec3.ZERO);
            return;
        }

        Vec3 radialDirection = playerOffset.normalize();
        Vec3 tangentDirection = new Vec3(-radialDirection.z, 0.0D, radialDirection.x);
        if (tangentDirection.lengthSqr() > 1.0E-7D) {
            tangentDirection = tangentDirection.normalize();
        }

        Vec3 currentVelocity = doomsdayStar.getVelocity();
        Vec3 desiredVelocity = tangentDirection.scale(TANGENT_SPEED)
                .subtract(radialDirection.scale((distance - TARGET_DISTANCE) * RADIAL_CORRECTION))
                .add(0.0D, (playerCenter.y - doomsdayStarCenter.y) * VERTICAL_CORRECTION, 0.0D);
        doomsdayStar.setVelocity(currentVelocity.lerp(desiredVelocity, VELOCITY_DAMPING));
    }

    @Override
    public void stop() {
        doomsdayStar.setBlockCollision(true);
        for (StellarDebris piece : debris) {
            piece.setRemove();
        }
        debris.clear();
        doomsdayStar.setGravity(-0.08F);
        doomsdayStar.setVelocity(Vec3.ZERO);
    }
}
