package first.summoner.common.minion.minion;


import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.minion.minion.goal.hornet.HornetAttackGoal;
import first.summoner.common.minion.minion.goal.hornet.HornetIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 黄蜂仆从 - 在目标上方盘旋，发射毒刺射弹。
 */
public class Hornet extends MomentumMinion {

    /** 最近一次攻击（发射毒刺）的 tick，-1 表示尚未攻击；已同步，用于驱动攻击动画。 */
    public int attackTime = -1;

    public Hornet() {
        super(SummonerAttachmentEntityRegister.HORNET);
        setGravity(0);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.INT, () -> attackTime, value -> attackTime = value);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new HornetAttackGoal(this));
        goalSelector.addGoal(1, new HornetIdleGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "hornet", 2, state -> {
            if (isAttackAnimationActive()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("attack.cast"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"));
        }));
    }

    /** 攻击动画窗口：攻击后 8 tick 内播放 attack.cast，其余时间循环 misc.idle。 */
    public boolean isAttackAnimationActive() {
        return attackTime != -1 && tickCount - attackTime < 8;
    }

    @Override
    public int getSearchDistance() {
        return 16;
    }

    @Override
    public void lookAtDirection(Vec3 direction) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        setDesiredRotation(targetYaw, 0, getRoll());
    }
}
