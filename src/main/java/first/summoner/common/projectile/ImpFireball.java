package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 小鬼火球射弹 - 穿透4个敌怪，100%施加着火了！效果。
 * 使用 GLOBAL 无敌帧模式（穿透攻击互相干扰）。
 */
public class ImpFireball extends Projectile implements IEntityCollision<ImpFireball>, IBlockCollision<ImpFireball> {

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
        Level level = getOwner().level();
        if (!level.isClientSide()) {
            ParticleHelper.create(level)
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xe1c316)
                                     .edgeColor(0xff7506)
                                     .lifetime(4)
                                     .lifetimeRandom(8)
                                     .spin(0.25f)
                                     .spinRandom(0.25F)
                                     .scale(0.015f)
                                     .scaleRandom(0.015f)
                    )
                    .pos(getPos())
                    .count(8)
                    .offset(0.2)
                    .emit();
        }
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hit : hitContexts) {
                if (hitCount >= 4) {
                    setCurrentPathNode(getCollisionAfterPathNode(hit.hitPoint(), getCurrentPathNode()));
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
                if (applied) {
                    living.igniteForTicks(60);
                    hitCount++;
                }
            }
        }
    }

    @Override
    public boolean isValidCollisionTarget(ImpFireball entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof MinionDamageSource sds) {
            return sds.getMinion().isTarget(target);
        }
        return IEntityCollision.super.isValidCollisionTarget(entity, target);
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
        return SummonerAttachmentEntityRegister.IMP_FIREBALL.get();
    }
}
