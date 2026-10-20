package org.confluence.mod.common.summoner.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.confluence.mod.common.summoner.attachmentEntity.IBlockCollision;
import org.confluence.mod.common.summoner.attachmentEntity.IEntityCollision;
import org.confluence.mod.common.summoner.register.SummonerAttachmentEntityTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 激光射弹 - 高速直线飞行的红色激光束。
 */
public class Laser extends Projectile implements IEntityCollision<Laser>, IBlockCollision<Laser> {

    public Laser() {
        super(SummonerAttachmentEntityTypes.LASER);
        setDrag(1);
        setGravity(0);
        setMaxTickCount(100);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.get(0);
        LivingEntity target = hit.entity();
        attack(target, getDamage(), 0);
        target.setRemainingFireTicks(Math.min(target.getRemainingFireTicks() + 20, 120));
        setCurrentPathNode(getCurrentPathNode().modifyPos(hit.hitPoint()));
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
