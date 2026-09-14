package first.summoner.common.projectile;


import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 毒唾液射弹 - 藤壶喷吐的唾液，命中后施加 5~10 秒中毒效果。
 */
public class PoisonSaliva extends Projectile implements IEntityCollision<PoisonSaliva>, IBlockCollision<PoisonSaliva> {

    public PoisonSaliva() {
        super(SummonerAttachmentEntityRegister.POISON_SALIVA);
        setDrag(1.0f);
        setMaxTickCount(60);
        setGravity(0);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        HitContext hit = hitContexts.getFirst();
                // 中毒持续 5~10 秒（100~200 tick）
            int duration = 100 + getRandom().nextInt(101);
            InvincibleData.attack(hit.entity())
                    .attacker(getUuid())
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .effect(new MobEffectInstance(MobEffects.POISON, duration, 0))
                    .apply();
        setCurrentPathNode(getCurrentPathNode().modifyPos(hit.hitPoint()));
        setRemove();
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
        setCurrentPathNode(getCurrentPathNode().modifyPos(context.position()));
        setRemove();
    }
}
