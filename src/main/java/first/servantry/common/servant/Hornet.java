package first.servantry.common.servant;

import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.projectile.HornetStinger;
import first.servantry.common.servant.goal.MomentumServantIdleGoal;
import first.servantry.common.servant.goal.hornet.HornetAttackGoal;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantryCurioRegister;
import first.servantry.utils.CuriosUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 黄蜂仆从 - 在目标上方盘旋，发射毒刺射弹。
 */
public class Hornet extends MomentumServant implements IBlockCollision<Hornet> {

    public Hornet() {
        super();
        setDrag(0.75f);
        setGravity(0);
        setRotationSpeed(15f);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new HornetAttackGoal(this));
        goalSelector.addGoal(1, new MomentumServantIdleGoal(this, 6, 0.01f, 64, true));
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
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void lookAtDirection(Vec3 direction) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        setDesiredRotation(targetYaw, 0, getRoll());
    }

    /**
     * 向目标发射毒刺射弹。
     */
    public void shootStingerAt(LivingEntity target) {
        Vec3 startPos = getPos();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 direction = targetCenter.subtract(startPos).normalize();
        HornetStinger stinger = new HornetStinger(getDamageSource(), startPos.add(direction.scale(-1)), direction);
        stinger.join(owner);
    }

    /**
     * 获取毒刺发射冷却时间。
     * 装备蜂巢背包时冷却缩短。
     */
    public int getStingerCooldown() {
        RandomSource random = owner.getRandom();
        if (CuriosUtil.isEquipped(owner, ServantryCurioRegister.HivePack.get())) {
            return 11 + random.nextIntBetweenInclusive(0, 2);
        }
        return 13 + random.nextIntBetweenInclusive(0, 4);
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return ServantryAttachmentEntityRegister.Hornet.get();
    }
}
