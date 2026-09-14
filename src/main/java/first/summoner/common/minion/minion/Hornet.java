package first.summoner.common.minion.minion;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.minion.minion.goal.MomentumMinionIdleGoal;
import first.summoner.common.minion.minion.goal.hornet.HornetAttackGoal;
import first.summoner.common.projectile.HornetStinger;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerCurioRegister;
import first.summoner.utils.CuriosUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 黄蜂仆从 - 在目标上方盘旋，发射毒刺射弹。
 */
public class Hornet extends MomentumMinion implements IBlockCollision<Hornet> {

    public Hornet() {
        super(SummonerAttachmentEntityRegister.HORNET);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new HornetAttackGoal(this));
        goalSelector.addGoal(1, new MomentumMinionIdleGoal(this, 6, 0.01f, 64, true));
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
        HornetStinger stinger = new HornetStinger();
        stinger.setOwner(owner);
        stinger.init(new PathNode(startPos, direction));
        stinger.copyAttributes(this);
        stinger.setVelocity(direction);
        if (CuriosUtil.isEquipped(owner, SummonerCurioRegister.HivePack.get())) {
            stinger.setDamage(stinger.getDamage() * 1.15f);
        }
        LyraHelper.get(level).add(stinger);
    }

    /**
     * 获取毒刺发射冷却时间。
     * 装备蜂巢背包时冷却缩短。
     */
    public int getStingerCooldown() {
        RandomSource random = getRandom();
        int cooldown = 13 + random.nextIntBetweenInclusive(0, 4);
        if (CuriosUtil.isEquipped(owner, SummonerCurioRegister.HivePack.get())) {
            cooldown = (int) (cooldown * 0.67);
        }
        return cooldown;
    }
}
