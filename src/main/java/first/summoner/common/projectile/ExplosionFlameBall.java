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
 * 爆炸烈焰火球射弹 - 命中时对半径 3 格内所有敌人造成等量伤害并点燃 10 秒。
 * <p>
 * 行为：
 * - 火焰色粒子拖尾（参考小鬼火球）
 * - 命中时爆发散射粒子，范围伤害 + 点燃
 * - 命中方块或在未命中消失时爆发少量火焰色粒子
 */
public class ExplosionFlameBall extends Projectile implements IEntityCollision<ExplosionFlameBall>, IBlockCollision<ExplosionFlameBall> {

    public ExplosionFlameBall() {
        super();
    }

    public ExplosionFlameBall(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1.0f);
        setMaxSpeed(1.2f);
        setMaxLife(60);
        setGravity(0);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getOwner().level();
        if (!level.isClientSide()) {
            // 火焰色粒子拖尾（参考小鬼火球）
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
            Vec3 hitPoint = hitContexts.getFirst().hitPoint();
            explode(source, hitPoint);
            setCurrentPathNode(getCollisionAfterPathNode(hitPoint, getCurrentPathNode()));
            setRemove();
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        if (getDamageSource() != null) {
            explode(getDamageSource(), context.position());
        }
        setCurrentPathNode(getCollisionAfterPathNode(context.position(), getCurrentPathNode()));
        setRemove();
    }

    @Override
    public void onRemove() {
        if (!owner.level().isClientSide() && getDamageSource() != null) {
            // 未命中目标消失（自然消散）时爆发少量火焰色粒子
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0xe1c316)
                            .edgeColor(0xff7506)
                            .lifetime(5)
                            .lifetimeRandom(5)
                            .spin(0.25f)
                            .spinRandom(0.1F)
                            .friction(0.75F)
                            .scale(0.04f)
                            .scaleRandom(0.01f)
                    )
                    .pos(getPos())
                    .offset(0.15)
                    .velocity(getVelocity())
                    .count(6)
                    .speed(0.5)
                    .spread(2)
                    .emit();
        }
    }

    /**
     * 爆炸：对命中点半径 3 格内所有敌人造成等量伤害并点燃 10 秒，爆发散射粒子。
     */
    private void explode(DamageSource source, Vec3 hitPoint) {
        List<LivingEntity> entities = LyraHelper.get(owner).getTargetCache()
                .getEntitiesInRadius(hitPoint, 3, entity -> {
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
            entity.igniteForTicks(200);
        }
        // 命中时爆发散射粒子
        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xe1c316)
                        .edgeColor(0xff7506)
                        .lifetime(5)
                        .lifetimeRandom(10)
                        .spin(0.25f)
                        .spinRandom(0.1F)
                        .friction(0.75F)
                        .scale(0.04f)
                        .scaleRandom(0.01f)
                )
                .pos(hitPoint)
                .offset(0.25)
                .velocity(getVelocity())
                .count(36)
                .speed(0.6)
                .spread(Math.TAU)
                .emit();
    }

    @Override
    public boolean isValidCollisionTarget(ExplosionFlameBall entity, LivingEntity target) {
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
        return SummonerAttachmentEntityRegister.EXPLOSION_FLAME_BALL.get();
    }
}
