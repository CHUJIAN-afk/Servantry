package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 眼球激光塔火球：直线飞行，首次命中后造成伤害并消失。
 * <p>
 * 刻意不提供渲染器（见 {@code EyeFireballRenderer}），仅由 {@link #tick()} 的粒子表现。
 */
public class EyeFireball extends Projectile implements IEntityCollision<EyeFireball>, IBlockCollision<EyeFireball> {

    public EyeFireball() {
        super(SummonerAttachmentEntityRegister.EYE_FIREBALL);
        setDrag(1.0F);
        setGravity(0.0F);
        setMaxTickCount(80);
    }

    @Override
    public void tick() {
        super.tick();
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xFF6B6B)
                        .edgeColor(0xFF2A2A)
                        .lifetime(5)
                        .lifetimeRandom(4)
                        .spin(0.2F)
                        .spinRandom(0.1F)
                        .friction(0.75F)
                        .scale(0.025F)
                        .scaleRandom(0.008F)
                )
                .pos(getPos())
                .count(4)
                .offset(0.08)
                .velocity(getVelocity().scale(-0.05))
                .emit();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        attack(hit.entity(), getDamage(), 5);
        setCurrentPathNode(getCurrentPathNode().modifyPos(hit.hitPoint()));
        setRemove();
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xFF6B6B)
                        .edgeColor(0xFF2A2A)
                        .lifetime(6)
                        .lifetimeRandom(5)
                        .spin(0.2F)
                        .spinRandom(0.1F)
                        .friction(0.7F)
                        .scale(0.03F)
                        .scaleRandom(0.008F)
                )
                .pos(getPos())
                .velocity(getVelocity().normalize())
                .count(20)
                .speed(0.5)
                .spread(Mth.TWO_PI)
                .emit();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setCurrentPathNode(getCurrentPathNode().modifyPos(context.position()));
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
}
