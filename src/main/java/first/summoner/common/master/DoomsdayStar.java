package first.summoner.common.master;

import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.client.MusicManager;
import first.summoner.common.master.goal.doomsdayStar.DoomsdayStarJumpGoal;
import first.summoner.common.master.goal.doomsdayStar.DoomsdayStarSummonGoal;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DoomsdayStar extends PlannedPathEntity {

    public enum GoalState {
        SUMMON,
        JUMP
    }

    private GoalState goalState = GoalState.SUMMON;
    private int phase = 1;
    private int soundData;
    private boolean summonBeamVisible;
    private Vec3 summonBeamEnd = Vec3.ZERO;

    public DoomsdayStar(EntityType<? extends DoomsdayStar> entityType, Level level) {
        super(entityType, level);
        setGravity(-0.08F);
        setDrag(0.98F);
        setPersistenceRequired();
        goalSelector.addGoal(0, new DoomsdayStarSummonGoal(GoalState.SUMMON, this));
        goalSelector.addGoal(1, new DoomsdayStarJumpGoal(GoalState.JUMP,this));
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getPhase, this::setPhase);
        fields.field(ByteBufCodecs.BOOL, this::isSummonBeamVisible, this::setSummonBeamVisible);
        fields.field(LyraStreamCodecs.VEC_3, this::getSummonBeamEnd, this::setSummonBeamEnd);
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
        } else {
            Vec3 start = position();
            Vec3 end = new Vec3(start.x, level().getMinBuildHeight(), start.z);
            BlockHitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            setSummonBeamEnd(hit.getType() == HitResult.Type.MISS ? end : hit.getLocation());
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

    public void setGoalState(GoalState goalState) {
        this.goalState = goalState;
    }

    public GoalState getGoalState() {
        return goalState;
    }

    public boolean isGoalEnabled(GoalState goalState) {
        return this.goalState == goalState;
    }

    public boolean isSummonBeamVisible() {
        return summonBeamVisible;
    }

    public void setSummonBeamVisible(boolean summonBeamVisible) {
        this.summonBeamVisible = summonBeamVisible;
    }

    public Vec3 getSummonBeamEnd() {
        return summonBeamEnd;
    }

    public void setSummonBeamEnd(Vec3 summonBeamEnd) {
        this.summonBeamEnd = summonBeamEnd;
    }
}
