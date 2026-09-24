package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.goal.sharknado.SharknadoAttackGoal;
import first.summoner.common.minion.minion.goal.sharknado.SharknadoIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * 鲨鱼龙卷仆从 - 碰撞攻击并发射鲨鱼射弹。
 */
public class Sharknado extends MomentumMinion implements IEntityCollision<Sharknado> {

    public Sharknado() {
        super(SummonerAttachmentEntityRegister.SHARKNADO);
        setGravity(0);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new SharknadoAttackGoal(this));
        goalSelector.addGoal(1, new SharknadoIdleGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        // 产生粒子
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0x04c7e3)
                        .edgeColor(0x047a95)
                        .lifetime(5)
                        .lifetimeRandom(10)
                        .spin(0.075f)
                        .spinRandom(0.3F)
                        .friction(0.9F)
                        .scale(0.02f)
                        .scaleRandom(0.001f)
                )
                .velocity(Vec3.ZERO)
                .pos(getPos())
                .offset(0.5)
                .count(2)
                .spread(1.5)
                .emit();
    }

    public int getSearchDistance() {
        return 32;
    }

    // ===================== IEntityCollision 实现 =====================

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.4, -0.4, -0.4, 0.4, 0.4, 0.4);
    }

    @Override
    public boolean canCollideAttack() {
        return getTarget() != null;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            attack(hit.entity(), getDamage(), 2);
        }
    }
}
