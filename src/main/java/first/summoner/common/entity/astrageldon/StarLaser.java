package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class StarLaser extends AstrageldonProjectile {

    private Vec3 direction = new Vec3(0, 0, 1);

    public StarLaser(EntityType<? extends StarLaser> type, Level level) {
        super(type, level, 40);
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 direction, float damage) {
        StarLaser laser = new StarLaser(SummonerEntityRegister.STAR_LASER.get(), owner.level());
        laser.setOwner(owner);
        laser.direction = direction;
        laser.damage = damage;
        laser.setCenterPose(center, direction);
        owner.level().addFreshEntity(laser);
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> direction, value -> direction = value);
    }

    public float getFrame(float partialTick) {
        return Math.max(0, age - 3 + partialTick * 3);
    }

    public Vec3 getPoint(int sample, float frame) {
        Vec3 reference = Math.abs(direction.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 side = direction.cross(reference).normalize();
        double wave = Math.sin(Math.toRadians(frame * 15 + sample * 3)) * 6.0 / 32 * Math.min(sample / 30.0, 1);
        return getBoundingBox().getCenter().add(direction.scale(sample * 12.0 / 32)).add(side.scale(wave));
    }

    @Override
    protected void step() {
        int alpha = 100 + (age + 1) * 4;
        if (alpha > 250) {
            discard();
        } else if (alpha <= 140) {
            Vec3 center = getBoundingBox().getCenter();
            AABB bounds = new AABB(center, center.add(direction.scale(45))).inflate(10.0 / 32);
            for (Player player : level().getEntitiesOfClass(Player.class, bounds, player -> player.isAlive() && !player.isSpectator())) {
                AABB box = player.getBoundingBox().inflate(4.0 / 32);
                for (int i = 0; i <= 120; i += 3) {
                    if (box.contains(getPoint(i, age + 1))) {
                        player.hurt(AstrageldonCombat.source(this, getOwner()), damage);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected boolean canDamage() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("DirectionX", direction.x);
        tag.putDouble("DirectionY", direction.y);
        tag.putDouble("DirectionZ", direction.z);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        direction = new Vec3(tag.getDouble("DirectionX"), tag.getDouble("DirectionY"), tag.getDouble("DirectionZ"));
    }
}
