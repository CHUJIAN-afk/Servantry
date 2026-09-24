package first.summoner.common.projectile;

import first.lyra.common.attachment.ImmunityData;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DemonFlame extends Projectile implements IEntityCollision<DemonFlame>, IBlockCollision<DemonFlame> {

    public DemonFlame() {
        super(SummonerAttachmentEntityRegister.DEMON_FLAME);
        setMaxTickCount(7);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hitContext : hitContexts) {
            LivingEntity target = hitContext.entity();
            ImmunityData immunityData = ImmunityData.get(target);
            if (!immunityData.isActive(getUuid())) {
                attack(target, getDamage(), 10);
                // attack 命中成功才写入无敌记录，据此判断本次是否真的打中
                if (immunityData.isActive(getUuid())) {
                    target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.CursedFlame, 100, 0));
                }
            }
        }
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }
}
