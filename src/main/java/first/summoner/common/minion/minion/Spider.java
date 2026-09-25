package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.GroundMinion;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.minion.minion.goal.spider.SpiderAttackGoal;
import first.summoner.common.minion.minion.goal.spider.SpiderIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 蜘蛛：按召唤次序切换三种形态，接触目标后吸附并持续咬击。
 */
public class Spider extends GroundMinion {

    public int variant = -1;

    public Spider() {
        super(SummonerAttachmentEntityRegister.SPIDER);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.INT, () -> variant, value -> variant = value);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new SpiderAttackGoal(this));
        goalSelector.addGoal(1, new SpiderIdleGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (variant < 0) {
            variant = Math.floorMod(getOrder(), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spider", 0, state -> isWalking()
                ? state.setAndContinue(RawAnimation.begin().thenLoop("move.walk"))
                : state.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"))));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.35, 0, -0.35, 0.35, 0.5, 0.35);
    }
}
