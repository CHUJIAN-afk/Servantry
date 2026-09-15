package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 激光射弹 - 高速直线飞行的红色激光束。
 */
public class Laser extends Projectile implements IEntityCollision<Laser>, IBlockCollision<Laser> {

    public Laser() {
        super(SummonerAttachmentEntityRegister.LASER);
        setDrag(1);
        setGravity(0);
        setMaxTickCount(100);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        DamageSource source = getDamageSource();
        InvincibleData.attack(target)
                .attacker(getUuid())
                .damageSource(source)
                .damageAmount(getDamage())
                .apply();
        target.setRemainingFireTicks(Math.min(target.getRemainingFireTicks() + 20, 120));
        currentPathNode = currentPathNode.modifyPos(hit.hitPoint());
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.03, -0.03, -1, 0.03, 0.03, 0);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }
}
