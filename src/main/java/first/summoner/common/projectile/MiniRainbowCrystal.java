package first.summoner.common.projectile;

import first.lyra.common.attachment.TargetCache;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.projectile.Projectile;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MiniRainbowCrystal extends Projectile {

    public static final int PULSE_TICKS = 10;
    public static final int SPARK_MIN_LIFE = 5;
    public static final int SPARK_MAX_LIFE = 20;
    public static final int LIFETIME = 40;
    private Vec3 sourcePosition = Vec3.ZERO;

    public MiniRainbowCrystal() {
        super(SummonerAttachmentEntityRegister.MINI_RAINBOW_CRYSTAL);
        setPhysics(false);
        setMaxTickCount(LIFETIME);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, this::getSourcePosition, this::setSourcePosition);
    }

    public Vec3 getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(Vec3 sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    @Override
    public void tick() {
        super.tick();
        // Lyra 在 tick() 后推进 tickCount，伤害与同步后的视觉年龄保持一致。
        int age = getTickCount() + 1;
        if (age == PULSE_TICKS || age == PULSE_TICKS * 2) {
            TargetCache targets = getTargetCache();
            for (LivingEntity target : targets.getEntitiesInRadius(getPos(), 3.5, targets::isTarget)) {
                attack(target, getDamage(), 0);
            }
        }
        if (age >= LIFETIME) {
            setRemove();
        }
    }
}
