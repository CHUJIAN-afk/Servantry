package first.summoner.common.master;

import first.lyra.common.entity.AttachmentEntityDamageSource;
import first.lyra.common.entity.LivingAttachmentEntity;
import first.lyra.common.entity.PathNode;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.MinionGoalSelector;
import first.lyra.common.sound.Playable;
import first.lyra.register.LyraDamageRegister;
import first.summoner.client.MusicManager;
import first.summoner.common.master.goal.doomsdayStar.DoomsdayStarHoverScatterGoal;
import first.summoner.common.master.goal.doomsdayStar.DoomsdayStarJumpGoal;
import first.summoner.common.master.goal.doomsdayStar.DoomsdayStarSummonGoal;
import first.summoner.common.projectile.DoomsdayStarGel;
import first.summoner.common.projectile.StarlightShockwave;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoomsdayStar extends LivingAttachmentEntity {

    public enum GoalState {
        SUMMON,
        JUMP,
        HOVER_SCATTER
    }

    private int phase = 1;
    private GoalState goalState = GoalState.SUMMON;
    private boolean fallLaserIndicatorRender;
    private Vec3 fallLaserIndicatorEnd = Vec3.ZERO;
    private LivingEntity target;
    private float lastScale = 1;
    private float scale = 1;

    public DoomsdayStar() {
        super(SummonerAttachmentEntityRegister.DOOMSDAY_STAR);
        setGravity(-0.08F);
        setDrag(0.98F);
    }

    @Override
    public void registerGoals(MinionGoalSelector goalSelector) {
        goalSelector.addGoal(0, new DoomsdayStarSummonGoal(GoalState.SUMMON, this));
        goalSelector.addGoal(1, new DoomsdayStarJumpGoal(GoalState.JUMP, this));
        goalSelector.addGoal(2, new DoomsdayStarHoverScatterGoal(GoalState.HOVER_SCATTER, this));
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getPhase, this::setPhase);
        fields.field(ByteBufCodecs.BOOL, this::isFallLaserIndicatorRender, this::setFallLaserIndicatorRender);
        fields.field(ByteBufCodecs.INT, () -> tickCount, time -> {
            MusicManager.play(phase == 1 ? SummonerSoundRegister.DoomsdayStarPhase1 : SummonerSoundRegister.DoomsdayStarPhase2);
            if (phase == 2) {
                MusicManager.stop(SummonerSoundRegister.DoomsdayStarPhase1);
            }
        });
        fields.field(ByteBufCodecs.FLOAT, () -> lastScale, value -> lastScale = value);
        fields.field(ByteBufCodecs.FLOAT, () -> scale, value -> scale = value);
    }

    public void fireStarlightShockwave() {
        Vec3 origin = getAbsoluteHitbox().getCenter();
        float damage = getAttackDamage();
        for (int i = 0; i < 36; i++) {
            double angle = Math.TAU * i / 36;
            Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            StarlightShockwave shockwave = new StarlightShockwave();
            shockwave.setCurrentPathNode(new PathNode(origin, (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)), 0.0F, 0.0F));
            shockwave.setVelocity(direction.scale(0.4));
            shockwave.setDamage(damage);
            shockwave.setDamageSourceSupplier((level, player) -> new AttachmentEntityDamageSource(LyraDamageRegister.getDamageTypeHolder(LyraDamageRegister.Summon, level), getHurtEntity(), getHurtEntity(), shockwave.getPos(), shockwave));
            shockwave.join(getLevel());
        }
    }

    public void fireDoomsdayStarGel() {
        if (target != null) {
            DoomsdayStarGel.fire(this, target);
        }
    }

    public void setScale(float scale) {
        this.scale = Math.clamp(scale, 0.4f, 1.6f);
    }

    public float getScale(float partialTick) {
        return Mth.lerp(partialTick, lastScale, scale);
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            lastScale = scale;
            if (scale < 1) {
                scale = Math.min(scale * 1.2f, 1);
            } else if (scale > 1) {
                scale = Math.max(scale * 0.8f, 1);
            }
            Vec3 velocity = getVelocity();
            setScale((float) (getScale(1) + velocity.y() * 0.1f));
            if (target == null || !target.isAlive() || target.isSpectator()) {
                target = getLevel().getNearestPlayer(getPos().x, getPos().y, getPos().z, 64.0D, false);
            }
        } else {
            Vec3 fallLaserStart = getRenderNode(1).pos();
            Vec3 fallLaserMinEnd = new Vec3(fallLaserStart.x, getLevel().getMinBuildHeight(), fallLaserStart.z);
            BlockHitResult fallLaserHit = getLevel().clip(new ClipContext(fallLaserStart, fallLaserMinEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
            setFallLaserIndicatorEnd(fallLaserHit.getType() == HitResult.Type.MISS ? fallLaserMinEnd : fallLaserHit.getLocation());
        }
        super.tick();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setOnGround(context.bottomSupported());
        setScale(getScale(1) - (float) Math.abs(getVelocity().y() * 0.1f));
        if (context.bottomSupported()) {
            if ((goalState == GoalState.JUMP || goalState == GoalState.SUMMON) && getVelocity().y() < -0.2) {
                Playable.play(SoundEvents.GENERIC_EXPLODE, getLevel(), getPos(), getHurtEntity().getSoundSource());
                fireStarlightShockwave();
            }
            setVelocity(Vec3.ZERO);
        }
    }

    @Override
    protected boolean onHurtEntityDeath(@NotNull DamageSource damageSource) {
        if (!getLevel().isClientSide() && phase == 1) {
            phase = 2;
            setHealth(getMaxHealth());
            setVelocity(Vec3.ZERO);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-1.0D, -1.0D, -1.0D, 1.0D, 1.0D, 1.0D);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
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

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    public boolean isFallLaserIndicatorRender() {
        return fallLaserIndicatorRender;
    }

    public void setFallLaserIndicatorRender(boolean fallLaserIndicatorRender) {
        this.fallLaserIndicatorRender = fallLaserIndicatorRender;
    }

    public Vec3 getFallLaserIndicatorEnd() {
        return fallLaserIndicatorEnd;
    }

    public void setFallLaserIndicatorEnd(Vec3 fallLaserIndicatorEnd) {
        this.fallLaserIndicatorEnd = fallLaserIndicatorEnd;
    }

    public void jump(Vec3 targetPos) {
        float drag = getDrag();
        float gravity = getCustomGravity();
        double targetHeight = targetPos.y - getPos().y;
        if (drag <= 0.0F || gravity >= 0.0F || targetHeight <= 0.0D) {
            setVelocity(Vec3.ZERO);
            return;
        }

        double low = 0.0D;
        double high = 1.0D;
        double highMaxHeight = 0.0D;
        for (int expansion = 0; expansion < 40; expansion++) {
            double velocity = high;
            double height = 0.0D;
            highMaxHeight = 0.0D;
            for (int tick = 1; tick <= 512; tick++) {
                velocity = velocity * drag + gravity;
                height += velocity;
                highMaxHeight = Math.max(highMaxHeight, height);
                if (velocity <= 0.0D) {
                    break;
                }
            }
            if (highMaxHeight >= targetHeight) {
                break;
            }
            high *= 2.0D;
        }
        if (highMaxHeight < targetHeight) {
            setVelocity(Vec3.ZERO);
            return;
        }

        for (int iteration = 0; iteration < 64; iteration++) {
            double velocity = (low + high) * 0.5D;
            double height = 0.0D;
            double maxHeight = 0.0D;
            for (int tick = 1; tick <= 512; tick++) {
                velocity = velocity * drag + gravity;
                height += velocity;
                maxHeight = Math.max(maxHeight, height);
                if (velocity <= 0.0D) {
                    break;
                }
            }
            if (maxHeight < targetHeight) {
                low = (low + high) * 0.5D;
            } else {
                high = (low + high) * 0.5D;
            }
        }

        double verticalVelocity = (low + high) * 0.5D;
        double simulatedVelocity = verticalVelocity;
        double height = 0.0D;
        double maxHeight = 0.0D;
        int apexTick = 0;
        for (int tick = 1; tick <= 512; tick++) {
            simulatedVelocity = simulatedVelocity * drag + gravity;
            height += simulatedVelocity;
            if (height > maxHeight) {
                maxHeight = height;
                apexTick = tick;
            }
            if (simulatedVelocity <= 0.0D) {
                break;
            }
        }

        double horizontalVelocity = 1.0D;
        double horizontalDistance = 0.0D;
        for (int tick = 1; tick <= apexTick; tick++) {
            horizontalVelocity *= drag;
            horizontalDistance += horizontalVelocity;
        }

        Vec3 horizontalOffset = new Vec3(targetPos.x - getPos().x, 0.0D, targetPos.z - getPos().z);
        Vec3 horizontalVelocityVector = horizontalDistance <= 1.0E-7D
                ? Vec3.ZERO
                : horizontalOffset.scale(1.0D / horizontalDistance);
        setVelocity(new Vec3(horizontalVelocityVector.x, verticalVelocity, horizontalVelocityVector.z));
    }
}
