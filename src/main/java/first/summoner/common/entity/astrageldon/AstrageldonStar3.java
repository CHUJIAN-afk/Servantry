package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AstrageldonStar3 extends AstrageldonProjectile {

    private Vec3 velocity = Vec3.ZERO;
    private Vec3 direction = new Vec3(0, 0, 1);
    private float scale = 0.4f;
    private float spin = 360;

    public AstrageldonStar3(EntityType<? extends AstrageldonStar3> type, Level level) {
        super(type, level, 360);
        damage = 10.9f;
    }

    public static void spawn(Astrageldon boss, Vec3 center, Vec3 velocity, Vec3 direction) {
        AstrageldonStar3 star = new AstrageldonStar3(SummonerEntityRegister.ASTRAGELDON_STAR_3.get(), boss.level());
        star.setOwner(boss);
        star.velocity = velocity;
        star.direction = direction.lengthSqr() > 0 ? direction.normalize() : Vec3.directionFromRotation(0, boss.getYRot());
        star.setCenterPose(center, star.direction);
        boss.level().addFreshEntity(star);
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> direction, value -> direction = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> scale, value -> scale = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> spin, value -> spin = value);
    }

    public List<Vec3> getAxes() {
        Vec3 reference = Math.abs(direction.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 tangent = direction.cross(reference).normalize();
        return List.of(direction.add(tangent).normalize(), direction.subtract(tangent).normalize(), direction.cross(tangent));
    }

    public float getCharge(float partialTick) {
        return Mth.clamp((age - 3 + partialTick * 3) / 40, 0, 1);
    }

    @Override
    protected void step() {
        if (getOwner() instanceof Astrageldon boss && boss.isAlive()) {
            if (age < 70) {
                if (age > 0) {
                    velocity = velocity.scale(0.9);
                }
                scale = Math.min(1, scale + 0.02f);
                spin = Math.max(0, spin * 0.95f - 2.3f);
                setCenterPose(getBoundingBox().getCenter().add(velocity.scale(1.0 / 32)), direction);
                setRoll(spin);
                if (age + 1 == 70) {
                    for (Vec3 axis : getAxes()) {
                        StarLaser.spawn(boss, getBoundingBox().getCenter(), axis, damage);
                        StarLaser.spawn(boss, getBoundingBox().getCenter(), axis.scale(-1), damage);
                    }
                }
            } else if (age >= 72) {
                discard();
            }
        } else {
            discard();
        }
    }

    @Override
    protected boolean canDamage() {
        return age < 3;
    }

    @Override
    protected void onHitPlayer(Player player) {
        AstrageldonCombat.astralBlight(player);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("VelocityX", velocity.x);
        tag.putDouble("VelocityY", velocity.y);
        tag.putDouble("VelocityZ", velocity.z);
        tag.putDouble("DirectionX", direction.x);
        tag.putDouble("DirectionY", direction.y);
        tag.putDouble("DirectionZ", direction.z);
        tag.putFloat("Scale", scale);
        tag.putFloat("Spin", spin);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        velocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        direction = new Vec3(tag.getDouble("DirectionX"), tag.getDouble("DirectionY"), tag.getDouble("DirectionZ"));
        scale = tag.getFloat("Scale");
        spin = tag.getFloat("Spin");
    }
}
