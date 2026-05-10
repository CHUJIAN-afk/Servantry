package first.servantry.common.servant;

import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.TwinsIdleGoal;
import first.servantry.common.servant.goal.TwinsLaserAttackGoal;
import first.servantry.register.ServantRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.AABB;

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
public class Twins extends MomentumServant implements IBlockCollision<Twins> {

    /**
     * 是否为激光眼（true=激光眼，false=咒焰眼）
     */
    private boolean isLaserEye = true;

    public Twins() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(1, new TwinsLaserAttackGoal(this));
        goalSelector.addGoal(2, new TwinsIdleGoal(this));
    }

    @Override
    public AABB getBlockCollisionBox() {
        return new AABB(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setVelocity(IBlockCollision.clearVelocity(getVelocity(), context));
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(isLaserEye);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        isLaserEye = buf.readBoolean();
    }

    @Override
    public int getSlotCost() {
        if (!isLaserEye()) {
            return 0;
        }
        return super.getSlotCost();
    }

    @Override
    public float getDamage() {
        return 2.4f;
    }

    @Override
    public float getKnockback() {
        return 0.1f;
    }

    @Override
    public ServantType<? extends MomentumServant> getType() {
        return ServantRegister.Twins.get();
    }

    // ===================== 访问器 =====================

    public boolean isLaserEye() {
        return isLaserEye;
    }

    public void setLaserEye(boolean laserEye) {
        this.isLaserEye = laserEye;
    }
}