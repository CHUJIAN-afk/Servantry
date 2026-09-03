package first.summoner.common.projectile;

import first.lyra.api.LyraHelper;
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
 * 禁戒弹射弹 - 命中时对半径 3.5 格内所有敌人造成范围伤害。
 * <p>
 * 行为：
 * - 金黄色粒子轨迹
 * - 命中时范围伤害并爆出大量金黄色粒子
 * - 命中方块或在空气中消失时也会爆炸
 * - 只能存在 0.62 秒（12 tick）
 * </p>
 */
public class ForbiddenOrb extends Projectile implements IEntityCollision<ForbiddenOrb>, IBlockCollision<ForbiddenOrb> {

    public ForbiddenOrb() {
        super();
    }

    public ForbiddenOrb(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1);
        setMaxSpeed(3);
        setMaxLife(60);
        setGravity(0);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getOwner().level();
        if (!level.isClientSide()) {
            // 金黄色粒子轨迹
            ParticleHelper.create(level)
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0xffd700)
                            .edgeColor(0xffa500)
                            .lifetime(4)
                            .lifetimeRandom(6)
                            .spin(0.25f)
                            .spinRandom(0.2F)
                            .friction(0.75F)
                            .scale(0.025f)
                            .scaleRandom(0.01f)
                    )
                    .pos(getPos())
                    .count(6)
                    .offset(0.15)
                    .velocity(getVelocity().scale(-0.1))
                    .emit();
        }
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            Vec3 hitPoint = hitContexts.getFirst().hitPoint();
            explode(source, hitPoint);
            setCurrentPathNode(getCollisionAfterPathNode(hitPoint, getCurrentPathNode()));
            setRemove();
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        DamageSource source = getDamageSource();
        if (source != null) {
            explode(source, context.position());
        }
        setCurrentPathNode(getCollisionAfterPathNode(context.position(), getCurrentPathNode()));
        setRemove();
    }

    @Override
    public void onRemove() {
        // 在空气中消失（自然消散）时也会爆炸
        DamageSource source = getDamageSource();
        if (!owner.level().isClientSide() && source != null) {
            explode(source, getPos());
        }
    }

    /**
     * 爆炸：对命中点半径 3.5 格内所有敌人造成范围伤害，爆出大量金黄色粒子。
     */
    private void explode(DamageSource source, Vec3 hitPoint) {
        List<LivingEntity> entities = LyraHelper.get(owner).getTargetCache()
                .getEntitiesInRadius(hitPoint, 3.5, entity -> {
                    if (source instanceof MinionDamageSource minionDamageSource) {
                        return minionDamageSource.getMinion().isTarget(entity);
                    }
                    return true;
                });
        for (LivingEntity entity : entities) {
            InvincibleData.attack(entity)
                    .attacker(getUuid())
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .apply();
        }
        // 命中时爆出大量金黄色粒子
        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xffd700)
                        .edgeColor(0xffa500)
                        .lifetime(5)
                        .lifetimeRandom(5)
                        .spin(0.25f)
                        .spinRandom(0.1F)
                        .friction(0.75F)
                        .scale(0.04f)
                        .scaleRandom(0.01f)
                )
                .pos(hitPoint)
                .offset(0.3)
                .velocity(getVelocity())
                .count(16)
                .speed(0.7)
                .spread(2)
                .emit();
    }

    @Override
    public boolean isValidCollisionTarget(ForbiddenOrb entity, LivingEntity target) {
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
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return SummonerAttachmentEntityRegister.FORBIDDEN_ORB.get();
    }
}
