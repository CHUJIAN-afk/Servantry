package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.minion.GroundMinion;
import first.summoner.common.minion.minion.goal.snowFlinx.SnowFlinxAttackGoal;
import first.summoner.common.minion.minion.goal.snowFlinx.SnowFlinxIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;

/**
 * 小雪怪：靠身体碰撞伤害所有接触到的敌人。
 */
public class SnowFlinx extends GroundMinion implements IEntityCollision<SnowFlinx> {

    public SnowFlinx() {
        super(SummonerAttachmentEntityRegister.SNOW_FLINX);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new SnowFlinxAttackGoal(this));
        goalSelector.addGoal(1, new SnowFlinxIdleGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "snow_flinx", 0, state -> {
            if (isWalking()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("move.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"));
        }));
    }

    @Override
    public int getSearchDistance() {
        return 16;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.3, -0.1, -0.3, 0.3, 0.8, 0.3);
    }

    @Override
    public boolean canCollideAttack() {
        return getTarget() != null;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            attack(hit.entity(), getDamage(), 4);
        }
    }
}
