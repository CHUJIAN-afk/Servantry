package first.summoner.common.minion.minion;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.goal.MomentumMinionIdleGoal;
import first.summoner.common.minion.minion.goal.sharknado.SharknadoAttackGoal;
import first.summoner.common.projectile.SharkDragon;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 鲨鱼龙卷仆从 - 碰撞攻击并发射鲨鱼射弹。
 */
public class Sharknado extends MomentumMinion implements IEntityCollision<Sharknado> {

    /**
     * 射击冷却
     */
    private int shootCooldown = 0;

    public Sharknado() {
        super(SummonerAttachmentEntityRegister.SHARKNADO);
        setGravity(0);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new SharknadoAttackGoal(this));
        goalSelector.addGoal(1, new MomentumMinionIdleGoal(this, 6, 0.01f, 64, false));
    }

    @Override
    public void tick() {
        super.tick();
        if (!getLevel().isClientSide()) {
            // 冷却衰减
            if (shootCooldown > 0) {
                shootCooldown--;
            }
            setDesiredRotation(getCurrentPathNode().yaw() + 25, getPitch(), getRoll());
            // 产生粒子
            GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                    .centerColor(0x04c7e3)
                    .edgeColor(0x047a95);
            ParticleHelper.create(getLevel())
                    .generic(genericParticleBuilder
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
    }

    public int getSearchDistance() {
        return 32;
    }

    /**
     * 发射鲨鱼射弹
     */
    public void shootAtTarget(LivingEntity target) {
        Vec3 start = getPos();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        SharkDragon projectile = new SharkDragon();
        projectile.setOwner(owner);
        projectile.init(new PathNode(start, direction));
        projectile.copyAttributes(this);
        projectile.setVelocity(direction);
        LyraHelper.get(level).add(projectile);
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
    public boolean isValidCollisionTarget(Sharknado entity, LivingEntity target) {
        return LyraHelper.get(level).getTargetCache().isTarget(owner, target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            InvincibleData.attack(hit.entity())
                    .attacker(getUuid())
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .invincibleTime(2)
                    .apply();
        }
    }

    // ===================== 访问器 =====================

    public int getShootCooldown() {
        return shootCooldown;
    }

    public void setShootCooldown(int cooldown) {
        this.shootCooldown = cooldown;
    }
}
