package first.servantry.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.*;
import first.lyra.common.projectile.Projectile;
import first.lyra.common.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 小鬼火球射弹 - 穿透4个敌怪，100%施加着火了！效果。
 * 使用 GLOBAL 无敌帧模式（穿透攻击互相干扰）。
 */
public class ImpFireball extends Projectile implements ICollideAttack<ImpFireball>, IBlockCollision<ImpFireball> {

    private int hitCount = 0;

    public ImpFireball() {
        super();
    }

    public ImpFireball(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1.0f);
        setMaxSpeed(1.0f);
        setMaxLife(50);
        setGravity(0);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hit : hitContexts) {
                if (hitCount >= 4) {
                    // 超过穿透上限：移动到最后一个命中实体位置，跳过其他实体，移除自身
                    currentPathNode = new PathNode(hit.hitPoint(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
                    setRemove();
                    return;
                }
                LivingEntity living = hit.entity();
                boolean applied = InvincibleData.attack(living)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .invincibleTime(2)
                        .global()
                        .apply();
                if (applied && living.getRemainingFireTicks() < 60) {
                    RandomSource random = getOwner().getRandom();
                    living.setRemainingFireTicks(60 + random.nextIntBetweenInclusive(0, 60));
                    hitCount++;
                }
            }
        }
    }

    @Override
    public boolean isValidCollisionTarget(ImpFireball entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource sds) {
            return sds.getServant().isTarget(target);
        }
        return ICollideAttack.super.isValidCollisionTarget(entity, target);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.15, -0.15, -0.15, 0.15, 0.15, 0.15);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.IMP_FIREBALL.get();
    }
}
