package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.minion.minion.goal.ufo.UFOAttackGoal;
import first.summoner.common.minion.minion.goal.ufo.UFOIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

/**
 * UFO仆从 - 悬浮在敌怪上方，发射蓝色激光瞬间命中目标。
 * 无朝向自旋，无俯仰角。
 */
public class UFO extends MomentumMinion {

    private int trailTimer = 0;

    public UFO() {
        super(SummonerAttachmentEntityRegister.UFO);
        setGravity(0);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getTrailTimer, this::setTrailTimer);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new UFOAttackGoal(this));
        goalSelector.addGoal(1, new UFOIdleGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (trailTimer > 0) {
            trailTimer--;
            if (trailTimer == 0) {
                setVelocity(getCurrentVelocity().scale(0.25));
            }
        }
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    /**
     * 无俯仰角：只设置yaw，pitch固定为0。
     */
    @Override
    public void lookAtDirection(Vec3 direction) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        setDesiredRotation(targetYaw, 0, 0);
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int timer) {
        this.trailTimer = timer;
    }

}
