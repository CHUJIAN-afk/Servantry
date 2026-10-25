package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class NebulaResidue extends AstrageldonProjectile {

    private Vec3 velocity = Vec3.ZERO;

    public NebulaResidue(EntityType<? extends NebulaResidue> type, Level level) {
        super(type, level, 60);
        damage = 19.1f;
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 velocity) {
        spawn(owner, center, velocity, 19.1f, 60);
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 velocity, float damage, int life) {
        NebulaResidue residue = new NebulaResidue(SummonerEntityRegister.NEBULA_RESIDUE.get(), owner.level());
        residue.setOwner(owner);
        residue.velocity = velocity;
        residue.damage = damage;
        residue.life = life;
        residue.setCenterPose(center, velocity);
        residue.addToLevel();
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
    }

    @Override
    protected void step() {
        velocity = velocity.add(velocity.normalize().scale(0.175));
        setCenterPose(getBoundingBox().getCenter().add(velocity.scale(1.0 / 32)), velocity);
        setRoll(getRoll() + (float) Math.toDegrees(velocity.x * 0.006));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("VelocityX", velocity.x);
        tag.putDouble("VelocityY", velocity.y);
        tag.putDouble("VelocityZ", velocity.z);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        velocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
    }
}
