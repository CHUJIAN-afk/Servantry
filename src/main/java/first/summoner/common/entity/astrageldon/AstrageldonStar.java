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
            if (age < 70) {
                float increment = (40 - age) / 70f;
                if (age > 40) {
                    increment *= 1 - (age - 40) / 30f;
                }
                orbitCounter += increment;
                double orbitAngle = Math.toRadians(angle + orbitCounter * 10);
                Vec3 outward = sphereDirection.lengthSqr() > 0 ? sphereDirection : direction(orbitAngle);
                Vec3 center = boss.getBoundingBox().getCenter().add(outward.scale((140 + orbitCounter * 0.5) / 32));
                setCenterPose(center, outward);
                setRoll(0);
                if (age + 1 == 70) {
                    fireLasers(boss, outward);
                }
            } else if (age >= 72) {
                // 保留一个同步 tick 的发射姿态，避免销毁前客户端仍停在旧位置。
                discard();
            }
        } else {
            discard();
        }
    }

    private void fireLasers(Astrageldon boss, Vec3 outward) {
        Vec3 reference = Math.abs(outward.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 tangent = outward.cross(reference).normalize();
        Vec3 bitangent = outward.cross(tangent);
        double spread = Math.toRadians(4);
        for (int i = 0; i < 3; i++) {
            double angle = i * Math.PI * 2 / 3;
            Vec3 side = tangent.scale(Math.cos(angle)).add(bitangent.scale(Math.sin(angle)));
            Vec3 shot = outward.scale(Math.cos(spread)).add(side.scale(Math.sin(spread)));
            AstrageldonLaser.spawn(boss, getBoundingBox().getCenter(), shot.scale(5), damage);
        }
    }

    @Override
    protected boolean canDamage() {
        return age < 70;
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
