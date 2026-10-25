package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AstrageldonLaser extends AstrageldonProjectile {

    private Vec3 velocity = Vec3.ZERO;
    private int alpha = 255;

    public AstrageldonLaser(EntityType<? extends AstrageldonLaser> type, Level level) {
        super(type, level, 90);
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 velocity, float damage) {
        AstrageldonLaser laser = new AstrageldonLaser(SummonerEntityRegister.ASTRAGELDON_LASER.get(), owner.level());
        laser.setOwner(owner);
        laser.velocity = velocity;
        laser.damage = damage;
        laser.setCenterPose(center, velocity);
        laser.addToLevel();
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.INT, () -> alpha, value -> alpha = value);
    }

    @Override
    protected void step() {
        alpha = Math.max(alpha - 20, 0);
        velocity = velocity.add(velocity.normalize().scale(0.7));
        setCenterPose(getBoundingBox().getCenter().add(velocity.scale(1.0 / 32)), velocity);
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
        tag.putInt("Alpha", alpha);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        velocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        alpha = tag.getInt("Alpha");
    }
}
