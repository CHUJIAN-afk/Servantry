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

public class SlimerSpear extends AstrageldonProjectile {

    private Vec3 velocity = Vec3.ZERO;

    public SlimerSpear(EntityType<? extends SlimerSpear> type, Level level) {
        super(type, level, 120);
        damage = 6;
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 direction) {
        SlimerSpear spear = new SlimerSpear(SummonerEntityRegister.SLIMER_SPEAR.get(), owner.level());
        spear.setOwner(owner);
        spear.velocity = direction.normalize().scale(14);
        spear.setCenterPose(center, direction);
        spear.addToLevel();
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
    }

    @Override
    protected int substeps() {
        return 24;
    }

    @Override
    protected void step() {
        setCenterPose(getBoundingBox().getCenter().add(velocity.scale(1.0 / 32)), velocity);
    }

    @Override
    protected void onHitPlayer(Player player) {
        AstrageldonCombat.interstellarCorruption(player);
        discard();
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
