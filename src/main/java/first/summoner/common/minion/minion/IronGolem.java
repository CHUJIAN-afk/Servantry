package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.GroundMinion;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.minion.minion.goal.ironGolem.IronGolemAttackGoal;
import first.summoner.common.minion.minion.goal.ironGolem.IronGolemIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 铁傀儡：贴身近战，命中播放动画、音效并上挑击退。
 * <p>
 * {@code attackTime} 记录挥拳起始 tick，动画在 20 tick 窗口内播放 {@code attacking}。
 */
public class IronGolem extends GroundMinion {

    public int attackTime = -1;

    public IronGolem() {
        super(SummonerAttachmentEntityRegister.IRON_GOLEM);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.INT, () -> attackTime, value -> attackTime = value);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new IronGolemAttackGoal(this));
        goalSelector.addGoal(1, new IronGolemIdleGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "iron_golem", 2, state -> {
            if (attackTime != -1 && getTickCount() - attackTime < 20) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("attacking"));
            }
            if (isWalking()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walking"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("standing"));
        }));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.7, 0, -0.7, 0.7, 2.6, 0.7);
    }
}
