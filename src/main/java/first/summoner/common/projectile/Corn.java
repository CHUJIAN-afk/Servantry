package first.summoner.common.projectile;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.lyra.common.sound.Playable;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 玉米炮射弹 - 抛物线飞行，命中实体或方块时对3格内所有敌对目标造成伤害。
 */
public class Corn extends Projectile implements IEntityCollision<Corn>, IBlockCollision<Corn> {

    /** AOE伤害半径 */
    private static final double AOE_RADIUS = 3.0;

    public Corn() {
        super(SummonerAttachmentEntityRegister.CORN);
        setDrag(0.99f);
        setMaxLife(200);
        setGravity(-0.05f);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        // 命中实体：在命中点处AOE
        if (!hitContexts.isEmpty()) {
            HitContext firstHit = hitContexts.getFirst();
            explodeAt(firstHit.hitPoint());
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        // 命中方块：在碰撞点处AOE
        explodeAt(context.position());
    }

    /**
     * 在指定位置爆炸，对3格内所有敌对目标造成伤害。
     */
    private void explodeAt(Vec3 explosionPos) {
        // 通过TargetCache获取附近实体
        LyraHelper helper = LyraHelper.get(owner);
        TargetCache targetCache = helper.getTargetCache();
        List<LivingEntity> nearbyEntities = targetCache.getEntitiesInRadius(explosionPos, AOE_RADIUS, this::isTarget);
        for (LivingEntity entity : nearbyEntities) {
            InvincibleData.attack(entity)
                    .attacker(getUuid())
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .invincibleTime(10)
                    .apply();
        }
        Vec3 pos = getPos();
        ParticleHelper.create(owner.level())
                .type(ParticleTypes.EXPLOSION)
                .pos(pos)
                .count(10)
                .emit();
        ParticleHelper.create(owner.level())
                .type(ParticleTypes.EXPLOSION_EMITTER)
                .pos(pos)
                .count(10)
                .emit();
        Playable.play(SoundEvents.GENERIC_EXPLODE, owner.level(), pos, owner.getSoundSource());
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }
}
