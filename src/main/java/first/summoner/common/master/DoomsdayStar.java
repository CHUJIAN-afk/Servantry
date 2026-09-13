package first.summoner.common.master;

import first.lyra.common.entity.SyncFieldDispatcher;
import first.summoner.client.MusicManager;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DoomsdayStar extends PlannedPathEntity {

    private int jumpCooldown;
    private int phase = 1;
    private int soundData;

    public DoomsdayStar(EntityType<? extends DoomsdayStar> entityType, Level level) {
        super(entityType, level);
        setGravity(-0.08F);
        setDrag(0.98F);
        setPersistenceRequired();
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getPhase, this::setPhase);
        fields.field(ByteBufCodecs.INT, () -> tickCount, time -> {
            MusicManager.play(phase == 1 ? SummonerSoundRegister.DoomsdayStarPhase1 : SummonerSoundRegister.DoomsdayStarPhase2);
            if (phase == 2) {
                MusicManager.stop(SummonerSoundRegister.DoomsdayStarPhase1);
            }
        });
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (phase == 2) {
                heal(getMaxHealth());
            }
            if (isAlive()) {
                soundData = Math.min(20, soundData + 2);
            }
            Player target = findTarget();
            ClientLevel level;

            if (target != null) {
                faceTarget(target);
                if (onGround()) {
                    if (jumpCooldown > 0) {
                        jumpCooldown--;
                    }
                    if (jumpCooldown <= 0) {
                        jumpTowards(target);
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (!level().isClientSide() && phase == 1) {
            phase = 2;
            setHealth(getMaxHealth());
            setVelocity(Vec3.ZERO);
            setDeltaMovement(Vec3.ZERO);
            jumpCooldown = 0;
            return;
        }
        soundData = 0;
        super.die(damageSource);
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase >= 2 ? 2 : 1;
    }

    private Player findTarget() {
        Player nearest = level().getNearestPlayer(this, 64.0D);
        if (nearest == null || !nearest.isAlive() || nearest.isSpectator() || !canAttack(nearest)) {
            return null;
        }
        return nearest;
    }

    private void faceTarget(Player target) {
        Vec3 horizontal = new Vec3(target.getX() - getX(), 0.0D, target.getZ() - getZ());
        if (horizontal.lengthSqr() <= 1.0E-7) {
            return;
        }
        horizontal = horizontal.normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-horizontal.x, horizontal.z));
        setPathRotation(yaw, 0.0F, currentPathNode.roll());
    }

    private void jumpTowards(Player target) {
        Vec3 horizontal = new Vec3(target.getX() - getX(), 0.0D, target.getZ() - getZ());
        if (horizontal.lengthSqr() > 1.0E-7) {
            horizontal = horizontal.normalize();
        } else {
            horizontal = Vec3.ZERO;
        }

        setVelocity(new Vec3(
                horizontal.x * 0.25D,
                0.42D,
                horizontal.z * 0.25D
        ));
        setOnGround(false);
        jumpCooldown = Math.max(1, getJumpDelay() / 3);
    }

    private int getJumpDelay() {
        return 10 + getRandom().nextInt(20);
    }
}
