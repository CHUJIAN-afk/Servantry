package first.summoner.common.projectile;


import first.lyra.common.attachment.ImmunityData;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
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
        HitContext hit = hitContexts.getFirst();
                // 中毒持续 5~10 秒（100~200 tick）
            int duration = 100 + getRandom().nextInt(101);
            ImmunityData immunityData = ImmunityData.get(hit.entity());
            if (!immunityData.isActive(getUuid())) {
                attack(hit.entity(), getDamage(), 0);
                // attack 命中成功才写入无敌记录，据此判断本次是否真的打中
                if (immunityData.isActive(getUuid())) {
                    hit.entity().addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
                }
            }
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
