package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.PathNode;
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
    private int alpha = 255;

    public AstrageldonStar(EntityType<? extends AstrageldonStar> type, Level level) {
        super(type, level, 360);
        damage = AstrageldonCombat.STAR_DAMAGE;
    }

    public static void spawn(Astrageldon boss, float angle) {
        AstrageldonStar star = new AstrageldonStar(SummonerEntityRegister.ASTRAGELDON_STAR.get(), boss.level());
        star.setOwner(boss);
        star.angle = angle;
        star.setPos(boss.getBoundingBox().getCenter().add(0, -star.getBbHeight() / 2, 0));
        boss.level().addFreshEntity(star);
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.FLOAT, () -> angle, value -> angle = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> orbitCounter, value -> orbitCounter = value);
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
            double orbitAngle = Math.toRadians(angle + orbitCounter * 10);
            Vec3 outward = direction(orbitAngle);
            Vec3 center = boss.getBoundingBox().getCenter().add(outward.scale((140 + orbitCounter * 0.5) / 32)).add(0, 0.5, 0);
            setPos(center.add(0, -getBbHeight() / 2, 0));
            setYRot(PathNode.yawFromDirection(outward));
            setXRot(PathNode.pitchFromDirection(outward));
            setRoll(Math.max(0, (50 - age) * 14.4f));
            if (age + 1 >= 70) {
                for (int i = -1; i <= 1; i++) {
                    AstrageldonLaser.spawn(boss, center, direction(orbitAngle + Math.toRadians(i * 4)).scale(5), damage);
                }
                discard();
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
        tag.putInt("Alpha", alpha);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        angle = tag.getFloat("Angle");
        orbitCounter = tag.getFloat("OrbitCounter");
        alpha = tag.getInt("Alpha");
    }
}
