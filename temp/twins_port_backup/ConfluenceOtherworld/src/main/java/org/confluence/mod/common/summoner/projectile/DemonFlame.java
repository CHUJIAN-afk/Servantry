package org.confluence.mod.common.summoner.projectile;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.confluence.mod.common.init.ModEffects;
import org.confluence.mod.common.summoner.attachmentEntity.IBlockCollision;
import org.confluence.mod.common.summoner.attachmentEntity.IEntityCollision;
import org.confluence.mod.common.summoner.register.SummonerAttachmentEntityTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DemonFlame extends Projectile implements IEntityCollision<DemonFlame>, IBlockCollision<DemonFlame> {

    public DemonFlame() {
        super(SummonerAttachmentEntityTypes.DEMON_FLAME);
        setMaxTickCount(7);
        setGravity(0);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hitContext : hitContexts) {
            LivingEntity target = hitContext.entity();
            if (attack(target, getDamage(), 10)) {
                target.addEffect(new MobEffectInstance(ModEffects.CURSED_INFERNO, 100, 0));
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
