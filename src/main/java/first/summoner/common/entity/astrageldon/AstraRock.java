package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AstraRock extends AstrageldonProjectile {

    private Vec3 target = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private int alpha = 255;

    public AstraRock(EntityType<? extends AstraRock> type, Level level) {
        super(type, level, 960);
    }

    public static void spawn(Entity owner, Vec3 center, Vec3 target, Vec3 velocity) {
        AstraRock rock = new AstraRock(SummonerEntityRegister.ASTRA_ROCK.get(), owner.level());
        rock.setOwner(owner);
        rock.target = target;
        rock.velocity = velocity;
        rock.setPos(center.add(0, -rock.getBbHeight() / 2, 0));
        owner.level().addFreshEntity(rock);
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> target, value -> target = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.INT, () -> alpha, value -> alpha = value);
    }

    @Override
    protected void step() {
        alpha = life < 51 ? Math.min(alpha + 5, 255) : Math.max(alpha - 5, 0);
        int counter = (age + 1) * 6;
        Vec3 center = getBoundingBox().getCenter();
        Vec3 offset = target.subtract(center).normalize().scale(Math.sin(Math.toRadians(counter)) * (3 + counter * 0.01));
        if (counter < 270) {
            velocity = velocity.scale(0.9875);
            center = center.subtract(offset.scale(1.0 / 32));
        } else {
            if (counter == 270) {
                Vec3 spread = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(random.nextDouble() * 5);
                velocity = offset.normalize().scale(-24).add(spread);
            }
            velocity = velocity.add(0, -0.02, 0);
        }
        Vec3 movement = velocity.scale(1.0 / 32);
        setPos(center.add(0, -getBbHeight() / 2, 0));
        if (counter >= 270 && !Entity.collideBoundingBox(this, movement, getBoundingBox(), level(), List.of()).equals(movement)) {
            discard();
        } else {
            setPos(position().add(movement));
        }
        setYRot(PathNode.yawFromDirection(velocity));
        setXRot(PathNode.pitchFromDirection(velocity));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("TargetX", target.x);
        tag.putDouble("TargetY", target.y);
        tag.putDouble("TargetZ", target.z);
        tag.putDouble("VelocityX", velocity.x);
        tag.putDouble("VelocityY", velocity.y);
        tag.putDouble("VelocityZ", velocity.z);
        tag.putInt("Alpha", alpha);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        target = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        velocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        alpha = tag.getInt("Alpha");
    }
}
