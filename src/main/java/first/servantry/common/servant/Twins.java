package first.servantry.common.servant;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.twins.TwinsCursedFlameAttackGoal;
import first.servantry.common.servant.goal.twins.TwinsIdleGoal;
import first.servantry.common.servant.goal.twins.TwinsLaserAttackGoal;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
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
public class Twins extends MomentumServant implements ICollideAttack<Twins>, IBlockCollision<Twins> {

    /**
     * 是否为激光眼（true=激光眼，false=咒焰眼）
     */
    private boolean isLaserEye = true;
    private Twins other = null;
    private int trailTimer = 0;

    public Twins() {
        super();
        setDrag(0.75f);
        setRotationSpeed(20);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new TwinsLaserAttackGoal(this));
        goalSelector.addGoal(0, new TwinsCursedFlameAttackGoal(this));
        goalSelector.addGoal(2, new TwinsIdleGoal(this));
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
    public boolean isValidCollisionTarget(Twins entity, LivingEntity target) {
        return isTarget(target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            InvincibleData.criteriaAttack(hit.entity(), getUuid(), 4, getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
        }
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (trailTimer > 0) {
                trailTimer--;
            }
        }
        super.tick();
    }

    public int getTargetDistance() {
        return 12;
    }

    @Override
    public boolean isRemove() {
        if (other == null || other.isRemove()) {
            return true;
        }
        return super.isRemove();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(isLaserEye);
        buf.writeInt(trailTimer);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        isLaserEye = buf.readBoolean();
        trailTimer = buf.readInt();
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return AttachmentEntityRegister.Twins.get();
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