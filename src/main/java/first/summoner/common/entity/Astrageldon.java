package first.summoner.common.entity;

import first.lyra.common.entity.IPathEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Astrageldon extends Monster implements GeoAnimatable, IPathEntity {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Phase phase = Phase.Phase11;

    public Astrageldon(EntityType<Astrageldon> entityType, Level level) {
        super(entityType, level);
        setNoGravity(false);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 128.0);
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase  = phase;
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            Vec3 vec3 = getDeltaMovement();
            setDeltaMovement(new Vec3(vec3.x() * 0.92, vec3.y(), vec3.z() * 0.92));
            if (getTarget() == null) {
                Player nearestPlayer = level().getNearestPlayer(this, 128);
                if (nearestPlayer != null) {
                    setTarget(nearestPlayer);
                }
                if (getTarget() == null) {
                    //setRemoved(RemovalReason.DISCARDED);
                }
            }
        } else {
            if (getPhase().ordinal() < 7) {
                //MusicManager.play(CatalystSoundRegister.AstrageldonPhase1);
            } else {
                //MusicManager.stop(CatalystSoundRegister.AstrageldonPhase1);
                //MusicManager.play(CatalystSoundRegister.AstrageldonPhase2);
            }
        }
        tickPath();
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean addEffect(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        return false;
    }

    @Override
    public void forceAddEffect(@NotNull MobEffectInstance instance, @Nullable Entity entity) {

    }

    @Override
    public void push(double x, double y, double z) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public double getTick(Object o) {
        return tickCount;
    }

    public enum Phase {
        Phase11,
        Phase12,
        Phase13,
        Phase14,
        Phase15,
        Phase16,
        Phase17,
        Phase21,
        Phase22,
        Phase23,
        Phase24,
        Phase25,
        Phase27,
        Phase28,
        Phase29,
        Phase210,
        Phase211,
    }
}
