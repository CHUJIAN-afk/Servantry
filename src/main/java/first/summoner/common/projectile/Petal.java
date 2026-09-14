package first.summoner.common.projectile;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * 花瓣射弹（花儿的粗暴展示）：从目标位置向周围释放，不穿透实体（命中即伤），
 * 穿透方块（无方块碰撞接口）。生成后随机延迟 0.33~0.67 秒才开始追踪附近 8 格内最近目标。
 * 伤害/穿透在创建时按效果设置（225% 结算伤害、50 穿透、不击退）。
 */
public class Petal extends Projectile implements IEntityCollision<Petal> {

    /** 追踪延迟（tick，0.33~0.67 秒 = 7~13 tick，创建时随机）。 */
    private int chaseDelay;
    /** 追踪目标（延迟结束后寻找）。 */
    private LivingEntity chaseTarget = null;

    public Petal() {
        super(SummonerAttachmentEntityRegister.PETAL);
        setDrag(0.82f);
        setMaxTickCount(40);
        this.chaseDelay = 7;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getLevel().isClientSide()) {
            if (chaseDelay > 0) {
                chaseDelay--;
            } else if (chaseTarget == null || !chaseTarget.isAlive()) {
                chaseTarget = findNearestTarget();
            }
            if (chaseTarget != null && chaseTarget.isAlive()) {
                Vec3 dir = chaseTarget.getBoundingBox().getCenter().subtract(getPos()).normalize();
                addVelocity(dir);
            }
        }
    }

    /** 寻找 8 格半径内最近的有效目标（TargetCache 目标选择）。 */
    private LivingEntity findNearestTarget() {
        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        TargetCache targetCache = LyraHelper.get(getLevel()).getTargetCache();
        for (LivingEntity candidate : targetCache.getEntitiesInRadius(getPos(), 8, living -> targetCache.isTarget(owner, living))) {
            double dist = candidate.position().distanceToSqr(getPos());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = candidate;
            }
        }
        return nearest;
    }

    /** 设置追踪延迟（tick）。 */
    public void setChaseDelay(int chaseDelay) {
        this.chaseDelay = chaseDelay;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.08, -0.08, -0.08, 0.08, 0.08, 0.08);
    }

    @Override
    public boolean isValidCollisionTarget(Petal entity, LivingEntity target) {
        return getTickCount() > 10 && LyraHelper.get(getLevel()).getTargetCache().isTarget(owner, target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
                UUID uuid = getUuid();
            for (HitContext hitContext : hitContexts) {
                InvincibleData.attack(hitContext.entity())
                        .attacker(uuid)
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .invincibleTime(3)
                        .apply();
            }
        setRemove();
    }
}
