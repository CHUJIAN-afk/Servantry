package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AstrageldonStar extends AstrageldonProjectile {

    private float angle;
    private float orbitCounter;
    private Vec3 sphereDirection = Vec3.ZERO;
    private int alpha = 255;

    public AstrageldonStar(EntityType<? extends AstrageldonStar> type, Level level) {
        super(type, level, 360);
        damage = AstrageldonCombat.STAR_DAMAGE;
    }

    public static void spawn(Astrageldon boss, float angle) {
        spawn(boss, angle, Vec3.ZERO);
    }

    public static void spawn(Astrageldon boss, Vec3 direction) {
        spawn(boss, 0, direction);
    }

    private static void spawn(Astrageldon boss, float angle, Vec3 direction) {
        AstrageldonStar star = new AstrageldonStar(SummonerEntityRegister.ASTRAGELDON_STAR.get(), boss.level());
        star.setOwner(boss);
        star.angle = angle;
        star.sphereDirection = direction;
        Vec3 outward = direction.lengthSqr() > 0 ? direction : star.direction(Math.toRadians(angle));
        star.setCenterPose(boss.getBoundingBox().getCenter().add(outward.scale(140.0 / 32)), outward);
        boss.level().addFreshEntity(star);
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.FLOAT, () -> angle, value -> angle = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> orbitCounter, value -> orbitCounter = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> sphereDirection, value -> sphereDirection = value);
        fields.field(LyraStreamCodecs.INT, () -> alpha, value -> alpha = value);
    }

    @Override
    protected void step() {
        if (getOwner() instanceof Astrageldon boss && boss.isAlive()) {
            alpha = life < 28 ? Math.min(alpha + 9, 255) : Math.max(alpha - 10, 0);
            float increment = (40 - age) / 70f;
            if (age > 40) {
                increment *= 1 - (age - 40) / 30f;
            }
            orbitCounter += increment;
            if (age + 1 >= 70) {
                // 从上一帧已同步的星体姿态发射，不再追加一次随 Boss 位移。
                Vec3 outward = Vec3.directionFromRotation(getXRot(), getYRot());
                Vec3 tangent = new Vec3(outward.z, 0, -outward.x).normalize();
                for (int i = -1; i <= 1; i++) {
                    double spread = Math.toRadians(i * 4);
                    Vec3 shot = outward.scale(Math.cos(spread)).add(tangent.scale(Math.sin(spread)));
                    AstrageldonLaser.spawn(boss, getBoundingBox().getCenter(), shot.scale(5), damage);
                }
                discard();
            } else {
                double orbitAngle = Math.toRadians(angle + orbitCounter * 10);
                Vec3 outward = sphereDirection.lengthSqr() > 0 ? sphereDirection : direction(orbitAngle);
                Vec3 center = boss.getBoundingBox().getCenter().add(outward.scale((140 + orbitCounter * 0.5) / 32));
                setCenterPose(center, outward);
                setRoll(0);
            }
        } else {
            discard();
        }
    }

    private Vec3 direction(double rotation) {
        double heading = Math.toRadians(angle);
        return new Vec3(Math.cos(heading) * Math.cos(rotation), -Math.sin(rotation), Math.sin(heading) * Math.cos(rotation));
    }

    @Override
    protected void onHitPlayer(Player player) {
        AstrageldonCombat.astralBlight(player);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Angle", angle);
        tag.putFloat("OrbitCounter", orbitCounter);
        tag.putDouble("SphereX", sphereDirection.x);
        tag.putDouble("SphereY", sphereDirection.y);
        tag.putDouble("SphereZ", sphereDirection.z);
        tag.putInt("Alpha", alpha);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        angle = tag.getFloat("Angle");
        orbitCounter = tag.getFloat("OrbitCounter");
        sphereDirection = new Vec3(tag.getDouble("SphereX"), tag.getDouble("SphereY"), tag.getDouble("SphereZ"));
        alpha = tag.getInt("Alpha");
    }
}
