package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AstrageldonStar2 extends AstrageldonProjectile {

    private Vec3 velocity = Vec3.ZERO;
    private float spin;
    private float scale = 1;

    public AstrageldonStar2(EntityType<? extends AstrageldonStar2> type, Level level) {
        super(type, level, 180);
    }

    public static void spawn(Astrageldon boss, Vec3 velocity, float spin) {
        spawn(boss, boss.getBoundingBox().getCenter(), velocity, spin);
    }

    public static void spawn(Astrageldon boss, Vec3 center, Vec3 velocity, float spin) {
        AstrageldonStar2 star = new AstrageldonStar2(SummonerEntityRegister.ASTRAGELDON_STAR_2.get(), boss.level());
        star.setOwner(boss);
        star.velocity = new Vec3(velocity.x, 12, velocity.z);
        star.spin = spin;
        star.setCenterPose(center, star.velocity);
        boss.level().addFreshEntity(star);
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> spin, value -> spin = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> scale, value -> {
            scale = value;
            refreshDimensions();
        });
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return super.getDimensions(pose).scale(scale);
    }

    @Override
    protected void step() {
        if (getOwner() instanceof Astrageldon boss && boss.isAlive()) {
            Vec3 center = getBoundingBox().getCenter().add(velocity.scale(1.0 / 32));
            velocity = velocity.multiply(1.018, 1, 1.018).yRot((float) Math.toRadians(1.5 * spin)).add(0, -0.3, 0);
            scale *= 0.998f;
            refreshDimensions();
            setCenterPose(center, velocity);
        } else {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("VelocityX", velocity.x);
        tag.putDouble("VelocityY", velocity.y);
        tag.putDouble("VelocityZ", velocity.z);
        tag.putFloat("Spin", spin);
        tag.putFloat("Scale", scale);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        velocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        spin = tag.getFloat("Spin");
        scale = tag.getFloat("Scale");
        refreshDimensions();
    }
}
