package first.summoner.common.projectile;


import first.lyra.common.attachment.ImmunityData;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 寒霜弹射弹 - 可穿透 2 次，每次衰减 15% 伤害，命中施加 10 秒减速。
 * <p>
 * 行为：
 * - 无重力，速度 1.5，最多存在 60 tick
 * - 命中第 3 个目标（造成伤害后）或命中方块时消失
 * - 飞行时生成淡蓝色粒子，命中/消失时爆出淡蓝色粒子
 */
public class FrostBolt extends Projectile implements IEntityCollision<FrostBolt>, IBlockCollision<FrostBolt> {

    private int hitCount = 0;

    public FrostBolt() {
        super(SummonerAttachmentEntityRegister.FROST_BOLT);
        setDrag(1f);
        setGravity(0);
        setMaxTickCount(60);
    }

    @Override
    public void tick() {
        super.tick();

        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0x8fe3ff)
                        .edgeColor(0x4fc1f5)
                        .lifetime(4)
                        .lifetimeRandom(8)
                        .spin(0.3f)
                        .spinRandom(0.1F)
                        .friction(0.8F)
                        .scale(0.03f)
                        .scaleRandom(0.008f)
                )
                .pos(getPos())
                .offset(0.22)
                .velocity(getVelocity().scale(-1))
                .count(6)
                .speed(0.2)
                .spread(1.5)
                .emit();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
                for (HitContext hit : hitContexts) {
                if (hitCount >= 3) {
                    setCurrentPathNode(getCurrentPathNode().modifyPos(hit.hitPoint()));
                    setRemove();
                    return;
                }
                LivingEntity living = hit.entity();
                ImmunityData immunityData = ImmunityData.get(living);
                boolean applied = false;
                if (!immunityData.isActive(getUuid())) {
                    attack(living, getDamage(), 3);
                    applied = immunityData.isActive(getUuid());
                }
                if (applied) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                }
                if (applied) {
                    hitCount++;
                    // 命中时目标爆出淡蓝色粒子（参考雷电球）
                    ParticleHelper.create(getLevel())
                            .generic(GenericParticleBuilder.create()
                                    .centerColor(0x8fe3ff)
                                    .edgeColor(0x4fc1f5)
                                    .lifetime(5)
                                    .lifetimeRandom(5)
                                    .spin(0.3f)
                                    .spinRandom(0.05F)
                                    .friction(0.75F)
                                    .scale(0.025f)
                                    .scaleRandom(0.005f)
                            )
                            .pos(living.getBoundingBox().getCenter())
                            .offset(0.15)
                            .velocity(getVelocity())
                            .count(6)
                            .speed(0.5)
                            .spread(2)
                            .emit();
                    // 每次穿透衰减 15% 伤害
                    setDamage(getDamage() * 0.85f);
                    if (hitCount >= 3) {
                        setCurrentPathNode(getCurrentPathNode().modifyPos(hit.hitPoint()));
                        setRemove();
                        return;
                    }
                }
            }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setCurrentPathNode(getCurrentPathNode().modifyPos(context.position()));
        setRemove();
    }

    @Override
    public void onRemove() {
        // 命中方块或在空气中消失时爆出少量淡蓝色粒子
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0x8fe3ff)
                        .edgeColor(0x4fc1f5)
                        .lifetime(5)
                        .lifetimeRandom(5)
                        .spin(0.3f)
                        .spinRandom(0.05F)
                        .friction(0.75F)
                        .scale(0.025f)
                        .scaleRandom(0.005f)
                )
                .pos(getPos())
                .offset(0.15)
                .velocity(getVelocity())
                .count(4)
                .speed(0.25)
                .spread(2)
                .emit();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.12, -0.12, -0.12, 0.12, 0.12, 0.12);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }
}
