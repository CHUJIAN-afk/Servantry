package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.GroundMinion;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.minion.minion.goal.vampireFrog.VampireFrogAttackGoal;
import first.summoner.common.minion.minion.goal.vampireFrog.VampireFrogIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 吸血鬼青蛙：贴近后按冷却出手。
 */
public class VampireFrog extends GroundMinion {

    public int attackTime = -1;

    public VampireFrog() {
        super(SummonerAttachmentEntityRegister.VAMPIRE_FROG);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.INT, () -> attackTime, value -> attackTime = value);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new VampireFrogAttackGoal(this));
        goalSelector.addGoal(1, new VampireFrogIdleGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "vampire_frog", 0, state -> {
            if (attackTime != -1 && getTickCount() - attackTime < 20) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("attack.strike"));
            }
            if (isWalking()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("move.walk"));
            }
            if (isFlying()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("move.fly"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"));
        }));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.45, 0, -0.45, 0.45, 0.7, 0.45);
    }
}
