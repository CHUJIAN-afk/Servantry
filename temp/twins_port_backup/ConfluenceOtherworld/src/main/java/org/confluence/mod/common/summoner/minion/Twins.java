package org.confluence.mod.common.summoner.minion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.confluence.mod.common.summoner.LyraStreamCodecs;
import org.confluence.mod.common.summoner.attachmentEntity.AttachmentEntityGoalSelector;
import org.confluence.mod.common.summoner.attachmentEntity.IBlockCollision;
import org.confluence.mod.common.summoner.attachmentEntity.IEntityCollision;
import org.confluence.mod.common.summoner.attachmentEntity.SyncFieldDispatcher;
import org.confluence.mod.common.summoner.minion.goal.twins.TwinsCursedFlameAttackGoal;
import org.confluence.mod.common.summoner.minion.goal.twins.TwinsIdleGoal;
import org.confluence.mod.common.summoner.minion.goal.twins.TwinsLaserAttackGoal;
import org.confluence.mod.common.summoner.register.SummonerAttachmentEntityTypes;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

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

    private static final RawAnimation PHASE_ONE = RawAnimation.begin().thenLoop("type_1");
    private static final RawAnimation PHASE_TWO = RawAnimation.begin().thenLoop("type_2");

    /**
     * 是否为激光眼（true=激光眼，false=咒焰眼）
     */
    private boolean isLaserEye = true;
    private Twins other = null;
    private int trailTimer = 0;
    /**
     * 激光眼连射 / 魔焰眼喷火期间为true，同步给客户端用于切换BOSS模型的二阶段形态。
     */
    private boolean attacking = false;

    public Twins() {
        super(SummonerAttachmentEntityTypes.TWINS);
        setGravity(0);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.BOOL, this::isLaserEye, value -> isLaserEye = value);
        fields.field(LyraStreamCodecs.INT, this::getTrailTimer, this::setTrailTimer);
        fields.field(LyraStreamCodecs.BOOL, this::isAttacking, value -> attacking = value);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new TwinsLaserAttackGoal(this));
        goalSelector.addGoal(1, new TwinsCursedFlameAttackGoal(this));
        goalSelector.addGoal(2, new TwinsIdleGoal(this));
    }

    /**
     * 平时循环一阶段形态，连射或喷火时切到二阶段形态。
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "twins", 5, state -> state.setAndContinue(attacking ? PHASE_TWO : PHASE_ONE)));
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
        return isFlameEye() && getTarget() != null;
    }

    @Override
    public boolean renderHitbox() {
        return !isLaserEye();
    }

    @Override
    public boolean isValidCollisionTarget(Twins entity, LivingEntity target) {
        return getTargetCache().isTarget(target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            attack(hit.entity(), getDamage(), 4);
        }
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && other != null && !other.isRemove();
    }

    @Override
    public void tick() {
        super.tick();

        if (trailTimer > 0) {
            trailTimer--;
        }
    }

    @Override
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

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }
}
