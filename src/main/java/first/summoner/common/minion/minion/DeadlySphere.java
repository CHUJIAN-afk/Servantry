package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.minion.minion.goal.deadlysphere.DeadlySphereAttackGoal;
import first.summoner.common.minion.minion.goal.deadlysphere.DeadlySphereIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 致命球 - 三次冲撞后进入长冷却的冲刺型仆从。
 */
public class DeadlySphere extends MomentumMinion implements IEntityCollision<DeadlySphere>, IBlockCollision<DeadlySphere> {

    public DeadlySphere() {
        super(SummonerAttachmentEntityRegister.DEADLY_SPHERE);
        setGravity(0);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new DeadlySphereAttackGoal(this));
        goalSelector.addGoal(1, new DeadlySphereIdleGoal(this));
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
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

    @Override
    public float getElasticity() {
        return 0.98f;
    }

    public int getSearchDistance() {
        return 32;
    }
}
