package first.servantry.common.servant;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.projectile.SharkDragonProjectile;
import first.servantry.common.servant.goal.sharknado.SharknadoAttackGoal;
import first.servantry.common.servant.goal.sharknado.SharknadoIdleGoal;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 鲨鱼龙卷仆从 - 碰撞攻击并发射鲨鱼射弹。
 */
public class Sharknado extends MomentumServant implements ICollideAttack<Sharknado> {

    /**
     * 射击冷却
     */
    private int shootCooldown = 0;

    public Sharknado() {
        super();
        setDrag(0.92f);
        setRotationSpeed(0);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new SharknadoAttackGoal(this));
        goalSelector.addGoal(1, new SharknadoIdleGoal(this));
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            // 冷却衰减
            if (shootCooldown > 0) {
                shootCooldown--;
            }
            setDesiredRotation(getCurrentPathNode().yaw() + 25, getPitch(), getRoll());
            // 产生粒子
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                            .color(0x04c7e3)
                            .edgeColor(0x047a95)
                            .colorRandom(0, 0.1F, 0.2F)
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
        super.tick();
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
        SharkDragonProjectile projectile = new SharkDragonProjectile(getDamageSource(), start, direction);
        projectile.copyDamageData(this);
        projectile.join(owner);
    }

    @Override
    public AttachmentEntityType<Sharknado> getType() {
        return AttachmentEntityRegister.Sharknado.get();
    }

    // ===================== ICollideAttack 实现 =====================

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.4, -0.4, -0.4, 0.4, 0.4, 0.4);
    }

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public boolean isValidCollisionTarget(Sharknado entity, LivingEntity target) {
        return isTarget(target);
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