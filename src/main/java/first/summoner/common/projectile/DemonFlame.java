package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DemonFlame extends Projectile implements IEntityCollision<DemonFlame>, IBlockCollision<DemonFlame> {

    public DemonFlame() {
        super(SummonerAttachmentEntityRegister.DEMON_FLAME);
        setDrag(0.92f);
        setMaxLife(7);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hitContext : hitContexts) {
                LivingEntity target = hitContext.entity();
                InvincibleData.attack(target)
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .invincibleTime(10)
                        .effect(new MobEffectInstance(SummonerMobEffectRegister.CursedFlame, 100, 0))
                        .apply();
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
        currentPathNode = new PathNode(context.position(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }
}
