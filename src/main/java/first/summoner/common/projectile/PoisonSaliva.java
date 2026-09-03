package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 毒唾液射弹 - 藤壶喷吐的唾液，命中后施加 5~10 秒中毒效果。
 */
public class PoisonSaliva extends Projectile implements IEntityCollision<PoisonSaliva>, IBlockCollision<PoisonSaliva> {

    public PoisonSaliva() {
        super();
    }

    public PoisonSaliva(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1.0f);
        setMaxSpeed(2f);
        setMaxLife(60);
        setGravity(0);
        setTrailTimer(60);
    }

    @Override
    public int getTrailDuration() {
        return 60;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        HitContext hit = hitContexts.getFirst();
        if (source != null) {
            // 中毒持续 5~10 秒（100~200 tick）
            int duration = 100 + getOwner().getRandom().nextInt(101);
            InvincibleData.attack(hit.entity())
                    .attacker(getUuid())
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .effect(new MobEffectInstance(MobEffects.POISON, duration, 0))
                    .apply();
        }
        setCurrentPathNode(getCollisionAfterPathNode(hit.hitPoint(), getCurrentPathNode()));
        setRemove();
    }

    @Override
    public boolean isValidCollisionTarget(PoisonSaliva entity, LivingEntity target) {
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
        setCurrentPathNode(getCollisionAfterPathNode(context.position(), getCurrentPathNode()));
        setRemove();
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return SummonerAttachmentEntityRegister.POISON_SALIVA.get();
    }
}
