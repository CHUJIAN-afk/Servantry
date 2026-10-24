package first.summoner.common.entity.astrageldon;

import first.lyra.utils.LyraStreamCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record AstrageldonDash(Vec3 start, Vec3 end, float counter, float initializeSpeed, float resolveSpeed, boolean resolving) {

    public static final StreamCodec<RegistryFriendlyByteBuf, AstrageldonDash> CODEC = StreamCodec.composite(
            LyraStreamCodecs.VEC_3, AstrageldonDash::start,
            LyraStreamCodecs.VEC_3, AstrageldonDash::end,
            LyraStreamCodecs.FLOAT, AstrageldonDash::counter,
            LyraStreamCodecs.FLOAT, AstrageldonDash::initializeSpeed,
            LyraStreamCodecs.FLOAT, AstrageldonDash::resolveSpeed,
            LyraStreamCodecs.BOOL, AstrageldonDash::resolving, AstrageldonDash::new);

    public AstrageldonDash(Vec3 start, Vec3 end, float speed) {
        this(start, end, 40, speed, 4, false);
    }

    public AstrageldonDash update() {
        float next = resolving ? counter - resolveSpeed : Math.max(0, counter - initializeSpeed);
        return new AstrageldonDash(start, end, next, initializeSpeed, resolveSpeed, resolving);
    }

    public AstrageldonDash resolve(float speed) {
        return new AstrageldonDash(start, end, counter, initializeSpeed, speed, true);
    }

    public boolean finished() {
        return resolving && counter < -40;
    }

    public float progress(int direction) {
        return progress(counter, direction);
    }

    public static float progress(float counter, int direction) {
        float value = Mth.clamp(counter / -40, 0, 1);
        return (float) Math.pow(direction < 0 ? 1 - value : value, 1.15);
    }

    public Vec3 formationPosition(float progress, int direction, double wing) {
        Vec3 forward = end.subtract(start).normalize().scale(direction);
        Vec3 side = new Vec3(-forward.z, 0, forward.x).normalize();
        return start.lerp(end, progress).add(side.scale(wing)).subtract(forward.scale(Math.abs(wing) * 0.35));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("StartX", start.x);
        tag.putDouble("StartY", start.y);
        tag.putDouble("StartZ", start.z);
        tag.putDouble("EndX", end.x);
        tag.putDouble("EndY", end.y);
        tag.putDouble("EndZ", end.z);
        tag.putFloat("Counter", counter);
        tag.putFloat("InitializeSpeed", initializeSpeed);
        tag.putFloat("ResolveSpeed", resolveSpeed);
        tag.putBoolean("Resolving", resolving);
        return tag;
    }

    public static AstrageldonDash load(CompoundTag tag) {
        return new AstrageldonDash(new Vec3(tag.getDouble("StartX"), tag.getDouble("StartY"), tag.getDouble("StartZ")),
                new Vec3(tag.getDouble("EndX"), tag.getDouble("EndY"), tag.getDouble("EndZ")),
                tag.getFloat("Counter"), tag.getFloat("InitializeSpeed"), tag.getFloat("ResolveSpeed"), tag.getBoolean("Resolving"));
    }
}
