package first.summoner.common.minion.minion;


import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.minion.minion.goal.hornet.HornetAttackGoal;
import first.summoner.common.minion.minion.goal.hornet.HornetIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.Vec3;

/**
 * 黄蜂仆从 - 在目标上方盘旋，发射毒刺射弹。
 */
public class Hornet extends MomentumMinion {

    public Hornet() {
        super(SummonerAttachmentEntityRegister.HORNET);
        setGravity(0);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new HornetAttackGoal(this));
        goalSelector.addGoal(1, new HornetIdleGoal(this));
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
