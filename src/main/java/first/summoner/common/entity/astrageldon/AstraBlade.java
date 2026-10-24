package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AstraBlade extends AstrageldonProjectile {

    private Vec3 origin = Vec3.ZERO;
    private Vec3 direction = Vec3.ZERO;
    private int remaining;
    private int alpha = 255;
    private boolean hostile = true;

    public AstraBlade(EntityType<? extends AstraBlade> type, Level level) {
        super(type, level, 100);
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 direction, int remaining) {
        AstraBlade blade = new AstraBlade(SummonerEntityRegister.ASTRA_BLADE.get(), owner.level());
        blade.setOwner(owner);
        blade.origin = center;
        blade.direction = direction;
        blade.remaining = remaining;
        blade.setPos(center.add(0, -blade.getBbHeight() / 2, 0));
        owner.level().addFreshEntity(blade);
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> origin, value -> origin = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> direction, value -> direction = value);
        fields.field(LyraStreamCodecs.INT, () -> remaining, value -> remaining = value);
        fields.field(LyraStreamCodecs.INT, () -> alpha, value -> alpha = value);
        fields.field(LyraStreamCodecs.BOOL, () -> hostile, value -> hostile = value);
    }

    @Override
    protected int substeps() {
        return 6;
    }

    @Override
    protected boolean canDamage() {
        return hostile;
    }

    @Override
    protected void onHitPlayer(Player player) {
        AstrageldonCombat.armorCrunch(player);
    }

    @Override
    protected void step() {
        if (life < 50) {
            alpha += 8;
            if (alpha > 100) {
                hostile = false;
            }
        } else {
            alpha -= 8;
        }
        alpha = Mth.clamp(alpha, 0, 255);
        double offset = -Math.sin(Math.toRadians(age * 1.8)) * 64;
        if (offset > 0) {
            offset *= 1.2;
        }
        setPos(origin.add(0, -(offset + 2) / 32 - getBbHeight() / 2, 0));
        if (age + 1 >= 10 && remaining > 1) {
            Entity owner = getOwner();
            spawn(owner == null ? this : owner, origin.add(direction), direction, remaining - 1);
            remaining = 0;
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("OriginX", origin.x);
        tag.putDouble("OriginY", origin.y);
        tag.putDouble("OriginZ", origin.z);
        tag.putDouble("DirectionX", direction.x);
        tag.putDouble("DirectionZ", direction.z);
        tag.putInt("Remaining", remaining);
        tag.putInt("Alpha", alpha);
        tag.putBoolean("Hostile", hostile);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        origin = new Vec3(tag.getDouble("OriginX"), tag.getDouble("OriginY"), tag.getDouble("OriginZ"));
        direction = new Vec3(tag.getDouble("DirectionX"), 0, tag.getDouble("DirectionZ"));
        remaining = tag.getInt("Remaining");
        alpha = tag.getInt("Alpha");
        hostile = tag.getBoolean("Hostile");
    }
}
