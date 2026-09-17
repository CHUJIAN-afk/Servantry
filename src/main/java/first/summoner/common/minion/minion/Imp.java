package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.goal.imp.ImpAttackGoal;
import first.summoner.common.minion.minion.goal.imp.ImpIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 小鬼仆从 - 在目标周围盘旋，发射穿透火球。
 */
public class Imp extends MomentumMinion {

    public Imp() {
        super(SummonerAttachmentEntityRegister.IMP);
        setGravity(0);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new ImpAttackGoal(this));
        goalSelector.addGoal(1, new ImpIdleGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getLevel();

        ParticleHelper.create(level)
                .generic(GenericParticleBuilder.create()
                                 .centerColor(0xe1c316)
                                 .edgeColor(0xff7506)
                                 .lifetime(6)
                                 .lifetimeRandom(4)
                                 .spin(0.25f)
                                 .spinRandom(0.25F)
                                 .scale(0.015f)
                                 .scaleRandom(0.015f)
                )
                .pos(getPos())
                .offset(0.2)
                .emit();
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
