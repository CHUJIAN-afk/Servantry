package first.summoner.common.minion.minion;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.MinionGoalSelector;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.minion.minion.goal.MomentumMinionIdleGoal;
import first.summoner.common.minion.minion.goal.twins.TwinsCursedFlameAttackGoal;
import first.summoner.common.minion.minion.goal.twins.TwinsLaserAttackGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 双子魔眼 - 由Retinazer和Spazmatism组成的双瞳仆从。
 * <p>
 * 单个仆从实体，通过AI控制表现出两种特性：
 * <ul>
 *   <li>Retinazer（激光眼）- 红色，发射激光攻击</li>
 *   <li>Spazmatism（咒焰眼）- 绿色，喷射咒焰攻击</li>
 * </ul>
 * </p>
 */
public class Twins extends MomentumMinion implements IEntityCollision<Twins>, IBlockCollision<Twins> {

    /**
     * 是否为激光眼（true=激光眼，false=咒焰眼）
     */
    private boolean isLaserEye = true;
    private Twins other = null;
    private int trailTimer = 0;

    public Twins() {
        super(SummonerAttachmentEntityRegister.TWINS);
        setDrag(0.75f);
        setRotationSpeed(20);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.BOOL, this::isLaserEye, value -> isLaserEye = value);
        fields.field(ByteBufCodecs.INT, this::getTrailTimer, this::setTrailTimer);
    }

    @Override
    public void registerGoals(MinionGoalSelector goalSelector) {
        goalSelector.addGoal(0, new TwinsLaserAttackGoal(this));
        goalSelector.addGoal(1, new TwinsCursedFlameAttackGoal(this));
        goalSelector.addGoal(2, new MomentumMinionIdleGoal(this, 6, 0.01f, 64, true));
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public boolean canCollideWithBlocks() {
        return !isExecutingPath();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public boolean canCollideAttack() {
        return !isLaserEye() && isTarget(getTarget());
    }

    @Override
    public boolean renderHitbox() {
        return !isLaserEye();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            InvincibleData.attack(hit.entity())
                    .attacker(getUuid())
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .invincibleTime(4)
                    .apply();
        }
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            if (other == null || other.isRemove()) {
                setRemove();
            }
            if (trailTimer > 0) {
                trailTimer--;
            }
        }
        super.tick();
    }

    public int getSearchDistance() {
        return 32;
    }

    public void setOther(Twins twins) {
        this.other = twins;
        twins.other = this;
    }

    public boolean isLaserEye() {
        return isLaserEye;
    }

    public boolean isFlameEye() {
        return !isLaserEye;
    }

    public void setFlameEye(boolean flameEye) {
        this.isLaserEye = !flameEye;
    }

    public void setLaserEye(boolean laserEye) {
        this.isLaserEye = laserEye;
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int trailTimer) {
        this.trailTimer = trailTimer;
    }
}
