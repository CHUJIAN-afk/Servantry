package first.summoner.common.projectile;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 流星射弹（星坠）：从目标周围上方生成，向目标位置直线移动（无追踪），
 * 命中实体或落地时对命中点半径 3 格内所有目标造成伤害（伤害在创建时按 133% 结算伤害设置）。
 */
public class Meteor extends Projectile implements IEntityCollision<Meteor>, IBlockCollision<Meteor> {

    public Meteor() {
        super(SummonerAttachmentEntityRegister.METEOR);
        setDrag(1);
        setMaxLife(60);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.12, -0.12, -0.12, 0.12, 0.12, 0.12);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.12, -0.12, -0.12, 0.12, 0.12, 0.12);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        explode(hitContexts.getFirst().entity());
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        explode(null);
    }

    private void explode(LivingEntity primary) {
        Vec3 pos = getPos();
        DamageSource source = getDamageSource();
        List<LivingEntity> targets = LyraHelper.get(owner).getTargetCache()
                .getEntitiesInRadius(pos, 2, null);
        for (LivingEntity target : targets) {
            InvincibleData.attack(target)
                    .attacker(getUuid())
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .apply();
        }
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                                 .centerColor(0xea9900)
                                 .edgeColor(0xea5700)
                                 .lifetime(10)
                                 .lifetimeRandom(20)
                                 .spin(0.3f)
                                 .spinRandom(0.05F)
                                 .friction(0.85F)
                                 .scale(0.025f)
                                 .scaleRandom(0.005f)
                )
                .pos(pos)
                .offset(0.15)
                .velocity(pos.offsetRandom(getRandom(), 5).subtract(pos).normalize())
                .count(80)
                .speed(0.35)
                .spread(Math.TAU)
                .emit();
        setRemove();
    }

}
