package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AstraRock extends AstrageldonProjectile {

    private Vec3 target = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private int alpha = 255;
    private boolean orbiting;
    private float angle;
    private float orbitCounter;
    private long orbitStart;
    private float radiusScale = 1;
    private boolean perpendicular;

    public AstraRock(EntityType<? extends AstraRock> type, Level level) {
        super(type, level, 960);
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 target, Vec3 velocity) {
        AstraRock rock = new AstraRock(SummonerEntityRegister.ASTRA_ROCK.get(), owner.level());
        rock.setOwner(owner);
        rock.target = target;
        rock.velocity = velocity;
        rock.setPos(center.add(0, -rock.getBbHeight() / 2, 0));
        rock.addToLevel();
    }

    public static void spawnOrbiting(Astrageldon boss, float angle, float radiusScale, boolean perpendicular) {
        AstraRock rock = new AstraRock(SummonerEntityRegister.ASTRA_ROCK.get(), boss.level());
        rock.setOwner(boss);
        rock.orbiting = true;
        rock.angle = angle;
        rock.radiusScale = radiusScale;
        rock.perpendicular = perpendicular;
        rock.orbitStart = boss.level().getGameTime();
        rock.damage = AstrageldonCombat.STAR_DAMAGE;
        double rotation = Math.toRadians(angle);
        Vec3 outward = perpendicular ? new Vec3(Math.cos(rotation), Math.sin(rotation), 0) : new Vec3(Math.cos(rotation), 0, Math.sin(rotation));
        rock.setCenterPose(boss.getBoundingBox().getCenter().add(outward.scale(radiusScale * 40 / 32)), outward);
        rock.addToLevel();
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> target, value -> target = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.INT, () -> alpha, value -> alpha = value);
        fields.field(LyraStreamCodecs.BOOL, () -> orbiting, value -> orbiting = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> angle, value -> angle = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> orbitCounter, value -> orbitCounter = value);
        fields.field(LyraStreamCodecs.VAR_LONG, () -> orbitStart, value -> orbitStart = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> radiusScale, value -> radiusScale = value);
        fields.field(LyraStreamCodecs.BOOL, () -> perpendicular, value -> perpendicular = value);
    }

    @Override
    protected int substeps() {
        return orbiting ? 6 : 3;
    }

    @Override
    protected void step() {
        alpha = life < 51 ? Math.min(alpha + 5, 255) : Math.max(alpha - 5, 0);
        if (orbiting) {
            if (getOwner() instanceof Astrageldon boss && boss.isAlive()) {
                tickOrbit(boss);
            } else {
                discard();
            }
        } else {
            tickFalling();
        }
    }

    private void tickOrbit(Astrageldon boss) {
        // 两个圆盘共用旋转，保持各自共面且始终互相垂直。
        orbitCounter = (level().getGameTime() - orbitStart) * 3 + (age % 6 + 1) * 0.5f;
        double radius = 40 + Math.min(orbitCounter * 4.4, 680);
        double rotation = Math.toRadians(angle + orbitCounter * 2);
        Vec3 local = perpendicular ? new Vec3(Math.cos(rotation), Math.sin(rotation), 0) : new Vec3(Math.cos(rotation), 0, Math.sin(rotation));
        Vec3 offset = local.scale(radiusScale * radius / 32)
                .xRot((float) Math.toRadians(orbitCounter * 0.75))
                .yRot((float) Math.toRadians(orbitCounter * 0.5));
        Vec3 center = boss.getBoundingBox().getCenter().add(offset);
        Vec3 movement = center.subtract(getBoundingBox().getCenter());
        setCenterPose(center, movement);
    }

    private void tickFalling() {
        int counter = (age + 1) * 6;
        Vec3 center = getBoundingBox().getCenter();
        Vec3 offset = target.subtract(center).normalize().scale(Math.sin(Math.toRadians(counter)) * (3 + counter * 0.01));
        if (counter < 270) {
            velocity = velocity.scale(0.9875);
            center = center.subtract(offset.scale(1.0 / 32));
        } else {
            if (counter == 270) {
                Vec3 spread = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(random.nextDouble() * 5);
                velocity = offset.normalize().scale(-24).add(spread);
            }
            velocity = velocity.add(0, -0.02, 0);
        }
        Vec3 movement = velocity.scale(1.0 / 32);
        setPos(center.add(0, -getBbHeight() / 2, 0));
        if (counter >= 270 && !Entity.collideBoundingBox(this, movement, getBoundingBox(), level(), List.of()).equals(movement)) {
            discard();
        } else {
            setPos(position().add(movement));
        }
        setYRot(PathNode.yawFromDirection(velocity));
        setXRot(PathNode.pitchFromDirection(velocity));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("TargetX", target.x);
        tag.putDouble("TargetY", target.y);
        tag.putDouble("TargetZ", target.z);
        tag.putDouble("VelocityX", velocity.x);
        tag.putDouble("VelocityY", velocity.y);
        tag.putDouble("VelocityZ", velocity.z);
        tag.putInt("Alpha", alpha);
        tag.putBoolean("Orbiting", orbiting);
        tag.putFloat("Angle", angle);
        tag.putFloat("OrbitCounter", orbitCounter);
        tag.putLong("OrbitStart", orbitStart);
        tag.putFloat("RadiusScale", radiusScale);
        tag.putBoolean("Perpendicular", perpendicular);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        target = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        velocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        alpha = tag.getInt("Alpha");
        orbiting = tag.getBoolean("Orbiting");
        angle = tag.getFloat("Angle");
        orbitCounter = tag.getFloat("OrbitCounter");
        orbitStart = tag.contains("OrbitStart") ? tag.getLong("OrbitStart") : level().getGameTime() - age / 6;
        radiusScale = tag.contains("RadiusScale") ? tag.getFloat("RadiusScale") : 1;
        perpendicular = tag.getBoolean("Perpendicular");
    }
}
